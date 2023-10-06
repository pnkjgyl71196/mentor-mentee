package com.ie.naukri.search.service;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.Rescore;
import co.elastic.clients.elasticsearch.core.search.SourceConfig;
import co.elastic.clients.elasticsearch.core.search.SourceFilter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.ie.naukri.search.commons.core.dtos.SearchRequestDTO;
import com.ie.naukri.search.commons.core.dtos.SearchResponseDTO;
import com.ie.naukri.search.commons.core.services.SearchService;
import com.ie.naukri.search.commons.dataaccess.SearchTemplate;
import com.ie.naukri.search.commons.es.services.SearchTemplateService;
import com.ie.naukri.search.model.*;
import com.ie.naukri.search.util.QueryBuilders;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Primary
@Slf4j
public class MentorSearchService implements SearchService {

    @Autowired
    private SearchTemplateService searchTemplateService;

    @Autowired
    SearchTemplateService moduleAwareSearchClient;


    public List<MentorDto> searchData(MentorSearchRequestDto searchRequestDto) throws IOException, InterruptedException {
//       searchTemplateService.getTemplate("demo1").search(searchRequestDto);
        return null;
    }

    @Override
    public <TDocument> SearchResponseDTO search(SearchRequestDTO searchRequestDTO, Class<TDocument> tDocumentClass) throws IOException {
        MentorSearchResponseDto searchResponseDto = new MentorSearchResponseDto();
        SearchTemplate searchTemplate = moduleAwareSearchClient.getTemplate("demo1");
        MentorSearchRequestModel searchRequestModel = (MentorSearchRequestModel) searchRequestDTO;
        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
        //create query here
        boolQueryBuilder.must(getQueries());
        boolQueryBuilder.should(QueryBuilders.queryStringQuery("java").fields(new ArrayList<>(Arrays.asList("PRJ_DETAILS", "PRJ_ROLE", "PRJ_TITLE"))).build()._toQuery());

        Query.Builder queryFinal = new Query.Builder();


        SourceConfig.Builder sourceBuilder = new SourceConfig.Builder();
        sourceBuilder.filter(new SourceFilter.Builder().includes(Arrays.asList("TOTAL_EXP", "ABSOLUTE_CTC", "PROFILE_TITLE", "PROFILE_KEYWORDS", "ORGN", "EXP_DESIG", "EXP_PROFILE", "PRJ_DETAILS", "PRJ_ROLE", "PRJ_SKILLS")).build());
        SearchRequest.Builder searchRequest = new SearchRequest.Builder().index("testindex").from(0).size(10).trackScores(true).query(boolQueryBuilder.build()._toQuery()).source(sourceBuilder.build()).timeout("2000");
        searchRequest.trackTotalHits((track) -> track.enabled(true));
        SearchRequest searchRequest1 = searchRequest.build();
        log.info("Elastic request{}", searchRequest1.toString());
        searchRequest.collapse(QueryBuilders.getCollapseQuery("RESID").build());
        searchRequest.query(boolQueryBuilder.build()._toQuery());
        SearchResponse<ElasticSearchDocument> searchResponse = searchTemplate.search(searchRequest1, ElasticSearchDocument.class);
        try {
            long numHits = 0L;
            List<MentorDto> results = new ArrayList<>();
            if (searchResponse != null) {
                List<Hit<ElasticSearchDocument>> hits = searchResponse.hits().hits();
                numHits = searchResponse.hits().total().value();
                for (Hit searchHit : hits) {
                    ElasticSearchDocument sourceAsMap = (ElasticSearchDocument) searchHit.source();
                    Integer id = Integer.parseInt(searchHit.id());
                    MentorDto result = getResultDTO(sourceAsMap);
                    results.add(result);
                }
            }
            searchResponseDto.setMentors(results);
            searchResponseDto.setTotalCount(numHits);
        } catch (Exception e) {
            log.error("Exception occurred while converting search response to searchResponseDTO", e);
        }
        return searchResponseDto;
    }

    private List<Query> getQueries() {
        Query termQuery = QueryBuilders.termQuery("PRJ_SKILLS_ID", 133);
        Query rangeQuery = QueryBuilders.rangeQuery("TOTAL_EXP", "3", "7");
        List<Query> queries = new ArrayList<>();
        queries.add(termQuery);
        queries.add(rangeQuery);
        return queries;
    }

    private MentorDto getResultDTO(ElasticSearchDocument sourceAsMap) {
        MentorDto result = new MentorDto();
        if (null != sourceAsMap) {
            if (sourceAsMap.getResId() != null) {

            }
            if (sourceAsMap.getOrgn() != null) {

            }
//            if(sourceAsMap.get() != null) {
//
//            }
            if (sourceAsMap.getName() != null) {

            }
            if (sourceAsMap.getName() != null) {

            }
            if (sourceAsMap.getName() != null) {

            }
            if (sourceAsMap.getName() != null) {

            }
            if (sourceAsMap.getName() != null) {

            }


        }
        return result;
    }

}
