-- Add timezone field to member table for international timezone support
-- IANA timezone database format (e.g., Africa/Accra, America/New_York, Europe/London)

ALTER TABLE member ADD COLUMN timezone VARCHAR(50) DEFAULT 'Africa/Accra';

-- Update existing records to have Africa/Accra (Ghana) as default
UPDATE member SET timezone = 'Africa/Accra' WHERE timezone IS NULL;

-- Add comment for documentation
ALTER TABLE member MODIFY COLUMN timezone VARCHAR(50)
COMMENT 'IANA timezone identifier (e.g., Africa/Accra, America/New_York)';
