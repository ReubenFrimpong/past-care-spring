-- ============================================================================
-- Migration V91: Update Congregation Pricing to Match Spreadsheet Exactly
-- ============================================================================
-- Date: 2026-01-01
-- Purpose: Correct pricing tiers to match user-provided 2025 pricing spreadsheet
-- Changes:
--   - Remove Tier 1 (1-200 members)
--   - Remove Tier 5 (2001+)
--   - Update Tier 2-4 pricing to match spreadsheet exactly
--   - Fixed dollar discounts: $1.50 (quarterly), $4.50 (biannual), $12 (annual)
-- ============================================================================

-- Deactivate Tier 1 and Tier 5 (not in spreadsheet)
UPDATE congregation_pricing_tiers
SET is_active = FALSE
WHERE tier_name IN ('TIER_1', 'TIER_5');

-- Update Tier 2 (201-500 members) to match spreadsheet
-- Monthly: $9.99 = GHS 119.88 (quoted: GHS 120)
-- Quarterly: $28.47 (3 * $9.99 - $1.50 discount)
-- Biannual: $55.44 (6 * $9.99 - $4.50 discount)
-- Annual: $107.88 (12 * $9.99 - $12.00 discount)
UPDATE congregation_pricing_tiers
SET
    display_name = 'Standard Church (201-500)',
    description = 'Everything you need to manage your church - perfect for growing congregations',
    monthly_price_usd = 9.99,
    quarterly_price_usd = 28.47,   -- 3 * 9.99 - 1.50 = 28.47
    biannual_price_usd = 55.44,    -- 6 * 9.99 - 4.50 = 55.44
    annual_price_usd = 107.88,     -- 12 * 9.99 - 12.00 = 107.88
    quarterly_discount_pct = 5.02, -- ($1.50 / $29.97) * 100 = 5.02%
    biannual_discount_pct = 7.51,  -- ($4.50 / $59.94) * 100 = 7.51%
    annual_discount_pct = 10.01,   -- ($12.00 / $119.88) * 100 = 10.01%
    features = '["Unlimited Members","Unlimited Users","Full Member Management","Event Management","Attendance Tracking","Donation & Pledge Tracking","SMS Notifications","Pastoral Care Tools","Advanced Analytics","Email & Priority Support","Mobile Access","Cloud Backup"]',
    display_order = 1
WHERE tier_name = 'TIER_2';

-- Update Tier 3 (501-1000 members) to match spreadsheet
-- Monthly: $13.99 = GHS 167.88 (quoted: GHS 168)
-- Quarterly: $40.47 (3 * $13.99 - $1.50 discount)
-- Biannual: $79.44 (6 * $13.99 - $4.50 discount)
-- Annual: $155.88 (12 * $13.99 - $12.00 discount)
UPDATE congregation_pricing_tiers
SET
    display_name = 'Professional Church (501-1000)',
    description = 'Advanced features for established churches with multiple ministries',
    monthly_price_usd = 13.99,
    quarterly_price_usd = 40.47,   -- 3 * 13.99 - 1.50 = 40.47
    biannual_price_usd = 79.44,    -- 6 * 13.99 - 4.50 = 79.44
    annual_price_usd = 155.88,     -- 12 * 13.99 - 12.00 = 155.88
    quarterly_discount_pct = 3.58, -- ($1.50 / $41.97) * 100 = 3.58%
    biannual_discount_pct = 5.36,  -- ($4.50 / $83.94) * 100 = 5.36%
    annual_discount_pct = 7.15,    -- ($12.00 / $167.88) * 100 = 7.15%
    features = '["Everything in Standard","Advanced SMS Notifications","Ministry Group Management","Fellowship Tracking","Detailed Analytics Dashboard","Priority Email Support","Custom Reports","Multi-location Support","Enhanced Security","API Access","Bulk Import/Export"]',
    display_order = 2
WHERE tier_name = 'TIER_3';

-- Update Tier 4 (1001+ members) to match spreadsheet
-- Monthly: $17.99 = GHS 215.88 (quoted: GHS 216)
-- Quarterly: $52.47 (3 * $17.99 - $1.50 discount)
-- Biannual: $103.44 (6 * $17.99 - $4.50 discount)
-- Annual: $203.88 (12 * $17.99 - $12.00 discount)
UPDATE congregation_pricing_tiers
SET
    min_members = 1001,
    max_members = NULL,  -- No upper limit for largest tier
    display_name = 'Enterprise Church (1001+)',
    description = 'Complete solution for large churches and multi-site organizations',
    monthly_price_usd = 17.99,
    quarterly_price_usd = 52.47,   -- 3 * 17.99 - 1.50 = 52.47
    biannual_price_usd = 103.44,   -- 6 * 17.99 - 4.50 = 103.44
    annual_price_usd = 203.88,     -- 12 * 17.99 - 12.00 = 203.88
    quarterly_discount_pct = 2.78, -- ($1.50 / $53.97) * 100 = 2.78%
    biannual_discount_pct = 4.17,  -- ($4.50 / $107.94) * 100 = 4.17%
    annual_discount_pct = 5.56,    -- ($12.00 / $215.88) * 100 = 5.56%
    features = '["Everything in Professional","Multi-campus Management","Advanced Permissions","Custom Branding","Premium Integrations","24/7 Phone Support","Training Sessions","Data Migration Assistance","Dedicated Account Manager","SLA Guarantee","Custom Development"]',
    display_order = 3
WHERE tier_name = 'TIER_4';

-- Update platform currency settings to GHS 12 = $1 USD (if not already set)
UPDATE platform_currency_settings
SET
    base_currency = 'USD',
    display_currency = 'GHS',
    exchange_rate = 12.0000,
    show_both_currencies = TRUE,
    primary_display_currency = 'GHS'
WHERE id = 1;

-- Insert default currency settings if not exists
INSERT INTO platform_currency_settings (
    base_currency,
    display_currency,
    exchange_rate,
    previous_rate,
    show_both_currencies,
    primary_display_currency,
    last_updated_at,
    last_updated_by,
    rate_history
)
SELECT
    'USD',
    'GHS',
    12.0000,
    12.0000,
    TRUE,
    'GHS',
    CURRENT_TIMESTAMP,
    1,
    JSON_ARRAY(JSON_OBJECT(
        'rate', 12.0000,
        'timestamp', CURRENT_TIMESTAMP,
        'updated_by', 1
    ))
WHERE NOT EXISTS (SELECT 1 FROM platform_currency_settings WHERE id = 1);

-- ============================================================================
-- Verification queries (commented out - for manual verification)
-- ============================================================================

-- Check active tiers
-- SELECT
--     tier_name,
--     display_name,
--     min_members,
--     max_members,
--     monthly_price_usd,
--     quarterly_price_usd,
--     biannual_price_usd,
--     annual_price_usd,
--     is_active
-- FROM congregation_pricing_tiers
-- ORDER BY display_order;

-- Verify pricing calculations
-- SELECT
--     tier_name,
--     CONCAT('$', monthly_price_usd) AS monthly_usd,
--     CONCAT('GHS ', ROUND(monthly_price_usd * 12, 2)) AS monthly_ghs,
--     CONCAT('$', quarterly_price_usd, ' (save $', ROUND((monthly_price_usd * 3) - quarterly_price_usd, 2), ')') AS quarterly,
--     CONCAT('$', annual_price_usd, ' (save $', ROUND((monthly_price_usd * 12) - annual_price_usd, 2), ')') AS annual
-- FROM congregation_pricing_tiers
-- WHERE is_active = TRUE
-- ORDER BY display_order;
