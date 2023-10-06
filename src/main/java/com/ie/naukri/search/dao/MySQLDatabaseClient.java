package com.ie.naukri.search.dao;

import com.ie.naukri.db.jdbc.templates.MasterSlaveAwareJdbcOperations;
import com.ie.naukri.db.services.MasterSlaveAwareShardedJdbcService;
import com.ie.naukri.search.DatabaseClient;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;

@Component
public class MySQLDatabaseClient implements DatabaseClient {

    @Inject
    MasterSlaveAwareShardedJdbcService jdbcService;

    private ConcurrentMap<String, MasterSlaveAwareJdbcOperations> jdbcOperationsMap;

    @PostConstruct
    public void init() {
        jdbcOperationsMap = jdbcService.getShardJdbcTemplates(Optional.empty());
    }

    @Override
    public <T> T query(String shard, String query, Map<String, Object> paramMap, Class<T> resultClass) {
        if (!jdbcOperationsMap.containsKey(shard)) {
            throw new RuntimeException("Invalid shard name");
        }
        T result = jdbcOperationsMap.get(shard).queryForObject(query, paramMap, resultClass);
        return result;
    }

    @Override
    public List<Map<String, Object>> queryForList(String shard, String query, Map<String, Object> paramMap) {
        if (!jdbcOperationsMap.containsKey(shard)) {
            throw new RuntimeException("Invalid shard name");
        }
        List<Map<String, Object>> rows = jdbcOperationsMap.get(shard).queryForList(query, paramMap);

        return rows;
    }


    public List<Map<String, Object>> query(String shard, String query) {
        if (!jdbcOperationsMap.containsKey(shard)) {
            throw new RuntimeException("Invalid shard name");
        }
        return jdbcOperationsMap.get(shard).queryForList(query);
    }

    public List<Map<String, Object>> query(String shard, String query, MapSqlParameterSource mapSqlParameterSource) {
        if (!jdbcOperationsMap.containsKey(shard)) {
            throw new RuntimeException("Invalid shard name");
        }
        return jdbcOperationsMap.get(shard).queryForList(query, mapSqlParameterSource);
    }

    @Override
    public int update(String shard, String query) {
        if (!jdbcOperationsMap.containsKey(shard)) {
            throw new RuntimeException("Invalid shard name");
        }
        return jdbcOperationsMap.get(shard).update(query);
    }

}
