package com.ie.naukri.search;


import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.util.List;
import java.util.Map;

public interface DatabaseClient {
    <T> T query(String shard, String query, Map<String, Object> paramMap, Class<T> resultClass);

    List<Map<String, Object>> queryForList(String shard, String query, Map<String, Object> paramMap);

    int update(String shard, String query);

    List<Map<String, Object>> query(String shard, String query);

    List<Map<String, Object>> query(String shard, String query, MapSqlParameterSource mapSqlParameterSource);
}
