-- Giving Module Phase 3: Pledge & Campaign Management
-- Update donation table to link with campaign and pledge entities

-- Add campaign_id foreign key
ALTER TABLE donation ADD COLUMN campaign_id BIGINT AFTER campaign;

-- Add pledge_id foreign key
ALTER TABLE donation ADD COLUMN pledge_id BIGINT AFTER campaign_id;

-- Add foreign key constraints
ALTER TABLE donation
ADD CONSTRAINT fk_donation_campaign FOREIGN KEY (campaign_id) REFERENCES campaign(id) ON DELETE SET NULL;

ALTER TABLE donation
ADD CONSTRAINT fk_donation_pledge FOREIGN KEY (pledge_id) REFERENCES pledge(id) ON DELETE SET NULL;

-- Add indexes for performance
CREATE INDEX idx_donation_campaign ON donation(campaign_id);
CREATE INDEX idx_donation_pledge ON donation(pledge_id);

-- The existing 'campaign' VARCHAR column is kept for backward compatibility
-- New donations will use campaign_id foreign key
