-- Add country code field to member table for international phone validation
-- ISO 3166-1 alpha-2 country codes (e.g., GH, US, NG, UK, IN)

ALTER TABLE member ADD COLUMN country_code VARCHAR(10) DEFAULT 'GH';

-- Update existing records to have Ghana as default
UPDATE member SET country_code = 'GH' WHERE country_code IS NULL;

-- Add comment for documentation
ALTER TABLE member MODIFY COLUMN country_code VARCHAR(10)
COMMENT 'ISO 3166-1 alpha-2 country code (e.g., GH, US, NG)';
