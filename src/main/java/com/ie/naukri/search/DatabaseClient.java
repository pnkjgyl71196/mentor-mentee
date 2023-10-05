package com.ie.naukri.search;


import com.ie.naukri.search.commons.core.dtos.IndexingDocumentDTO;

import java.util.List;
import java.util.Map;

public interface DatabaseClient {
    <T> T query(String shard, String query, Map<String, Object> paramMap, Class<T> resultClass);

    List<Map<String,Object>> queryForList(String shard, String query, Map<String, Object> paramMap);

    int update(String shard, String query);

    List<Map<String, Object>> query(String shard, String query);
}
