-- Create crisis_affected_location table for multi-location support
-- Allows a single crisis to affect multiple geographic locations

CREATE TABLE crisis_affected_location (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    crisis_id BIGINT NOT NULL,
    suburb VARCHAR(100),
    city VARCHAR(100),
    district VARCHAR(100),
    region VARCHAR(100),
    country_code VARCHAR(2),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_crisis_affected_location_crisis
        FOREIGN KEY (crisis_id) REFERENCES crisis(id)
        ON DELETE CASCADE,

    -- Indexes for performance
    INDEX idx_crisis_affected_location_crisis (crisis_id),
    INDEX idx_crisis_affected_location_suburb (suburb),
    INDEX idx_crisis_affected_location_city (city),
    INDEX idx_crisis_affected_location_district (district),
    INDEX idx_crisis_affected_location_region (region),
    INDEX idx_crisis_affected_location_country (country_code)
);
