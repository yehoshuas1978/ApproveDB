package com.susswein.approvedb.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.susswein.approvedb.config.SecurityConfig;
import com.susswein.approvedb.model.ChangeRequest;
import com.susswein.approvedb.repository.ChangeRequestRepository;
import com.susswein.approvedb.service.ExecutionService;
import com.susswein.approvedb.service.SqlGeneratorService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

@WebMvcTest(WebhookController.class)
@AutoConfigureMockMvc(addFilters = false)
public class WebhookControllerTest {

    @TestConfiguration
    static class Config {
        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChangeRequestRepository repository;

    @MockitoBean
    private SqlGeneratorService sqlGenerator;

    @MockitoBean
    private ExecutionService executor;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testReceiveWebhook() throws Exception {
        String payload = "{" +
            "\"sourceSystem\": \"test-system\"," +
            "\"operationType\": \"CREATE_SCHEMA\"," +
            "\"dialect\": \"postgresql\"," +
            "\"parameters\": {\"schemaName\": \"test_db\"}" +
        "}";

        Mockito.when(sqlGenerator.generateSql(anyString(), anyString(), anyString())).thenReturn("CREATE SCHEMA test_db;");

        mockMvc.perform(post("/api/change-requests")
               .contentType(MediaType.APPLICATION_JSON)
               .content(payload))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.status").value("RECEIVED"));
        
        Mockito.verify(repository).save(any(ChangeRequest.class));
    }

    @Test
    public void testApproveRequest() throws Exception {
        ChangeRequest req = new ChangeRequest();
        req.setId("test-id");
        req.setState("PENDING");
        req.setGeneratedSql("CREATE SCHEMA test_db;");
        req.setTargetConnectionRef("conn-1");
        
        Mockito.when(repository.findById("test-id")).thenReturn(Optional.of(req));

        mockMvc.perform(post("/api/change-requests/test-id/approve"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.status").value("APPROVED_AND_EXECUTED"));
               
        Mockito.verify(executor).execute("CREATE SCHEMA test_db;", "conn-1");
        Mockito.verify(repository).save(req);
    }
    
    @Test
    public void testRejectRequest() throws Exception {
        ChangeRequest req = new ChangeRequest();
        req.setId("test-id");
        req.setState("PENDING");
        
        Mockito.when(repository.findById("test-id")).thenReturn(Optional.of(req));

        mockMvc.perform(post("/api/change-requests/test-id/reject"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.status").value("REJECTED"));
               
        Mockito.verify(repository).save(req);
    }
}
