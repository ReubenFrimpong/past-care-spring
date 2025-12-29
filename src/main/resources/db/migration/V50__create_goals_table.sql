-- V50: Create goals table for goal tracking functionality
-- Dashboard Phase 2.3: Goal Tracking
-- Allows churches to set and track goals for attendance, giving, members, and events

CREATE TABLE goals (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    church_id BIGINT NOT NULL COMMENT 'The church this goal belongs to (tenant isolation)',
    goal_type VARCHAR(50) NOT NULL COMMENT 'ATTENDANCE, GIVING, MEMBERS, EVENTS',
    target_value DECIMAL(15,2) NOT NULL COMMENT 'The target value to achieve',
    current_value DECIMAL(15,2) DEFAULT 0 COMMENT 'Current progress towards goal',
    start_date DATE NOT NULL COMMENT 'When the goal period starts',
    end_date DATE NOT NULL COMMENT 'When the goal period ends',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT 'ACTIVE, COMPLETED, FAILED, CANCELLED',
    title VARCHAR(200) NOT NULL COMMENT 'Goal title/name',
    description TEXT COMMENT 'Optional goal description',
    created_by BIGINT NOT NULL COMMENT 'User who created this goal',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_goal_church FOREIGN KEY (church_id) REFERENCES churches(id) ON DELETE CASCADE,
    CONSTRAINT fk_goal_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE RESTRICT,

    INDEX idx_goal_church_id (church_id),
    INDEX idx_goal_type (goal_type),
    INDEX idx_goal_status (status),
    INDEX idx_goal_dates (start_date, end_date),
    INDEX idx_goal_church_status (church_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
