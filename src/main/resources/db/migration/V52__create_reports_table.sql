-- Create reports table
CREATE TABLE reports (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    report_type VARCHAR(100) NOT NULL,
    is_custom BOOLEAN NOT NULL DEFAULT FALSE,
    filters TEXT,
    fields TEXT,
    sorting TEXT,
    grouping TEXT,
    created_by BIGINT,
    church_id BIGINT NOT NULL,
    is_template BOOLEAN DEFAULT FALSE,
    is_shared BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (church_id) REFERENCES churches(id) ON DELETE CASCADE,
    INDEX idx_church_type (church_id, report_type),
    INDEX idx_church_custom (church_id, is_custom),
    INDEX idx_church_template (church_id, is_template)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create report_shared_users table
CREATE TABLE report_shared_users (
    report_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    FOREIGN KEY (report_id) REFERENCES reports(id) ON DELETE CASCADE,
    INDEX idx_report_user (report_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
