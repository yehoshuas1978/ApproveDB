package com.susswein.approvedb.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.susswein.approvedb.model.ChangeRequest;
import com.susswein.approvedb.repository.ChangeRequestRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@RestController
@RequestMapping("/api/change-requests")
public class WebhookController {

    private final ChangeRequestRepository repository;
    private final ObjectMapper objectMapper;
    private final com.susswein.approvedb.service.SqlGeneratorService sqlGenerator;
    private final com.susswein.approvedb.service.ExecutionService executor;

    public WebhookController(ChangeRequestRepository repository, ObjectMapper objectMapper,
                             com.susswein.approvedb.service.SqlGeneratorService sqlGenerator,
                             com.susswein.approvedb.service.ExecutionService executor) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.sqlGenerator = sqlGenerator;
        this.executor = executor;
    }

    @PostMapping
    public ResponseEntity<?> receiveWebhook(@RequestBody WebhookRequest request) {
        ChangeRequest cr = new ChangeRequest();
        cr.setSourceSystem(request.getSourceSystem());
        cr.setSourceEventId(request.getSourceEventId());
        cr.setIdempotencyKey(request.getIdempotencyKey());
        cr.setOperationType(request.getOperationType());
        cr.setDialect(request.getDialect());
        cr.setTargetConnectionRef(request.getTargetConnectionRef());
        cr.setSourceTargetRef(request.getSourceTargetRef());
        cr.setSourceTargetNameSnapshot(request.getSourceTargetNameSnapshot());
        cr.setNote(request.getNote());
        cr.setState("PENDING");
        cr.setRiskLevel("LOW");
        
        try {
            String params = objectMapper.writeValueAsString(request.getParameters());
            cr.setParameters(params);
            cr.setGeneratedSql(sqlGenerator.generateSql(request.getDialect(), request.getOperationType(), params));
        } catch (Exception e) {
            cr.setParameters("{}");
            cr.setGeneratedSql("-- Error generating SQL");
        }

        repository.save(cr);
        return ResponseEntity.ok(Map.of("status", "RECEIVED", "id", cr.getId()));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approveRequest(@PathVariable String id) {
        ChangeRequest req = repository.findById(id).orElseThrow();
        req.setState("APPROVED");
        req.setDecisionAt(java.time.LocalDateTime.now());
        
        executor.execute(req.getGeneratedSql(), req.getTargetConnectionRef());
        req.setExecutedAt(java.time.LocalDateTime.now());
        req.setExecutionResult("SUCCESS");
        
        repository.save(req);
        return ResponseEntity.ok(Map.of("status", "APPROVED_AND_EXECUTED"));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<?> rejectRequest(@PathVariable String id) {
        ChangeRequest req = repository.findById(id).orElseThrow();
        req.setState("REJECTED");
        repository.save(req);
        return ResponseEntity.ok(Map.of("status", "REJECTED"));
    }
}

class WebhookRequest {
    private String sourceSystem;
    private String sourceEventId;
    private String idempotencyKey;
    private String operationType;
    private String dialect;
    private String targetConnectionRef;
    private String sourceTargetRef;
    private String sourceTargetNameSnapshot;
    private Map<String, Object> parameters;
    private String note;

    public String getSourceSystem() { return sourceSystem; }
    public void setSourceSystem(String sourceSystem) { this.sourceSystem = sourceSystem; }
    public String getSourceEventId() { return sourceEventId; }
    public void setSourceEventId(String sourceEventId) { this.sourceEventId = sourceEventId; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public String getOperationType() { return operationType; }
    public void setOperationType(String operationType) { this.operationType = operationType; }
    public String getDialect() { return dialect; }
    public void setDialect(String dialect) { this.dialect = dialect; }
    public String getTargetConnectionRef() { return targetConnectionRef; }
    public void setTargetConnectionRef(String targetConnectionRef) { this.targetConnectionRef = targetConnectionRef; }
    public String getSourceTargetRef() { return sourceTargetRef; }
    public void setSourceTargetRef(String sourceTargetRef) { this.sourceTargetRef = sourceTargetRef; }
    public String getSourceTargetNameSnapshot() { return sourceTargetNameSnapshot; }
    public void setSourceTargetNameSnapshot(String sourceTargetNameSnapshot) { this.sourceTargetNameSnapshot = sourceTargetNameSnapshot; }
    public Map<String, Object> getParameters() { return parameters; }
    public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
