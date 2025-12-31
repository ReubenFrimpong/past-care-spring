-- Create invitation_codes table
CREATE TABLE invitation_codes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    church_id BIGINT NOT NULL,
    created_by_user_id BIGINT NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    max_uses INT,
    used_count INT NOT NULL DEFAULT 0,
    expires_at DATETIME,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    default_role VARCHAR(30) NOT NULL DEFAULT 'MEMBER',
    created_at DATETIME NOT NULL,
    last_used_at DATETIME,

    CONSTRAINT fk_invitation_church FOREIGN KEY (church_id) REFERENCES churches(id) ON DELETE CASCADE,
    CONSTRAINT fk_invitation_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id) ON DELETE CASCADE,

    INDEX idx_invitation_church (church_id),
    INDEX idx_invitation_code (code),
    INDEX idx_invitation_active (is_active),
    INDEX idx_invitation_expires (expires_at)
);
