package com.ie.naukri.search.service;

import co.elastic.clients.elasticsearch.core.BulkResponse;
import com.ie.naukri.search.commons.es.services.SearchTemplateService;
import com.ie.naukri.search.dao.Cache;
import com.ie.naukri.search.dao.MySQLDatabaseClient;
import com.ie.naukri.search.model.ElasticSearchDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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

        for (int i = 0; ; i++) {
            String query = "select NAME,TOTAL_EXP,ABSOLUTE_CTC,ACTIVE,MOD_DT," +
                    "t2.TITLE as PROFILE_TITLE,t2.KEYWORDS as PROFILE_KEYWORDS,EXPID, " +
                    "t1.RESID, ORGN, t1.DESIG as EXP_DESIG, t1.PROFILE as EXP_PROFILE, t1.KEYWORDS as EXP_KEYWORDS, " +
                    "ORGNID, t1.KEYWORDS_ID as EXP_KEYWORDS_ID, " +
                    "DESIG_ID, EXP_TYPE, DETAILS as PRJ_DETAILS,t3.ROLE as PRJ_ROLE,t3.TITLE as PRJ_TITLE," +
                    "SKILLS as PRJ_SKILLS,SKILLS_ID as PRJ_SKILLS_ID " +
                    "from cv_exp t1 inner join cv_profile t2 on t1.RESID = t2.RESID " +
                    "left join cv_itprojects t3 on t1.RESID = t3.RESID " +
                    "where t2.ACTIVE = 'y' AND ( LENGTH(t1.KEYWORDS) > 0 or t3.RESID is not NULL ) " +
                    "AND TOTAL_EXP > 1 AND EXP_TYPE ='F'";
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
                        elasticSearchDocument.setPrjSkillsId(getMasterIds(prjSkillIds));
                        elasticSearchDocument.setPrjSkills(getMasterLabels(elasticSearchDocument.getPrjSkillsId()));
                    }
                    String expKeywordId = (String) map.get("EXP_KEYWORDS_ID");
                    if (StringUtils.hasLength(expKeywordId)) {
                        // remove docs where all are longtail and longtail is new
                        List<String> skillIds = Arrays.stream(((String) map.get("EXP_KEYWORDS_ID")).split(",")).collect(Collectors.toList());
                        elasticSearchDocument.setExpKeywordsId(getMasterIds(skillIds));
                        elasticSearchDocument.setExpKeywords(getMasterLabels(elasticSearchDocument.getExpKeywordsId()));
                    }

                    if (CollectionUtils.isEmpty(elasticSearchDocument.getPrjSkillsId()) &&
                            CollectionUtils.isEmpty(elasticSearchDocument.getExpKeywordsId())) {
                        log.info("Excluding {} ", elasticSearchDocument.getExpId().toString());
                        continue;
                    }

                    elasticSearchDocument.setName((String) map.get("NAME"));
                    String totalExp = (String) map.get("TOTAL_EXP");
                    if (totalExp.contains(".")) {
                        String[] str = totalExp.split("\\.");
                        if (str.length == 2 && (str[1].equals("-1") || str[1].isEmpty())) {
                            totalExp = str[0];
                        }
                    }
                    elasticSearchDocument.setTotalExp(totalExp);
                    elasticSearchDocument.setResId((Integer) map.get("RESID"));
                    elasticSearchDocument.setAbsoluteCtc((Integer) map.get("ABSOLUTE_CTC"));
                    elasticSearchDocument.setActive((String) map.get("ACTIVE"));
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String formattedDateTime = dateFormat.format(((Timestamp) map.get("MOD_DT")).getTime());
                    elasticSearchDocument.setModDt(formattedDateTime);
                    if (map.get("PROFILE_TITLE") != null) {
                        elasticSearchDocument.setProfileTitle((String) map.get("PROFILE_TITLE"));
                    }
                    byte[] prjDetails;
                    if (map.get("PRJ_DETAILS") != null && (prjDetails = (byte[]) map.get("PRJ_DETAILS")) != null && prjDetails.length > 0) {
                        elasticSearchDocument.setPrjDetails(new String(prjDetails));
                    }
                    if (map.get("PROFILE_KEYWORDS") != null) {
                        elasticSearchDocument.setProfileKeywords((String) map.get("PROFILE_KEYWORDS"));
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
                    elasticSearchDocument.setOrgnId((String) map.get("ORGNID"));
                    elasticSearchDocument.setDesigId((String) map.get("DESIG_ID"));
                    elasticSearchDocument.setExpType((String) map.get("EXP_TYPE"));
                    if (map.get("PRJ_ROLE") != null) {
                        elasticSearchDocument.setPrjRole((String) map.get("PRJ_ROLE"));
                    }
                    if (map.get("PRJ_TITLE") != null) {
                        elasticSearchDocument.setPrjTitle((String) map.get("PRJ_TITLE"));
                    }
                } catch (Exception e) {
                    log.error("Error while processing doc: [{}]{}, message: [{}]", map, elasticSearchDocument, e.getMessage());
                }
                docList.add(elasticSearchDocument);
            }
            if (!docList.isEmpty()) {
                BulkResponse response = searchTemplateService.getTemplate("demo1").bulkIndex("testindex", docList);
                log.info("indexing response:[{}]", response);
            }
        }

    }

    private List<String> getMasterIds(List<String> ids) {
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

    private String getMasterLabels(List<String> ids) {
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

}
