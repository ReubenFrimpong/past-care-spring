-- Consolidate subscription plans to match final pricing model
-- Removes unused plans and ensures correct plans exist
-- Based on: PRICING_MODEL_REVISED.md
--
-- Final Pricing Model:
-- - STARTER: Free plan (2GB storage, limited features) - Demo/Test
-- - STANDARD: $9.99/month (2GB storage, all features) - Main production plan
--
-- Removes: BASIC, PRO, ENTERPRISE (not in pricing model)

-- ============================================================================
-- 1. Deactivate plans not in pricing model
-- ============================================================================

UPDATE subscription_plans
SET is_active = 0
WHERE name IN ('BASIC', 'PRO', 'ENTERPRISE')
  AND is_active = 1;

-- ============================================================================
-- 2. Remove STARTER/FREE plans (NO FREE PLAN MODEL)
-- ============================================================================
-- IMPORTANT: There is NO free plan in PastCare
-- Churches must pay GHC 150/month or use partnership codes for promotional access

DELETE FROM subscription_plans WHERE name IN ('STARTER', 'FREE', 'TRIAL');

-- ============================================================================
-- 3. Ensure STANDARD plan exists (Main Paid Plan - $9.99/month)
-- ============================================================================

-- First, update existing STANDARD plan if it exists
UPDATE subscription_plans
SET
    display_name = 'PastCare Standard',
    description = 'Perfect for churches of all sizes - Full featured plan',
    price = 9.99,
    billing_interval = 'MONTHLY',
    storage_limit_mb = 2048,  -- 2 GB base
    user_limit = 999999,      -- Unlimited users
    is_free = 0,
    is_active = 1,
    display_order = 1,
    features = '["Unlimited Members","Unlimited Users","Member Management","Event Management","Attendance Tracking","Donation Tracking","SMS Notifications","Pastoral Care Tools","Advanced Analytics","Priority Support","2GB Base Storage","Storage Add-ons Available"]'
WHERE name = 'STANDARD';

-- If STANDARD doesn't exist, insert it
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
    'STANDARD' as name,
    'PastCare Standard' as display_name,
    'Perfect for churches of all sizes - Full featured plan' as description,
    9.99 as price,
    'MONTHLY' as billing_interval,
    2048 as storage_limit_mb,  -- 2 GB base
    999999 as user_limit,      -- Unlimited users
    0 as is_free,
    1 as is_active,
    1 as display_order,
    '["Unlimited Members","Unlimited Users","Member Management","Event Management","Attendance Tracking","Donation Tracking","SMS Notifications","Pastoral Care Tools","Advanced Analytics","Priority Support","2GB Base Storage","Storage Add-ons Available"]' as features,
    NOW() as created_at
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM subscription_plans WHERE name = 'STANDARD'
)
LIMIT 1;

-- ============================================================================
-- 4. Summary of Active Plans
-- ============================================================================

-- After this migration, only these plans should be active:
-- 1. STARTER (Free, 2GB, limited features)
-- 2. STANDARD ($9.99/month, 2GB, all features)
--
-- Storage add-ons will be implemented separately in the storage_addons table
-- if needed for tracking additional storage purchases.

-- ============================================================================
-- 5. Update existing church subscriptions on deprecated plans
-- ============================================================================

-- Move churches from BASIC plan to STANDARD plan
UPDATE church_subscriptions cs
SET plan_id = (SELECT id FROM subscription_plans WHERE name = 'STANDARD' LIMIT 1)
WHERE plan_id IN (
    SELECT id FROM subscription_plans WHERE name = 'BASIC'
);

-- Move churches from PRO plan to STANDARD plan (they'll keep same features)
UPDATE church_subscriptions cs
SET plan_id = (SELECT id FROM subscription_plans WHERE name = 'STANDARD' LIMIT 1)
WHERE plan_id IN (
    SELECT id FROM subscription_plans WHERE name = 'PRO'
);

-- Move churches from ENTERPRISE plan to STANDARD plan
UPDATE church_subscriptions cs
SET plan_id = (SELECT id FROM subscription_plans WHERE name = 'STANDARD' LIMIT 1)
WHERE plan_id IN (
    SELECT id FROM subscription_plans WHERE name = 'ENTERPRISE'
);

-- ============================================================================
-- Verification Query (for manual check after migration)
-- ============================================================================

-- Run this to verify migration success:
-- SELECT name, display_name, price, storage_limit_mb, is_active, is_free
-- FROM subscription_plans
-- ORDER BY display_order;
--
-- Expected result:
-- STARTER   | Starter Plan        | 0.00  | 2048 | 1 | 1
-- STANDARD  | PastCare Standard  | 9.99  | 2048 | 1 | 0
-- BASIC     | (deactivated)       | ...   | ...  | 0 | 0
-- PRO       | (deactivated)       | ...   | ...  | 0 | 0
-- ENTERPRISE| (deactivated)       | ...   | ...  | 0 | 0
