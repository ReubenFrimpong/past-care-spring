-- V51: Create insights table for advanced analytics
-- Dashboard Phase 2.4: Advanced Analytics
-- Stores AI-generated insights, anomalies, and recommendations for church management

CREATE TABLE insights (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    church_id BIGINT NOT NULL COMMENT 'The church this insight belongs to (tenant isolation)',
    insight_type VARCHAR(50) NOT NULL COMMENT 'ANOMALY, TREND, PREDICTION, RECOMMENDATION, WARNING',
    category VARCHAR(50) NOT NULL COMMENT 'ATTENDANCE, GIVING, MEMBERS, ENGAGEMENT, PASTORAL_CARE',
    title VARCHAR(200) NOT NULL COMMENT 'Insight title/headline',
    description TEXT NOT NULL COMMENT 'Detailed insight description',
    severity VARCHAR(20) DEFAULT 'INFO' COMMENT 'INFO, LOW, MEDIUM, HIGH, CRITICAL',
    actionable BOOLEAN DEFAULT FALSE COMMENT 'Whether this insight requires action',
    action_url VARCHAR(500) COMMENT 'Optional URL to take action',
    dismissed BOOLEAN DEFAULT FALSE COMMENT 'Whether user has dismissed this insight',
    dismissed_at TIMESTAMP NULL COMMENT 'When the insight was dismissed',
    dismissed_by BIGINT COMMENT 'User who dismissed the insight',
    metadata JSON COMMENT 'Additional metadata as JSON (data points, thresholds, etc.)',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_insight_church FOREIGN KEY (church_id) REFERENCES churches(id) ON DELETE CASCADE,
    CONSTRAINT fk_insight_dismissed_by FOREIGN KEY (dismissed_by) REFERENCES users(id) ON DELETE SET NULL,

    INDEX idx_insight_church_id (church_id),
    INDEX idx_insight_type (insight_type),
    INDEX idx_insight_category (category),
    INDEX idx_insight_severity (severity),
    INDEX idx_insight_dismissed (dismissed),
    INDEX idx_insight_church_dismissed (church_id, dismissed),
    INDEX idx_insight_created (created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
