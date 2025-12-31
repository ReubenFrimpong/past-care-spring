-- Allow null church_id in refresh_tokens table for SUPERADMIN users
ALTER TABLE refresh_tokens MODIFY COLUMN church_id BIGINT NULL;
