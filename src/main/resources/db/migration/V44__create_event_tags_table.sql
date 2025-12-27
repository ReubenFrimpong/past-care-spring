-- V44: Create event_tags table
-- Purpose: Support flexible categorization and filtering of events using tags
-- Features: Many-to-many relationship, church-scoped tags, tag-based event discovery

CREATE TABLE event_tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    -- Tenant isolation
    church_id BIGINT NOT NULL,

    -- Relationship
    event_id BIGINT NOT NULL,

    -- Tag information
    tag VARCHAR(100) NOT NULL,
    tag_color VARCHAR(20),

    -- Audit fields
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by_id BIGINT,

    -- Foreign key constraints
    CONSTRAINT fk_event_tag_church FOREIGN KEY (church_id) REFERENCES churches(id) ON DELETE CASCADE,
    CONSTRAINT fk_event_tag_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    CONSTRAINT fk_event_tag_created_by FOREIGN KEY (created_by_id) REFERENCES users(id),

    -- Unique constraint: One tag per event (case-insensitive)
    UNIQUE KEY uk_event_tag (event_id, tag),

    -- Indexes for performance
    INDEX idx_event_tag_church (church_id),
    INDEX idx_event_tag_event (event_id),
    INDEX idx_event_tag_name (church_id, tag),
    INDEX idx_event_tag_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create index for finding events by tag
CREATE INDEX idx_event_tag_search ON event_tags(church_id, tag, event_id);
