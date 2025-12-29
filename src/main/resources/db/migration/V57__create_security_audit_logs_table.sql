-- V57__create_security_audit_logs_table.sql
-- Security audit log table for tracking cross-tenant access violations
-- Created: 2025-12-29

CREATE TABLE security_audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT COMMENT 'User who attempted the action',
    attempted_church_id BIGINT COMMENT 'Church ID that was attempted to be accessed',
    actual_church_id BIGINT COMMENT 'Actual church ID of the user',
    resource_type VARCHAR(100) COMMENT 'Type and ID of resource (e.g., Member:123)',
    violation_type VARCHAR(50) COMMENT 'Type of violation (e.g., CROSS_TENANT_ACCESS)',
    message TEXT COMMENT 'Detailed message about the violation',
    severity VARCHAR(20) COMMENT 'Severity: LOW, MEDIUM, HIGH, CRITICAL',
    ip_address VARCHAR(45) COMMENT 'IP address of the request',
    user_agent TEXT COMMENT 'User agent of the request',
    timestamp DATETIME NOT NULL COMMENT 'When the violation occurred',
    reviewed BOOLEAN DEFAULT FALSE COMMENT 'Whether reviewed by security team',
    review_notes TEXT COMMENT 'Notes from security team review',
    reviewed_at DATETIME COMMENT 'When reviewed',
    reviewed_by BIGINT COMMENT 'User ID who reviewed',

    -- Indexes for efficient queries
    INDEX idx_user_id (user_id),
    INDEX idx_timestamp (timestamp DESC),
    INDEX idx_user_timestamp (user_id, timestamp DESC),
    INDEX idx_church_timestamp (actual_church_id, timestamp DESC),
    INDEX idx_reviewed (reviewed, timestamp DESC),
    INDEX idx_severity (severity, timestamp DESC)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Security audit log for tracking violations and suspicious activity';
