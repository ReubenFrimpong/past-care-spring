-- Create partnership_codes table
CREATE TABLE partnership_codes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL,
    grace_period_days INT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    expires_at DATETIME NULL,
    max_uses INT NULL,
    current_uses INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_code (code),
    INDEX idx_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default partnership codes for testing
INSERT INTO partnership_codes (code, description, grace_period_days, is_active, expires_at, max_uses, current_uses, created_at, updated_at)
VALUES
    ('PARTNER2025', 'General partnership code for 2025', 30, TRUE, '2025-12-31 23:59:59', NULL, 0, NOW(), NOW()),
    ('TRIAL14', 'Standard 14-day trial', 14, TRUE, NULL, NULL, 0, NOW(), NOW()),
    ('LAUNCH2025', 'Launch promotion - 60 days', 60, TRUE, '2025-06-30 23:59:59', 100, 0, NOW(), NOW());
