package com.susswein.approvedb.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ExecutionServiceTest {

    @Test
    public void testExecutionDoesNotThrow() {
        ExecutionService service = new ExecutionService();
        assertDoesNotThrow(() -> {
            service.execute("CREATE SCHEMA public;", "test_target_ref");
        });
    }
}
