-- V47: Create widgets table for dashboard widget catalog
-- Dashboard Phase 2.1: Custom Layouts MVP

CREATE TABLE widgets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    widget_key VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    category VARCHAR(50) NOT NULL,
    icon VARCHAR(50),
    default_width INT NOT NULL DEFAULT 1,
    default_height INT NOT NULL DEFAULT 1,
    min_width INT NOT NULL DEFAULT 1,
    min_height INT NOT NULL DEFAULT 1,
    required_role VARCHAR(20),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Seed data for 17 existing dashboard widgets
INSERT INTO widgets (widget_key, name, description, category, default_width, default_height, required_role, is_active, created_at, updated_at) VALUES
('stats_overview', 'Statistics Overview', 'Key church metrics and statistics', 'STATS', 2, 1, NULL, TRUE, NOW(), NOW()),
('pastoral_care', 'Pastoral Care Needs', 'Urgent pastoral care needs requiring attention', 'PASTORAL_CARE', 2, 2, NULL, TRUE, NOW(), NOW()),
('events', 'Upcoming Events', 'Church calendar and upcoming events', 'OPERATIONS', 2, 2, NULL, TRUE, NOW(), NOW()),
('prayer_requests', 'Prayer Requests', 'Active prayer requests from members', 'PASTORAL_CARE', 2, 2, NULL, TRUE, NOW(), NOW()),
('recent_activity', 'Recent Activity', 'Latest updates and activities', 'OPERATIONS', 2, 2, NULL, TRUE, NOW(), NOW()),
('birthdays', 'Birthdays This Week', 'Member birthdays this week', 'PASTORAL_CARE', 1, 1, NULL, TRUE, NOW(), NOW()),
('anniversaries', 'Anniversaries This Month', 'Membership anniversaries this month', 'PASTORAL_CARE', 1, 1, NULL, TRUE, NOW(), NOW()),
('irregular_attenders', 'Irregular Attenders', 'Members needing follow-up', 'PASTORAL_CARE', 1, 2, NULL, TRUE, NOW(), NOW()),
('member_growth', 'Member Growth', 'Member growth trend chart', 'ANALYTICS', 2, 2, NULL, TRUE, NOW(), NOW()),
('location_stats', 'Member Locations', 'Geographic distribution of members', 'ANALYTICS', 2, 2, NULL, TRUE, NOW(), NOW()),
('attendance_summary', 'Attendance Summary', 'Monthly attendance statistics', 'ANALYTICS', 2, 1, NULL, TRUE, NOW(), NOW()),
('service_analytics', 'Service Analytics', 'Service type breakdown and trends', 'ANALYTICS', 2, 2, NULL, TRUE, NOW(), NOW()),
('top_members', 'Top Active Members', 'Most engaged members', 'ANALYTICS', 1, 2, NULL, TRUE, NOW(), NOW()),
('fellowship_health', 'Fellowship Health', 'Fellowship comparison and health metrics', 'ANALYTICS', 2, 2, NULL, TRUE, NOW(), NOW()),
('donations', 'Donation Statistics', 'Financial overview and donation metrics', 'OPERATIONS', 2, 1, 'TREASURER', TRUE, NOW(), NOW()),
('crises', 'Crisis Management', 'Active crisis situations', 'PASTORAL_CARE', 2, 1, NULL, TRUE, NOW(), NOW()),
('counseling', 'Counseling Sessions', 'Upcoming counseling sessions', 'PASTORAL_CARE', 2, 2, NULL, TRUE, NOW(), NOW()),
('sms_credits', 'SMS Credits Balance', 'SMS credit balance and usage', 'OPERATIONS', 1, 1, NULL, TRUE, NOW(), NOW());

-- Indexes for performance
CREATE INDEX idx_widget_category ON widgets(category);
CREATE INDEX idx_widget_active ON widgets(is_active);
CREATE INDEX idx_widget_role ON widgets(required_role);
