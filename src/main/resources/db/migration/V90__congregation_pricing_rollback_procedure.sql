-- ============================================================================
-- Migration: V90 - Congregation Pricing Rollback Procedure
-- ============================================================================
-- Purpose: Creates stored procedure for rolling back congregation pricing
--          migration in case of critical issues
-- Date: 2026-01-01
-- Author: PastCare Team
--
-- IMPORTANT: This procedure should only be used in emergency situations
-- ============================================================================

DELIMITER $$

-- ============================================================================
-- Stored Procedure: rollback_congregation_pricing_migration
-- ============================================================================

CREATE PROCEDURE IF NOT EXISTS rollback_congregation_pricing_migration(
    IN p_church_id BIGINT,
    IN p_admin_user_id BIGINT,
    IN p_rollback_reason TEXT
)
BEGIN
    DECLARE v_migration_id BIGINT;
    DECLARE v_old_plan_id BIGINT;
    DECLARE v_old_monthly_price DECIMAL(10, 2);

    -- Start transaction
    START TRANSACTION;

    -- Get migration record
    SELECT
        id,
        old_plan_id,
        old_monthly_price
    INTO
        v_migration_id,
        v_old_plan_id,
        v_old_monthly_price
    FROM pricing_model_migrations
    WHERE church_id = p_church_id
      AND migration_status = 'COMPLETED'
    ORDER BY migrated_at DESC
    LIMIT 1;

    IF v_migration_id IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'No completed migration found for this church';
    END IF;

    -- Restore old subscription plan
    UPDATE church_subscriptions
    SET
        plan_id = v_old_plan_id,
        pricing_tier_id = NULL,
        billing_interval_id = NULL,
        subscription_amount = v_old_monthly_price
    WHERE church_id = p_church_id;

    -- Clear church tier assignment
    UPDATE churches
    SET eligible_pricing_tier_id = NULL
    WHERE id = p_church_id;

    -- Mark migration as rolled back
    UPDATE pricing_model_migrations
    SET
        migration_status = 'ROLLED_BACK',
        migration_notes = CONCAT(
            migration_notes,
            '\n\n--- ROLLBACK ---\n',
            'Rolled back at: ', NOW(), '\n',
            'Rolled back by user ID: ', p_admin_user_id, '\n',
            'Reason: ', p_rollback_reason
        )
    WHERE id = v_migration_id;

    COMMIT;

    -- Return success message
    SELECT
        'ROLLBACK_SUCCESS' AS status,
        p_church_id AS church_id,
        v_old_plan_id AS restored_plan_id,
        v_old_monthly_price AS restored_monthly_price,
        'Migration successfully rolled back' AS message;

END$$

-- ============================================================================
-- Stored Procedure: bulk_rollback_congregation_pricing
-- ============================================================================

CREATE PROCEDURE IF NOT EXISTS bulk_rollback_congregation_pricing(
    IN p_admin_user_id BIGINT,
    IN p_rollback_reason TEXT
)
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE v_church_id BIGINT;
    DECLARE v_rollback_count INT DEFAULT 0;
    DECLARE v_error_count INT DEFAULT 0;

    -- Cursor for all migrated churches
    DECLARE church_cursor CURSOR FOR
        SELECT DISTINCT church_id
        FROM pricing_model_migrations
        WHERE migration_status = 'COMPLETED';

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
    BEGIN
        SET v_error_count = v_error_count + 1;
    END;

    OPEN church_cursor;

    read_loop: LOOP
        FETCH church_cursor INTO v_church_id;
        IF done THEN
            LEAVE read_loop;
        END IF;

        -- Rollback individual church
        CALL rollback_congregation_pricing_migration(
            v_church_id,
            p_admin_user_id,
            p_rollback_reason
        );

        SET v_rollback_count = v_rollback_count + 1;
    END LOOP;

    CLOSE church_cursor;

    -- Return summary
    SELECT
        'BULK_ROLLBACK_COMPLETE' AS status,
        v_rollback_count AS successful_rollbacks,
        v_error_count AS failed_rollbacks,
        CONCAT(
            'Rolled back ', v_rollback_count, ' churches. ',
            v_error_count, ' errors occurred.'
        ) AS message;

END$$

-- ============================================================================
-- Stored Procedure: get_migration_status
-- ============================================================================

CREATE PROCEDURE IF NOT EXISTS get_migration_status()
BEGIN
    SELECT
        COUNT(*) AS total_migrations,
        SUM(CASE WHEN migration_status = 'COMPLETED' THEN 1 ELSE 0 END) AS completed,
        SUM(CASE WHEN migration_status = 'ROLLED_BACK' THEN 1 ELSE 0 END) AS rolled_back,
        SUM(CASE WHEN migration_status = 'FAILED' THEN 1 ELSE 0 END) AS failed,
        SUM(CASE WHEN migration_status = 'PENDING' THEN 1 ELSE 0 END) AS pending,
        ROUND(AVG(new_monthly_price - old_monthly_price), 2) AS avg_price_change_usd,
        ROUND(SUM(new_monthly_price - old_monthly_price), 2) AS total_price_change_usd
    FROM pricing_model_migrations;

    -- Tier distribution
    SELECT
        cpt.tier_name,
        cpt.display_name,
        COUNT(*) AS church_count,
        ROUND(AVG(pmm.new_member_count), 0) AS avg_member_count,
        MIN(pmm.new_member_count) AS min_members,
        MAX(pmm.new_member_count) AS max_members
    FROM pricing_model_migrations pmm
    JOIN congregation_pricing_tiers cpt ON pmm.new_pricing_tier_id = cpt.id
    WHERE pmm.migration_status = 'COMPLETED'
    GROUP BY cpt.tier_name, cpt.display_name
    ORDER BY cpt.display_order;

END$$

DELIMITER ;

-- ============================================================================
-- End of Migration V90
-- ============================================================================
-- Usage Examples:
--
-- 1. Rollback single church:
-- CALL rollback_congregation_pricing_migration(1, 100, 'Customer request');
--
-- 2. Rollback all churches (EMERGENCY ONLY):
-- CALL bulk_rollback_congregation_pricing(100, 'Critical bug in pricing calculation');
--
-- 3. Check migration status:
-- CALL get_migration_status();
-- ============================================================================
