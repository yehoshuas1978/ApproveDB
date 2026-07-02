package com.susswein.approvedb.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "target_connections")
public class TargetConnection {
    @Id
    private String id = UUID.randomUUID().toString();
    private String name;
    private String sourceSystem;
    private String sourceTargetRef;
    private String sourceTargetNameSnapshot;
    private String dialect;
    private String host;
    private Integer port;
    private String databaseName;
    private String defaultSchema;
    private String username;
    private String passwordEncrypted;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSourceSystem() { return sourceSystem; }
    public void setSourceSystem(String sourceSystem) { this.sourceSystem = sourceSystem; }
    public String getSourceTargetRef() { return sourceTargetRef; }
    public void setSourceTargetRef(String sourceTargetRef) { this.sourceTargetRef = sourceTargetRef; }
    public String getSourceTargetNameSnapshot() { return sourceTargetNameSnapshot; }
    public void setSourceTargetNameSnapshot(String sourceTargetNameSnapshot) { this.sourceTargetNameSnapshot = sourceTargetNameSnapshot; }
    public String getDialect() { return dialect; }
    public void setDialect(String dialect) { this.dialect = dialect; }
    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }
    public Integer getPort() { return port; }
    public void setPort(Integer port) { this.port = port; }
    public String getDatabaseName() { return databaseName; }
    public void setDatabaseName(String databaseName) { this.databaseName = databaseName; }
    public String getDefaultSchema() { return defaultSchema; }
    public void setDefaultSchema(String defaultSchema) { this.defaultSchema = defaultSchema; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordEncrypted() { return passwordEncrypted; }
    public void setPasswordEncrypted(String passwordEncrypted) { this.passwordEncrypted = passwordEncrypted; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
