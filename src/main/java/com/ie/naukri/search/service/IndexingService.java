package com.ie.naukri.search.service;

import co.elastic.clients.elasticsearch.core.BulkResponse;
import com.ie.naukri.search.commons.es.services.SearchTemplateService;
import com.ie.naukri.search.dao.MySQLDatabaseClient;
import com.ie.naukri.search.model.ElasticSearchDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class IndexingService {

    @Autowired
    SearchTemplateService searchTemplateService;

    @Autowired
    MySQLDatabaseClient mySQLDatabaseClient;

    public void indexData() throws IOException, InterruptedException {

        for (int i=0; i<20;i++) {
            String query = "select NAME,TOTAL_EXP,ABSOLUTE_CTC,ACTIVE,MOD_DT,t2.TITLE as PROFILE_TITLE,t2.KEYWORDS as PROFILE_KEYWORDS,EXPID, t1.RESID, ORGN, t1.DESIG as EXP_DESIG, t1.PROFILE as EXP_PROFILE, t1.KEYWORDS as EXP_KEYWORDS, ORGNID, t1.KEYWORDS_ID as EXP_KEYWORDS_ID, DESIG_ID, EXP_TYPE, DETAILS as PRJ_DETAILS,t3.ROLE as PRJ_ROLE,t3.TITLE as PRJ_TITLE,SKILLS as PRJ_SKILLS,SKILLS_ID as PRJ_SKILLS_ID from cv_exp t1 inner join cv_profile t2 on t1.RESID = t2.RESID left join cv_itprojects t3 on t1.RESID = t3.RESID where t2.ACTIVE = 'y' and ( LENGTH(t1.KEYWORDS) > 0 or t3.RESID is not NULL ) AND TOTAL_EXP>1 and EXP_TYPE ='F'";
            query = query + " limit " + i*100  + ",100";
            List<Map<String, Object>> result = mySQLDatabaseClient.query("demo", query);
            List<ElasticSearchDocument> docList = new ArrayList<>();
            for (Map<String, Object> map : result) {
                ElasticSearchDocument elasticSearchDocument = new ElasticSearchDocument();
                try {
                    elasticSearchDocument.setName((String) map.get("NAME"));
                    String totalExp = (String) map.get("TOTAL_EXP");
                    if (totalExp.contains(".")) {
                        String[] str = totalExp.split("\\.");
                        if (str.length == 2 && str[1].equals("-1")) {
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
                    elasticSearchDocument.setProfileTitle((String) map.get("PROFILE_TITLE"));
                    byte[] prjDetails = (byte[]) map.get("PRJ_DETAILS");
                    if (prjDetails != null && prjDetails.length > 0) {
                        elasticSearchDocument.setPrjDetails(new String(prjDetails));
                    }
                    elasticSearchDocument.setProfileKeywords((String) map.get("PROFILE_KEYWORDS"));
                    elasticSearchDocument.setOrgn((String) map.get("ORGN"));
                    elasticSearchDocument.setExpDesig((String) map.get("EXP_DESIG"));
                    elasticSearchDocument.setExpProfile((String) map.get("EXP_PROFILE"));
                    elasticSearchDocument.setExpKeywords((String) map.get("EXP_KEYWORDS"));
                    elasticSearchDocument.setOrgnId((String) map.get("ORGNID"));
                    elasticSearchDocument.setDesigId((String) map.get("DESIG_ID"));
                    elasticSearchDocument.setExpType((String) map.get("EXP_TYPE"));
//            elasticSearchDocument.setPrjDetails((String) map.get("PRJ_DETAILS"));
                    elasticSearchDocument.setPrjRole((String) map.get("PRJ_ROLE"));
                    elasticSearchDocument.setPrjTitle((String) map.get("PRJ_TITLE"));
                    elasticSearchDocument.setPrjSkills((String) map.get("PRJ_SKILLS"));
                    String prjSkillId = (String) map.get("PRJ_SKILLS_ID");
                    if (!StringUtils.isEmpty(prjSkillId)) {
                        // remove docs where all are longtail and longtail is new
                        List<String> prjSkillIds = Arrays.stream(prjSkillId.split(",")).collect(Collectors.toList());
                        boolean allAlphanumeric = prjSkillIds.stream().allMatch(s -> s.matches("^[a-zA-Z0-9]+$"));
//                        if (allAlphanumeric) continue;

                        elasticSearchDocument.setPrjSkillsId(Arrays.stream(prjSkillId.split(",")).collect(Collectors.toList()));
                    }
                    String expKeywordId = (String) map.get("EXP_KEYWORDS_ID");
                    if (!StringUtils.isEmpty(expKeywordId)) {
                        // remove docs where all are longtail and longtail is new
                        List<String> skillIds = Arrays.stream(((String) map.get("EXP_KEYWORDS_ID")).split(",")).collect(Collectors.toList());
                        boolean allAlphanumeric = skillIds.stream().allMatch(s -> s.matches("^[a-zA-Z0-9]+$"));
//                        if (allAlphanumeric) continue;

                        elasticSearchDocument.setExpKeywordsId(Arrays.stream(((String) map.get("EXP_KEYWORDS_ID")).split(",")).collect(Collectors.toList()));
                    }
                    elasticSearchDocument.setExpId(((Integer) map.get("EXPID")));
                    elasticSearchDocument.setId(elasticSearchDocument.getExpId().toString());
                    elasticSearchDocument.setElasticDocumentStatus("index");
                } catch (Exception e) {
                    log.error("Error while processing doc: [{}], message: [{}]", map, e.getMessage());
                }
                docList.add(elasticSearchDocument);
            }
            if (docList.size() != 0) {
                BulkResponse response = searchTemplateService.getTemplate("demo1").bulkIndex("testindex", docList);
//            Thread.sleep(1000);
                log.info("indexing response:[{}]", response);
            }
        }

    }


}
