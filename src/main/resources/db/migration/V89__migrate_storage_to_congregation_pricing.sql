-- ============================================================================
-- Migration: V89 - Migrate from Storage-Based to Congregation-Based Pricing
-- ============================================================================
-- Purpose: Migrate all existing churches from old storage-based pricing model
--          to new congregation-size-based pricing model
-- Date: 2026-01-01
-- Author: PastCare Team
--
-- This migration:
-- 1. Updates all churches with current member counts
-- 2. Assigns appropriate pricing tiers based on congregation size
-- 3. Migrates church_subscriptions to use new pricing tiers
-- 4. Marks old subscription_plans as deprecated
-- 5. Creates audit trail in pricing_model_migrations
-- ============================================================================

-- ============================================================================
-- Step 1: Update churches with current member counts
-- ============================================================================

UPDATE churches c
SET
    cached_member_count = (
        SELECT COUNT(*)
        FROM members m
        WHERE m.church_id = c.id
    ),
    member_count_last_updated = NOW();

-- ============================================================================
-- Step 2: Assign eligible pricing tiers based on member count
-- ============================================================================

UPDATE churches c
SET eligible_pricing_tier_id = (
    SELECT id
    FROM congregation_pricing_tiers cpt
    WHERE c.cached_member_count >= cpt.min_members
      AND (cpt.max_members IS NULL OR c.cached_member_count <= cpt.max_members)
      AND cpt.is_active = TRUE
    ORDER BY cpt.min_members DESC
    LIMIT 1
);

-- ============================================================================
-- Step 3: Update church_subscriptions with new pricing tiers
-- ============================================================================

-- Set pricing_tier_id for all active subscriptions
UPDATE church_subscriptions cs
JOIN churches c ON cs.church_id = c.id
SET
    cs.pricing_tier_id = c.eligible_pricing_tier_id,
    cs.current_member_count = c.cached_member_count,
    cs.member_count_last_checked = NOW(),
    cs.tier_upgrade_required = FALSE;

-- Set default billing interval to MONTHLY for existing subscriptions
UPDATE church_subscriptions cs
SET cs.billing_interval_id = (
    SELECT id FROM subscription_billing_intervals WHERE interval_name = 'MONTHLY' LIMIT 1
)
WHERE cs.billing_interval_id IS NULL;

-- ============================================================================
-- Step 4: Mark old subscription plans as deprecated
-- ============================================================================

-- Mark all existing plans as deprecated
UPDATE subscription_plans sp
SET
    is_deprecated = TRUE,
    deprecation_date = NOW(),
    replacement_tier_id = (
        -- Default replacement: TIER_2 (201-500) for most churches
        SELECT id FROM congregation_pricing_tiers WHERE tier_name = 'TIER_2' LIMIT 1
    )
WHERE sp.is_deprecated = FALSE OR sp.is_deprecated IS NULL;

-- Special case: UNLIMITED plan maps to TIER_5
UPDATE subscription_plans sp
SET replacement_tier_id = (
    SELECT id FROM congregation_pricing_tiers WHERE tier_name = 'TIER_5' LIMIT 1
)
WHERE sp.name = 'UNLIMITED';

-- Special case: STANDARD plan maps to TIER_1 or TIER_2 based on typical usage
UPDATE subscription_plans sp
SET replacement_tier_id = (
    SELECT id FROM congregation_pricing_tiers WHERE tier_name = 'TIER_1' LIMIT 1
)
WHERE sp.name = 'STANDARD';

-- ============================================================================
-- Step 5: Create migration audit records
-- ============================================================================

-- Create migration records for all churches with active subscriptions
INSERT INTO pricing_model_migrations (
    church_id,
    old_plan_id,
    old_storage_limit_mb,
    old_monthly_price,
    new_pricing_tier_id,
    new_member_count,
    new_monthly_price,
    migrated_at,
    migrated_by,
    migration_notes,
    migration_status
)
SELECT
    cs.church_id,
    cs.plan_id,
    COALESCE(sp.storage_limit_mb, 0),
    COALESCE(sp.price, 0.00),
    cs.pricing_tier_id,
    cs.current_member_count,
    cpt.monthly_price_usd,
    NOW(),
    NULL,  -- Automated migration, no specific SUPERADMIN
    CONCAT(
        'Automated migration from storage-based (', COALESCE(sp.name, 'UNKNOWN'), ') ',
        'to congregation-based (', cpt.tier_name, '). ',
        'Member count: ', cs.current_member_count, '. ',
        'Old price: $', COALESCE(sp.price, 0.00), '/month, ',
        'New price: $', cpt.monthly_price_usd, '/month'
    ),
    'COMPLETED'
FROM church_subscriptions cs
LEFT JOIN subscription_plans sp ON cs.plan_id = sp.id
JOIN congregation_pricing_tiers cpt ON cs.pricing_tier_id = cpt.id
WHERE cs.pricing_tier_id IS NOT NULL;

-- ============================================================================
-- Step 6: Update subscription amounts to reflect new pricing
-- ============================================================================

-- Update subscription_amount based on new tier pricing
UPDATE church_subscriptions cs
JOIN congregation_pricing_tiers cpt ON cs.pricing_tier_id = cpt.id
JOIN subscription_billing_intervals sbi ON cs.billing_interval_id = sbi.id
SET cs.subscription_amount = (
    CASE sbi.interval_name
        WHEN 'MONTHLY' THEN cpt.monthly_price_usd
        WHEN 'QUARTERLY' THEN cpt.quarterly_price_usd
        WHEN 'BIANNUAL' THEN cpt.biannual_price_usd
        WHEN 'ANNUAL' THEN cpt.annual_price_usd
        ELSE cpt.monthly_price_usd
    END
)
WHERE cs.pricing_tier_id IS NOT NULL;

-- ============================================================================
-- Step 7: Verification Queries (for manual verification after migration)
-- ============================================================================

-- These queries can be run manually to verify migration success:

-- Query 1: Check all churches have pricing tiers assigned
-- SELECT
--     c.id,
--     c.name,
--     c.cached_member_count,
--     cpt.tier_name,
--     cpt.display_name
-- FROM churches c
-- LEFT JOIN congregation_pricing_tiers cpt ON c.eligible_pricing_tier_id = cpt.id
-- WHERE c.eligible_pricing_tier_id IS NULL;
-- Expected: 0 rows (all churches should have tiers)

-- Query 2: Check all subscriptions migrated
-- SELECT
--     cs.id,
--     c.name AS church_name,
--     sp.name AS old_plan,
--     cpt.tier_name AS new_tier,
--     cs.current_member_count,
--     sp.price AS old_price,
--     cpt.monthly_price_usd AS new_price
-- FROM church_subscriptions cs
-- JOIN churches c ON cs.church_id = c.id
-- LEFT JOIN subscription_plans sp ON cs.plan_id = sp.id
-- LEFT JOIN congregation_pricing_tiers cpt ON cs.pricing_tier_id = cpt.id
-- WHERE cs.pricing_tier_id IS NULL;
-- Expected: 0 rows (all subscriptions should have new tiers)

-- Query 3: Migration summary statistics
-- SELECT
--     'Total Churches Migrated' AS metric,
--     COUNT(*) AS value
-- FROM pricing_model_migrations
-- WHERE migration_status = 'COMPLETED'
-- UNION ALL
-- SELECT
--     'Average Member Count' AS metric,
--     ROUND(AVG(new_member_count), 0) AS value
-- FROM pricing_model_migrations
-- UNION ALL
-- SELECT
--     'Total Revenue Change (Monthly)' AS metric,
--     ROUND(SUM(new_monthly_price - old_monthly_price), 2) AS value
-- FROM pricing_model_migrations;

-- ============================================================================
-- Step 8: Create indexes for performance
-- ============================================================================

-- Index for tier upgrade checks
CREATE INDEX IF NOT EXISTS idx_subscription_tier_upgrade
    ON church_subscriptions(pricing_tier_id, current_member_count, tier_upgrade_required);

-- Index for migration status queries
CREATE INDEX IF NOT EXISTS idx_migration_status_church
    ON pricing_model_migrations(church_id, migration_status);

-- ============================================================================
-- End of Migration V89
-- ============================================================================
-- Post-Migration Notes:
-- 1. Run verification queries above to ensure migration success
-- 2. Check that all churches have cached_member_count > 0
-- 3. Verify pricing_model_migrations table has records for all churches
-- 4. Test tier upgrade detection for churches near tier boundaries
-- 5. Ensure exchange rate is set correctly in platform_currency_settings
-- ============================================================================
