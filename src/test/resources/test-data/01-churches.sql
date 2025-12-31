-- Seed data for test churches
-- Three test churches for multi-tenancy testing

INSERT INTO churches (id, name, email, phone_number, address, website, active, created_at, updated_at)
VALUES
    (1, 'Alpha Test Church', 'admin@alphatestchurch.com', '+254700000001', '123 Alpha Street, Nairobi, Kenya', 'https://alphatestchurch.com', true, NOW(), NOW()),
    (2, 'Beta Test Church', 'admin@betatestchurch.com', '+254700000002', '456 Beta Avenue, Mombasa, Kenya', 'https://betatestchurch.com', true, NOW(), NOW()),
    (3, 'Gamma Test Church', 'admin@gammatestchurch.com', '+254700000003', '789 Gamma Road, Kisumu, Kenya', 'https://gammatestchurch.com', true, NOW(), NOW());

-- Note: Church IDs are explicitly set for predictable testing
-- Church 1 (Alpha) - Main test church for most tests
-- Church 2 (Beta) - Used for multi-tenancy isolation tests
-- Church 3 (Gamma) - Used for cross-church access denial tests
