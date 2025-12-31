-- Add data retention tracking fields to church_subscriptions table
-- These fields support the 90-day data deletion policy for suspended subscriptions

-- Timestamp when subscription was suspended (used to calculate deletion date)
ALTER TABLE church_subscriptions
ADD COLUMN suspended_at TIMESTAMP;

-- Date when church data will be permanently deleted (suspended_at + 90 days + extensions)
ALTER TABLE church_subscriptions
ADD COLUMN data_retention_end_date DATE;

-- Number of days SUPERADMIN has extended the retention period beyond 90 days (default 0)
ALTER TABLE church_subscriptions
ADD COLUMN retention_extension_days INT DEFAULT 0;

-- Timestamp when the 7-day deletion warning email was sent to the church
ALTER TABLE church_subscriptions
ADD COLUMN deletion_warning_sent_at TIMESTAMP;

-- Note/reason for retention extension (entered by SUPERADMIN)
ALTER TABLE church_subscriptions
ADD COLUMN retention_extension_note VARCHAR(500);

-- Add index for finding subscriptions eligible for deletion
CREATE INDEX idx_data_retention_end_date ON church_subscriptions(data_retention_end_date)
WHERE data_retention_end_date IS NOT NULL;

-- Add index for finding subscriptions needing deletion warnings
CREATE INDEX idx_deletion_warning ON church_subscriptions(deletion_warning_sent_at, data_retention_end_date)
WHERE data_retention_end_date IS NOT NULL;

-- Comments for documentation
COMMENT ON COLUMN church_subscriptions.suspended_at IS 'Timestamp when subscription was suspended. Used to calculate 90-day deletion deadline.';
COMMENT ON COLUMN church_subscriptions.data_retention_end_date IS 'Date when church data will be permanently deleted. Calculated as: suspended_at + 90 days + retention_extension_days.';
COMMENT ON COLUMN church_subscriptions.retention_extension_days IS 'Number of days SUPERADMIN has extended the retention period beyond the default 90 days.';
COMMENT ON COLUMN church_subscriptions.deletion_warning_sent_at IS 'Timestamp when the 7-day warning email was sent to alert the church of pending deletion.';
COMMENT ON COLUMN church_subscriptions.retention_extension_note IS 'SUPERADMIN note explaining why retention period was extended (e.g., "Church requested payment plan").';
