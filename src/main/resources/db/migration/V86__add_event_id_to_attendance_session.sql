-- ============================================================================
-- Migration V86: Link Attendance Sessions to Events
-- Description: Add event_id foreign key to attendance_session table to enable
--              seamless integration between Events and Attendance modules
-- Author: Claude Sonnet 4.5
-- Date: 2025-12-31
-- ============================================================================

-- Add event_id column to attendance_session table
ALTER TABLE attendance_session
ADD COLUMN event_id BIGINT,
ADD CONSTRAINT fk_attendance_session_event
    FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE SET NULL;

-- Add index for performance
CREATE INDEX idx_attendance_session_event ON attendance_session(event_id);

-- Add comment for documentation
ALTER TABLE attendance_session
MODIFY COLUMN event_id BIGINT
COMMENT 'Optional link to Events module - allows attendance tracking for event-based services';

-- ============================================================================
-- END OF MIGRATION V86
-- ============================================================================
