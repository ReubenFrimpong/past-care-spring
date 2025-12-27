-- Add geographic search fields to crisis table for auto-detecting affected members
ALTER TABLE crisis
    ADD COLUMN affected_suburb VARCHAR(100),
    ADD COLUMN affected_city VARCHAR(100),
    ADD COLUMN affected_district VARCHAR(100),
    ADD COLUMN affected_region VARCHAR(100),
    ADD COLUMN affected_country_code VARCHAR(2);

-- Add indexes for geographic queries
CREATE INDEX idx_crisis_affected_suburb ON crisis(affected_suburb);
CREATE INDEX idx_crisis_affected_city ON crisis(affected_city);
CREATE INDEX idx_crisis_affected_region ON crisis(affected_region);
CREATE INDEX idx_crisis_affected_country ON crisis(affected_country_code);
