package com.susswein.approvedb.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SqlGeneratorServiceTest {

    private SqlGeneratorService sqlGeneratorService;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();
        sqlGeneratorService = new SqlGeneratorService(objectMapper);
    }

    @Test
    public void testGenerateCreateSchemaPostgres() throws Exception {
        String params = "{\"schemaName\": \"test_schema\"}";
        String sql = sqlGeneratorService.generateSql("postgresql", "CREATE_SCHEMA", params);
        assertEquals("CREATE SCHEMA IF NOT EXISTS test_schema;", sql);
    }

    @Test
    public void testGenerateCreateTablePostgres() throws Exception {
        String params = "{\"schemaName\": \"public\", \"tableName\": \"users\"}";
        String sql = sqlGeneratorService.generateSql("postgresql", "CREATE_TABLE", params);
        assertEquals("CREATE TABLE IF NOT EXISTS public.users (id SERIAL PRIMARY KEY);", sql);
    }

    @Test
    public void testUnsupportedDialect() throws Exception {
        String sql = sqlGeneratorService.generateSql("mysql", "CREATE_SCHEMA", "{}");
        assertEquals("-- Unsupported dialect or operation", sql);
    }
}
