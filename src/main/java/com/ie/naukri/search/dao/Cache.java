package com.ie.naukri.search.dao;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ie.naukri.search.model.EntityDTO;
import com.ie.naukri.search.service.RestClient;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class Cache {

    @Inject
    ObjectMapper objectMapper;

    @Value("${entityMasterSkillDesignationUrl}")
    private String entityMasterSkillDesignationUrl;

    @Value("${entityLongTailSkillDesignationUrl}")
    private String entityLongTailSkillDesignationUrl;

    @Autowired
    private RestClient restClient;

    private static final Map<Long, String> masterSkillsMap = new HashMap<>();

    private static final Map<String, Long> longTailToMasterSkillsMap = new HashMap<>();

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

    public boolean exists(Long id) {
        return masterSkillsMap.containsKey(id);
    }

    public Long getMappedMasterId(String id) {
        String trimmedId = id.trim();
        if (longTailToMasterSkillsMap.containsKey(trimmedId)) {
            return longTailToMasterSkillsMap.get(trimmedId);
        }
        try {
            String response = restClient.execute(
                    entityLongTailSkillDesignationUrl + "/skill/" + trimmedId, HttpMethod.GET, null,
                    String.class);
            if (!response.isEmpty()) {
                JSONObject jsonObject = new JSONObject(response);
                if (jsonObject.has("skillLongtail") && !jsonObject.isNull("skillLongtail")) {
                    JSONObject skillLongTail = jsonObject.getJSONObject("skillLongtail");
                    longTailToMasterSkillsMap.put(trimmedId, null);
                    if (skillLongTail.has("status") && !skillLongTail.isNull("status")
                            && "MERGED".equals(skillLongTail.getString("status")) && !skillLongTail.isNull("labelTypeGlobalId")) {
                        Long mappedId = skillLongTail.getLong("labelTypeGlobalId");
                        longTailToMasterSkillsMap.put(trimmedId, mappedId);
                    }
                }
            }
        } catch (Exception e) {
            log.error("exception in getting masterId for longTailId [{}]: ", trimmedId, e);
        }
        return longTailToMasterSkillsMap.get(trimmedId);
    }


}
