-- Giving Module Phase 3: Pledge & Campaign Management
-- Create campaign table for tracking fundraising campaigns

CREATE TABLE campaign (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    -- Tenant isolation
    church_id BIGINT NOT NULL,

    -- Campaign details
    name VARCHAR(200) NOT NULL,
    description TEXT,
    goal_amount DECIMAL(12, 2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'GHS',

    -- Timeline
    start_date DATE NOT NULL,
    end_date DATE,

    -- Status
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, PAUSED, COMPLETED, CANCELLED
    is_public BOOLEAN NOT NULL DEFAULT TRUE, -- Show in member portal?

    -- Progress tracking
    current_amount DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    total_pledges DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    total_donations INT NOT NULL DEFAULT 0,
    total_pledges_count INT NOT NULL DEFAULT 0,

    -- Display options
    show_thermometer BOOLEAN NOT NULL DEFAULT TRUE,
    show_donor_list BOOLEAN NOT NULL DEFAULT TRUE,
    featured BOOLEAN NOT NULL DEFAULT FALSE, -- Featured on dashboard

    -- Audit fields
    created_by_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Foreign keys
    CONSTRAINT fk_campaign_church FOREIGN KEY (church_id) REFERENCES church(id) ON DELETE CASCADE,
    CONSTRAINT fk_campaign_created_by FOREIGN KEY (created_by_id) REFERENCES user(id) ON DELETE SET NULL,

    -- Indexes for performance
    INDEX idx_campaign_church (church_id),
    INDEX idx_campaign_status (status),
    INDEX idx_campaign_dates (start_date, end_date),
    INDEX idx_campaign_church_status (church_id, status)
);
