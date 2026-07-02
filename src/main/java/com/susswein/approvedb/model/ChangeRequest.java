package com.susswein.approvedb.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "change_requests")
public class ChangeRequest {
    @Id
    private String id = UUID.randomUUID().toString();
    private String sourceSystem;
    private String sourceEventId;
    private String idempotencyKey;
    private String operationType;
    private String dialect;
    private String targetConnectionRef;
    private String sourceTargetRef;
    private String sourceTargetNameSnapshot;
    private String parameters;
    private String note;
    private String state;
    private String riskLevel;
    private String generatedSql;
    private String validationEvidence;
    private LocalDateTime receivedAt = LocalDateTime.now();
    private LocalDateTime decisionAt;
    private String decisionBy;
    private String decisionReason;
    private LocalDateTime executedAt;
    private String executionResult;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
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
    public String getParameters() { return parameters; }
    public void setParameters(String parameters) { this.parameters = parameters; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public String getGeneratedSql() { return generatedSql; }
    public void setGeneratedSql(String generatedSql) { this.generatedSql = generatedSql; }
    public String getValidationEvidence() { return validationEvidence; }
    public void setValidationEvidence(String validationEvidence) { this.validationEvidence = validationEvidence; }
    public LocalDateTime getReceivedAt() { return receivedAt; }
    public void setReceivedAt(LocalDateTime receivedAt) { this.receivedAt = receivedAt; }
    public LocalDateTime getDecisionAt() { return decisionAt; }
    public void setDecisionAt(LocalDateTime decisionAt) { this.decisionAt = decisionAt; }
    public String getDecisionBy() { return decisionBy; }
    public void setDecisionBy(String decisionBy) { this.decisionBy = decisionBy; }
    public String getDecisionReason() { return decisionReason; }
    public void setDecisionReason(String decisionReason) { this.decisionReason = decisionReason; }
    public LocalDateTime getExecutedAt() { return executedAt; }
    public void setExecutedAt(LocalDateTime executedAt) { this.executedAt = executedAt; }
    public String getExecutionResult() { return executionResult; }
    public void setExecutionResult(String executionResult) { this.executionResult = executionResult; }
}
