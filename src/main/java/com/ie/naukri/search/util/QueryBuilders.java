package com.ie.naukri.search.util;

import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.search.FieldCollapse;
import co.elastic.clients.json.JsonData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class QueryBuilders {
    public static Query termsQuery(String field, Boolean check) {
        return co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders.terms()
                .field(field)
                .terms(
                        a ->
                                a.value(
                                        SearchServiceUtils.getFieldList(
                                                Collections.singletonList(check), Boolean.class)))
                .build()
                ._toQuery();
    }

    public static Query termsQuery(String field, List<?> list) {
        if (list.isEmpty()) {
            throw new IllegalArgumentException("List is empty");
        }

        if (list.get(0) instanceof String) {
            return SearchServiceUtils.getTermsQuery(
                            field, list.stream().map(e -> (String) e).collect(Collectors.toList()), String.class)
                    .build()
                    ._toQuery();
        } else if (list.get(0) instanceof Long) {
            return SearchServiceUtils.getTermsQuery(
                    field, list.stream().map(e -> (Long) e).collect(Collectors.toList()), Long.class).build()._toQuery();
        } else if (list.get(0) instanceof Integer) {
            return SearchServiceUtils.getTermsQuery(
                    field, list.stream().map(e -> (Integer) e).collect(Collectors.toList()), Integer.class).build()._toQuery();
        }


        throw new IllegalArgumentException("Unsupported list element type" + list.get(0).getClass());
    }

    public static FieldCollapse.Builder getCollapseQuery(String field){
        FieldCollapse.Builder query = new FieldCollapse.Builder();
        query.field(field);
        return query;
    }

    public static TermsQuery.Builder termsQueryBuilder(String field, List<?> list) {
        if (list.isEmpty()) {
            throw new IllegalArgumentException("List is empty");
        }

        if (list.get(0) instanceof String) {
            return SearchServiceUtils.getTermsQuery(
                    field, list.stream().map(e -> (String) e).collect(Collectors.toList()), String.class);
        } else if (list.get(0) instanceof Long) {
            return SearchServiceUtils.getTermsQuery(
                    field, list.stream().map(e -> (Long) e).collect(Collectors.toList()), Long.class);
        }

        throw new IllegalArgumentException("Unsupported list element type");
    }

    public static Query termQuery(String field, Object value) {
        TermQuery.Builder termQueryBuilder = co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders.term().field(field);
        if (value instanceof String) {
            termQueryBuilder.value((String) value);
        } else if (value instanceof Double) {
            termQueryBuilder.value((Double) value);
        } else if (value instanceof Integer) {
            termQueryBuilder.value((Integer) value);
        } else if (value instanceof Boolean) {
            termQueryBuilder.value((Boolean) value);
        } else if (value instanceof List) {
            return termsQuery(field, (List<?>) value);
        } else if (value instanceof Set) {
            return termsQuery(field, new ArrayList<>((Set) value));
        } else {
            throw new IllegalArgumentException("Unsupported value type");
        }
        return termQueryBuilder.build()._toQuery();
    }

    public static Query rangeQuery(String field, Comparable<?> min, Comparable<?> max) {
        RangeQuery.Builder rangeQuery = new RangeQuery.Builder();
        if(min!=null){
            rangeQuery.gte(JsonData.of(min));
        }
        if (max != null){
            rangeQuery.lte(JsonData.of(max));
        }
        return rangeQuery
                .field(field)
                .build()._toQuery();
    }

    public static RangeQuery.Builder rangeQueryBuilder(String field, Comparable<?> min, Comparable<?> max) {
        return co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders.range()
                .field(field)
                .gte(JsonData.of(min))
                .lte(JsonData.of(max));
    }


    public static MatchQuery.Builder matchQuery(String field, String value) {
        return co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders.match()
                .query(value)
                .field(field);
    }

    public static QueryStringQuery.Builder queryStringQuery(String value) {
        return co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders.queryString().query(value);
    }

    public static MultiMatchQuery.Builder multiMatchQuery(String cleanedKeyword) {
        return co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders.multiMatch().query(cleanedKeyword);
    }
}
