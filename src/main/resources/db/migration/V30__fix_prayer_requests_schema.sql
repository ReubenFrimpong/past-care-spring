-- Fix prayer_requests table schema
-- This migration reconciles differences between V14 and V28

-- Drop the old 'request' column from V14 if it exists
ALTER TABLE prayer_requests DROP COLUMN IF EXISTS request;

-- Add new columns if they don't exist (from V28 schema)
ALTER TABLE prayer_requests
    ADD COLUMN IF NOT EXISTS title VARCHAR(200) NOT NULL DEFAULT '',
    ADD COLUMN IF NOT EXISTS description TEXT,
    ADD COLUMN IF NOT EXISTS submitted_by_user_id BIGINT;

-- Update column names to match V28 schema
ALTER TABLE prayer_requests
    CHANGE COLUMN IF EXISTS answered_at answered_date DATETIME,
    CHANGE COLUMN IF EXISTS expires_at expiration_date DATE;

-- Add missing columns from V28
ALTER TABLE prayer_requests
    ADD COLUMN IF NOT EXISTS prayer_count INT DEFAULT 0,
    ADD COLUMN IF NOT EXISTS tags VARCHAR(500);

-- Modify columns to match V28 types and defaults
ALTER TABLE prayer_requests
    MODIFY COLUMN category VARCHAR(50) NOT NULL,
    MODIFY COLUMN priority VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    MODIFY COLUMN is_anonymous BOOLEAN DEFAULT FALSE,
    MODIFY COLUMN is_urgent BOOLEAN DEFAULT FALSE,
    MODIFY COLUMN is_public BOOLEAN DEFAULT TRUE;

-- Add foreign key for submitted_by_user_id if it doesn't exist
ALTER TABLE prayer_requests
    ADD CONSTRAINT IF NOT EXISTS fk_prayer_request_submitted_by
    FOREIGN KEY (submitted_by_user_id) REFERENCES users(id) ON DELETE CASCADE;

-- Add missing indexes from V28
CREATE INDEX IF NOT EXISTS idx_prayer_request_priority ON prayer_requests(priority);
CREATE INDEX IF NOT EXISTS idx_prayer_request_expiration_date ON prayer_requests(expiration_date);
CREATE INDEX IF NOT EXISTS idx_prayer_request_created_at ON prayer_requests(created_at);
CREATE INDEX IF NOT EXISTS idx_prayer_request_submitted_by ON prayer_requests(submitted_by_user_id);

-- Remove default empty string from title after data migration
ALTER TABLE prayer_requests MODIFY COLUMN title VARCHAR(200) NOT NULL;
