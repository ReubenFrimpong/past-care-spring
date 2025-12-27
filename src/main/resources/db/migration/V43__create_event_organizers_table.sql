-- V43: Create event_organizers table
-- Purpose: Link multiple organizers to events (many-to-many relationship)
-- Features: Primary organizer designation, role-based organization, contact management

CREATE TABLE event_organizers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    -- Tenant isolation
    church_id BIGINT NOT NULL,

    -- Relationship
    event_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,

    -- Organizer role
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    role VARCHAR(100),

    -- Contact preferences
    is_contact_person BOOLEAN DEFAULT FALSE,
    contact_email VARCHAR(200),
    contact_phone VARCHAR(20),

    -- Responsibilities
    responsibilities TEXT,

    -- Audit fields
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by_id BIGINT,
    deleted_at DATETIME,

    -- Foreign key constraints
    CONSTRAINT fk_organizer_church FOREIGN KEY (church_id) REFERENCES churches(id) ON DELETE CASCADE,
    CONSTRAINT fk_organizer_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    CONSTRAINT fk_organizer_member FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE,
    CONSTRAINT fk_organizer_created_by FOREIGN KEY (created_by_id) REFERENCES users(id),

    -- Unique constraint: One entry per member per event
    UNIQUE KEY uk_event_member_organizer (event_id, member_id),

    -- Indexes for performance
    INDEX idx_organizer_church (church_id),
    INDEX idx_organizer_event (event_id),
    INDEX idx_organizer_member (member_id),
    INDEX idx_organizer_primary (event_id, is_primary),
    INDEX idx_organizer_contact (is_contact_person),
    INDEX idx_organizer_deleted (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create index for finding events organized by a specific member
CREATE INDEX idx_organizer_member_events ON event_organizers(member_id, event_id, is_primary);
