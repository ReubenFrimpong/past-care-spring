-- Phase 6: Member Self-Service Portal
-- Creates portal_users table for member authentication and self-service access

CREATE TABLE IF NOT EXISTS portal_users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    member_id BIGINT UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING_VERIFICATION',
    verification_token VARCHAR(100),
    verification_token_expiry DATETIME,
    email_verified_at DATETIME,
    approved_at DATETIME,
    approved_by BIGINT,
    rejection_reason VARCHAR(500),
    last_login_at DATETIME,
    is_active BOOLEAN NOT NULL DEFAULT FALSE,
    password_reset_token VARCHAR(100),
    password_reset_token_expiry DATETIME,
    church_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_portal_user_member FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE,
    CONSTRAINT fk_portal_user_approved_by FOREIGN KEY (approved_by) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_portal_user_church FOREIGN KEY (church_id) REFERENCES churches(id) ON DELETE CASCADE,
    CONSTRAINT uk_portal_user_email_church UNIQUE (email, church_id),
    CONSTRAINT uk_portal_user_member UNIQUE (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Indexes for performance
CREATE INDEX idx_portal_users_email ON portal_users(email);
CREATE INDEX idx_portal_users_status ON portal_users(status);
CREATE INDEX idx_portal_users_church ON portal_users(church_id);
CREATE INDEX idx_portal_users_verification_token ON portal_users(verification_token);
CREATE INDEX idx_portal_users_password_reset_token ON portal_users(password_reset_token);
CREATE INDEX idx_portal_users_email_verified ON portal_users(email_verified_at);
CREATE INDEX idx_portal_users_approved ON portal_users(approved_at);

-- Comments
ALTER TABLE portal_users COMMENT = 'Phase 6: Portal users for member self-service access';
