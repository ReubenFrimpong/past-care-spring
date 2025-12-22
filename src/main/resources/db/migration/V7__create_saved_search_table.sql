-- Create saved_searches table for storing member search queries
CREATE TABLE saved_searches (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    church_id BIGINT NOT NULL,
    created_by_user_id BIGINT NOT NULL,
    search_name VARCHAR(200) NOT NULL,
    search_criteria TEXT NOT NULL,
    is_public BOOLEAN NOT NULL DEFAULT FALSE,
    is_dynamic BOOLEAN NOT NULL DEFAULT FALSE,
    description TEXT,
    last_executed TIMESTAMP NULL,
    last_result_count BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_saved_search_church
        FOREIGN KEY (church_id) REFERENCES churches(id) ON DELETE CASCADE,
    CONSTRAINT fk_saved_search_user
        FOREIGN KEY (created_by_user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Indexes for performance
CREATE INDEX idx_saved_search_church_user ON saved_searches(church_id, created_by_user_id);
CREATE INDEX idx_saved_search_name ON saved_searches(search_name);
CREATE INDEX idx_saved_search_public ON saved_searches(church_id, is_public);
