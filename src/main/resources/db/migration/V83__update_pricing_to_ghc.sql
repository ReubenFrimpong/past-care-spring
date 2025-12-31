-- Update pricing model from USD to GHC (Ghana Cedis)
-- Reason: Currently unable to charge in USD
--
-- IMPORTANT: NO FREE PLAN
-- - STANDARD: GHC 150.00/month - ONLY plan (no free/trial/starter plans)
--
-- Note: This replaces V81 pricing which was in USD ($9.99)

-- ============================================================================
-- 1. Update STANDARD plan to GHC 150
-- ============================================================================

UPDATE subscription_plans
SET
    price = 150.00,
    description = 'Perfect for churches of all sizes - Full featured plan (GHC 150/month)',
    features = '["Unlimited Members","Unlimited Users","Member Management","Event Management","Attendance Tracking","Donation Tracking","SMS Notifications","Pastoral Care Tools","Advanced Analytics","Priority Support","2GB Base Storage","Storage Add-ons Available"]'
WHERE name = 'STANDARD';

-- ============================================================================
-- 2. Remove any FREE/STARTER/TRIAL plans (NO FREE PLAN MODEL)
-- ============================================================================

DELETE FROM subscription_plans WHERE name IN ('STARTER', 'FREE', 'TRIAL');

-- ============================================================================
-- 3. Update storage add-on pricing (if they exist)
-- ============================================================================

-- Storage add-ons in GHC (converted from USD with approximate rate):
-- Original USD pricing × 15 = GHC pricing
--
-- +3 GB:  $1.50  → GHC 22.50
-- +8 GB:  $3.00  → GHC 45.00
-- +18 GB: $6.00  → GHC 90.00
-- +48 GB: $12.00 → GHC 180.00
--
-- Note: Storage add-ons table may not exist yet
-- This is a placeholder for future implementation

-- ============================================================================
-- Verification Query
-- ============================================================================

-- Run this to verify pricing update:
-- SELECT name, display_name, price, billing_interval, is_active, is_free
-- FROM subscription_plans
-- WHERE is_active = 1
-- ORDER BY display_order;
--
-- Expected result:
-- STANDARD | PastCare Standard   | 150.00 | MONTHLY | 1 | 0
-- (NO OTHER PLANS - single plan model)

-- ============================================================================
-- IMPORTANT NOTES
-- ============================================================================

-- Currency: GHC (Ghana Cedis)
-- Exchange Rate Used: Approximately 1 USD = 15 GHC
-- Standard Plan: GHC 150.00/month (previously $9.99)
-- Payment Gateway: Paystack (supports GHC payments)
--
-- This pricing change is permanent. If you need to revert to USD:
-- UPDATE subscription_plans SET price = 9.99 WHERE name = 'STANDARD';
