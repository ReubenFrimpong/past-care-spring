-- Create care_needs table for pastoral care management
CREATE TABLE care_needs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    church_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    type VARCHAR(50) NOT NULL,
    priority VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    assigned_to_user_id BIGINT,
    created_by_user_id BIGINT NOT NULL,
    due_date DATETIME,
    resolved_date DATETIME,
    resolution_notes TEXT,
    follow_up_required BOOLEAN NOT NULL DEFAULT FALSE,
    follow_up_date DATETIME,
    follow_up_status VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (church_id) REFERENCES church(id),
    FOREIGN KEY (member_id) REFERENCES member(id),
    FOREIGN KEY (assigned_to_user_id) REFERENCES user(id),
    FOREIGN KEY (created_by_user_id) REFERENCES user(id),

    INDEX idx_care_need_member (member_id),
    INDEX idx_care_need_status (status),
    INDEX idx_care_need_priority (priority),
    INDEX idx_care_need_assigned_to (assigned_to_user_id),
    INDEX idx_care_need_church (church_id),
    INDEX idx_care_need_created_at (created_at)
);

-- Create care_need_tags table for flexible categorization
CREATE TABLE care_need_tags (
    care_need_id BIGINT NOT NULL,
    tag VARCHAR(50) NOT NULL,
    FOREIGN KEY (care_need_id) REFERENCES care_needs(id) ON DELETE CASCADE,
    INDEX idx_care_need_tags (care_need_id)
);
