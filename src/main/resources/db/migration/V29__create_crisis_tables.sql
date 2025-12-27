-- Create crisis table
CREATE TABLE crisis (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    church_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    crisis_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    reported_by_user_id BIGINT NOT NULL,
    reported_date DATETIME(6) NOT NULL,
    incident_date DATETIME(6),
    location VARCHAR(200),
    affected_members_count INT,
    response_team_notes TEXT,
    resolution_notes TEXT,
    resolved_date DATETIME(6),
    follow_up_required BOOLEAN DEFAULT FALSE,
    follow_up_date DATETIME(6),
    resources_mobilized VARCHAR(500),
    communication_sent BOOLEAN DEFAULT FALSE,
    emergency_contact_notified BOOLEAN DEFAULT FALSE,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT fk_crisis_church FOREIGN KEY (church_id) REFERENCES churches(id) ON DELETE CASCADE,
    CONSTRAINT fk_crisis_reported_by FOREIGN KEY (reported_by_user_id) REFERENCES users(id) ON DELETE RESTRICT,

    INDEX idx_crisis_church (church_id),
    INDEX idx_crisis_reported_by (reported_by_user_id),
    INDEX idx_crisis_status (status),
    INDEX idx_crisis_type (crisis_type),
    INDEX idx_crisis_severity (severity),
    INDEX idx_crisis_incident_date (incident_date),
    INDEX idx_crisis_reported_date (reported_date)
);

-- Create crisis_affected_member junction table
CREATE TABLE crisis_affected_member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    crisis_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    notes VARCHAR(500),
    is_primary_contact BOOLEAN DEFAULT FALSE,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT fk_crisis_affected_member_crisis FOREIGN KEY (crisis_id) REFERENCES crisis(id) ON DELETE CASCADE,
    CONSTRAINT fk_crisis_affected_member_member FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE,

    INDEX idx_crisis_affected_member_crisis (crisis_id),
    INDEX idx_crisis_affected_member_member (member_id),

    UNIQUE KEY uk_crisis_member (crisis_id, member_id)
);
