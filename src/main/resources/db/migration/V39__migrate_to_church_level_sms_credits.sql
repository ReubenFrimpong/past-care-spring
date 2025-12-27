-- ==========================================
-- V39: Migrate SMS Credits from User-Level to Church-Level
-- ==========================================
-- This migration transitions the SMS credit system from individual user wallets
-- to a shared church-wide credit pool, enabling better resource management
-- and automated messaging capabilities.

-- Step 1: Create new church_sms_credits table
CREATE TABLE church_sms_credits (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    church_id BIGINT NOT NULL,
    balance DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total_purchased DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total_used DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    last_purchase_at TIMESTAMP NULL,
    low_balance_alert_sent BOOLEAN DEFAULT FALSE,
    low_balance_threshold DECIMAL(10,2) DEFAULT 50.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_church_sms_credit_church FOREIGN KEY (church_id) REFERENCES churches(id) ON DELETE CASCADE,
    CONSTRAINT uk_church_credits UNIQUE KEY (church_id),
    INDEX idx_church_balance (church_id, balance)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Step 2: Migrate existing user credits to church-level
-- Aggregate all user credits per church into the new church_sms_credits table
INSERT INTO church_sms_credits (church_id, balance, total_purchased, total_used, created_at)
SELECT
    c.id as church_id,
    COALESCE(SUM(sc.balance), 0.00) as balance,
    COALESCE(SUM(sc.total_purchased), 0.00) as total_purchased,
    COALESCE(SUM(sc.total_used), 0.00) as total_used,
    NOW() as created_at
FROM churches c
LEFT JOIN users u ON u.church_id = c.id
LEFT JOIN sms_credits sc ON sc.user_id = u.id
GROUP BY c.id;

-- Step 3: Add church_id to sms_transactions table
ALTER TABLE sms_transactions
    ADD COLUMN church_id BIGINT AFTER id,
    ADD INDEX idx_church_transactions (church_id, created_at);

-- Step 4: Populate church_id from user's church
UPDATE sms_transactions st
JOIN users u ON st.user_id = u.id
SET st.church_id = u.church_id
WHERE st.church_id IS NULL;

-- Step 5: Make church_id required and add foreign key
ALTER TABLE sms_transactions
    MODIFY COLUMN church_id BIGINT NOT NULL,
    ADD CONSTRAINT fk_transaction_church FOREIGN KEY (church_id) REFERENCES churches(id) ON DELETE CASCADE;

-- Step 6: Rename user_id to performed_by_user_id for clarity
ALTER TABLE sms_transactions
    CHANGE COLUMN user_id performed_by_user_id BIGINT NULL,
    ADD INDEX idx_performed_by (performed_by_user_id);

-- Step 7: Create backup of old sms_credits table
CREATE TABLE sms_credits_backup_20251227 AS SELECT * FROM sms_credits;

-- Step 8: Add migration_completed flag to track progress
ALTER TABLE church_sms_credits
    ADD COLUMN migration_completed BOOLEAN DEFAULT TRUE;

-- Verification query (commented out - run manually to verify)
-- SELECT
--     c.name as church_name,
--     csc.balance as new_church_balance,
--     (SELECT COALESCE(SUM(balance), 0) FROM sms_credits sc2
--      JOIN users u2 ON sc2.user_id = u2.id
--      WHERE u2.church_id = c.id) as old_user_totals,
--     CASE WHEN csc.balance = (SELECT COALESCE(SUM(balance), 0) FROM sms_credits sc3
--                              JOIN users u3 ON sc3.user_id = u3.id
--                              WHERE u3.church_id = c.id)
--         THEN 'MATCH' ELSE 'MISMATCH' END as verification_status
-- FROM churches c
-- LEFT JOIN church_sms_credits csc ON csc.church_id = c.id;

-- Note: The old sms_credits table is preserved for rollback safety
-- It can be dropped manually after thorough testing:
-- DROP TABLE sms_credits;
