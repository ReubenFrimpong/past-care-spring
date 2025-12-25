-- Fellowship Module Phase 1: Enhanced Fellowship Management
-- Add fields for fellowship leaders, meeting schedule, types, capacity, and images

-- Add new columns to fellowship table
ALTER TABLE fellowship
ADD COLUMN description TEXT COMMENT 'Fellowship description and purpose',
ADD COLUMN image_url VARCHAR(500) COMMENT 'Fellowship image/logo URL',
ADD COLUMN fellowship_type VARCHAR(50) NOT NULL DEFAULT 'OTHER' COMMENT 'Type of fellowship',
ADD COLUMN leader_id BIGINT COMMENT 'Primary fellowship leader',
ADD COLUMN meeting_day VARCHAR(20) COMMENT 'Day of week for regular meetings',
ADD COLUMN meeting_time TIME COMMENT 'Time for regular meetings',
ADD COLUMN meeting_location_id BIGINT COMMENT 'Location where fellowship meets',
ADD COLUMN max_capacity INT COMMENT 'Maximum number of members',
ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Whether fellowship is active',
ADD COLUMN accepting_members BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Whether fellowship is accepting new members';

-- Add foreign key constraints
ALTER TABLE fellowship
ADD CONSTRAINT fk_fellowship_leader
  FOREIGN KEY (leader_id) REFERENCES users(id)
  ON DELETE SET NULL,
ADD CONSTRAINT fk_fellowship_meeting_location
  FOREIGN KEY (meeting_location_id) REFERENCES locations(id)
  ON DELETE SET NULL;

-- Add indexes for better query performance
CREATE INDEX idx_fellowship_type ON fellowship(fellowship_type);
CREATE INDEX idx_fellowship_leader ON fellowship(leader_id);
CREATE INDEX idx_fellowship_is_active ON fellowship(is_active);
CREATE INDEX idx_fellowship_accepting_members ON fellowship(accepting_members);

-- Create co-leaders junction table for multiple co-leaders
CREATE TABLE fellowship_coleaders (
  fellowship_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  PRIMARY KEY (fellowship_id, user_id),
  CONSTRAINT fk_fellowship_coleaders_fellowship
    FOREIGN KEY (fellowship_id) REFERENCES fellowship(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_fellowship_coleaders_user
    FOREIGN KEY (user_id) REFERENCES users(id)
    ON DELETE CASCADE
);

CREATE INDEX idx_fellowship_coleaders_fellowship ON fellowship_coleaders(fellowship_id);
CREATE INDEX idx_fellowship_coleaders_user ON fellowship_coleaders(user_id);

-- Create fellowship join requests table
CREATE TABLE fellowship_join_requests (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  fellowship_id BIGINT NOT NULL,
  member_id BIGINT NOT NULL,
  request_message TEXT COMMENT 'Optional message from member',
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, APPROVED, REJECTED',
  requested_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  reviewed_at DATETIME COMMENT 'When request was reviewed',
  reviewed_by BIGINT COMMENT 'User who reviewed the request',
  review_notes TEXT COMMENT 'Notes from reviewer',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_join_request_fellowship
    FOREIGN KEY (fellowship_id) REFERENCES fellowship(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_join_request_member
    FOREIGN KEY (member_id) REFERENCES members(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_join_request_reviewer
    FOREIGN KEY (reviewed_by) REFERENCES users(id)
    ON DELETE SET NULL
);

CREATE INDEX idx_join_request_fellowship ON fellowship_join_requests(fellowship_id);
CREATE INDEX idx_join_request_member ON fellowship_join_requests(member_id);
CREATE INDEX idx_join_request_status ON fellowship_join_requests(status);
CREATE INDEX idx_join_request_requested_at ON fellowship_join_requests(requested_at);

-- Ensure unique constraint on active join requests (one pending request per member per fellowship)
CREATE UNIQUE INDEX idx_unique_pending_join_request
ON fellowship_join_requests(fellowship_id, member_id, status)
WHERE status = 'PENDING';
