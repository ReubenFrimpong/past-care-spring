-- V49: Create dashboard_templates table for role-based dashboard templates
-- This allows predefined dashboard layouts for different roles

CREATE TABLE dashboard_templates (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    template_name VARCHAR(100) NOT NULL,
    description TEXT,
    role VARCHAR(50) NOT NULL COMMENT 'ADMIN, PASTOR, TREASURER, FELLOWSHIP_LEADER, MEMBER',
    layout_config TEXT NOT NULL COMMENT 'JSON configuration matching DashboardLayout format',
    is_default BOOLEAN DEFAULT FALSE COMMENT 'Whether this is the default template for the role',
    preview_image_url VARCHAR(500) COMMENT 'Optional preview screenshot URL',
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_template_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_template_role (role),
    INDEX idx_template_default (is_default)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default templates for different roles

-- 1. ADMIN Template: Comprehensive overview with all key metrics
INSERT INTO dashboard_templates (template_name, description, role, layout_config, is_default, created_by) VALUES
('Admin Dashboard', 'Comprehensive overview for church administrators with all key metrics and statistics', 'ADMIN',
'{
  "version": 1,
  "gridColumns": 4,
  "widgets": [
    {"widgetKey": "stats_overview", "position": {"x": 0, "y": 0}, "size": {"width": 2, "height": 1}, "visible": true},
    {"widgetKey": "member_growth", "position": {"x": 2, "y": 0}, "size": {"width": 2, "height": 1}, "visible": true},
    {"widgetKey": "attendance_summary", "position": {"x": 0, "y": 1}, "size": {"width": 2, "height": 1}, "visible": true},
    {"widgetKey": "donation_stats", "position": {"x": 2, "y": 1}, "size": {"width": 2, "height": 1}, "visible": true},
    {"widgetKey": "pastoral_care", "position": {"x": 0, "y": 2}, "size": {"width": 2, "height": 1}, "visible": true},
    {"widgetKey": "upcoming_events", "position": {"x": 2, "y": 2}, "size": {"width": 2, "height": 1}, "visible": true},
    {"widgetKey": "irregular_attenders", "position": {"x": 0, "y": 3}, "size": {"width": 2, "height": 1}, "visible": true},
    {"widgetKey": "top_members", "position": {"x": 2, "y": 3}, "size": {"width": 2, "height": 1}, "visible": true},
    {"widgetKey": "sms_credits", "position": {"x": 0, "y": 4}, "size": {"width": 1, "height": 1}, "visible": true},
    {"widgetKey": "fellowship_health", "position": {"x": 1, "y": 4}, "size": {"width": 1, "height": 1}, "visible": true},
    {"widgetKey": "crisis_stats", "position": {"x": 2, "y": 4}, "size": {"width": 1, "height": 1}, "visible": true},
    {"widgetKey": "counseling_sessions", "position": {"x": 3, "y": 4}, "size": {"width": 1, "height": 1}, "visible": true}
  ]
}', TRUE, NULL);

-- 2. PASTOR Template: Focus on pastoral care and member engagement
INSERT INTO dashboard_templates (template_name, description, role, layout_config, is_default, created_by) VALUES
('Pastor Dashboard', 'Focused on pastoral care, member engagement, and spiritual health', 'PASTOR',
'{
  "version": 1,
  "gridColumns": 4,
  "widgets": [
    {"widgetKey": "pastoral_care", "position": {"x": 0, "y": 0}, "size": {"width": 2, "height": 1}, "visible": true},
    {"widgetKey": "upcoming_events", "position": {"x": 2, "y": 0}, "size": {"width": 2, "height": 1}, "visible": true},
    {"widgetKey": "birthdays_week", "position": {"x": 0, "y": 1}, "size": {"width": 1, "height": 1}, "visible": true},
    {"widgetKey": "anniversaries_month", "position": {"x": 1, "y": 1}, "size": {"width": 1, "height": 1}, "visible": true},
    {"widgetKey": "irregular_attenders", "position": {"x": 2, "y": 1}, "size": {"width": 2, "height": 1}, "visible": true},
    {"widgetKey": "attendance_summary", "position": {"x": 0, "y": 2}, "size": {"width": 2, "height": 1}, "visible": true},
    {"widgetKey": "fellowship_health", "position": {"x": 2, "y": 2}, "size": {"width": 2, "height": 1}, "visible": true},
    {"widgetKey": "crisis_stats", "position": {"x": 0, "y": 3}, "size": {"width": 1, "height": 1}, "visible": true},
    {"widgetKey": "counseling_sessions", "position": {"x": 1, "y": 3}, "size": {"width": 1, "height": 1}, "visible": true},
    {"widgetKey": "recent_activities", "position": {"x": 2, "y": 3}, "size": {"width": 2, "height": 1}, "visible": true}
  ]
}', TRUE, NULL);

-- 3. TREASURER Template: Financial focus
INSERT INTO dashboard_templates (template_name, description, role, layout_config, is_default, created_by) VALUES
('Treasurer Dashboard', 'Financial oversight with donation tracking and giving statistics', 'TREASURER',
'{
  "version": 1,
  "gridColumns": 4,
  "widgets": [
    {"widgetKey": "donation_stats", "position": {"x": 0, "y": 0}, "size": {"width": 2, "height": 1}, "visible": true},
    {"widgetKey": "stats_overview", "position": {"x": 2, "y": 0}, "size": {"width": 2, "height": 1}, "visible": true},
    {"widgetKey": "member_growth", "position": {"x": 0, "y": 1}, "size": {"width": 2, "height": 1}, "visible": true},
    {"widgetKey": "attendance_summary", "position": {"x": 2, "y": 1}, "size": {"width": 2, "height": 1}, "visible": true},
    {"widgetKey": "top_members", "position": {"x": 0, "y": 2}, "size": {"width": 2, "height": 1}, "visible": true},
    {"widgetKey": "upcoming_events", "position": {"x": 2, "y": 2}, "size": {"width": 2, "height": 1}, "visible": true},
    {"widgetKey": "recent_activities", "position": {"x": 0, "y": 3}, "size": {"width": 2, "height": 1}, "visible": true},
    {"widgetKey": "sms_credits", "position": {"x": 2, "y": 3}, "size": {"width": 1, "height": 1}, "visible": true}
  ]
}', TRUE, NULL);

-- 4. FELLOWSHIP_LEADER Template: Small groups and member engagement
INSERT INTO dashboard_templates (template_name, description, role, layout_config, is_default, created_by) VALUES
('Fellowship Leader Dashboard', 'Small groups management and member engagement tracking', 'FELLOWSHIP_LEADER',
'{
  "version": 1,
  "gridColumns": 4,
  "widgets": [
    {"widgetKey": "fellowship_health", "position": {"x": 0, "y": 0}, "size": {"width": 2, "height": 1}, "visible": true},
    {"widgetKey": "upcoming_events", "position": {"x": 2, "y": 0}, "size": {"width": 2, "height": 1}, "visible": true},
    {"widgetKey": "attendance_summary", "position": {"x": 0, "y": 1}, "size": {"width": 2, "height": 1}, "visible": true},
    {"widgetKey": "member_growth", "position": {"x": 2, "y": 1}, "size": {"width": 2, "height": 1}, "visible": true},
    {"widgetKey": "birthdays_week", "position": {"x": 0, "y": 2}, "size": {"width": 1, "height": 1}, "visible": true},
    {"widgetKey": "anniversaries_month", "position": {"x": 1, "y": 2}, "size": {"width": 1, "height": 1}, "visible": true},
    {"widgetKey": "irregular_attenders", "position": {"x": 2, "y": 2}, "size": {"width": 2, "height": 1}, "visible": true},
    {"widgetKey": "top_members", "position": {"x": 0, "y": 3}, "size": {"width": 2, "height": 1}, "visible": true},
    {"widgetKey": "recent_activities", "position": {"x": 2, "y": 3}, "size": {"width": 2, "height": 1}, "visible": true}
  ]
}', TRUE, NULL);

-- 5. MEMBER Template: Basic member view
INSERT INTO dashboard_templates (template_name, description, role, layout_config, is_default, created_by) VALUES
('Member Dashboard', 'Simple overview for regular church members', 'MEMBER',
'{
  "version": 1,
  "gridColumns": 4,
  "widgets": [
    {"widgetKey": "upcoming_events", "position": {"x": 0, "y": 0}, "size": {"width": 2, "height": 1}, "visible": true},
    {"widgetKey": "recent_activities", "position": {"x": 2, "y": 0}, "size": {"width": 2, "height": 1}, "visible": true},
    {"widgetKey": "birthdays_week", "position": {"x": 0, "y": 1}, "size": {"width": 1, "height": 1}, "visible": true},
    {"widgetKey": "anniversaries_month", "position": {"x": 1, "y": 1}, "size": {"width": 1, "height": 1}, "visible": true},
    {"widgetKey": "service_analytics", "position": {"x": 2, "y": 1}, "size": {"width": 2, "height": 1}, "visible": true}
  ]
}', TRUE, NULL);
