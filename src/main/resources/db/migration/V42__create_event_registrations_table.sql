-- V42: Create event_registrations table
-- Purpose: Track member registrations for events requiring sign-up
-- Features: Approval workflow, waitlist support, attendance tracking, guest management

CREATE TABLE event_registrations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    -- Tenant isolation
    church_id BIGINT NOT NULL,

    -- Registration details
    event_id BIGINT NOT NULL,
    member_id BIGINT,

    -- Guest registration support (for non-members)
    is_guest BOOLEAN NOT NULL DEFAULT FALSE,
    guest_name VARCHAR(200),
    guest_email VARCHAR(200),
    guest_phone VARCHAR(20),

    -- Registration status
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    registration_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Guest count
    number_of_guests INT NOT NULL DEFAULT 0,
    guest_names TEXT,

    -- Approval workflow
    approved_by_id BIGINT,
    approved_at DATETIME,
    rejection_reason TEXT,

    -- Attendance tracking (links to existing attendance module)
    attended BOOLEAN DEFAULT FALSE,
    attendance_record_id BIGINT,
    check_in_time DATETIME,
    check_out_time DATETIME,

    -- Waitlist management
    is_on_waitlist BOOLEAN DEFAULT FALSE,
    waitlist_position INT,
    promoted_from_waitlist_at DATETIME,

    -- Communication
    confirmation_sent BOOLEAN DEFAULT FALSE,
    reminder_sent BOOLEAN DEFAULT FALSE,

    -- Cancellation
    is_cancelled BOOLEAN DEFAULT FALSE,
    cancellation_reason TEXT,
    cancelled_at DATETIME,

    -- Notes
    notes TEXT,
    special_requirements TEXT,

    -- Audit fields
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by_id BIGINT,
    updated_by_id BIGINT,
    deleted_at DATETIME,

    -- Foreign key constraints
    CONSTRAINT fk_registration_church FOREIGN KEY (church_id) REFERENCES churches(id) ON DELETE CASCADE,
    CONSTRAINT fk_registration_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    CONSTRAINT fk_registration_member FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE,
    CONSTRAINT fk_registration_attendance FOREIGN KEY (attendance_record_id) REFERENCES attendances(id) ON DELETE SET NULL,
    CONSTRAINT fk_registration_approved_by FOREIGN KEY (approved_by_id) REFERENCES users(id),
    CONSTRAINT fk_registration_created_by FOREIGN KEY (created_by_id) REFERENCES users(id),
    CONSTRAINT fk_registration_updated_by FOREIGN KEY (updated_by_id) REFERENCES users(id),

    -- Constraints
    CONSTRAINT chk_registration_guest_details CHECK (
        (is_guest = FALSE AND member_id IS NOT NULL) OR
        (is_guest = TRUE AND guest_name IS NOT NULL)
    ),
    CONSTRAINT chk_registration_guests_count CHECK (number_of_guests >= 0),
    CONSTRAINT chk_registration_waitlist_position CHECK (waitlist_position IS NULL OR waitlist_position > 0),

    -- Unique constraint: One registration per member per event (excluding cancelled)
    UNIQUE KEY uk_event_member_registration (event_id, member_id, is_cancelled),

    -- Indexes for performance
    INDEX idx_registration_church (church_id),
    INDEX idx_registration_event (event_id),
    INDEX idx_registration_member (member_id),
    INDEX idx_registration_status (church_id, status),
    INDEX idx_registration_waitlist (event_id, is_on_waitlist, waitlist_position),
    INDEX idx_registration_attendance (attended, check_in_time),
    INDEX idx_registration_guest (is_guest),
    INDEX idx_registration_cancelled (is_cancelled),
    INDEX idx_registration_deleted (deleted_at),
    INDEX idx_registration_date (registration_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create composite index for event registration list queries
CREATE INDEX idx_registration_event_status ON event_registrations(event_id, status, is_on_waitlist);

-- Create index for member's registration history
CREATE INDEX idx_registration_member_history ON event_registrations(member_id, registration_date, is_cancelled);
