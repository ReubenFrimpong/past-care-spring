-- Add Unlimited Storage Plan for GHS 499/month
-- This migration adds a premium unlimited storage plan

-- ============================================================================
-- Insert UNLIMITED plan
-- ============================================================================

INSERT INTO subscription_plans (
    name,
    display_name,
    description,
    price,
    billing_interval,
    storage_limit_mb,
    user_limit,
    is_free,
    is_active,
    display_order,
    features,
    created_at
)
SELECT
    'UNLIMITED' as name,
    'PastCare Unlimited' as display_name,
    'For large churches with extensive media needs' as description,
    499.00 as price,
    'MONTHLY' as billing_interval,
    -1 as storage_limit_mb,  -- -1 indicates unlimited storage
    999999 as user_limit,    -- Unlimited users
    0 as is_free,
    1 as is_active,
    2 as display_order,
    '["Unlimited Members","Unlimited Users","Member Management","Event Management","Attendance Tracking","Donation Tracking","SMS Notifications","Pastoral Care Tools","Advanced Analytics","Priority Support","Unlimited Storage","No Upload Limits","Premium Features"]' as features,
    NOW() as created_at
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM subscription_plans WHERE name = 'UNLIMITED'
)
LIMIT 1;

-- ============================================================================
-- Update existing UNLIMITED plan if it already exists
-- ============================================================================

UPDATE subscription_plans
SET
    display_name = 'PastCare Unlimited',
    description = 'For large churches with extensive media needs',
    price = 499.00,
    billing_interval = 'MONTHLY',
    storage_limit_mb = -1,  -- -1 indicates unlimited storage
    user_limit = 999999,
    is_free = 0,
    is_active = 1,
    display_order = 2,
    features = '["Unlimited Members","Unlimited Users","Member Management","Event Management","Attendance Tracking","Donation Tracking","SMS Notifications","Pastoral Care Tools","Advanced Analytics","Priority Support","Unlimited Storage","No Upload Limits","Premium Features"]'
WHERE name = 'UNLIMITED';
