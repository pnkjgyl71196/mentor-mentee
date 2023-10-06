package com.ie.naukri.search.service;

import co.elastic.clients.elasticsearch.core.BulkResponse;
import com.ie.naukri.search.commons.es.services.SearchTemplateService;
import com.ie.naukri.search.dao.Cache;
import com.ie.naukri.search.dao.MySQLDatabaseClient;
import com.ie.naukri.search.model.ElasticSearchDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class IndexingService {

    @Autowired
    private SearchTemplateService searchTemplateService;

    @Autowired
    private MySQLDatabaseClient mySQLDatabaseClient;

    @Autowired
    private Cache cache;

    private Pattern MASTER_ID_PATTERN = Pattern.compile("^[0-9]+$");

    public void indexData() throws IOException, InterruptedException {

        for (int i = 0; i < 500; i++) {
            String query = "select NAME,TOTAL_EXP,ABSOLUTE_CTC,ACTIVE,MOD_DT,CITY," +
                    "t2.TITLE as PROFILE_TITLE,t2.KEYWORDS_ID as PROFILE_KEYWORDS_ID, EXPID, " +
                    "t1.RESID, ORGN, t1.DESIG as EXP_DESIG, t1.PROFILE as EXP_PROFILE, t1.KEYWORDS as EXP_KEYWORDS, " +
                    "ORGNID, t1.KEYWORDS_ID as EXP_KEYWORDS_ID, " +
                    "DESIG_ID, EXP_TYPE, DETAILS as PRJ_DETAILS,t3.ROLE as PRJ_ROLE,t3.TITLE as PRJ_TITLE," +
                    "SKILLS as PRJ_SKILLS,SKILLS_ID as PRJ_SKILLS_ID " +
                    "from cv_exp t1 inner join cv_profile t2 on t1.RESID = t2.RESID " +
                    "left join cv_itprojects t3 on t1.RESID = t3.RESID " +
                    "where t2.ACTIVE = 'y' AND ( LENGTH(t1.KEYWORDS) > 0 or t3.RESID is not NULL ) AND MOD_DT is not null " +
                    "AND TOTAL_EXP > 1 AND EXP_TYPE ='F' AND " +
                    "(ORGNID IS NOT NULL OR ORGNID != '0')  AND (DESIG_ID is not NULL OR DESIG_ID != '0')";
            query = query + " limit " + i * 100 + ",100";
            List<Map<String, Object>> result = mySQLDatabaseClient.query("demo", query);
            List<ElasticSearchDocument> docList = new ArrayList<>();
            for (Map<String, Object> map : result) {
                ElasticSearchDocument elasticSearchDocument = new ElasticSearchDocument();
                try {

                    elasticSearchDocument.setExpId(((Integer) map.get("EXPID")));
                    elasticSearchDocument.setId(elasticSearchDocument.getExpId().toString());
                    elasticSearchDocument.setElasticDocumentStatus("index");

                    String prjSkillId = (String) map.get("PRJ_SKILLS_ID");
                    if (StringUtils.hasLength(prjSkillId)) {
                        // remove docs where all are longtail and longtail is new
                        List<String> prjSkillIds = Arrays.stream(prjSkillId.split(",")).collect(Collectors.toList());
                        elasticSearchDocument.setPrjSkillsId(getMasterSkillIds(prjSkillIds));
                        elasticSearchDocument.setPrjSkills(getMasterSkillLabels(elasticSearchDocument.getPrjSkillsId()));
                    }
                    String expKeywordId = (String) map.get("EXP_KEYWORDS_ID");
                    if (StringUtils.hasLength(expKeywordId)) {
                        // remove docs where all are longtail and longtail is new
                        List<String> skillIds = Arrays.stream(((String) map.get("EXP_KEYWORDS_ID")).split(",")).collect(Collectors.toList());
                        elasticSearchDocument.setExpKeywordsId(getMasterSkillIds(skillIds));
                        elasticSearchDocument.setExpKeywords(getMasterSkillLabels(elasticSearchDocument.getExpKeywordsId()));
                    }

                    String profileKeywordsId = (String) map.get("PROFILE_KEYWORDS_ID");
                    if (StringUtils.hasLength(profileKeywordsId)) {
                        // remove docs where all are longtail and longtail is new
                        List<String> profileKeywordsIds = Arrays.stream(((String) map.get("PROFILE_KEYWORDS_ID")).split(",")).collect(Collectors.toList());
                        elasticSearchDocument.setProfileKeywordsId(getMasterSkillIds(profileKeywordsIds));
                        elasticSearchDocument.setProfileKeywords(getMasterSkillLabels(elasticSearchDocument.getProfileKeywordsId()));
                    }

                    if (CollectionUtils.isEmpty(elasticSearchDocument.getPrjSkillsId()) &&
                            CollectionUtils.isEmpty(elasticSearchDocument.getExpKeywordsId()) &&
                            CollectionUtils.isEmpty(elasticSearchDocument.getProfileKeywordsId())) {
                        log.info("Excluding {} ", elasticSearchDocument.getExpId().toString());
                        continue;
                    }

                    elasticSearchDocument.setName((String) map.get("NAME"));
                    String totalExp = (String) map.get("TOTAL_EXP");
                    if (totalExp.contains(".")) {
                        String[] str = totalExp.split("\\.");
                        if (str.length == 2 && (str[1].equals("-1") || str[1].isEmpty())) {
                            totalExp = str[0];
                            if ("99".equals(totalExp)) {
                                log.info("Excluding {} due to experience", elasticSearchDocument.getExpId().toString());
                                continue;
                            }
                        }
                    }
                    elasticSearchDocument.setOrgnId((String) map.get("ORGNID"));
                    elasticSearchDocument.setTotalExp(totalExp);
                    elasticSearchDocument.setResId((Integer) map.get("RESID"));
                    elasticSearchDocument.setAbsoluteCtc((Integer) map.get("ABSOLUTE_CTC"));
                    elasticSearchDocument.setActive((String) map.get("ACTIVE"));
                    if (map.get("MOD_DT") != null) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String formattedDateTime = dateFormat.format(((Timestamp) map.get("MOD_DT")).getTime());
                        elasticSearchDocument.setModDt(formattedDateTime);
                    }
                    if (map.get("PROFILE_TITLE") != null) {
                        elasticSearchDocument.setProfileTitle((String) map.get("PROFILE_TITLE"));
                    }
                    byte[] prjDetails;
                    if (map.get("PRJ_DETAILS") != null && (prjDetails = (byte[]) map.get("PRJ_DETAILS")) != null && prjDetails.length > 0) {
                        elasticSearchDocument.setPrjDetails(new String(prjDetails));
                    }
                    if (map.get("ORGN") != null) {
                        elasticSearchDocument.setOrgn((String) map.get("ORGN"));
                    }
                    if (map.get("EXP_DESIG") != null) {
                        elasticSearchDocument.setExpDesig((String) map.get("EXP_DESIG"));
                    }
                    if (map.get("EXP_PROFILE") != null) {
                        elasticSearchDocument.setExpProfile((String) map.get("EXP_PROFILE"));
                    }
                    if (map.get("DESIG_ID") != null) {
                        elasticSearchDocument.setDesigId(getMasterDesignationId((String) map.get("DESIG_ID")));
                    }
                    elasticSearchDocument.setExpType((String) map.get("EXP_TYPE"));
                    if (map.get("PRJ_ROLE") != null) {
                        elasticSearchDocument.setPrjRole((String) map.get("PRJ_ROLE"));
                    }
                    if (map.get("PRJ_TITLE") != null) {
                        elasticSearchDocument.setPrjTitle((String) map.get("PRJ_TITLE"));
                    }
                    if (map.get("CITY") != null) {
                        Integer cityId = (Integer) map.get("CITY");
                        elasticSearchDocument.setCityIds(getMasterCityIds(Collections.singletonList(cityId)));
                        elasticSearchDocument.setCityLabels(getMasterCityLabel(elasticSearchDocument.getCityIds()));
                    }
                    updateEducationDetails(elasticSearchDocument);
                } catch (Exception e) {
                    log.error("Error while processing doc: [{}]{}", map, elasticSearchDocument, e);
                }
                docList.add(elasticSearchDocument);
            }
            if (!docList.isEmpty()) {
                BulkResponse response = searchTemplateService.getTemplate("demo1").bulkIndex("testindex", docList);
                log.info("indexing response:[{}]", response);
            }
        }

    }

    private List<String> getMasterSkillIds(List<String> ids) {
        final List<String> masterIds = new ArrayList<>(ids.size());
        for (String id : ids) {
            if (MASTER_ID_PATTERN.matcher(id).find()) {
                masterIds.add(id);
            } else {
                Long mappedId = cache.getMappedSkillMasterId(id);
                if (mappedId != null) {
                    masterIds.add(String.valueOf(mappedId));
                }
            }
        }
        return masterIds;
    }

    private String getMasterDesignationId(String id) {
        if (MASTER_ID_PATTERN.matcher(id).find()) {
            return id;
        } else {
            Long mappedId = cache.getMappedDesignationMasterId(id);
            return mappedId != null ? String.valueOf(mappedId) : id;
        }
    }

    private List<String> getMasterCityIds(List<Integer> cityIds) {
        final List<String> masterIds = new ArrayList<>();
        for (Integer id : cityIds) {
            if (cache.existInMasterCity(Long.valueOf(id))) {
                masterIds.add(String.valueOf(id));
            } else {
                Long mappedId = cache.getMappedCityMasterId(String.valueOf(id));
                if (mappedId != null) {
                    masterIds.add(String.valueOf(mappedId));
                }
            }
        }
        return masterIds;
    }

    private String getMasterSkillLabels(List<String> ids) {
        StringBuilder builder = new StringBuilder();
        for (String id : ids) {
            String label = cache.getMasterSkillLabel(Long.valueOf(id));
            if (label != null) {
                if (builder.length() > 0) {
                    builder.append(",");
                }
                builder.append(label);
            }
        }
        return builder.toString();
    }

    private String getMasterDesignationLabel(String id) {
        return cache.getMasterDesignationLabel(Long.valueOf(id));
    }

    private String getMasterCityLabel(List<String> ids) {
        StringBuilder builder = new StringBuilder();
        for (String id : ids) {
            String label = cache.getMasterCityLabel(Long.valueOf(id));
            if (label != null) {
                if (builder.length() > 0) {
                    builder.append(",");
                }
                builder.append(label);
            }
        }
        return builder.toString();
    }

    public void updateEducationDetails(ElasticSearchDocument elasticSearchDocument) {
        Integer resId = elasticSearchDocument.getResId();
        String query = "select COURSE_ID,EDUCATION_TYPE,SPEC_ID,COURSE_TYPE,ENTITY_INSTITUTE_ID,IS_PREMIUM from cv_education where RESID=:RESID and IS_PRIMARY = 1 order by EDUCATION_TYPE desc limit 1 ";
        MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
        mapSqlParameterSource.addValue("RESID", resId);

        List<Map<String, Object>> mapList = mySQLDatabaseClient.query("demo", query, mapSqlParameterSource);
        if (!mapList.isEmpty()) {
            if (mapList.get(0).get("EDUCATION_TYPE") != null) {
                elasticSearchDocument.setEducationType((Integer) mapList.get(0).get("EDUCATION_TYPE"));
            }
            if (mapList.get(0).get("COURSE_ID") != null) {
                elasticSearchDocument.setCourseId((Integer) mapList.get(0).get("COURSE_ID"));
                elasticSearchDocument.setCourseLabel(
                        getCourseLabel(elasticSearchDocument.getCourseId(), elasticSearchDocument.getEducationType()));
            }
            if (mapList.get(0).get("SPEC_ID") != null) {
                elasticSearchDocument.setSpecId((Integer) mapList.get(0).get("SPEC_ID"));
                elasticSearchDocument.setSpecificationLabel(
                        getSpecLabel(String.valueOf(elasticSearchDocument.getSpecId()), elasticSearchDocument.getEducationType()));
            }
            if (mapList.get(0).get("COURSE_TYPE") != null) {
                elasticSearchDocument.setCourseType((String) mapList.get(0).get("COURSE_TYPE"));
            }
            if (mapList.get(0).get("ENTITY_INSTITUTE_ID") != null) {
                elasticSearchDocument.setInstituteId((String) mapList.get(0).get("ENTITY_INSTITUTE_ID"));
                elasticSearchDocument.setInstituteLabel(getInstituteLabel(elasticSearchDocument.getInstituteId()));
            }
            if (mapList.get(0).get("IS_PREMIUM") != null && (Boolean) mapList.get(0).get("IS_PREMIUM")) {
                elasticSearchDocument.setPremium(true);
            }
            elasticSearchDocument.setPremium(false);
        }
    }

    private String getInstituteLabel(String instituteId) {
        String label = null;
        if (MASTER_ID_PATTERN.matcher(instituteId).find()) {
            label = cache.getMasterInstituteLabel(Long.valueOf(instituteId));
        }
        if (label == null) {
            Long mappedId = cache.getMappedInstituteMasterId(instituteId);
            return mappedId != null ? cache.getMasterInstituteLabel(mappedId) : cache.getInstituteLabel(instituteId);
        }
        return label;
    }

    private String getCourseLabel(Integer courseId, Integer educationType) {
        if (educationType == null) {
            return null;
        }
        String label = null;
        switch (educationType) {
            case 1:
                label = cache.getMasterUGCourseLabel(Long.valueOf(courseId));
                if (label == null) {
                    return cache.getUGCourseLongTailLabel(String.valueOf(courseId));
                }
                break;
            case 2:
                label = cache.getMasterPGCourseLabel(Long.valueOf(courseId));
                if (label == null) {
                    return cache.getPGCourseLongTailLabel(String.valueOf(courseId));
                }
                break;
            case 3:
                label = cache.getMasterPPGCourseLabel(Long.valueOf(courseId));
                if (label == null) {
                    return cache.getPPGCourseLongTailLabel(String.valueOf(courseId));
                }
                break;
        }
        return label;
    }

    private String getSpecLabel(String courseId, Integer educationType) {
        if (educationType == null) {
            return null;
        }
        String label = null;
        switch (educationType) {
            case 1:
                label = cache.getMasterUGSpecLabel(Long.valueOf(courseId));
                if (label == null) {
                    return cache.getUGSpecLongTailLabel(courseId);
                }
                break;
            case 2:
                label = cache.getMasterPGSpecLabel(Long.valueOf(courseId));
                if (label == null) {
                    return cache.getPGSpecLongTailLabel(courseId);
                }
                break;
            case 3:
                label = cache.getMasterPPGSpecLabel(Long.valueOf(courseId));
                if (label == null) {
                    return cache.getPPGSpecLongTailLabel(courseId);
                }
                break;
        }
        return label;
    }

}
