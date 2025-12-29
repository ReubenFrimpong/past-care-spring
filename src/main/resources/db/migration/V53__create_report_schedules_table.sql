-- Create report_schedules table
CREATE TABLE report_schedules (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    report_id BIGINT NOT NULL,
    frequency VARCHAR(50) NOT NULL,
    day_of_week INT,
    day_of_month INT,
    execution_time TIME NOT NULL,
    format VARCHAR(50) NOT NULL,
    next_execution_date TIMESTAMP,
    last_execution_date TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    church_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (report_id) REFERENCES reports(id) ON DELETE CASCADE,
    FOREIGN KEY (church_id) REFERENCES churches(id) ON DELETE CASCADE,
    INDEX idx_church_active (church_id, is_active),
    INDEX idx_next_execution (next_execution_date, is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create report_schedule_recipients table
CREATE TABLE report_schedule_recipients (
    schedule_id BIGINT NOT NULL,
    email VARCHAR(255) NOT NULL,
    FOREIGN KEY (schedule_id) REFERENCES report_schedules(id) ON DELETE CASCADE,
    INDEX idx_schedule_email (schedule_id, email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
