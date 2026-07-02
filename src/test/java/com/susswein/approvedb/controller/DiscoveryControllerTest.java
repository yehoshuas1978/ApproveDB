package com.susswein.approvedb.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import com.susswein.approvedb.config.SecurityConfig;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DiscoveryController.class)
@Import(SecurityConfig.class)
public class DiscoveryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testGetCapabilities() throws Exception {
        mockMvc.perform(get("/api/capabilities"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.application").value("ApproveDB"))
               .andExpect(jsonPath("$.capabilities.operationWebhooks").value(true));
    }
}
