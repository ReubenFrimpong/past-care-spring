-- Phase 6: Prayer Requests Table
-- Allows members to submit prayer requests through the portal

CREATE TABLE IF NOT EXISTS prayer_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    request TEXT NOT NULL,
    category VARCHAR(50),
    priority VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    is_anonymous BOOLEAN NOT NULL DEFAULT FALSE,
    is_urgent BOOLEAN NOT NULL DEFAULT FALSE,
    is_public BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    answered_at DATETIME,
    testimony TEXT,
    expires_at DATETIME,
    church_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_prayer_request_member FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE,
    CONSTRAINT fk_prayer_request_church FOREIGN KEY (church_id) REFERENCES churches(id) ON DELETE CASCADE
);

-- Indexes for efficient querying
CREATE INDEX idx_prayer_request_member ON prayer_requests(member_id);
CREATE INDEX idx_prayer_request_church ON prayer_requests(church_id);
CREATE INDEX idx_prayer_request_status ON prayer_requests(status);
CREATE INDEX idx_prayer_request_created ON prayer_requests(created_at);
CREATE INDEX idx_prayer_request_public ON prayer_requests(is_public, status);
CREATE INDEX idx_prayer_request_urgent ON prayer_requests(is_urgent, church_id);
CREATE INDEX idx_prayer_request_expires ON prayer_requests(expires_at, status);
