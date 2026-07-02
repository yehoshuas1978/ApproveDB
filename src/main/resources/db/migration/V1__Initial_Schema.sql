CREATE TABLE target_connections (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    source_system VARCHAR(255),
    source_target_ref VARCHAR(255),
    source_target_name_snapshot VARCHAR(255),
    dialect VARCHAR(50) NOT NULL,
    host VARCHAR(255) NOT NULL,
    port INTEGER NOT NULL,
    database_name VARCHAR(255) NOT NULL,
    default_schema VARCHAR(255),
    username VARCHAR(255) NOT NULL,
    password_encrypted TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE change_requests (
    id VARCHAR(36) PRIMARY KEY,
    source_system VARCHAR(255) NOT NULL,
    source_event_id VARCHAR(255) NOT NULL,
    idempotency_key VARCHAR(255) NOT NULL,
    operation_type VARCHAR(50) NOT NULL,
    dialect VARCHAR(50) NOT NULL,
    target_connection_ref VARCHAR(255),
    source_target_ref VARCHAR(255),
    source_target_name_snapshot VARCHAR(255),
    parameters TEXT NOT NULL,
    note TEXT,
    state VARCHAR(50) NOT NULL,
    risk_level VARCHAR(50) NOT NULL,
    generated_sql TEXT,
    validation_evidence TEXT,
    received_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    decision_at TIMESTAMP,
    decision_by VARCHAR(255),
    decision_reason TEXT,
    executed_at TIMESTAMP,
    execution_result TEXT
);

CREATE TABLE audit_logs (
    id VARCHAR(36) PRIMARY KEY,
    event_type VARCHAR(100) NOT NULL,
    actor VARCHAR(255),
    target_id VARCHAR(255),
    target_type VARCHAR(100),
    details TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
