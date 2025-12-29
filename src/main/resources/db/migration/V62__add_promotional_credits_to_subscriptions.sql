-- Add promotional credit fields to church_subscriptions table
-- Allows offering free months, discounts, or credits to specific churches

ALTER TABLE church_subscriptions
ADD COLUMN free_months_remaining INT DEFAULT 0 COMMENT 'Number of free months remaining (promotional)',
ADD COLUMN promotional_note VARCHAR(255) COMMENT 'Note about the promotional credit (e.g., "Holiday promotion", "Referral bonus")',
ADD COLUMN promotional_granted_by BIGINT COMMENT 'User ID who granted the promotional credit',
ADD COLUMN promotional_granted_at TIMESTAMP COMMENT 'When the promotional credit was granted';

-- Create index for querying subscriptions with promotional credits
CREATE INDEX idx_church_subscriptions_free_months ON church_subscriptions(free_months_remaining);

-- Add comment to table
ALTER TABLE church_subscriptions COMMENT = 'Church subscriptions with billing, trial, and promotional credit tracking';
