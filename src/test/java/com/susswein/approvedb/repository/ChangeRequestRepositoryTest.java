package com.susswein.approvedb.repository;

import com.susswein.approvedb.model.ChangeRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.flyway.enabled=false"
})
@TestPropertySource(locations = "classpath:application-test.yml")
public class ChangeRequestRepositoryTest {

    @Autowired
    private ChangeRequestRepository repository;

    @Test
    public void testFindByStateOrderByReceivedAtDesc() {
        ChangeRequest cr1 = new ChangeRequest();
        cr1.setSourceSystem("test1");
        cr1.setSourceEventId("evt1");
        cr1.setIdempotencyKey("idemp1");
        cr1.setOperationType("CREATE_TABLE");
        cr1.setDialect("postgresql");
        cr1.setParameters("{}");
        cr1.setState("PENDING");
        cr1.setRiskLevel("LOW");
        cr1.setReceivedAt(LocalDateTime.now().minusMinutes(5));
        
        ChangeRequest cr2 = new ChangeRequest();
        cr2.setSourceSystem("test2");
        cr2.setSourceEventId("evt2");
        cr2.setIdempotencyKey("idemp2");
        cr2.setOperationType("CREATE_SCHEMA");
        cr2.setDialect("postgresql");
        cr2.setParameters("{}");
        cr2.setState("PENDING");
        cr2.setRiskLevel("LOW");
        cr2.setReceivedAt(LocalDateTime.now());
        
        ChangeRequest cr3 = new ChangeRequest();
        cr3.setSourceSystem("test3");
        cr3.setSourceEventId("evt3");
        cr3.setIdempotencyKey("idemp3");
        cr3.setOperationType("DROP_TABLE");
        cr3.setDialect("postgresql");
        cr3.setParameters("{}");
        cr3.setState("APPROVED");
        cr3.setRiskLevel("HIGH");
        cr3.setReceivedAt(LocalDateTime.now().minusMinutes(10));

        repository.saveAll(List.of(cr1, cr2, cr3));

        List<ChangeRequest> pending = repository.findByStateOrderByReceivedAtDesc("PENDING");
        
        assertEquals(2, pending.size());
        // cr2 was received most recently
        assertEquals("test2", pending.get(0).getSourceSystem());
        assertEquals("test1", pending.get(1).getSourceSystem());
    }
}
