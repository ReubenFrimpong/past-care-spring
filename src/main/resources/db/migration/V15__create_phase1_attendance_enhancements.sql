-- ============================================================================
-- Migration V15: Phase 1 Attendance Module Enhancements
-- Description: Add QR codes, geofencing, visitor management, and reminders
-- Author: Claude Sonnet 4.5
-- Date: 2025-12-24
-- ============================================================================

-- ============================================================================
-- SECTION 1: Enhance Existing Tables
-- ============================================================================

-- Add new fields to attendance_session table
ALTER TABLE attendance_session
ADD COLUMN service_type VARCHAR(50) NOT NULL DEFAULT 'SUNDAY_MAIN_SERVICE',
ADD COLUMN qr_code_data VARCHAR(500) UNIQUE,
ADD COLUMN qr_code_url TEXT,
ADD COLUMN qr_code_expires_at TIMESTAMP,
ADD COLUMN geofence_latitude DECIMAL(10, 8),
ADD COLUMN geofence_longitude DECIMAL(11, 8),
ADD COLUMN geofence_radius_meters INT DEFAULT 100,
ADD COLUMN allow_late_checkin BOOLEAN DEFAULT TRUE,
ADD COLUMN late_cutoff_minutes INT DEFAULT 30,
ADD COLUMN is_recurring BOOLEAN DEFAULT FALSE,
ADD COLUMN recurrence_pattern VARCHAR(100),
ADD COLUMN max_capacity INT,
ADD COLUMN check_in_opens_at TIMESTAMP,
ADD COLUMN check_in_closes_at TIMESTAMP;

-- Add indexes for performance
CREATE INDEX idx_attendance_session_service_type ON attendance_session(service_type);
CREATE INDEX idx_attendance_session_date_type ON attendance_session(session_date, service_type);
CREATE INDEX idx_attendance_session_qr_expires ON attendance_session(qr_code_expires_at);

-- Add new fields to attendance table
ALTER TABLE attendance
ADD COLUMN check_in_method VARCHAR(30) NOT NULL DEFAULT 'MANUAL',
ADD COLUMN check_in_time TIMESTAMP,
ADD COLUMN is_late BOOLEAN DEFAULT FALSE,
ADD COLUMN minutes_late INT,
ADD COLUMN check_in_location_lat DECIMAL(10, 8),
ADD COLUMN check_in_location_long DECIMAL(11, 8),
ADD COLUMN device_info VARCHAR(200);

-- Add indexes for performance
CREATE INDEX idx_attendance_check_in_method ON attendance(check_in_method);
CREATE INDEX idx_attendance_is_late ON attendance(is_late);
CREATE INDEX idx_attendance_check_in_time ON attendance(check_in_time);

-- ============================================================================
-- SECTION 2: Create New Tables
-- ============================================================================

-- Table: visitor
-- Purpose: Track guests/visitors before they become members
CREATE TABLE visitor (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    church_id BIGINT NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20) UNIQUE NOT NULL,
    email VARCHAR(100),
    age_group VARCHAR(20),
    how_heard_about_us VARCHAR(50),
    invited_by_member_id BIGINT,
    is_first_time BOOLEAN DEFAULT TRUE,
    visit_count INT DEFAULT 0,
    last_visit_date DATE,
    assigned_to_user_id BIGINT,
    follow_up_status VARCHAR(30),
    converted_to_member BOOLEAN DEFAULT FALSE,
    converted_member_id BIGINT,
    conversion_date DATE,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (church_id) REFERENCES church(id) ON DELETE CASCADE,
    FOREIGN KEY (invited_by_member_id) REFERENCES member(id) ON DELETE SET NULL,
    FOREIGN KEY (assigned_to_user_id) REFERENCES user(id) ON DELETE SET NULL,
    FOREIGN KEY (converted_member_id) REFERENCES member(id) ON DELETE SET NULL,

    INDEX idx_visitor_church (church_id),
    INDEX idx_visitor_phone (phone_number),
    INDEX idx_visitor_email (email),
    INDEX idx_visitor_first_time (is_first_time),
    INDEX idx_visitor_follow_up (follow_up_status),
    INDEX idx_visitor_converted (converted_to_member),
    INDEX idx_visitor_last_visit (last_visit_date)
);

-- Table: visitor_attendance
-- Purpose: Link visitors to attendance sessions
CREATE TABLE visitor_attendance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    visitor_id BIGINT NOT NULL,
    attendance_session_id BIGINT NOT NULL,
    check_in_method VARCHAR(30) NOT NULL DEFAULT 'MANUAL',
    check_in_time TIMESTAMP,
    is_late BOOLEAN DEFAULT FALSE,
    minutes_late INT,
    check_in_location_lat DECIMAL(10, 8),
    check_in_location_long DECIMAL(11, 8),
    device_info VARCHAR(200),
    remarks TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (visitor_id) REFERENCES visitor(id) ON DELETE CASCADE,
    FOREIGN KEY (attendance_session_id) REFERENCES attendance_session(id) ON DELETE CASCADE,

    UNIQUE KEY unique_visitor_session (visitor_id, attendance_session_id),
    INDEX idx_visitor_attendance_visitor (visitor_id),
    INDEX idx_visitor_attendance_session (attendance_session_id),
    INDEX idx_visitor_attendance_check_in_time (check_in_time)
);

-- Table: attendance_reminder
-- Purpose: Scheduled reminders for irregular attenders
CREATE TABLE attendance_reminder (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    church_id BIGINT NOT NULL,
    created_by_user_id BIGINT NOT NULL,
    message TEXT NOT NULL,
    target_group VARCHAR(50) NOT NULL,
    fellowship_id BIGINT,
    scheduled_for TIMESTAMP NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'SCHEDULED',
    send_via_sms BOOLEAN DEFAULT FALSE,
    send_via_email BOOLEAN DEFAULT FALSE,
    send_via_whatsapp BOOLEAN DEFAULT FALSE,
    is_recurring BOOLEAN DEFAULT FALSE,
    recurrence_pattern VARCHAR(100),
    recipient_count INT DEFAULT 0,
    sent_count INT DEFAULT 0,
    delivered_count INT DEFAULT 0,
    failed_count INT DEFAULT 0,
    sent_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    cancelled_by_user_id BIGINT,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (church_id) REFERENCES church(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by_user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (fellowship_id) REFERENCES fellowship(id) ON DELETE SET NULL,
    FOREIGN KEY (cancelled_by_user_id) REFERENCES user(id) ON DELETE SET NULL,

    INDEX idx_reminder_church (church_id),
    INDEX idx_reminder_status (status),
    INDEX idx_reminder_scheduled_for (scheduled_for),
    INDEX idx_reminder_target_group (target_group),
    INDEX idx_reminder_fellowship (fellowship_id)
);

-- Table: reminder_recipient
-- Purpose: Track reminder delivery status for each member
CREATE TABLE reminder_recipient (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reminder_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    sent_at TIMESTAMP,
    delivered_at TIMESTAMP,
    failed_at TIMESTAMP,
    failure_reason VARCHAR(500),
    sms_sent BOOLEAN DEFAULT FALSE,
    email_sent BOOLEAN DEFAULT FALSE,
    whatsapp_sent BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (reminder_id) REFERENCES attendance_reminder(id) ON DELETE CASCADE,
    FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE,

    UNIQUE KEY unique_reminder_member (reminder_id, member_id),
    INDEX idx_recipient_reminder (reminder_id),
    INDEX idx_recipient_member (member_id),
    INDEX idx_recipient_status (status),
    INDEX idx_recipient_sent_at (sent_at)
);

-- ============================================================================
-- SECTION 3: Add Comments for Documentation
-- ============================================================================

-- attendance_session table comments
ALTER TABLE attendance_session
COMMENT = 'Enhanced attendance sessions with QR codes, geofencing, and recurring support';

ALTER TABLE attendance_session
MODIFY COLUMN service_type VARCHAR(50) NOT NULL DEFAULT 'SUNDAY_MAIN_SERVICE'
COMMENT 'Type of service (SUNDAY_MAIN_SERVICE, MIDWEEK_SERVICE, etc.)';

ALTER TABLE attendance_session
MODIFY COLUMN qr_code_data VARCHAR(500) UNIQUE
COMMENT 'Encrypted QR code payload for check-in';

ALTER TABLE attendance_session
MODIFY COLUMN qr_code_url TEXT
COMMENT 'Base64 encoded QR code image or file path';

ALTER TABLE attendance_session
MODIFY COLUMN qr_code_expires_at TIMESTAMP
COMMENT 'Expiry timestamp for QR code validity';

ALTER TABLE attendance_session
MODIFY COLUMN geofence_latitude DECIMAL(10, 8)
COMMENT 'Session location latitude for geofence check-in';

ALTER TABLE attendance_session
MODIFY COLUMN geofence_longitude DECIMAL(11, 8)
COMMENT 'Session location longitude for geofence check-in';

ALTER TABLE attendance_session
MODIFY COLUMN geofence_radius_meters INT DEFAULT 100
COMMENT 'Geofence radius in meters (default 100m)';

ALTER TABLE attendance_session
MODIFY COLUMN allow_late_checkin BOOLEAN DEFAULT TRUE
COMMENT 'Allow late check-ins after session start time';

ALTER TABLE attendance_session
MODIFY COLUMN late_cutoff_minutes INT DEFAULT 30
COMMENT 'Minutes after start time to consider as late (default 30 min)';

ALTER TABLE attendance_session
MODIFY COLUMN is_recurring BOOLEAN DEFAULT FALSE
COMMENT 'Whether this session is part of a recurring schedule';

ALTER TABLE attendance_session
MODIFY COLUMN recurrence_pattern VARCHAR(100)
COMMENT 'Recurrence pattern (e.g., WEEKLY, MONTHLY)';

ALTER TABLE attendance_session
MODIFY COLUMN max_capacity INT
COMMENT 'Maximum attendance capacity for the session';

ALTER TABLE attendance_session
MODIFY COLUMN check_in_opens_at TIMESTAMP
COMMENT 'Timestamp when check-in window opens';

ALTER TABLE attendance_session
MODIFY COLUMN check_in_closes_at TIMESTAMP
COMMENT 'Timestamp when check-in window closes';

-- attendance table comments
ALTER TABLE attendance
MODIFY COLUMN check_in_method VARCHAR(30) NOT NULL DEFAULT 'MANUAL'
COMMENT 'Method used for check-in (MANUAL, QR_CODE, GEOFENCE, MOBILE_APP)';

ALTER TABLE attendance
MODIFY COLUMN check_in_time TIMESTAMP
COMMENT 'Actual timestamp when member checked in';

ALTER TABLE attendance
MODIFY COLUMN is_late BOOLEAN DEFAULT FALSE
COMMENT 'Whether the check-in was after the session start time';

ALTER TABLE attendance
MODIFY COLUMN minutes_late INT
COMMENT 'Number of minutes late if is_late is true';

ALTER TABLE attendance
MODIFY COLUMN check_in_location_lat DECIMAL(10, 8)
COMMENT 'Latitude where member checked in (for geofence verification)';

ALTER TABLE attendance
MODIFY COLUMN check_in_location_long DECIMAL(11, 8)
COMMENT 'Longitude where member checked in (for geofence verification)';

ALTER TABLE attendance
MODIFY COLUMN device_info VARCHAR(200)
COMMENT 'Device information for mobile check-ins (user agent)';

-- visitor table comments
ALTER TABLE visitor
COMMENT = 'Guest/visitor tracking before conversion to membership';

-- visitor_attendance table comments
ALTER TABLE visitor_attendance
COMMENT = 'Attendance records for visitors (guests) at sessions';

-- attendance_reminder table comments
ALTER TABLE attendance_reminder
COMMENT = 'Scheduled attendance reminders for irregular attenders';

-- reminder_recipient table comments
ALTER TABLE reminder_recipient
COMMENT = 'Individual reminder delivery tracking per member';

-- ============================================================================
-- END OF MIGRATION V15
-- ============================================================================
