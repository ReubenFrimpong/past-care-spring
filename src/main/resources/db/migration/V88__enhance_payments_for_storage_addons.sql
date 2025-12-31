-- Enhance payments table to track storage addon purchases
-- Adds columns for addon linking, proration tracking, and original pricing

-- Add storage addon foreign key (nullable - only set for addon payments)
ALTER TABLE payments
ADD COLUMN storage_addon_id BIGINT REFERENCES storage_addons(id) ON DELETE SET NULL;

-- Add proration tracking fields
ALTER TABLE payments
ADD COLUMN is_prorated BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE payments
ADD COLUMN prorated_days INTEGER;

ALTER TABLE payments
ADD COLUMN original_amount DECIMAL(10, 2);

-- Add constraint: if prorated, must have days and original amount
ALTER TABLE payments
ADD CONSTRAINT check_proration_fields CHECK (
    (is_prorated = FALSE AND prorated_days IS NULL AND original_amount IS NULL) OR
    (is_prorated = TRUE AND prorated_days IS NOT NULL AND original_amount IS NOT NULL)
);

-- Update payment_type column to support new addon payment types
-- Current values: SUBSCRIPTION, ONE_TIME, UPGRADE, DOWNGRADE
-- New values: STORAGE_ADDON, STORAGE_ADDON_RENEWAL, STORAGE_ADDON_PRORATED

COMMENT ON COLUMN payments.storage_addon_id IS 'Links payment to storage addon purchase (NULL for base subscription payments)';
COMMENT ON COLUMN payments.is_prorated IS 'True if this is a prorated payment (mid-cycle purchase)';
COMMENT ON COLUMN payments.prorated_days IS 'Number of days remaining in billing period when addon was purchased';
COMMENT ON COLUMN payments.original_amount IS 'Full monthly price before proration (for reference)';

-- Indexes for performance
CREATE INDEX idx_payments_storage_addon_id ON payments(storage_addon_id);
CREATE INDEX idx_payments_is_prorated ON payments(is_prorated);
CREATE INDEX idx_payments_church_addon ON payments(church_id, storage_addon_id) WHERE storage_addon_id IS NOT NULL;

-- Note: payment_type values are now:
-- SUBSCRIPTION - Base subscription payment (initial or renewal)
-- STORAGE_ADDON - Storage addon purchase (one-time or prorated)
-- STORAGE_ADDON_RENEWAL - Storage addon renewal (recurring)
-- STORAGE_ADDON_PRORATED - Prorated addon purchase (first month mid-cycle)
-- ONE_TIME - One-time purchase (e.g., SMS credits)
-- UPGRADE - Plan upgrade
-- DOWNGRADE - Plan downgrade
