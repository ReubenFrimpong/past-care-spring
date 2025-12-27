-- SMS Rates Table (Local and International Pricing)
CREATE TABLE sms_rates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    church_id BIGINT,
    country_code VARCHAR(10) NOT NULL,
    country_name VARCHAR(100) NOT NULL,
    rate_per_sms DECIMAL(10, 4) NOT NULL,
    is_local BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    description VARCHAR(500),
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    FOREIGN KEY (church_id) REFERENCES churches(id) ON DELETE CASCADE,
    INDEX idx_country_code (country_code),
    INDEX idx_church_active (church_id, is_active),
    INDEX idx_is_local (is_local)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default rates (based on Africa's Talking pricing)
-- Ghana (Local) - approximately 0.02 GHS per SMS
INSERT INTO sms_rates (church_id, country_code, country_name, rate_per_sms, is_local, is_active, description, created_at, updated_at)
VALUES (NULL, '+233', 'Ghana', 0.0200, TRUE, TRUE, 'Local Ghana SMS rate', NOW(), NOW());

-- International rates (sample - should be updated based on actual Africa''s Talking pricing)
INSERT INTO sms_rates (church_id, country_code, country_name, rate_per_sms, is_local, is_active, description, created_at, updated_at)
VALUES
(NULL, '+1', 'USA/Canada', 0.0800, FALSE, TRUE, 'USA and Canada SMS rate', NOW(), NOW()),
(NULL, '+44', 'United Kingdom', 0.0700, FALSE, TRUE, 'UK SMS rate', NOW(), NOW()),
(NULL, '+234', 'Nigeria', 0.0400, FALSE, TRUE, 'Nigeria SMS rate', NOW(), NOW()),
(NULL, '+254', 'Kenya', 0.0400, FALSE, TRUE, 'Kenya SMS rate', NOW(), NOW()),
(NULL, '+27', 'South Africa', 0.0400, FALSE, TRUE, 'South Africa SMS rate', NOW(), NOW()),
(NULL, '+256', 'Uganda', 0.0400, FALSE, TRUE, 'Uganda SMS rate', NOW(), NOW()),
(NULL, '+255', 'Tanzania', 0.0400, FALSE, TRUE, 'Tanzania SMS rate', NOW(), NOW()),
(NULL, '+250', 'Rwanda', 0.0400, FALSE, TRUE, 'Rwanda SMS rate', NOW(), NOW()),
(NULL, 'OTHER', 'Other Countries', 0.1000, FALSE, TRUE, 'Default rate for other countries', NOW(), NOW());
