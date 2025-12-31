-- Cleanup script to truncate all tables before/after tests
-- Disable foreign key checks temporarily
SET FOREIGN_KEY_CHECKS = 0;

-- Truncate all tables in reverse dependency order
TRUNCATE TABLE sms_logs;
TRUNCATE TABLE sms_templates;
TRUNCATE TABLE communication_logs;

TRUNCATE TABLE event_registrations;
TRUNCATE TABLE event_organizers;
TRUNCATE TABLE event_tags;
TRUNCATE TABLE events;

TRUNCATE TABLE crisis_affected_members;
TRUNCATE TABLE crises;
TRUNCATE TABLE prayer_request_approvals;
TRUNCATE TABLE prayer_requests;
TRUNCATE TABLE counseling_sessions;
TRUNCATE TABLE visits;
TRUNCATE TABLE care_need_assignments;
TRUNCATE TABLE care_needs;

TRUNCATE TABLE pledge_payments;
TRUNCATE TABLE pledges;
TRUNCATE TABLE campaign_donations;
TRUNCATE TABLE campaigns;
TRUNCATE TABLE donations;

TRUNCATE TABLE attendance_records;
TRUNCATE TABLE attendance_sessions;

TRUNCATE TABLE fellowship_join_requests;
TRUNCATE TABLE user_fellowships;
TRUNCATE TABLE fellowship_multiplication_history;
TRUNCATE TABLE fellowships;

TRUNCATE TABLE member_relationships;
TRUNCATE TABLE member_tags;
TRUNCATE TABLE member_lifecycle_events;
TRUNCATE TABLE households;
TRUNCATE TABLE members;

TRUNCATE TABLE subscription_payments;
TRUNCATE TABLE church_subscriptions;
TRUNCATE TABLE subscription_plans;

TRUNCATE TABLE users;
TRUNCATE TABLE churches;

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;
