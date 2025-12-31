-- Add advanced user management fields
-- Date: 2025-12-29
-- Purpose: Add isActive, lastLoginAt, and mustChangePassword fields for enhanced user management

-- Add isActive column (soft delete)
ALTER TABLE "user" ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE;

-- Add lastLoginAt column (track last login time)
ALTER TABLE "user" ADD COLUMN IF NOT EXISTS last_login_at TIMESTAMP;

-- Add mustChangePassword column (force password reset on first login)
ALTER TABLE "user" ADD COLUMN IF NOT EXISTS must_change_password BOOLEAN NOT NULL DEFAULT FALSE;

-- Create index on isActive for faster queries
CREATE INDEX IF NOT EXISTS idx_user_is_active ON "user"(is_active);

-- Create index on lastLoginAt for activity tracking
CREATE INDEX IF NOT EXISTS idx_user_last_login_at ON "user"(last_login_at);

-- Update existing users to set mustChangePassword=false (they already have passwords)
UPDATE "user" SET must_change_password = FALSE WHERE must_change_password IS NULL;

-- Comment on columns
COMMENT ON COLUMN "user".is_active IS 'Soft delete flag - false means user is deactivated';
COMMENT ON COLUMN "user".last_login_at IS 'Timestamp of last successful login';
COMMENT ON COLUMN "user".must_change_password IS 'Requires user to change password on next login (set true for new users)';
