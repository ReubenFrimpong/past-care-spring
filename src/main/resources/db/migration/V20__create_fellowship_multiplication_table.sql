-- Create fellowship_multiplication table for tracking fellowship multiplication events
CREATE TABLE fellowship_multiplication (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_fellowship_id BIGINT NOT NULL,
    child_fellowship_id BIGINT NOT NULL,
    multiplication_date DATE NOT NULL,
    reason TEXT,
    members_transferred INT,
    notes TEXT,
    recorded_by_id BIGINT,
    church_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Foreign keys
    CONSTRAINT fk_fm_parent_fellowship FOREIGN KEY (parent_fellowship_id) REFERENCES fellowship(id) ON DELETE CASCADE,
    CONSTRAINT fk_fm_child_fellowship FOREIGN KEY (child_fellowship_id) REFERENCES fellowship(id) ON DELETE CASCADE,
    CONSTRAINT fk_fm_recorded_by FOREIGN KEY (recorded_by_id) REFERENCES user(id) ON DELETE SET NULL,
    CONSTRAINT fk_fm_church FOREIGN KEY (church_id) REFERENCES church(id) ON DELETE CASCADE,

    -- Indexes for performance
    INDEX idx_fm_parent (parent_fellowship_id),
    INDEX idx_fm_child (child_fellowship_id),
    INDEX idx_fm_date (multiplication_date),
    INDEX idx_fm_church (church_id)
);
