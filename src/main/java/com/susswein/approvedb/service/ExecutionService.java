package com.susswein.approvedb.service;

import org.springframework.stereotype.Service;

@Service
public class ExecutionService {

    public void execute(String sql, String targetConnectionRef) {
        // Mock Execution for Phase 1
        System.out.println("Executing SQL on " + targetConnectionRef + ":");
        System.out.println(sql);
    }
}
