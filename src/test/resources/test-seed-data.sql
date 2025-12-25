-- E2E Test Seed Data
-- This file seeds the test database with data required for e2e tests

-- Clean up existing test data (in correct order to handle foreign key constraints)
DELETE FROM portal_users WHERE email LIKE '%@example.com';
DELETE FROM user WHERE email = 'testuser@example.com';
DELETE FROM households WHERE church_id = 999;
DELETE FROM member WHERE church_id = 999;
DELETE FROM church WHERE name = 'Test Church E2E';

-- Insert test church
INSERT INTO church (id, name, address, phone_number, email, active, created_at, updated_at)
VALUES (999, 'Test Church E2E', '123 Test Street', '+233241234567', 'test@church.com', TRUE, NOW(), NOW())
ON DUPLICATE KEY UPDATE name = 'Test Church E2E', active = TRUE;

-- Insert test member (approved)
INSERT INTO member (id, church_id, first_name, last_name, sex, phone_number, marital_status, created_at, updated_at, status)
VALUES (9991, 999, 'Approved', 'Member', 'male', '+233241111111', 'single', NOW(), NOW(), 'MEMBER')
ON DUPLICATE KEY UPDATE first_name = 'Approved';

-- Insert portal user - approved member
INSERT INTO portal_users (id, member_id, church_id, email, password_hash, email_verified_at, status, is_active, created_at, updated_at, approved_at)
VALUES (9991, 9991, 999, 'approved.member@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J0CPl5k9VaY5/g1VU8yF5jZ8/zX6.O', NOW(), 'APPROVED', TRUE, NOW(), NOW(), NOW())
ON DUPLICATE KEY UPDATE email = 'approved.member@example.com', email_verified_at = NOW(), status = 'APPROVED', is_active = TRUE;
-- Password: ApprovedPassword123!

-- Insert test member (unverified)
INSERT INTO member (id, church_id, first_name, last_name, sex, phone_number, marital_status, created_at, updated_at, status)
VALUES (9992, 999, 'Unverified', 'Member', 'female', '+233241111112', 'single', NOW(), NOW(), 'VISITOR')
ON DUPLICATE KEY UPDATE first_name = 'Unverified';

-- Insert portal user - pending verification
INSERT INTO portal_users (id, member_id, church_id, email, password_hash, email_verified_at, status, is_active, created_at, updated_at)
VALUES (9992, 9992, 999, 'unverified@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J0CPl5k9VaY5/g1VU8yF5jZ8/zX6.O', NULL, 'PENDING_VERIFICATION', TRUE, NOW(), NOW())
ON DUPLICATE KEY UPDATE email = 'unverified@example.com', email_verified_at = NULL, status = 'PENDING_VERIFICATION', is_active = TRUE;
-- Password: UnverifiedPassword123!

-- Insert test member (pending approval)
INSERT INTO member (id, church_id, first_name, last_name, sex, phone_number, marital_status, created_at, updated_at, status)
VALUES (9993, 999, 'Pending', 'Member', 'male', '+233241111113', 'single', NOW(), NOW(), 'VISITOR')
ON DUPLICATE KEY UPDATE first_name = 'Pending';

-- Insert portal user - pending approval
INSERT INTO portal_users (id, member_id, church_id, email, password_hash, email_verified_at, status, is_active, created_at, updated_at)
VALUES (9993, 9993, 999, 'pending@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J0CPl5k9VaY5/g1VU8yF5jZ8/zX6.O', NOW(), 'PENDING_APPROVAL', TRUE, NOW(), NOW())
ON DUPLICATE KEY UPDATE email = 'pending@example.com', email_verified_at = NOW(), status = 'PENDING_APPROVAL', is_active = TRUE;
-- Password: PendingPassword123!

-- Insert admin user for main app tests
INSERT INTO user (id, church_id, email, password, name, role, account_locked, failed_login_attempts, created_at, updated_at)
VALUES (9999, 999, 'testuser@example.com', '$2a$10$8eGGhfHQvVR5I2hXd7LaUe5z6qgP.Ux7YZC1J0qHOPZjZFxvYzF9O', 'Test User', 'ADMIN', FALSE, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE email = 'testuser@example.com', name = 'Test User', role = 'ADMIN', account_locked = FALSE;
-- Password: password123

-- Insert additional test members for relationship tests
INSERT INTO member (id, church_id, first_name, last_name, sex, phone_number, marital_status, created_at, updated_at, status)
VALUES
  (9994, 999, 'John', 'Smith', 'male', '+233241111114', 'married', NOW(), NOW(), 'MEMBER'),
  (9995, 999, 'Jane', 'Smith', 'female', '+233241111115', 'married', NOW(), NOW(), 'MEMBER'),
  (9996, 999, 'Child', 'Smith', 'male', '+233241111116', 'single', NOW(), NOW(), 'MEMBER')
ON DUPLICATE KEY UPDATE first_name = VALUES(first_name);

-- Link John and Jane as spouses
UPDATE member SET spouse_id = 9995 WHERE id = 9994;
UPDATE member SET spouse_id = 9994 WHERE id = 9995;

-- Create test household
INSERT INTO households (id, church_id, household_name, household_head_id, household_phone, household_email, created_at, updated_at)
VALUES (9991, 999, 'Smith Family', 9994, '+233241111114', 'smith@family.com', NOW(), NOW())
ON DUPLICATE KEY UPDATE household_name = 'Smith Family';

COMMIT;
