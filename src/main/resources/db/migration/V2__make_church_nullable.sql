-- Migration: Make church_id nullable in user table to support SUPERADMIN role
-- SUPERADMIN users don't belong to any specific church

ALTER TABLE user MODIFY COLUMN church_id BIGINT NULL;

-- Add comment to the column
ALTER TABLE user MODIFY COLUMN church_id BIGINT NULL COMMENT 'Church ID - null for SUPERADMIN users';
