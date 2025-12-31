-- Add denormalized storage limit cache to churches table
-- Prevents N+1 queries on every file upload validation
-- Updated only when addons are purchased/canceled (rare operations)

-- Add storage limit cache fields
ALTER TABLE churches
ADD COLUMN total_storage_limit_mb BIGINT NOT NULL DEFAULT 2048; -- Base 2GB = 2048 MB

ALTER TABLE churches
ADD COLUMN storage_limit_updated_at TIMESTAMP;

-- Create index for storage limit queries
CREATE INDEX idx_churches_storage_limit ON churches(total_storage_limit_mb);

-- Initialize storage_limit_updated_at for existing churches
UPDATE churches
SET storage_limit_updated_at = CURRENT_TIMESTAMP
WHERE storage_limit_updated_at IS NULL;

-- Comments for documentation
COMMENT ON COLUMN churches.total_storage_limit_mb IS 'Cached total storage limit in MB: base 2048 MB + sum of active addons (denormalized for performance)';
COMMENT ON COLUMN churches.storage_limit_updated_at IS 'Last time storage limit was recalculated (when addon purchased/canceled)';

-- Note: This cache is updated by StorageAddonBillingService when:
-- 1. Addon is purchased and activated (increase limit)
-- 2. Addon is canceled or suspended (decrease limit)
-- 3. Subscription is suspended (remove addon capacity)
-- 4. Subscription is manually activated (restore addon capacity)
--
-- Formula: total_storage_limit_mb = 2048 (base) + SUM(storage_gb * 1024) WHERE status='ACTIVE'
