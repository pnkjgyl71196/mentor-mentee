package com.ie.naukri.search.util;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.TermsAggregation;
import co.elastic.clients.elasticsearch._types.aggregations.TermsExclude;
import co.elastic.clients.elasticsearch._types.aggregations.TermsInclude;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQuery;
import co.elastic.clients.elasticsearch.core.search.ScoreMode;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.util.NamedValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;

import java.util.*;

@Slf4j
public class SearchServiceUtils<T> {
    private static ObjectMapper objectMapper = new ObjectMapper();

    @Value("${naukri-resdexsearch-services-main.experiment:#{null}}")
    Boolean experimentFlag;




    public static void addAggregation(Map<String, Aggregation> map, Map<String, Integer> clusterMap, String key, String field, String countOrder, Integer defaultSize, String[] include, String[] exclude, String aggregationName) {
        try {
            if (aggregationName == null) {
                aggregationName = key;
            }
            if (StringUtils.isNotBlank(field)) {

                TermsAggregation.Builder termsAggregation = new TermsAggregation.Builder();
                termsAggregation.field(field).size(defaultSize == null ? clusterMap.get(key) : defaultSize);
                if (StringUtils.isNotEmpty(countOrder)) {
                    switch (countOrder) {
                        case "count_desc":
                            termsAggregation.order(new NamedValue<>("_count",SortOrder.Desc),new NamedValue<>("_key",SortOrder.Asc));
                            break;
                        case "count_asc":
                            termsAggregation.order(new NamedValue<>("_count", SortOrder.Asc),new NamedValue<>("_key",SortOrder.Asc));
                            break;
                    }
                }
                if (include != null ) {
                    termsAggregation.include(TermsInclude.of(a -> a.terms(Arrays.asList(include))));
                }
                if (exclude != null) {
                    termsAggregation.exclude(TermsExclude.of(a -> a.terms(Arrays.asList(exclude))));
                }
                termsAggregation.minDocCount(1);
                map.put(aggregationName, termsAggregation.build()._toAggregation());
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Exception occurred while adding aggregation key: {} and field : {}", key, field);
        }
    }

    public static <T> List<FieldValue> getFieldList(List<T> list, Class tClass) {
        List<FieldValue> fieldValues = new ArrayList<>();
        if (tClass == String.class) {
            list.forEach(a -> fieldValues.add(FieldValue.of((String) a)));
        } else if (tClass == Long.class) {
            list.forEach(a -> fieldValues.add(FieldValue.of((Long) a)));
        } else if (tClass == Integer.class) {
            list.forEach(a -> fieldValues.add(FieldValue.of((Integer) a)));
        } else if (tClass == Double.class) {
            list.forEach(a -> fieldValues.add(FieldValue.of((Double) a)));
        } else if (tClass == Float.class) {
            list.forEach(a -> fieldValues.add(FieldValue.of((Float) a)));
        }
        return fieldValues;
    }

    public static <T> TermsQuery.Builder getTermsQuery(String field, List<T> list, Class tclass) {
        return QueryBuilders.terms().field(field).terms(a -> a.value(SearchServiceUtils.getFieldList(list, tclass)));
    }

    public static Map<String, JsonData> getJsonDataMap(Map<String, Object> params) throws JsonProcessingException {
        Map<String,JsonData> map = new LinkedHashMap<>();
        for(String key : params.keySet()){
            JsonData val = JsonData.fromJson(objectMapper.writeValueAsString(params.get(key)));
            map.put(key,val);
        }
        return map;
    }


    public static ScoreMode getScoreMode(String queryRescoreMode) {
        if(queryRescoreMode.equalsIgnoreCase("total")){
            return ScoreMode.Total;
        }
        else return ScoreMode.Max;
    }
}
