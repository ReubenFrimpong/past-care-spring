-- Create tier change history table for complete audit trail of all tier upgrades
-- Tracks financial details, proration calculations, and payment status

CREATE TABLE tier_change_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    church_id BIGINT NOT NULL,
    subscription_id BIGINT NOT NULL,

    -- Tier change details
    old_tier_id BIGINT NOT NULL,
    old_tier_name VARCHAR(50) NOT NULL,
    new_tier_id BIGINT NOT NULL,
    new_tier_name VARCHAR(50) NOT NULL,

    -- Billing interval change (nullable if tier-only upgrade)
    old_interval_id BIGINT,
    old_interval_name VARCHAR(50),
    new_interval_id BIGINT,
    new_interval_name VARCHAR(50),

    -- Financial details (USD - reference currency)
    days_remaining INTEGER NOT NULL,
    days_used INTEGER NOT NULL,
    old_price_usd DECIMAL(10,2) NOT NULL,
    new_price_usd DECIMAL(10,2) NOT NULL,
    unused_credit_usd DECIMAL(10,2) NOT NULL,
    prorated_charge_usd DECIMAL(10,2) NOT NULL,
    net_charge_usd DECIMAL(10,2) NOT NULL,

    -- Financial details (GHS - payment currency)
    old_price_ghs DECIMAL(10,2) NOT NULL,
    new_price_ghs DECIMAL(10,2) NOT NULL,
    unused_credit_ghs DECIMAL(10,2) NOT NULL,
    prorated_charge_ghs DECIMAL(10,2) NOT NULL,
    net_charge_ghs DECIMAL(10,2) NOT NULL,

    -- Payment tracking
    payment_reference VARCHAR(255) NOT NULL UNIQUE,
    payment_status VARCHAR(50) NOT NULL, -- PENDING, COMPLETED, FAILED
    paystack_authorization_code VARCHAR(255),

    -- Dates
    old_next_billing_date DATE NOT NULL,
    new_next_billing_date DATE NOT NULL,
    change_requested_at DATETIME(6) NOT NULL,
    payment_completed_at DATETIME(6),

    -- Metadata
    change_type VARCHAR(50) NOT NULL, -- TIER_UPGRADE, INTERVAL_CHANGE, COMBINED
    initiated_by_user_id BIGINT,
    reason VARCHAR(255), -- "Exceeded member limit", "User requested", etc.

    -- Foreign keys
    CONSTRAINT fk_tier_change_church
        FOREIGN KEY (church_id) REFERENCES churches(id),
    CONSTRAINT fk_tier_change_subscription
        FOREIGN KEY (subscription_id) REFERENCES church_subscriptions(id),
    CONSTRAINT fk_tier_change_old_tier
        FOREIGN KEY (old_tier_id) REFERENCES congregation_pricing_tiers(id),
    CONSTRAINT fk_tier_change_new_tier
        FOREIGN KEY (new_tier_id) REFERENCES congregation_pricing_tiers(id),
    CONSTRAINT fk_tier_change_old_interval
        FOREIGN KEY (old_interval_id) REFERENCES subscription_billing_intervals(id),
    CONSTRAINT fk_tier_change_new_interval
        FOREIGN KEY (new_interval_id) REFERENCES subscription_billing_intervals(id),
    CONSTRAINT fk_tier_change_user
        FOREIGN KEY (initiated_by_user_id) REFERENCES users(id),

    -- Indexes for performance
    INDEX idx_tier_change_church_id (church_id),
    INDEX idx_tier_change_subscription_id (subscription_id),
    INDEX idx_tier_change_payment_reference (payment_reference),
    INDEX idx_tier_change_requested_at (change_requested_at),
    INDEX idx_tier_change_status (payment_status),
    INDEX idx_tier_change_type (change_type)
);

-- Add comment for documentation
ALTER TABLE tier_change_history COMMENT = 'Complete audit trail of all subscription tier changes and upgrades';
