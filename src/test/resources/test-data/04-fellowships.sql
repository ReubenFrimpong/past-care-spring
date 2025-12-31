-- Seed data for test fellowships
-- Fellowship groups for testing fellowship management

-- Alpha Test Church Fellowships (Church ID: 1)
INSERT INTO fellowships (id, name, description, fellowship_type, leader_id, max_capacity, meeting_day, meeting_time, meeting_location, church_id, active, created_at, updated_at)
VALUES
    (1, 'Youth Fellowship', 'Fellowship for young adults aged 18-35', 'YOUTH', 5, 50, 'FRIDAY', '18:00:00', 'Youth Hall, Alpha Church', 1, true, NOW(), NOW()),
    (2, 'Men Fellowship', 'Fellowship for all men in the church', 'MEN', 1, 40, 'SATURDAY', '08:00:00', 'Main Hall, Alpha Church', 1, true, NOW(), NOW()),
    (3, 'Women Fellowship', 'Fellowship for all women in the church', 'WOMEN', 2, 45, 'WEDNESDAY', '15:00:00', 'Women Center, Alpha Church', 1, true, NOW(), NOW()),
    (4, 'Couples Fellowship', 'Fellowship for married couples', 'COUPLES', 1, 30, 'SUNDAY', '16:00:00', 'Conference Room, Alpha Church', 1, true, NOW(), NOW());

-- Beta Test Church Fellowships (Church ID: 2)
INSERT INTO fellowships (id, name, description, fellowship_type, leader_id, max_capacity, meeting_day, meeting_time, meeting_location, church_id, active, created_at, updated_at)
VALUES
    (5, 'Beta Youth Group', 'Youth fellowship at Beta Church', 'YOUTH', 8, 35, 'THURSDAY', '17:30:00', 'Youth Center, Beta Church', 2, true, NOW(), NOW());

-- Gamma Test Church Fellowships (Church ID: 3)
INSERT INTO fellowships (id, name, description, fellowship_type, leader_id, max_capacity, meeting_day, meeting_time, meeting_location, church_id, active, created_at, updated_at)
VALUES
    (6, 'Gamma Fellowship', 'Main fellowship at Gamma Church', 'GENERAL', 9, 60, 'FRIDAY', '19:00:00', 'Main Hall, Gamma Church', 3, true, NOW(), NOW());

-- Note: Fellowship IDs are explicitly set for predictable testing
-- Fellowships 1-4: Alpha Church (for most tests)
-- Fellowship 5: Beta Church (for multi-tenancy isolation)
-- Fellowship 6: Gamma Church (for cross-church access denial)
