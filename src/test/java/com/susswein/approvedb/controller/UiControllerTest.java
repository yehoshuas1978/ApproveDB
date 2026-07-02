package com.susswein.approvedb.controller;

import com.susswein.approvedb.config.SecurityConfig;
import com.susswein.approvedb.model.ChangeRequest;
import com.susswein.approvedb.repository.ChangeRequestRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UiController.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)
public class UiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChangeRequestRepository repository;

    @Test
    @WithMockUser
    public void testDashboard() throws Exception {
        ChangeRequest req = new ChangeRequest();
        req.setOperationType("CREATE_SCHEMA");
        
        Mockito.when(repository.findByStateOrderByReceivedAtDesc("PENDING")).thenReturn(List.of(req));

        mockMvc.perform(get("/"))
               .andExpect(status().isOk())
               .andExpect(view().name("dashboard"))
               .andExpect(model().attributeExists("requests"));
    }
    
    @Test
    @WithMockUser
    public void testRequestDetail() throws Exception {
        ChangeRequest req = new ChangeRequest();
        req.setId("test-id");
        
        Mockito.when(repository.findById("test-id")).thenReturn(Optional.of(req));

        mockMvc.perform(get("/requests/test-id"))
               .andExpect(status().isOk())
               .andExpect(view().name("request-detail"))
               .andExpect(model().attributeExists("request"));
    }
}
