package com.ie.naukri.search.controller;

import com.ie.naukri.search.commons.core.dtos.SearchResponseDTO;
import com.ie.naukri.search.commons.core.services.SearchService;
import com.ie.naukri.search.model.ElasticSearchDocument;
import com.ie.naukri.search.model.MentorDto;
import com.ie.naukri.search.model.MentorSearchRequestDto;
import com.ie.naukri.search.model.MentorSearchResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
public class SearchController {

//    @Autowired
//    MentorSearchService searchService;
//
    @Autowired
    private SearchService searchService;

    @PostMapping("/search")

    public SearchResponseDTO searchMentors(@RequestBody MentorSearchRequestDto searchRequestDto) {
        try {
            SearchResponseDTO searchResponse = searchService.search(searchRequestDto, ElasticSearchDocument.class);
            return searchResponse;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
