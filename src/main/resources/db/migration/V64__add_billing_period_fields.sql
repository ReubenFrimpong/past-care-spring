-- Add billing period fields to church_subscriptions table
-- This supports multi-period billing (monthly, quarterly, biannual, yearly)

ALTER TABLE church_subscriptions
ADD COLUMN billing_period VARCHAR(20) DEFAULT 'MONTHLY' AFTER auto_renew,
ADD COLUMN billing_period_months INT DEFAULT 1 AFTER billing_period;

-- Add comment explaining the fields
ALTER TABLE church_subscriptions
MODIFY COLUMN billing_period VARCHAR(20) DEFAULT 'MONTHLY'
COMMENT 'Billing period: MONTHLY, QUARTERLY (3 months), BIANNUAL (6 months), YEARLY (12 months)';

ALTER TABLE church_subscriptions
MODIFY COLUMN billing_period_months INT DEFAULT 1
COMMENT 'Number of months in the billing period (1, 3, 6, or 12)';

-- Create index for billing period queries
CREATE INDEX idx_church_subscriptions_billing_period ON church_subscriptions(billing_period);

-- Update existing subscriptions to have default billing period
UPDATE church_subscriptions
SET billing_period = 'MONTHLY',
    billing_period_months = 1
WHERE billing_period IS NULL OR billing_period_months IS NULL;
