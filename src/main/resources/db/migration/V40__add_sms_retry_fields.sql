-- Add retry tracking fields to sms_messages table
ALTER TABLE sms_messages
    ADD COLUMN retry_count INT NOT NULL DEFAULT 0,
    ADD COLUMN last_retry_at TIMESTAMP NULL,
    ADD INDEX idx_sms_retry_status (status, retry_count, last_retry_at);

-- Update existing messages to have retry_count = 0
UPDATE sms_messages SET retry_count = 0 WHERE retry_count IS NULL;
