package com.ie.naukri.search.dao;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ie.naukri.search.model.EntityDTO;
import com.ie.naukri.search.service.RestClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class Cache {

    @Inject
    ObjectMapper objectMapper;

    @Autowired
    private MySQLDatabaseClient mySQLDatabaseClient;


    @Value("${entityMasterSkillDesignationUrl}")
    private String entityMasterSkillDesignationUrl;

    @Value("${entityLongTailSkillDesignationUrl}")
    private String entityLongTailSkillDesignationUrl;

    @Autowired
    private RestClient restClient;

    private static final Map<Long, String> masterSkillsMap = new HashMap<>();
    private static final Map<Long, String> masterDesignationsMap = new HashMap<>();

    private static final Map<String, Long> longTailToMasterSkillsMap = new HashMap<>();
    private static final Map<String, Long> longTailToMasterDesignationsMap = new HashMap<>();

    @PostConstruct
    public void init() throws Exception {
        prepareMasterSkillsMap();
        prepareMasterDesignationMap();
        populateSkillLongTail();
        populateDesignationLongTail();
    }

    public void prepareMasterSkillsMap() throws Exception {
        String entityMasterSkills = restClient.execute(
                entityMasterSkillDesignationUrl + "/skill?includeDeleted=false", HttpMethod.GET, null,
                String.class);
        TypeReference<List<EntityDTO>> typeReference = new TypeReference<List<EntityDTO>>() {
        };
        List<EntityDTO> skills = objectMapper.readValue(entityMasterSkills, typeReference);
        for (EntityDTO entityDTO : skills) {
            if (entityDTO.getLabel() != null && !entityDTO.getLabel().trim().isEmpty()) {
                masterSkillsMap.put(entityDTO.getId(), entityDTO.getLabel().trim());
            }
        }
    }

    public void prepareMasterDesignationMap() throws Exception {
        String entityMasterSkills = restClient.execute(
                entityMasterSkillDesignationUrl + "/designation?includeDeleted=false", HttpMethod.GET, null,
                String.class);
        TypeReference<List<EntityDTO>> typeReference = new TypeReference<List<EntityDTO>>() {
        };
        List<EntityDTO> skills = objectMapper.readValue(entityMasterSkills, typeReference);
        for (EntityDTO entityDTO : skills) {
            if (entityDTO.getLabel() != null && !entityDTO.getLabel().trim().isEmpty()) {
                masterDesignationsMap.put(entityDTO.getId(), entityDTO.getLabel().trim());
            }
        }
    }

    public String getMasterSkillLabel(Long id) {
        return masterSkillsMap.get(id);
    }

    public String getMasterDesignationLabel(Long id) {
        return masterDesignationsMap.get(id);
    }

    private void populateSkillLongTail() {
        List<Map<String, Object>> result = mySQLDatabaseClient.query("entity", "select variant_id,global_id from skill_longtail where global_id is not null");
        for (Map<String, Object> map : result) {
            if (map.get("variant_id") != null && map.get("global_id") != null) {
                String variantId = ((String) map.get("variant_id")).trim();
                if (!variantId.isEmpty()) {
                    longTailToMasterSkillsMap.put((String) map.get("variant_id"), Long.valueOf((Integer) map.get("global_id")));
                }
            }
        }
    }

    private void populateDesignationLongTail() {
        List<Map<String, Object>> result = mySQLDatabaseClient.query("entity", "select variant_id,global_id from designation_longtail where global_id is not null");
        for (Map<String, Object> map : result) {
            if (map.get("variant_id") != null && map.get("global_id") != null) {
                String variantId = ((String) map.get("variant_id")).trim();
                if (!variantId.isEmpty()) {
                    longTailToMasterDesignationsMap.put((String) map.get("variant_id"), Long.valueOf((Integer) map.get("global_id")));
                }
            }
        }
    }

    public Long getMappedSkillMasterId(String id) {
        String trimmedId = id.trim();
        if (longTailToMasterSkillsMap.containsKey(trimmedId)) {
            return longTailToMasterSkillsMap.get(trimmedId);
        }
        return null;
//        try {
//            String response = restClient.execute(
//                    entityLongTailSkillDesignationUrl + "/skill/" + trimmedId, HttpMethod.GET, null,
//                    String.class);
//            if (!response.isEmpty()) {
//                JSONObject jsonObject = new JSONObject(response);
//                if (jsonObject.has("skillLongtail") && !jsonObject.isNull("skillLongtail")) {
//                    JSONObject skillLongTail = jsonObject.getJSONObject("skillLongtail");
//                    longTailToMasterSkillsMap.put(trimmedId, null);
//                    if (skillLongTail.has("status") && !skillLongTail.isNull("status")
//                            && "MERGED".equals(skillLongTail.getString("status")) && !skillLongTail.isNull("labelTypeGlobalId")) {
//                        Long mappedId = skillLongTail.getLong("labelTypeGlobalId");
//                        longTailToMasterSkillsMap.put(trimmedId, mappedId);
//                    }
//                }
//            }
//        } catch (Exception e) {
//            log.error("exception in getting masterId for longTail Skill Id [{}]: ", trimmedId, e);
//        }
//        return longTailToMasterSkillsMap.get(trimmedId);
    }

    public Long getMappedDesignationMasterId(String id) {
        String trimmedId = id.trim();
        if (longTailToMasterDesignationsMap.containsKey(trimmedId)) {
            return longTailToMasterDesignationsMap.get(trimmedId);
        }
        return null;
//        try {
//            String response = restClient.execute(
//                    entityLongTailSkillDesignationUrl + "/desig/" + trimmedId, HttpMethod.GET, null,
//                    String.class);
//            if (!response.isEmpty()) {
//                JSONObject jsonObject = new JSONObject(response);
//                if (jsonObject.has("skillLongtail") && !jsonObject.isNull("skillLongtail")) {
//                    JSONObject skillLongTail = jsonObject.getJSONObject("skillLongtail");
//                    longTailToMasterDesignationsMap.put(trimmedId, null);
//                    if (skillLongTail.has("status") && !skillLongTail.isNull("status")
//                            && "MERGED".equals(skillLongTail.getString("status")) && !skillLongTail.isNull("labelTypeGlobalId")) {
//                        Long mappedId = skillLongTail.getLong("labelTypeGlobalId");
//                        longTailToMasterDesignationsMap.put(trimmedId, mappedId);
//                    }
//                }
//            }
//        } catch (Exception e) {
//            log.error("exception in getting masterId for longTail Designation Id [{}]: ", trimmedId, e);
//        }
    }

}
