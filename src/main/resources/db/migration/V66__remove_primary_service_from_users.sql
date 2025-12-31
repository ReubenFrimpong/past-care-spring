-- Remove primary_service column from user table
-- This field is no longer needed for user management

ALTER TABLE user DROP COLUMN IF EXISTS primary_service;
