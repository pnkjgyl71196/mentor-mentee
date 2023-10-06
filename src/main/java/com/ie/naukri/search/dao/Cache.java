package com.ie.naukri.search.dao;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ie.naukri.search.model.EntityDTO;
import com.ie.naukri.search.service.RestClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
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

    @Value("${masterCityIdLabelURL}")
    private String masterCityIdLabelURL;

    @Value("${longTailCityIdLabelURL}")
    private String longTailCityIdLabelURL;

    @Value("${instituteMasterURL}")
    private String instituteMasterURL;

    @Autowired
    private RestClient restClient;

    private static final Map<Long, String> masterSkillsMap = new HashMap<>();
    private static final Map<Long, String> masterDesignationsMap = new HashMap<>();
    private static final Map<Long, String> masterCitiesMap = new HashMap<>();
    private static final Map<Long, String> masterInstitutesMap = new HashMap<>();
    private static final Map<Long, String> masterUGCourseMap = new HashMap<>();
    private static final Map<Long, String> masterPGCourseMap = new HashMap<>();
    private static final Map<Long, String> masterPPGCourseMap = new HashMap<>();
    private static final Map<Long, String> masterUGSpecMap = new HashMap<>();
    private static final Map<Long, String> masterPGSpecMap = new HashMap<>();
    private static final Map<Long, String> masterPPGSpecMap = new HashMap<>();

    private static final Map<String, Long> longTailToMasterSkillsMap = new HashMap<>();
    private static final Map<String, Long> longTailToMasterDesignationsMap = new HashMap<>();
    private static final Map<String, Long> longTailToMasterCityMap = new HashMap<>();
    private static final Map<String, Long> longTailToMasterInstituteMap = new HashMap<>();
    private static final Map<String, String> longTailInstituteLabelMap = new HashMap<>();
    private static final Map<String, String> longTailUGCourseLabelMap = new HashMap<>();
    private static final Map<String, String> longTailPGCourseLabelMap = new HashMap<>();
    private static final Map<String, String> longTailPPGCourseLabelMap = new HashMap<>();
    private static final Map<String, String> longTailUGSpecLabelMap = new HashMap<>();
    private static final Map<String, String> longTailPGSpecLabelMap = new HashMap<>();
    private static final Map<String, String> longTailPPGSpecLabelMap = new HashMap<>();

    @PostConstruct
    public void init() throws Exception {

        prepareMasterSkillsMap();
        prepareMasterDesignationMap();
        prepareMasterCityMap();
        prepareMasterInstituteMap();
        prepareMasterUGCourseMap();
        prepareMasterPGCourseMap();
        prepareMasterPPGCourseMap();
        prepareMasterUGSpecMap();
        prepareMasterPGSpecMap();
        prepareMasterPPGSpecMap();

        populateSkillLongTail();
        populateDesignationLongTail();
        populateCityLongTail();
        populateInstituteLongTail();

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

    public void prepareMasterCityMap() throws Exception {
        String cityIdLabel = restClient.execute(masterCityIdLabelURL, HttpMethod.GET, null,
                String.class);
        TypeReference<List<EntityDTO>> typeReference = new TypeReference<List<EntityDTO>>() {
        };
        List<EntityDTO> groups = objectMapper.readValue(cityIdLabel, typeReference);
        for (EntityDTO entity : groups) {
            masterCitiesMap.put(entity.getId(), entity.getLabel());
        }
    }

    public void prepareMasterInstituteMap() throws Exception {
        String cityIdLabel = restClient.execute(instituteMasterURL, HttpMethod.GET, null,
                String.class);
        TypeReference<List<EntityDTO>> typeReference = new TypeReference<List<EntityDTO>>() {
        };
        List<EntityDTO> groups = objectMapper.readValue(cityIdLabel, typeReference);
        for (EntityDTO entity : groups) {
            masterInstitutesMap.put(entity.getId(), entity.getLabel());
        }
    }

    public void prepareMasterUGCourseMap() {
        List<Map<String, Object>> result = mySQLDatabaseClient.query("entity", "select global_id,name from ugcourse_master");
        for (Map<String, Object> map : result) {
            masterUGCourseMap.put(Long.valueOf((Integer) map.get("global_id")), (String) map.get("name"));
        }
    }

    public void prepareMasterUGSpecMap() {
        List<Map<String, Object>> result = mySQLDatabaseClient.query("entity", "select global_id,name from ugspec_master");
        for (Map<String, Object> map : result) {
            masterUGSpecMap.put(Long.valueOf((Integer) map.get("global_id")), (String) map.get("name"));
        }
    }

    public void prepareMasterPGCourseMap() {
        List<Map<String, Object>> result = mySQLDatabaseClient.query("entity", "select global_id,name from pgcourse_master");
        for (Map<String, Object> map : result) {
            masterPGCourseMap.put(Long.valueOf((Integer) map.get("global_id")), (String) map.get("name"));
        }
    }

    public void prepareMasterPGSpecMap() {
        List<Map<String, Object>> result = mySQLDatabaseClient.query("entity", "select global_id,name from pgspec_master");
        for (Map<String, Object> map : result) {
            masterPGSpecMap.put(Long.valueOf((Integer) map.get("global_id")), (String) map.get("name"));
        }
    }

    public void prepareMasterPPGCourseMap() {
        List<Map<String, Object>> result = mySQLDatabaseClient.query("entity", "select global_id,name from ppgcourse_master");
        for (Map<String, Object> map : result) {
            masterPPGCourseMap.put(Long.valueOf((Integer) map.get("global_id")), (String) map.get("name"));
        }
    }

    public void prepareMasterPPGSpecMap() {
        List<Map<String, Object>> result = mySQLDatabaseClient.query("entity", "select global_id,name from ppgspec_master");
        for (Map<String, Object> map : result) {
            masterPPGSpecMap.put(Long.valueOf((Integer) map.get("global_id")), (String) map.get("name"));
        }
    }

    public String getMasterSkillLabel(Long id) {
        return masterSkillsMap.get(id);
    }

    public String getMasterDesignationLabel(Long id) {
        return masterDesignationsMap.get(id);
    }

    public String getMasterCityLabel(Long id) {
        return masterCitiesMap.get(id);
    }

    public boolean existInMasterCity(Long id) {
        return masterCitiesMap.containsKey(id);
    }

    public String getMasterInstituteLabel(Long id) {
        return masterInstitutesMap.get(id);
    }

    public String getMasterUGCourseLabel(Long id) {
        return masterUGCourseMap.get(id);
    }

    public String getMasterUGSpecLabel(Long id) {
        return masterUGSpecMap.get(id);
    }

    public String getMasterPGCourseLabel(Long id) {
        return masterPGCourseMap.get(id);
    }

    public String getMasterPGSpecLabel(Long id) {
        return masterPGSpecMap.get(id);
    }

    public String getMasterPPGCourseLabel(Long id) {
        return masterPPGCourseMap.get(id);
    }

    public String getMasterPPGSpecLabel(Long id) {
        return masterPPGSpecMap.get(id);
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

    private void populateCityLongTail() {
        List<Map<String, Object>> result = mySQLDatabaseClient.query("entity", "select variant_id,global_id from city_longtail where global_id is not null");
        for (Map<String, Object> map : result) {
            if (map.get("variant_id") != null && map.get("global_id") != null) {
                String variantId = ((String) map.get("variant_id")).trim();
                if (!variantId.isEmpty()) {
                    longTailToMasterCityMap.put((String) map.get("variant_id"), Long.valueOf((Integer) map.get("global_id")));
                }
            }
        }
    }

    private void populateInstituteLongTail() {
        List<Map<String, Object>> result = mySQLDatabaseClient.query("entity", "select variant_id,global_id from institute_longtail where global_id is not null");
        for (Map<String, Object> map : result) {
            if (map.get("variant_id") != null && map.get("global_id") != null) {
                String variantId = ((String) map.get("variant_id")).trim();
                if (!variantId.isEmpty()) {
                    longTailToMasterInstituteMap.put((String) map.get("variant_id"), Long.valueOf((Integer) map.get("global_id")));
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
    }

    public Long getMappedDesignationMasterId(String id) {
        String trimmedId = id.trim();
        if (longTailToMasterDesignationsMap.containsKey(trimmedId)) {
            return longTailToMasterDesignationsMap.get(trimmedId);
        }
        return null;
    }

    public Long getMappedCityMasterId(String id) {
        String trimmedId = id.trim();
        if (longTailToMasterCityMap.containsKey(trimmedId)) {
            return longTailToMasterCityMap.get(trimmedId);
        }
        return null;
    }

    public Long getMappedInstituteMasterId(String id) {
        String trimmedId = id.trim();
        if (longTailToMasterInstituteMap.containsKey(trimmedId)) {
            return longTailToMasterInstituteMap.get(trimmedId);
        }
        return null;
    }

    public String getInstituteLabel(String variantId) {
        if (longTailInstituteLabelMap.containsKey(variantId)) {
            return longTailInstituteLabelMap.get(variantId);
        }
        String query = "select variant_name from institute_longtail where variant_id=:variant_id";
        MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
        mapSqlParameterSource.addValue("variant_id", variantId);
        List<Map<String, Object>> mapList = mySQLDatabaseClient.query("entity", query, mapSqlParameterSource);
        longTailInstituteLabelMap.put(variantId, null);
        if (!mapList.isEmpty()) {
            String label = (String) mapList.get(0).get("variant_name");
            longTailInstituteLabelMap.put(variantId, label);
        }
        return null;
    }

    public String getUGCourseLongTailLabel(String variantId) {
        if (longTailUGCourseLabelMap.containsKey(variantId)) {
            return longTailUGCourseLabelMap.get(variantId);
        }
        String query = "select variant_name from ugcourse_longtail where variant_id=:variant_id";
        MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
        mapSqlParameterSource.addValue("variant_id", variantId);
        List<Map<String, Object>> mapList = mySQLDatabaseClient.query("entity", query, mapSqlParameterSource);
        longTailUGCourseLabelMap.put(variantId, null);
        if (!mapList.isEmpty()) {
            String label = (String) mapList.get(0).get("variant_name");
            longTailUGCourseLabelMap.put(variantId, label);
        }
        return null;
    }

    public String getUGSpecLongTailLabel(String variantId) {
        if (longTailUGSpecLabelMap.containsKey(variantId)) {
            return longTailUGSpecLabelMap.get(variantId);
        }
        String query = "select variant_name from ugspec_longtail where variant_id=:variant_id";
        MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
        mapSqlParameterSource.addValue("variant_id", variantId);
        List<Map<String, Object>> mapList = mySQLDatabaseClient.query("entity", query, mapSqlParameterSource);
        longTailUGSpecLabelMap.put(variantId, null);
        if (!mapList.isEmpty()) {
            String label = (String) mapList.get(0).get("variant_name");
            longTailUGSpecLabelMap.put(variantId, label);
        }
        return null;
    }

    public String getPGCourseLongTailLabel(String variantId) {
        if (longTailPGCourseLabelMap.containsKey(variantId)) {
            return longTailPGCourseLabelMap.get(variantId);
        }
        String query = "select variant_name from pgcourse_longtail where variant_id=:variant_id";
        MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
        mapSqlParameterSource.addValue("variant_id", variantId);
        List<Map<String, Object>> mapList = mySQLDatabaseClient.query("entity", query, mapSqlParameterSource);
        longTailPGCourseLabelMap.put(variantId, null);
        if (!mapList.isEmpty()) {
            String label = (String) mapList.get(0).get("variant_name");
            longTailPGCourseLabelMap.put(variantId, label);
        }
        return null;
    }

    public String getPGSpecLongTailLabel(String variantId) {
        if (longTailPGSpecLabelMap.containsKey(variantId)) {
            return longTailPGSpecLabelMap.get(variantId);
        }
        String query = "select variant_name from pgspec_longtail where variant_id=:variant_id";
        MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
        mapSqlParameterSource.addValue("variant_id", variantId);
        List<Map<String, Object>> mapList = mySQLDatabaseClient.query("entity", query, mapSqlParameterSource);
        longTailPGSpecLabelMap.put(variantId, null);
        if (!mapList.isEmpty()) {
            String label = (String) mapList.get(0).get("variant_name");
            longTailPGSpecLabelMap.put(variantId, label);
        }
        return null;
    }

    public String getPPGCourseLongTailLabel(String variantId) {
        if (longTailPPGCourseLabelMap.containsKey(variantId)) {
            return longTailPPGCourseLabelMap.get(variantId);
        }
        String query = "select variant_name from ppgcourse_longtail where variant_id=:variant_id";
        MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
        mapSqlParameterSource.addValue("variant_id", variantId);
        List<Map<String, Object>> mapList = mySQLDatabaseClient.query("entity", query, mapSqlParameterSource);
        longTailPPGCourseLabelMap.put(variantId, null);
        if (!mapList.isEmpty()) {
            String label = (String) mapList.get(0).get("variant_name");
            longTailPPGCourseLabelMap.put(variantId, label);
        }
        return null;
    }

    public String getPPGSpecLongTailLabel(String variantId) {
        if (longTailPPGSpecLabelMap.containsKey(variantId)) {
            return longTailPPGSpecLabelMap.get(variantId);
        }
        String query = "select variant_name from ppgspec_longtail where variant_id=:variant_id";
        MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
        mapSqlParameterSource.addValue("variant_id", variantId);
        List<Map<String, Object>> mapList = mySQLDatabaseClient.query("entity", query, mapSqlParameterSource);
        longTailPPGSpecLabelMap.put(variantId, null);
        if (!mapList.isEmpty()) {
            String label = (String) mapList.get(0).get("variant_name");
            longTailPPGSpecLabelMap.put(variantId, label);
        }
        return null;
    }

}
