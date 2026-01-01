-- ============================================================================
-- Migration: V88 - Create Congregation-Based Pricing Model
-- ============================================================================
-- Purpose: Complete migration from storage-based to congregation-size-based pricing
-- Date: 2026-01-01
-- Author: PastCare Team
--
-- This migration creates the new pricing model based on congregation size
-- instead of storage limits. It supports:
-- - Pricing tiers based on member count (1-200, 201-500, 501-1000, 1001-2000, 2001+)
-- - Dual currency display (USD base, GHS display)
-- - Configurable exchange rates
-- - Multiple billing intervals with discounts
-- ============================================================================

-- ============================================================================
-- Table 1: congregation_pricing_tiers
-- ============================================================================
-- Stores pricing tiers based on congregation size ranges
-- ============================================================================

CREATE TABLE IF NOT EXISTS congregation_pricing_tiers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    -- Tier identification
    tier_name VARCHAR(50) NOT NULL UNIQUE COMMENT 'Internal tier identifier (e.g., TIER_1)',
    display_name VARCHAR(100) NOT NULL COMMENT 'User-friendly tier name',
    description TEXT COMMENT 'Detailed tier description',

    -- Member count range
    min_members INT NOT NULL COMMENT 'Minimum members for this tier (inclusive)',
    max_members INT NULL COMMENT 'Maximum members for this tier (NULL = unlimited)',

    -- Pricing in base currency (USD)
    monthly_price_usd DECIMAL(10, 2) NOT NULL COMMENT 'Price per month in USD',
    quarterly_price_usd DECIMAL(10, 2) NOT NULL COMMENT 'Price for 3 months in USD',
    biannual_price_usd DECIMAL(10, 2) NOT NULL COMMENT 'Price for 6 months in USD',
    annual_price_usd DECIMAL(10, 2) NOT NULL COMMENT 'Price for 12 months in USD',

    -- Discount percentages for psychological appeal
    quarterly_discount_pct DECIMAL(5, 2) DEFAULT 0 COMMENT 'Discount % for quarterly vs monthly',
    biannual_discount_pct DECIMAL(5, 2) DEFAULT 0 COMMENT 'Discount % for biannual vs monthly',
    annual_discount_pct DECIMAL(5, 2) DEFAULT 0 COMMENT 'Discount % for annual vs monthly',

    -- Features (JSON array)
    features TEXT COMMENT 'JSON array of feature strings',

    -- Settings
    is_active BOOLEAN DEFAULT TRUE COMMENT 'Whether this tier is available for new signups',
    display_order INT NOT NULL COMMENT 'Order to display tiers (1 = first)',

    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Indexes
    INDEX idx_member_range (min_members, max_members),
    INDEX idx_active (is_active),
    INDEX idx_display_order (display_order),

    -- Constraints
    CONSTRAINT chk_member_range CHECK (max_members IS NULL OR max_members >= min_members),
    CONSTRAINT chk_pricing_positive CHECK (
        monthly_price_usd >= 0 AND
        quarterly_price_usd >= 0 AND
        biannual_price_usd >= 0 AND
        annual_price_usd >= 0
    )
) COMMENT 'Pricing tiers based on congregation size';

-- ============================================================================
-- Table 2: platform_currency_settings
-- ============================================================================
-- Stores exchange rate and currency display preferences
-- ============================================================================

CREATE TABLE IF NOT EXISTS platform_currency_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    -- Currency configuration
    base_currency VARCHAR(3) DEFAULT 'USD' COMMENT 'Base currency for pricing (USD)',
    display_currency VARCHAR(3) DEFAULT 'GHS' COMMENT 'Display currency for customers (GHS)',
    exchange_rate DECIMAL(10, 4) NOT NULL DEFAULT 12.0000 COMMENT 'GHS per 1 USD',

    -- Rate update tracking
    last_updated_by BIGINT NULL COMMENT 'SUPERADMIN who last updated rate',
    last_updated_at TIMESTAMP NULL COMMENT 'When rate was last updated',
    previous_rate DECIMAL(10, 4) NULL COMMENT 'Previous exchange rate before last update',

    -- Rate history (JSON array of {rate, date, updatedBy})
    rate_history TEXT COMMENT 'JSON array of historical rate changes',

    -- Display preferences
    show_both_currencies BOOLEAN DEFAULT TRUE COMMENT 'Show both USD and GHS',
    primary_display_currency VARCHAR(3) DEFAULT 'GHS' COMMENT 'Which currency to show first',

    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Foreign keys
    CONSTRAINT fk_currency_updater FOREIGN KEY (last_updated_by)
        REFERENCES users(id) ON DELETE SET NULL
) COMMENT 'Platform-wide currency and exchange rate settings';

-- ============================================================================
-- Table 3: subscription_billing_intervals
-- ============================================================================
-- Defines available billing intervals (monthly, quarterly, etc.)
-- ============================================================================

CREATE TABLE IF NOT EXISTS subscription_billing_intervals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    -- Interval configuration
    interval_name VARCHAR(20) NOT NULL UNIQUE COMMENT 'Internal name (MONTHLY, QUARTERLY, BIANNUAL, ANNUAL)',
    display_name VARCHAR(50) NOT NULL COMMENT 'User-friendly name',
    months INT NOT NULL COMMENT 'Number of months this interval covers',
    display_order INT NOT NULL COMMENT 'Order to display intervals',
    is_active BOOLEAN DEFAULT TRUE COMMENT 'Whether available for selection',

    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Indexes
    INDEX idx_active (is_active),
    INDEX idx_display_order (display_order),

    -- Constraints
    CONSTRAINT chk_months_positive CHECK (months > 0)
) COMMENT 'Available billing intervals for subscriptions';

-- ============================================================================
-- Table 4: pricing_model_migrations
-- ============================================================================
-- Tracks migration from storage-based to congregation-based pricing
-- ============================================================================

CREATE TABLE IF NOT EXISTS pricing_model_migrations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    -- Church being migrated
    church_id BIGINT NOT NULL,

    -- Old model (storage-based)
    old_plan_id BIGINT NULL COMMENT 'Previous subscription plan',
    old_storage_limit_mb BIGINT NULL COMMENT 'Previous storage limit',
    old_monthly_price DECIMAL(10, 2) NULL COMMENT 'Previous monthly price',

    -- New model (congregation-based)
    new_pricing_tier_id BIGINT NOT NULL COMMENT 'New pricing tier assigned',
    new_member_count INT NOT NULL COMMENT 'Member count at time of migration',
    new_monthly_price DECIMAL(10, 2) NOT NULL COMMENT 'New monthly price',

    -- Migration tracking
    migrated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    migrated_by BIGINT NULL COMMENT 'SUPERADMIN who performed migration',
    migration_notes TEXT COMMENT 'Notes about migration',
    migration_status ENUM('PENDING', 'COMPLETED', 'ROLLED_BACK', 'FAILED') DEFAULT 'COMPLETED',

    -- Indexes
    INDEX idx_church (church_id),
    INDEX idx_status (migration_status),

    -- Foreign keys
    CONSTRAINT fk_migration_church FOREIGN KEY (church_id)
        REFERENCES churches(id) ON DELETE CASCADE,
    CONSTRAINT fk_migration_old_plan FOREIGN KEY (old_plan_id)
        REFERENCES subscription_plans(id) ON DELETE SET NULL,
    CONSTRAINT fk_migration_new_tier FOREIGN KEY (new_pricing_tier_id)
        REFERENCES congregation_pricing_tiers(id) ON DELETE RESTRICT,
    CONSTRAINT fk_migration_user FOREIGN KEY (migrated_by)
        REFERENCES users(id) ON DELETE SET NULL
) COMMENT 'Audit trail for pricing model migration';

-- ============================================================================
-- Modify existing tables
-- ============================================================================

-- Add columns to church_subscriptions
ALTER TABLE church_subscriptions
    ADD COLUMN IF NOT EXISTS pricing_tier_id BIGINT NULL COMMENT 'New congregation-based tier' AFTER plan_id,
    ADD COLUMN IF NOT EXISTS billing_interval_id BIGINT NULL COMMENT 'Billing interval (monthly, quarterly, etc.)' AFTER billing_period_months,
    ADD COLUMN IF NOT EXISTS current_member_count INT DEFAULT 0 COMMENT 'Cached member count for tier calculation',
    ADD COLUMN IF NOT EXISTS member_count_last_checked TIMESTAMP NULL COMMENT 'When member count was last updated',
    ADD COLUMN IF NOT EXISTS tier_upgrade_required BOOLEAN DEFAULT FALSE COMMENT 'True if member count exceeds tier max',
    ADD COLUMN IF NOT EXISTS tier_upgrade_notification_sent TIMESTAMP NULL COMMENT 'When upgrade notification was sent';

-- Add foreign keys to church_subscriptions
ALTER TABLE church_subscriptions
    ADD CONSTRAINT fk_subscription_pricing_tier
        FOREIGN KEY (pricing_tier_id) REFERENCES congregation_pricing_tiers(id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_subscription_billing_interval
        FOREIGN KEY (billing_interval_id) REFERENCES subscription_billing_intervals(id) ON DELETE SET NULL;

-- Add indexes to church_subscriptions
CREATE INDEX IF NOT EXISTS idx_pricing_tier ON church_subscriptions(pricing_tier_id);
CREATE INDEX IF NOT EXISTS idx_member_count ON church_subscriptions(current_member_count);
CREATE INDEX IF NOT EXISTS idx_tier_upgrade ON church_subscriptions(tier_upgrade_required);

-- Add columns to subscription_plans (for deprecation tracking)
ALTER TABLE subscription_plans
    ADD COLUMN IF NOT EXISTS is_deprecated BOOLEAN DEFAULT FALSE COMMENT 'True if no longer available for new signups',
    ADD COLUMN IF NOT EXISTS deprecation_date TIMESTAMP NULL COMMENT 'When plan was deprecated',
    ADD COLUMN IF NOT EXISTS replacement_tier_id BIGINT NULL COMMENT 'Which congregation tier replaces this plan';

-- Add foreign key to subscription_plans
ALTER TABLE subscription_plans
    ADD CONSTRAINT fk_plan_replacement_tier
        FOREIGN KEY (replacement_tier_id) REFERENCES congregation_pricing_tiers(id) ON DELETE SET NULL;

-- Add columns to churches (for member count caching)
ALTER TABLE churches
    ADD COLUMN IF NOT EXISTS cached_member_count INT DEFAULT 0 COMMENT 'Cached total member count',
    ADD COLUMN IF NOT EXISTS member_count_last_updated TIMESTAMP NULL COMMENT 'When cache was last refreshed',
    ADD COLUMN IF NOT EXISTS eligible_pricing_tier_id BIGINT NULL COMMENT 'Recommended tier based on member count';

-- Add foreign key to churches
ALTER TABLE churches
    ADD CONSTRAINT fk_church_eligible_tier
        FOREIGN KEY (eligible_pricing_tier_id) REFERENCES congregation_pricing_tiers(id) ON DELETE SET NULL;

-- Add index to churches
CREATE INDEX IF NOT EXISTS idx_church_member_count ON churches(cached_member_count);

-- ============================================================================
-- Insert default data
-- ============================================================================

-- Insert billing intervals
INSERT INTO subscription_billing_intervals (interval_name, display_name, months, display_order, is_active)
VALUES
    ('MONTHLY', 'Monthly', 1, 1, TRUE),
    ('QUARTERLY', 'Quarterly (3 Months)', 3, 2, TRUE),
    ('BIANNUAL', 'Biannual (6 Months)', 6, 3, TRUE),
    ('ANNUAL', 'Annual (12 Months)', 12, 4, TRUE)
ON DUPLICATE KEY UPDATE
    display_name = VALUES(display_name),
    months = VALUES(months);

-- Insert congregation pricing tiers
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
(
    'TIER_1',
    'Small Church (1-200)',
    'Perfect for small congregations getting started with digital church management',
    1,
    200,
    5.99,
    16.47,
    34.94,
    69.88,
    8.00,
    3.00,
    3.00,
    '["Member Management","Event Planning","Attendance Tracking","Donation Management","Basic SMS Notifications","Prayer Request Tracking","Volunteer Coordination","Email Support","Mobile Access","Cloud Backup"]',
    TRUE,
    1
),
(
    'TIER_2',
    'Growing Church (201-500)',
    'Ideal for growing congregations with expanding ministry needs',
    201,
    500,
    9.99,
    28.97,
    57.94,
    117.88,
    3.00,
    3.00,
    1.00,
    '["Everything in Small Church","Advanced SMS Notifications","Ministry Group Management","Fellowship Tracking","Detailed Analytics","Priority Email Support","Custom Reports","Multi-location Support","Enhanced Security","API Access"]',
    TRUE,
    2
),
(
    'TIER_3',
    'Medium Church (501-1000)',
    'Comprehensive features for established churches with multiple ministries',
    501,
    1000,
    13.99,
    40.97,
    81.94,
    164.88,
    2.00,
    2.00,
    2.00,
    '["Everything in Growing Church","Advanced Analytics Dashboard","Bulk Import/Export","Custom Workflows","Dedicated Account Manager","Phone Support","Staff Management","Service Planning","Media Library","Integration Support"]',
    TRUE,
    3
),
(
    'TIER_4',
    'Large Church (1001-2000)',
    'Enterprise features for large congregations with complex organizational needs',
    1001,
    2000,
    17.99,
    52.97,
    105.94,
    209.88,
    2.00,
    2.00,
    2.00,
    '["Everything in Medium Church","Multi-campus Management","Advanced Permissions","Custom Branding","Premium Integrations","24/7 Support","Training Sessions","Data Migration Assistance","Custom Development","SLA Guarantee"]',
    TRUE,
    4
),
(
    'TIER_5',
    'Enterprise (2001+)',
    'Complete enterprise solution for mega churches and multi-site organizations',
    2001,
    NULL,
    22.99,
    67.47,
    134.94,
    267.88,
    2.00,
    2.00,
    2.00,
    '["Everything in Large Church","Unlimited Locations","White Label Options","Custom Feature Development","Dedicated Infrastructure","On-premise Deployment Option","Executive Dashboard","Strategic Planning Tools","Annual Business Review","Premium Training Program"]',
    TRUE,
    5
)
ON DUPLICATE KEY UPDATE
    display_name = VALUES(display_name),
    description = VALUES(description),
    monthly_price_usd = VALUES(monthly_price_usd),
    quarterly_price_usd = VALUES(quarterly_price_usd),
    biannual_price_usd = VALUES(biannual_price_usd),
    annual_price_usd = VALUES(annual_price_usd),
    quarterly_discount_pct = VALUES(quarterly_discount_pct),
    biannual_discount_pct = VALUES(biannual_discount_pct),
    annual_discount_pct = VALUES(annual_discount_pct),
    features = VALUES(features);

-- Insert initial currency settings
INSERT INTO platform_currency_settings (
    base_currency,
    display_currency,
    exchange_rate,
    last_updated_at,
    show_both_currencies,
    primary_display_currency,
    rate_history
) VALUES (
    'USD',
    'GHS',
    12.0000,
    NOW(),
    TRUE,
    'GHS',
    '[]'
)
ON DUPLICATE KEY UPDATE
    exchange_rate = 12.0000;

-- ============================================================================
-- End of Migration V88
-- ============================================================================
