-- V48: Create dashboard_layouts table for user dashboard customization
-- Dashboard Phase 2.1: Custom Layouts MVP

CREATE TABLE dashboard_layouts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    church_id BIGINT NOT NULL,
    layout_name VARCHAR(200) NOT NULL DEFAULT 'My Dashboard',
    is_default BOOLEAN NOT NULL DEFAULT TRUE,

    -- JSON configuration structure:
    -- {
    --   "version": 1,
    --   "gridColumns": 4,
    --   "widgets": [
    --     {
    --       "widgetKey": "stats_overview",
    --       "position": {"x": 0, "y": 0},
    --       "size": {"width": 2, "height": 1},
    --       "visible": true
    --     }
    --   ]
    -- }
    layout_config TEXT NOT NULL,

    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_dashboard_layout_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_dashboard_layout_church
        FOREIGN KEY (church_id) REFERENCES churches(id) ON DELETE CASCADE,

    -- Each user can have only one default layout per church
    -- Note: MySQL doesn't support partial unique indexes, so we enforce this in application logic
    CONSTRAINT uk_user_church_layout UNIQUE (user_id, church_id, layout_name)
);

-- Indexes for performance
CREATE INDEX idx_dashboard_layout_user ON dashboard_layouts(user_id);
CREATE INDEX idx_dashboard_layout_church ON dashboard_layouts(church_id);
CREATE INDEX idx_dashboard_layout_default ON dashboard_layouts(user_id, is_default);
