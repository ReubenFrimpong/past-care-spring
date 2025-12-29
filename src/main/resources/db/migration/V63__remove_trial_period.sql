-- Remove trial period from subscription system
-- All new churches start directly on FREE STARTER plan

-- Update existing TRIALING subscriptions to ACTIVE
UPDATE church_subscriptions
SET status = 'ACTIVE'
WHERE status = 'TRIALING';

-- Drop trial_end_date column (no longer needed)
ALTER TABLE church_subscriptions
DROP COLUMN IF EXISTS trial_end_date;

-- Update existing free plan subscriptions to have no billing dates
UPDATE church_subscriptions cs
INNER JOIN subscription_plans sp ON cs.plan_id = sp.id
SET cs.next_billing_date = NULL,
    cs.current_period_end = NULL,
    cs.auto_renew = false
WHERE sp.is_free = true;

-- Add comment explaining the change
-- No trial period: Churches start on FREE plan immediately
-- They can upgrade at any time by subscribing to a paid plan
