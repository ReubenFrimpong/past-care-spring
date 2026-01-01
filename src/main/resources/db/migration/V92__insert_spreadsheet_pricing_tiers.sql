-- ============================================================================
-- Migration V92: Insert Spreadsheet Pricing Tiers
-- ============================================================================
-- Date: 2026-01-01
-- Purpose: Insert the 4 pricing tiers from PastCare 2025 Pricing spreadsheet
--
-- PRICING STRUCTURE (from spreadsheet):
-- All tiers have IDENTICAL features - only member limit differs
--
-- Tier 1: 1-200 members    @ $5.99/month  (GHS 70)
-- Tier 2: 201-500 members  @ $9.99/month  (GHS 120)
-- Tier 3: 501-1000 members @ $13.99/month (GHS 170)
-- Tier 4: 1001+ members    @ $17.99/month (GHS 215)
--
-- Discounts: $1.50 (quarterly), $4.50 (biannual), $12.00 (annual)
-- Exchange Rate: GHS 12 = $1 USD
-- ============================================================================

-- Delete any existing tiers to ensure clean state
DELETE FROM congregation_pricing_tiers;

-- Insert all 4 tiers matching the pricing spreadsheet exactly
INSERT INTO congregation_pricing_tiers (
    tier_name,
    display_name,
    description,
    min_members,
    max_members,
    monthly_price_usd,
    quarterly_price_usd,
    biannual_price_usd,
    annual_price_usd,
    quarterly_discount_pct,
    biannual_discount_pct,
    annual_discount_pct,
    features,
    is_active,
    display_order
) VALUES
-- ============================================================================
-- Tier 1: Small Church (1-200 Members)
-- Monthly: $5.99 = GHS 71.88 (Quoted: GHS 70)
-- Quarterly: $16.47 (3 * 5.99 - 1.50) = GHS 197.64 (Quoted: GHS 200)
-- Biannual: $31.44 (6 * 5.99 - 4.50) = GHS 377.28 (Quoted: GHS 380)
-- Annual: $59.88 (12 * 5.99 - 12.00) = GHS 718.56 (Quoted: GHS 720)
-- ============================================================================
(
    'TIER_1',
    'Small Church (1-200)',
    'All features included. Supports 1-200 church members.',
    1,
    200,
    5.99,
    16.47,
    31.44,
    59.88,
    8.35,    -- ($1.50 / $17.97) * 100
    12.52,   -- ($4.50 / $35.94) * 100
    16.69,   -- ($12.00 / $71.88) * 100
    '["Unlimited Users","Full Member Management","Event Management","Attendance Tracking","Donation & Pledge Tracking","SMS Notifications","Pastoral Care Tools","Fellowship Management","Analytics & Reports","Mobile Access","Cloud Backup","Email Support"]',
    TRUE,
    1
),
-- ============================================================================
-- Tier 2: Standard Church (201-500 Members)
-- Monthly: $9.99 = GHS 119.88 (Quoted: GHS 120)
-- Quarterly: $28.47 (3 * 9.99 - 1.50) = GHS 341.64 (Quoted: GHS 340)
-- Biannual: $55.44 (6 * 9.99 - 4.50) = GHS 665.28 (Quoted: GHS 665)
-- Annual: $107.88 (12 * 9.99 - 12.00) = GHS 1294.56 (Quoted: GHS 1295)
-- ============================================================================
(
    'TIER_2',
    'Standard Church (201-500)',
    'All features included. Supports 201-500 church members.',
    201,
    500,
    9.99,
    28.47,
    55.44,
    107.88,
    5.02,    -- ($1.50 / $29.97) * 100
    7.51,    -- ($4.50 / $59.94) * 100
    10.01,   -- ($12.00 / $119.88) * 100
    '["Unlimited Users","Full Member Management","Event Management","Attendance Tracking","Donation & Pledge Tracking","SMS Notifications","Pastoral Care Tools","Fellowship Management","Analytics & Reports","Mobile Access","Cloud Backup","Email Support"]',
    TRUE,
    2
),
-- ============================================================================
-- Tier 3: Professional Church (501-1000 Members)
-- Monthly: $13.99 = GHS 167.88 (Quoted: GHS 170)
-- Quarterly: $40.47 (3 * 13.99 - 1.50) = GHS 485.64 (Quoted: GHS 485)
-- Biannual: $79.44 (6 * 13.99 - 4.50) = GHS 953.28 (Quoted: GHS 955)
-- Annual: $155.88 (12 * 13.99 - 12.00) = GHS 1870.56 (Quoted: GHS 1870)
-- ============================================================================
(
    'TIER_3',
    'Professional Church (501-1000)',
    'All features included. Supports 501-1000 church members.',
    501,
    1000,
    13.99,
    40.47,
    79.44,
    155.88,
    3.58,    -- ($1.50 / $41.97) * 100
    5.36,    -- ($4.50 / $83.94) * 100
    7.15,    -- ($12.00 / $167.88) * 100
    '["Unlimited Users","Full Member Management","Event Management","Attendance Tracking","Donation & Pledge Tracking","SMS Notifications","Pastoral Care Tools","Fellowship Management","Analytics & Reports","Mobile Access","Cloud Backup","Email Support"]',
    TRUE,
    3
),
-- ============================================================================
-- Tier 4: Enterprise Church (1001+ Members)
-- Monthly: $17.99 = GHS 215.88 (Quoted: GHS 215)
-- Quarterly: $52.47 (3 * 17.99 - 1.50) = GHS 629.64 (Quoted: GHS 630)
-- Biannual: $103.44 (6 * 17.99 - 4.50) = GHS 1241.28 (Quoted: GHS 1245)
-- Annual: $203.88 (12 * 17.99 - 12.00) = GHS 2446.56 (Quoted: GHS 2450)
-- ============================================================================
(
    'TIER_4',
    'Enterprise Church (1001+)',
    'All features included. Supports 1001+ church members.',
    1001,
    NULL,    -- No upper limit
    17.99,
    52.47,
    103.44,
    203.88,
    2.78,    -- ($1.50 / $53.97) * 100
    4.17,    -- ($4.50 / $107.94) * 100
    5.56,    -- ($12.00 / $215.88) * 100
    '["Unlimited Users","Full Member Management","Event Management","Attendance Tracking","Donation & Pledge Tracking","SMS Notifications","Pastoral Care Tools","Fellowship Management","Analytics & Reports","Mobile Access","Cloud Backup","Email Support"]',
    TRUE,
    4
);

-- ============================================================================
-- Ensure billing intervals exist
-- ============================================================================
INSERT INTO subscription_billing_intervals (interval_name, display_name, months, display_order, is_active)
VALUES
    ('MONTHLY', 'Monthly', 1, 1, TRUE),
    ('QUARTERLY', 'Quarterly (3 Months)', 3, 2, TRUE),
    ('BIANNUAL', 'Biannual (6 Months)', 6, 3, TRUE),
    ('ANNUAL', 'Annual (12 Months)', 12, 4, TRUE)
ON DUPLICATE KEY UPDATE
    display_name = VALUES(display_name),
    months = VALUES(months);

-- ============================================================================
-- Ensure currency settings exist (GHS 12 = $1 USD)
-- ============================================================================
INSERT INTO platform_currency_settings (
    base_currency,
    display_currency,
    exchange_rate,
    previous_rate,
    show_both_currencies,
    primary_display_currency,
    last_updated_at,
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
    JSON_ARRAY(JSON_OBJECT(
        'rate', 12.0000,
        'timestamp', NOW(),
        'updated_by', 1
    ))
WHERE NOT EXISTS (SELECT 1 FROM platform_currency_settings LIMIT 1);
