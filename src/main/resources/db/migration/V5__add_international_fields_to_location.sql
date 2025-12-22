-- Add international address fields to locations table
-- Supports addresses from all countries while maintaining Ghana-specific fields for backward compatibility

-- Add country fields
ALTER TABLE locations ADD COLUMN country_code VARCHAR(2) NOT NULL DEFAULT 'GH';
ALTER TABLE locations ADD COLUMN country_name VARCHAR(100) NOT NULL DEFAULT 'Ghana';

-- Add international address fields
ALTER TABLE locations ADD COLUMN state VARCHAR(100) COMMENT 'State (USA, Australia) or can map to region for Ghana';
ALTER TABLE locations ADD COLUMN province VARCHAR(100) COMMENT 'Province (Canada) or administrative division';
ALTER TABLE locations ADD COLUMN postal_code VARCHAR(20) COMMENT 'ZIP code (USA), Postcode (UK), Postal Code (Canada)';
ALTER TABLE locations ADD COLUMN address_line1 VARCHAR(200) COMMENT 'Primary address line (street number and name)';
ALTER TABLE locations ADD COLUMN address_line2 VARCHAR(200) COMMENT 'Secondary address line (apartment, suite, building)';

-- Create indexes for efficient querying
CREATE INDEX idx_country_code ON locations(country_code);
CREATE INDEX idx_postal_code ON locations(postal_code);

-- Update existing Ghana records to populate new fields
UPDATE locations
SET
    country_code = 'GH',
    country_name = 'Ghana',
    state = region
WHERE country_code IS NULL OR country_code = 'GH';

-- Add comments for documentation
ALTER TABLE locations MODIFY COLUMN country_code VARCHAR(2) NOT NULL
    COMMENT 'ISO 3166-1 alpha-2 country code (e.g., GH, US, GB, CA)';

ALTER TABLE locations MODIFY COLUMN country_name VARCHAR(100) NOT NULL
    COMMENT 'Full country name (e.g., Ghana, United States, United Kingdom)';
