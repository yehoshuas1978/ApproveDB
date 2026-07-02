package com.susswein.approvedb.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SqlGeneratorService {

    private final ObjectMapper mapper;

    public SqlGeneratorService(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public String generateSql(String dialect, String operationType, String parametersJson) throws Exception {
        Map<String, Object> params = mapper.readValue(parametersJson, Map.class);
        
        // Mock SQL Generation for Phase 1
        if ("postgresql".equalsIgnoreCase(dialect)) {
            switch (operationType) {
                case "CREATE_SCHEMA":
                    return String.format("CREATE SCHEMA IF NOT EXISTS %s;", params.get("schemaName"));
                case "CREATE_TABLE":
                    return String.format("CREATE TABLE IF NOT EXISTS %s.%s (id SERIAL PRIMARY KEY);", 
                        params.get("schemaName"), params.get("tableName"));
                default:
                    return "-- Unsupported operation: " + operationType;
            }
        }
        return "-- Unsupported dialect or operation";
    }
}
