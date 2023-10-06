package com.ie.naukri.search.service;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
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
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Primary
@Slf4j
public class MentorSearchService implements SearchService {

    @Autowired
    private SearchTemplateService searchTemplateService;

    @Autowired
    SearchTemplateService moduleAwareSearchClient;

    @Override
    public <TDocument> SearchResponseDTO search(SearchRequestDTO searchRequestDTO, Class<TDocument> tDocumentClass) throws IOException {
        MentorSearchResponseDto searchResponseDto = new MentorSearchResponseDto();
        SearchTemplate searchTemplate = moduleAwareSearchClient.getTemplate("demo1");
        MentorSearchRequestModel searchRequestModel = new ObjectMapper().convertValue(searchRequestDTO, MentorSearchRequestModel.class);
        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
        //create query here
        List<Query> queries = getQueries(searchRequestModel);
        if (!queries.isEmpty()) {
            boolQueryBuilder.must(queries);
        }
        if (!StringUtils.isEmpty(searchRequestModel.getResId())) {
            Query termsQuery = QueryBuilders.termsQuery("RESID", Collections.singletonList(searchRequestModel.getResId()));
            boolQueryBuilder.must(termsQuery);
        }
        if (!StringUtils.isEmpty(searchRequestModel.getSkillId())) {
            QueryStringQuery queryStringQuery = QueryBuilders.queryStringQuery("PRJ_SKILLS_ID:"+searchRequestModel.getSkillId()+" OR EXP_KEYWORDS_ID:" + searchRequestModel.getSkillId()).build();
            boolQueryBuilder.must(queryStringQuery._toQuery());
        }
        if (!StringUtils.isEmpty(searchRequestModel.getSkill())) {
            QueryStringQuery queryStringQuery = QueryBuilders.queryStringQuery(searchRequestModel.getSkill()).fields(new ArrayList<>(Arrays.asList("PRJ_DETAILS", "PRJ_ROLE", "PRJ_TITLE"))).build();
            boolQueryBuilder.should(queryStringQuery._toQuery());
        }

        SourceConfig.Builder sourceBuilder = new SourceConfig.Builder();
        sourceBuilder.filter(new SourceFilter.Builder().includes(Arrays.asList("NAME","ACTIVE","TOTAL_EXP","ABSOLUTE_CTC", "PROFILE_TITLE", "PROFILE_KEYWORDS", "ORGN", "MOD_DT","PROFILE_KEYWORDS_ID","EXP_DESIG","EXP_TYPE","EXP_PROFILE", "PRJ_DETAILS", "PRJ_ROLE","PRJ_SKILLS", "PRJ_TITLE", "RESID","EXP_KEYWORDS","EXPID","ORGNID","EXP_KEYWORDS_ID","DESIG_ID","PRJ_SKILLS_ID","CITY_ID","CITY","COURSE_ID","COURSE_LABEL","EDUCATION_TYPE","SPEC_ID","SPEC_LABEL","COURSE_TYPE","ENTITY_INSTITUTE_ID","ENTITY_INSTITUTE_LABEL","IS_PREMIUM")).build());
        SearchRequest.Builder searchRequest = new SearchRequest.Builder().index("testindex").from(0).size(10).trackScores(true).query(boolQueryBuilder.build()._toQuery()).source(sourceBuilder.build());
        searchRequest.trackTotalHits((track) -> track.enabled(true));
        searchRequest.collapse(QueryBuilders.getCollapseQuery("RESID").build());
        SearchRequest searchRequest1 = searchRequest.build();
        log.info("Elastic request{}", searchRequest1.toString());
        SearchResponse<ElasticSearchDocument> searchResponse = searchTemplate.search(searchRequest1, ElasticSearchDocument.class);
        try {
            long numHits = 0L;
            List<MentorDto> results = new ArrayList<>();
            if (searchResponse != null) {
                List<Hit<ElasticSearchDocument>> hits = searchResponse.hits().hits();
                numHits = searchResponse.hits().total().value();
                for (Hit searchHit : hits) {
                    ElasticSearchDocument sourceAsMap = (ElasticSearchDocument) searchHit.source();
//                    Integer id = Integer.parseInt(searchHit.id());
                    MentorDto result = new ObjectMapper().convertValue(sourceAsMap, MentorDto.class);
                    Set<String> uniqueSkill = new HashSet<>();
                    if (!StringUtils.isEmpty(result.getPrjSkills())) {
                        uniqueSkill.addAll(Arrays.stream(result.getPrjSkills().split(",")).collect(Collectors.toSet()));
                    }
                    if (!StringUtils.isEmpty(result.getProfileKeywords())) {
                        uniqueSkill.addAll(Arrays.stream(result.getProfileKeywords().split(",")).collect(Collectors.toSet()));
                    }
                    if (!StringUtils.isEmpty(result.getExpKeywords())) {
                        uniqueSkill.addAll(Arrays.stream(result.getExpKeywords().split(",")).collect(Collectors.toSet()));
                    }
                    result.setSkills(String.join(", ", uniqueSkill));
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

    private List<Query> getQueries(MentorSearchRequestModel searchRequestModel) {
        List<Query> queries = new ArrayList<>();
        if (searchRequestModel.getCtc() != null) {
            Query rangeQuery = QueryBuilders.rangeQuery("ABSOLUTE_CTC", searchRequestModel.getCtc(),null);
            queries.add(rangeQuery);
        }
        if (!StringUtils.isEmpty(searchRequestModel.getTotalExp())) {
            Query rangeQuery = QueryBuilders.rangeQuery("TOTAL_EXP", Float.parseFloat(searchRequestModel.getTotalExp())+1, Float.parseFloat(searchRequestModel.getTotalExp())+5);
            queries.add(rangeQuery);
        }
        return queries;
    }

//    private MentorDto getResultDTO(ElasticSearchDocument sourceAsMap) {
//        MentorDto result = new MentorDto();
//        if (null != sourceAsMap) {
//            if (sourceAsMap.getResId() != null) {
//             result.setResId(sourceAsMap.getResId());
//            }
//            if (sourceAsMap.getOrgn() != null) {
//             result.setCurrentOrg(sourceAsMap.getOrgn());
//            }
////            if(sourceAsMap.get() != null) {
////
////            }
//            if (sourceAsMap.getName() != null) {
//               result.setName(sourceAsMap.getName());
//            }
////            if (sourceAsMap.getName() != null) {
////
////            }
//            if (sourceAsMap.getExpKeywords() != null) {
//             result.setSkills(sourceAsMap.getExpKeywords());
//            }
//            if (sourceAsMap.getTotalExp() != null) {
//result.setTotalExp(sourceAsMap.getTotalExp());
//            }
////            if (sourceAsMap.getOrgn() != null) {
////
////            }
//
//
//        }
//        return result;
//    }

}
