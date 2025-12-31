-- Remove automatic 7-day grace period from church subscriptions
-- Grace period should be explicitly granted by SUPERADMIN, not automatic

-- Update all existing subscriptions to have 0 grace period
-- This ensures no automatic grace period is given
UPDATE church_subscriptions
SET grace_period_days = 0
WHERE grace_period_days IS NULL OR grace_period_days = 7;

-- Add a comment to the column for clarity
ALTER TABLE church_subscriptions
MODIFY COLUMN grace_period_days INT DEFAULT 0
COMMENT 'Grace period days after payment failure. Default 0 (no automatic grace period). Must be explicitly granted by SUPERADMIN.';
