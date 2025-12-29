-- V55__create_storage_usage_table.sql
-- Table to track storage usage for each church (tenant)
-- Used for billing and subscription management

CREATE TABLE storage_usage (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    church_id BIGINT NOT NULL,

    -- Storage metrics in megabytes (MB)
    file_storage_mb DOUBLE NOT NULL DEFAULT 0.0 COMMENT 'File storage: photos, images, documents, attachments',
    database_storage_mb DOUBLE NOT NULL DEFAULT 0.0 COMMENT 'Database storage: estimated from row counts',
    total_storage_mb DOUBLE NOT NULL DEFAULT 0.0 COMMENT 'Total storage: file + database',

    -- JSON breakdowns for detailed analysis
    file_storage_breakdown TEXT COMMENT 'JSON breakdown by category: profilePhotos, eventImages, documents, attachments',
    database_storage_breakdown TEXT COMMENT 'JSON breakdown by entity: members, donations, events, etc.',

    -- Timestamps
    calculated_at DATETIME NOT NULL COMMENT 'When this storage snapshot was calculated',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign key to church
    CONSTRAINT fk_storage_usage_church FOREIGN KEY (church_id) REFERENCES church(id) ON DELETE CASCADE,

    -- Index for fast lookup of latest usage per church
    INDEX idx_church_calculated (church_id, calculated_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Tracks storage usage for billing';
