package com.susswein.approvedb.controller;

import com.susswein.approvedb.model.TargetConnection;
import com.susswein.approvedb.repository.TargetConnectionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/target-connections")
public class AdminApiController {

    private final TargetConnectionRepository repository;

    public AdminApiController(TargetConnectionRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<TargetConnection> listConnections() {
        return repository.findAll();
    }

    @PostMapping
    public ResponseEntity<?> createConnection(@RequestBody TargetConnection connection) {
        TargetConnection saved = repository.save(connection);
        return ResponseEntity.ok(saved);
    }
}
