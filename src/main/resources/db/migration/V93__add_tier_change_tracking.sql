-- Add tier change tracking columns to church_subscriptions table
-- This enables tracking of tier upgrades and prevents concurrent upgrade attempts

ALTER TABLE church_subscriptions
ADD COLUMN last_tier_change_date DATETIME(6),
ADD COLUMN last_tier_id BIGINT,
ADD COLUMN pending_tier_change BOOLEAN DEFAULT FALSE,
ADD COLUMN pending_tier_id BIGINT,
ADD COLUMN pending_billing_interval_id BIGINT,
ADD COLUMN tier_change_count INTEGER DEFAULT 0;

-- Add foreign key constraint to track previous tier
ALTER TABLE church_subscriptions
ADD CONSTRAINT fk_church_subscription_last_tier
FOREIGN KEY (last_tier_id) REFERENCES congregation_pricing_tiers(id);

-- Add foreign key constraint for pending tier (used during payment processing)
ALTER TABLE church_subscriptions
ADD CONSTRAINT fk_church_subscription_pending_tier
FOREIGN KEY (pending_tier_id) REFERENCES congregation_pricing_tiers(id);

-- Add foreign key constraint for pending billing interval
ALTER TABLE church_subscriptions
ADD CONSTRAINT fk_church_subscription_pending_interval
FOREIGN KEY (pending_billing_interval_id) REFERENCES subscription_billing_intervals(id);

-- Add index for pending tier changes to quickly find churches with pending upgrades
CREATE INDEX idx_church_subscriptions_pending_tier_change
ON church_subscriptions(pending_tier_change)
WHERE pending_tier_change = TRUE;

-- Add index for tier change count for analytics
CREATE INDEX idx_church_subscriptions_tier_change_count
ON church_subscriptions(tier_change_count);
