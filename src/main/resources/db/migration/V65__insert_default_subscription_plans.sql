-- Insert default subscription plans for PastCare
-- This migration ensures subscription plans are available on fresh database installations

-- Check if plans already exist to avoid duplicates
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
        'STANDARD' as name,
        'PastCare Standard' as display_name,
        'Perfect for small to medium congregations' as description,
        9.99 as price,
        'MONTHLY' as billing_interval,
        2048 as storage_limit_mb,  -- 2 GB
        999999 as user_limit,
        0 as is_free,
        1 as is_active,
        1 as display_order,
        '["Unlimited Members","Unlimited Users","Member Management","Event Management","Attendance Tracking","Donation Tracking","SMS Notifications","Pastoral Care Tools","Advanced Analytics","Priority Support"]' as features,
        NOW() as created_at
) AS tmp
WHERE NOT EXISTS (
    SELECT 1 FROM subscription_plans WHERE name = 'STANDARD'
) LIMIT 1;
