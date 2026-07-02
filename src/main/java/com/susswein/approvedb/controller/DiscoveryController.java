package com.susswein.approvedb.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api/capabilities")
public class DiscoveryController {

    @GetMapping
    public Map<String, Object> getCapabilities() {
        return Map.of(
            "application", "ApproveDB",
            "version", "0.1.0",
            "capabilities", Map.of(
                "operationWebhooks", true,
                "httpsConnectionSync", true,
                "filesystemConnectionSync", true,
                "directHttpConnectionSync", false
            ),
            "filesystemSync", Map.of(
                "supported", true,
                "requiresEncryptedFiles", true,
                "requiresSignedFiles", true,
                "pollIntervalSeconds", 30
            ),
            "supportedDialects", java.util.List.of("postgresql", "mysql")
        );
    }
}
