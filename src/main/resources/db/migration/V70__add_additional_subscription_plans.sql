-- Add additional subscription tiers (BASIC, PRO, ENTERPRISE)

-- BASIC Plan
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
SELECT * FROM (
    SELECT
        'BASIC' as name,
        'Basic Plan' as display_name,
        'Perfect for small churches just getting started' as description,
        9.99 as price,
        'MONTHLY' as billing_interval,
        2048 as storage_limit_mb,
        999999 as user_limit,
        0 as is_free,
        1 as is_active,
        1 as display_order,
        '["Unlimited Members","Up to 5 Users","Member Management","Event Management","Attendance Tracking","Donation Tracking","Email Support"]' as features,
        NOW() as created_at
) AS tmp
WHERE NOT EXISTS (
    SELECT 1 FROM subscription_plans WHERE name = 'BASIC'
) LIMIT 1;

-- PRO Plan
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
SELECT * FROM (
    SELECT
        'PRO' as name,
        'Pro Plan' as display_name,
        'For growing churches with advanced needs' as description,
        19.99 as price,
        'MONTHLY' as billing_interval,
        10240 as storage_limit_mb,
        999999 as user_limit,
        0 as is_free,
        1 as is_active,
        2 as display_order,
        '["Unlimited Members","Unlimited Users","Member Management","Event Management","Attendance Tracking","Donation Tracking","SMS Notifications","Pastoral Care Tools","Advanced Analytics","Priority Support","Custom Reports"]' as features,
        NOW() as created_at
) AS tmp
WHERE NOT EXISTS (
    SELECT 1 FROM subscription_plans WHERE name = 'PRO'
) LIMIT 1;

-- ENTERPRISE Plan
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
SELECT * FROM (
    SELECT
        'ENTERPRISE' as name,
        'Enterprise Plan' as display_name,
        'For large congregations with enterprise features' as description,
        49.99 as price,
        'MONTHLY' as billing_interval,
        51200 as storage_limit_mb,
        999999 as user_limit,
        0 as is_free,
        1 as is_active,
        3 as display_order,
        '["Unlimited Members","Unlimited Users","Member Management","Event Management","Attendance Tracking","Donation Tracking","SMS Notifications","Pastoral Care Tools","Advanced Analytics","Priority Support","Custom Reports","Multi-site Support","Dedicated Account Manager","Custom Integrations"]' as features,
        NOW() as created_at
) AS tmp
WHERE NOT EXISTS (
    SELECT 1 FROM subscription_plans WHERE name = 'ENTERPRISE'
) LIMIT 1;
