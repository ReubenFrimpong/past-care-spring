-- Fellowship Phase 3: Member Retention Tracking
-- Create fellowship_member_history table to track membership changes

CREATE TABLE fellowship_member_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fellowship_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    action VARCHAR(20) NOT NULL,
    effective_date DATE NOT NULL,
    notes TEXT,
    recorded_by_id BIGINT,
    church_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_fmh_fellowship FOREIGN KEY (fellowship_id) REFERENCES fellowship(id) ON DELETE CASCADE,
    CONSTRAINT fk_fmh_member FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE,
    CONSTRAINT fk_fmh_recorded_by FOREIGN KEY (recorded_by_id) REFERENCES user(id) ON DELETE SET NULL,
    CONSTRAINT fk_fmh_church FOREIGN KEY (church_id) REFERENCES church(id) ON DELETE CASCADE,

    INDEX idx_fmh_fellowship (fellowship_id),
    INDEX idx_fmh_member (member_id),
    INDEX idx_fmh_date (effective_date),
    INDEX idx_fmh_action (action),
    INDEX idx_fmh_church (church_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Comment on enum values
-- action can be: JOINED, LEFT, TRANSFERRED_IN, TRANSFERRED_OUT, INACTIVE, REACTIVATED
