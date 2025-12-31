-- Create complaints table
CREATE TABLE complaints (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    church_id BIGINT NOT NULL,
    submitted_by_user_id BIGINT NOT NULL,
    assigned_to_user_id BIGINT,
    category VARCHAR(50) NOT NULL,
    subject VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'SUBMITTED',
    priority VARCHAR(30) NOT NULL DEFAULT 'MEDIUM',
    admin_response TEXT,
    submitted_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    resolved_at DATETIME,
    is_anonymous BOOLEAN NOT NULL DEFAULT FALSE,
    contact_email VARCHAR(255),
    contact_phone VARCHAR(50),
    internal_notes TEXT,
    tags VARCHAR(500),

    CONSTRAINT fk_complaint_church FOREIGN KEY (church_id) REFERENCES churches(id) ON DELETE CASCADE,
    CONSTRAINT fk_complaint_submitted_by FOREIGN KEY (submitted_by_user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_complaint_assigned_to FOREIGN KEY (assigned_to_user_id) REFERENCES users(id) ON DELETE SET NULL,

    INDEX idx_complaint_church (church_id),
    INDEX idx_complaint_status (status),
    INDEX idx_complaint_category (category),
    INDEX idx_complaint_priority (priority),
    INDEX idx_complaint_submitted_by (submitted_by_user_id),
    INDEX idx_complaint_assigned_to (assigned_to_user_id),
    INDEX idx_complaint_submitted_at (submitted_at),
    INDEX idx_complaint_church_status (church_id, status)
);

-- Create complaint_activities table for audit trail
CREATE TABLE complaint_activities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    church_id BIGINT NOT NULL,
    complaint_id BIGINT NOT NULL,
    performed_by_user_id BIGINT NOT NULL,
    activity_type VARCHAR(50) NOT NULL,
    old_value VARCHAR(1000),
    new_value VARCHAR(1000),
    description TEXT,
    performed_at DATETIME NOT NULL,
    visible_to_complainant BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_activity_church FOREIGN KEY (church_id) REFERENCES churches(id) ON DELETE CASCADE,
    CONSTRAINT fk_activity_complaint FOREIGN KEY (complaint_id) REFERENCES complaints(id) ON DELETE CASCADE,
    CONSTRAINT fk_activity_performed_by FOREIGN KEY (performed_by_user_id) REFERENCES users(id) ON DELETE CASCADE,

    INDEX idx_activity_complaint (complaint_id),
    INDEX idx_activity_church (church_id),
    INDEX idx_activity_performed_at (performed_at),
    INDEX idx_activity_type (activity_type)
);
