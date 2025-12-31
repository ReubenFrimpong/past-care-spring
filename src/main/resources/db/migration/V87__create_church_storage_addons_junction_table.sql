-- Junction table to track which churches purchased which storage addons
-- Supports prorated billing, renewal synchronization with base subscription, and audit trail

CREATE TABLE church_storage_addons (
    id BIGSERIAL PRIMARY KEY,

    -- Foreign keys
    church_id BIGINT NOT NULL REFERENCES churches(id) ON DELETE CASCADE,
    storage_addon_id BIGINT NOT NULL REFERENCES storage_addons(id) ON DELETE RESTRICT,

    -- Purchase details
    purchased_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    purchase_price DECIMAL(10, 2) NOT NULL, -- Lock price at purchase time (prevents future price changes)
    purchase_reference VARCHAR(100) UNIQUE, -- Paystack reference: ADDON-{UUID}

    -- Prorating (first month only)
    is_prorated BOOLEAN NOT NULL DEFAULT FALSE,
    prorated_amount DECIMAL(10, 2), -- Actual charged amount if prorated
    prorated_days INTEGER, -- Days remaining in billing period when purchased

    -- Renewal details (synced with base subscription)
    current_period_start DATE NOT NULL,
    current_period_end DATE NOT NULL,
    next_renewal_date DATE NOT NULL, -- MUST match base subscription's next_billing_date

    -- Status management
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, CANCELED, SUSPENDED
    canceled_at TIMESTAMP,
    cancellation_reason TEXT,
    suspended_at TIMESTAMP,

    -- Audit trail
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT unique_church_addon UNIQUE (church_id, storage_addon_id),
    CONSTRAINT valid_status CHECK (status IN ('ACTIVE', 'CANCELED', 'SUSPENDED')),
    CONSTRAINT valid_prorated_amount CHECK (
        (is_prorated = FALSE AND prorated_amount IS NULL AND prorated_days IS NULL) OR
        (is_prorated = TRUE AND prorated_amount IS NOT NULL AND prorated_days IS NOT NULL)
    )
);

-- Indexes for performance
CREATE INDEX idx_church_storage_addons_church_id ON church_storage_addons(church_id);
CREATE INDEX idx_church_storage_addons_status ON church_storage_addons(status);
CREATE INDEX idx_church_storage_addons_church_status ON church_storage_addons(church_id, status);
CREATE INDEX idx_church_storage_addons_next_renewal ON church_storage_addons(next_renewal_date);
CREATE INDEX idx_church_storage_addons_reference ON church_storage_addons(purchase_reference);

-- Trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_church_storage_addons_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER church_storage_addons_updated_at
    BEFORE UPDATE ON church_storage_addons
    FOR EACH ROW
    EXECUTE FUNCTION update_church_storage_addons_updated_at();

-- Comments for documentation
COMMENT ON TABLE church_storage_addons IS 'Junction table tracking storage addon purchases by churches with billing and renewal information';
COMMENT ON COLUMN church_storage_addons.purchase_price IS 'Price locked at purchase time - prevents future price changes from affecting renewal';
COMMENT ON COLUMN church_storage_addons.is_prorated IS 'True if first month was prorated (purchased mid-cycle)';
COMMENT ON COLUMN church_storage_addons.next_renewal_date IS 'CRITICAL: Must stay synchronized with base subscription next_billing_date';
COMMENT ON COLUMN church_storage_addons.status IS 'ACTIVE: currently in use, CANCELED: will not renew (active until period end), SUSPENDED: subscription suspended';
