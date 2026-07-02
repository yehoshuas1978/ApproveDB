package com.susswein.approvedb.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.susswein.approvedb.model.TargetConnection;
import com.susswein.approvedb.repository.TargetConnectionRepository;
import com.susswein.approvedb.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminApiController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AdminApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TargetConnectionRepository repository;

    @Test
    public void testListConnectionsWithAdminRole() throws Exception {
        TargetConnection tc = new TargetConnection();
        tc.setId("123");
        tc.setName("Local DB");
        tc.setDatabaseName("local_db");
        tc.setDialect("PostgreSQL");
        Mockito.when(repository.findAll()).thenReturn(List.of(tc));

        mockMvc.perform(get("/api/admin/target-connections"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$[0].name").value("Local DB"));
    }

    @Test
    public void testCreateConnection() throws Exception {
        TargetConnection tc = new TargetConnection();
        tc.setName("new db");
        
        Mockito.when(repository.save(any(TargetConnection.class))).thenReturn(tc);

        mockMvc.perform(post("/api/admin/target-connections")
               .contentType(MediaType.APPLICATION_JSON)
               .content("{\"name\":\"new db\"}"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.name").value("new db"));
    }
}
