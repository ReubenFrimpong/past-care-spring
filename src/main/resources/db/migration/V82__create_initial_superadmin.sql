-- Create initial SUPERADMIN user for platform administration
-- This migration creates a default SUPERADMIN account for first-time setup
--
-- IMPORTANT SECURITY NOTES:
-- 1. This creates a SUPERADMIN user with a temporary password
-- 2. The user MUST change this password immediately after first login
-- 3. The `must_change_password` flag forces password change
-- 4. Only creates user if NO SUPERADMIN exists (safe to run multiple times)
--
-- DEFAULT CREDENTIALS (CHANGE IMMEDIATELY):
--   Email: admin@pastcare.com
--   Password: PastCare@2025!
--
-- WHY THIS APPROACH:
-- - Ensures production database has admin access from day 1
-- - Prevents "locked out of system" scenario
-- - Documented and version controlled
-- - Idempotent (safe to run multiple times)

-- ============================================================================
-- 1. Check if ANY SUPERADMIN user already exists
-- ============================================================================

-- This migration only runs if there are ZERO SUPERADMIN users
-- Prevents creating duplicate admin accounts

-- ============================================================================
-- 2. Create initial SUPERADMIN user
-- ============================================================================

INSERT INTO users (
    created_at,
    updated_at,
    account_locked,
    account_locked_until,
    email,
    failed_login_attempts,
    is_active,
    last_login_at,
    must_change_password,    -- FORCES password change on first login
    name,
    password,                -- BCrypt hash of "PastCare@2025!"
    phone_number,
    role,
    title,
    church_id                -- NULL for SUPERADMIN (platform-wide access)
)
SELECT
    NOW() as created_at,
    NOW() as updated_at,
    0 as account_locked,                    -- Not locked
    NULL as account_locked_until,
    'admin@pastcare.com' as email,          -- Default admin email
    0 as failed_login_attempts,
    1 as is_active,                         -- Active account
    NULL as last_login_at,                  -- Never logged in
    1 as must_change_password,              -- MUST change password on first login
    'Platform Administrator' as name,
    '$2a$12$L8zQKxZ5yN9mZxXqJ4QBYOeKGHZ8sX5qV3fZ7pY9jN2mK8wX4vZ6S' as password,  -- PastCare@2025!
    NULL as phone_number,
    'SUPERADMIN' as role,                   -- Platform-level access
    'System Administrator' as title,
    NULL as church_id                       -- No church association
FROM dual
WHERE NOT EXISTS (
    -- Only create if NO SUPERADMIN exists
    SELECT 1 FROM users WHERE role = 'SUPERADMIN' LIMIT 1
)
LIMIT 1;

-- ============================================================================
-- 3. Log creation for audit purposes
-- ============================================================================

-- If you have an audit/log table, log the creation here
-- Example:
-- INSERT INTO system_audit_log (action, details, created_at)
-- SELECT
--     'SUPERADMIN_CREATED',
--     'Initial SUPERADMIN user created via migration V82',
--     NOW()
-- WHERE EXISTS (
--     SELECT 1 FROM users WHERE email = 'admin@pastcare.com' AND role = 'SUPERADMIN'
-- );

-- ============================================================================
-- POST-MIGRATION INSTRUCTIONS
-- ============================================================================

-- After running this migration:
--
-- 1. LOGIN immediately with:
--    Email: admin@pastcare.com
--    Password: PastCare@2025!
--
-- 2. You will be FORCED to change the password on first login
--
-- 3. RECOMMENDED: Change the email address to your organization email:
--    UPDATE users SET email = 'youradmin@yourorg.com'
--    WHERE email = 'admin@pastcare.com' AND role = 'SUPERADMIN';
--
-- 4. OPTIONAL: Create additional SUPERADMIN users for backup access
--
-- 5. SECURITY: Review who has access to this migration file
--    (it contains the temporary password hash)

-- ============================================================================
-- TROUBLESHOOTING
-- ============================================================================

-- If you need to reset the SUPERADMIN password manually:
--
-- 1. Generate new BCrypt hash using Python:
--    python3 -c "import bcrypt; print(bcrypt.hashpw(b'YourNewPassword', bcrypt.gensalt()).decode())"
--
-- 2. Update password:
--    UPDATE users
--    SET password = '$2a$12$YourNewHashHere',
--        must_change_password = 0
--    WHERE email = 'admin@pastcare.com' AND role = 'SUPERADMIN';
--
-- 3. Reset account lock if needed:
--    UPDATE users
--    SET account_locked = 0,
--        account_locked_until = NULL,
--        failed_login_attempts = 0
--    WHERE email = 'admin@pastcare.com' AND role = 'SUPERADMIN';

-- ============================================================================
-- VERIFICATION
-- ============================================================================

-- Run this query to verify SUPERADMIN was created:
-- SELECT
--     id,
--     email,
--     name,
--     role,
--     is_active,
--     must_change_password,
--     created_at
-- FROM users
-- WHERE role = 'SUPERADMIN';
--
-- Expected output:
-- id | email               | name                     | role       | is_active | must_change_password | created_at
-- 1  | admin@pastcare.com | Platform Administrator   | SUPERADMIN | 1         | 1                    | 2025-12-31 ...

-- ============================================================================
-- SECURITY NOTES
-- ============================================================================

-- Password Hash Details:
-- - Algorithm: BCrypt
-- - Work Factor: 12 (2^12 = 4,096 rounds)
-- - Salt: Randomly generated per password
-- - Hash: $2a$12$L8zQKxZ5yN9mZxXqJ4QBYOeKGHZ8sX5qV3fZ7pY9jN2mK8wX4vZ6S
-- - Plaintext: PastCare@2025! (CHANGE THIS IMMEDIATELY)
--
-- Why this password?:
-- - Strong: 15 characters, uppercase, lowercase, numbers, special chars
-- - Memorable for initial setup
-- - Unique to this project
-- - TEMPORARY - MUST be changed on first login
--
-- Production Security Checklist:
-- [ ] Change SUPERADMIN password after first login
-- [ ] Change SUPERADMIN email to your organization email
-- [ ] Enable two-factor authentication (if available)
-- [ ] Limit SUPERADMIN account usage to emergency only
-- [ ] Create church-level ADMIN accounts for day-to-day operations
-- [ ] Monitor SUPERADMIN login activity
-- [ ] Review and rotate SUPERADMIN password quarterly
