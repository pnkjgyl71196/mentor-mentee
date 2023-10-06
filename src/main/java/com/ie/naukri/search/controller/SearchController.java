package com.ie.naukri.search.controller;

import com.ie.naukri.search.commons.core.dtos.SearchResponseDTO;
import com.ie.naukri.search.commons.core.services.SearchService;
import com.ie.naukri.search.dao.Cache;
import com.ie.naukri.search.dao.MySQLDatabaseClient;
import com.ie.naukri.search.model.ElasticSearchDocument;
import com.ie.naukri.search.model.MentorSearchRequestDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
public class SearchController {

//    @Autowired
//    MentorSearchService searchService;
//
@Autowired
private Cache cache;

    @Autowired
    private SearchService searchService;

    @Autowired
    private MySQLDatabaseClient mySQLDatabaseClient;

    @PostMapping("/search")
    public SearchResponseDTO searchMentors(@RequestBody MentorSearchRequestDto searchRequestDto) {
        try {
            SearchResponseDTO searchResponse = searchService.search(searchRequestDto, ElasticSearchDocument.class);
            return searchResponse;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/getMentors")
    public SearchResponseDTO getMentors(@RequestParam("userId") String resId,
                                                      @RequestParam("skill") String skillId) {

        String query = "SELECT RESID, USERNAME,NAME,ABSOLUTE_CTC,TOTAL_EXP from cv_profile where RESID="+resId;
        MentorSearchRequestDto dto = new MentorSearchRequestDto();
        List<Map<String, Object>> resultMap = mySQLDatabaseClient.query("demo", query);
        if (resultMap.size() > 0) {
            Map<String, Object> data = resultMap.get(0);
            if (data.containsKey("ABSOLUTE_CTC")) {
                dto.setCtc((Integer) data.get("ABSOLUTE_CTC"));
            }
            if (data.containsKey("TOTAL_EXP")) {
                if (!"99.-1".equals((String)data.get("TOTAL_EXP"))) {
                    dto.setTotalExp((String) data.get("TOTAL_EXP"));
                }
            }
            dto.setSkillId(skillId);
            dto.setSkill(cache.getMasterSkillLabel(Long.valueOf(skillId)));
        }
        return searchMentors(dto);
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/getMentorDetail")
    public SearchResponseDTO getMentor(@RequestParam("userId") String resId) {
        MentorSearchRequestDto dto = new MentorSearchRequestDto();
        dto.setResId(resId);
        return searchMentors(dto);
    }
}
