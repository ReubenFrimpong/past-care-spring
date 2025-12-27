-- V41: Create events table
-- Purpose: Core events management table supporting church events, services, conferences, outreach, etc.
-- Features: Multi-location support, recurring events, capacity management, registration control

CREATE TABLE events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    -- Tenant isolation
    church_id BIGINT NOT NULL,

    -- Basic information
    name VARCHAR(200) NOT NULL,
    description TEXT,
    event_type VARCHAR(50) NOT NULL,

    -- Date and time
    start_date DATETIME NOT NULL,
    end_date DATETIME NOT NULL,
    timezone VARCHAR(50) DEFAULT 'Africa/Nairobi',

    -- Location details
    location_type VARCHAR(50) NOT NULL,
    physical_location VARCHAR(500),
    virtual_link VARCHAR(1000),
    virtual_platform VARCHAR(100),

    -- Geographic location (for physical events)
    location_id BIGINT,

    -- Registration management
    requires_registration BOOLEAN NOT NULL DEFAULT FALSE,
    registration_deadline DATETIME,
    max_capacity INT,
    current_registrations INT NOT NULL DEFAULT 0,
    allow_waitlist BOOLEAN DEFAULT FALSE,
    auto_approve_registrations BOOLEAN DEFAULT TRUE,

    -- Visibility and access control
    visibility VARCHAR(50) NOT NULL DEFAULT 'PUBLIC',

    -- Recurrence support
    is_recurring BOOLEAN NOT NULL DEFAULT FALSE,
    recurrence_pattern VARCHAR(50),
    recurrence_end_date DATE,
    parent_event_id BIGINT,

    -- Organizer information
    primary_organizer_id BIGINT,

    -- Additional information
    notes TEXT,
    reminder_sent BOOLEAN DEFAULT FALSE,
    reminder_days_before INT DEFAULT 1,

    -- Status tracking
    is_cancelled BOOLEAN DEFAULT FALSE,
    cancellation_reason TEXT,
    cancelled_at DATETIME,
    cancelled_by_id BIGINT,

    -- Audit fields
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by_id BIGINT NOT NULL,
    updated_by_id BIGINT,
    deleted_at DATETIME,

    -- Foreign key constraints
    CONSTRAINT fk_event_church FOREIGN KEY (church_id) REFERENCES churches(id) ON DELETE CASCADE,
    CONSTRAINT fk_event_location FOREIGN KEY (location_id) REFERENCES locations(id) ON DELETE SET NULL,
    CONSTRAINT fk_event_primary_organizer FOREIGN KEY (primary_organizer_id) REFERENCES members(id) ON DELETE SET NULL,
    CONSTRAINT fk_event_parent FOREIGN KEY (parent_event_id) REFERENCES events(id) ON DELETE CASCADE,
    CONSTRAINT fk_event_created_by FOREIGN KEY (created_by_id) REFERENCES users(id),
    CONSTRAINT fk_event_updated_by FOREIGN KEY (updated_by_id) REFERENCES users(id),
    CONSTRAINT fk_event_cancelled_by FOREIGN KEY (cancelled_by_id) REFERENCES users(id),

    -- Constraints
    CONSTRAINT chk_event_dates CHECK (end_date >= start_date),
    CONSTRAINT chk_event_capacity CHECK (max_capacity IS NULL OR max_capacity > 0),
    CONSTRAINT chk_event_current_registrations CHECK (current_registrations >= 0),
    CONSTRAINT chk_event_registration_deadline CHECK (registration_deadline IS NULL OR registration_deadline <= start_date),

    -- Indexes for performance
    INDEX idx_event_church (church_id),
    INDEX idx_event_dates (church_id, start_date, end_date),
    INDEX idx_event_type (church_id, event_type),
    INDEX idx_event_location (location_id),
    INDEX idx_event_visibility (church_id, visibility),
    INDEX idx_event_recurring (is_recurring, parent_event_id),
    INDEX idx_event_registration (requires_registration, registration_deadline),
    INDEX idx_event_cancelled (is_cancelled),
    INDEX idx_event_deleted (deleted_at),
    INDEX idx_event_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create index for upcoming events query (most common)
CREATE INDEX idx_event_upcoming ON events(church_id, start_date, is_cancelled, deleted_at);

-- Create index for event search by name
CREATE INDEX idx_event_name ON events(church_id, name(100));
