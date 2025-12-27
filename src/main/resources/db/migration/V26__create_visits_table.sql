-- Create visits table
CREATE TABLE visits (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    church_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    care_need_id BIGINT,
    type VARCHAR(20) NOT NULL,
    visit_date DATE NOT NULL,
    start_time TIME,
    end_time TIME,
    location_id BIGINT,
    location_details VARCHAR(500),
    purpose TEXT,
    notes TEXT,
    outcomes TEXT,
    follow_up_required BOOLEAN DEFAULT FALSE,
    follow_up_date DATE,
    is_completed BOOLEAN DEFAULT FALSE,
    is_confidential BOOLEAN DEFAULT FALSE,
    created_by_id BIGINT,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    
    CONSTRAINT fk_visit_church FOREIGN KEY (church_id) REFERENCES churches(id) ON DELETE CASCADE,
    CONSTRAINT fk_visit_member FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE,
    CONSTRAINT fk_visit_care_need FOREIGN KEY (care_need_id) REFERENCES care_needs(id) ON DELETE SET NULL,
    CONSTRAINT fk_visit_location FOREIGN KEY (location_id) REFERENCES locations(id) ON DELETE SET NULL,
    CONSTRAINT fk_visit_created_by FOREIGN KEY (created_by_id) REFERENCES users(id) ON DELETE SET NULL,
    
    INDEX idx_visit_church (church_id),
    INDEX idx_visit_member (member_id),
    INDEX idx_visit_care_need (care_need_id),
    INDEX idx_visit_date (visit_date),
    INDEX idx_visit_type (type),
    INDEX idx_visit_completed (is_completed),
    INDEX idx_visit_created_at (created_at)
);

-- Create visit_attendees join table
CREATE TABLE visit_attendees (
    visit_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    
    PRIMARY KEY (visit_id, user_id),
    CONSTRAINT fk_visit_attendees_visit FOREIGN KEY (visit_id) REFERENCES visits(id) ON DELETE CASCADE,
    CONSTRAINT fk_visit_attendees_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    INDEX idx_visit_attendees_visit (visit_id),
    INDEX idx_visit_attendees_user (user_id)
);
