-- Create households table for family/household management
CREATE TABLE households (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    church_id BIGINT NOT NULL,
    household_name VARCHAR(200) NOT NULL,
    household_head_id BIGINT,
    location_id BIGINT,
    notes TEXT,
    established_date DATE,
    household_image_url VARCHAR(500),
    household_email VARCHAR(200),
    household_phone VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Foreign keys
    CONSTRAINT fk_households_church FOREIGN KEY (church_id) REFERENCES churches(id) ON DELETE CASCADE,
    CONSTRAINT fk_households_head FOREIGN KEY (household_head_id) REFERENCES members(id) ON DELETE SET NULL,
    CONSTRAINT fk_households_location FOREIGN KEY (location_id) REFERENCES locations(id) ON DELETE SET NULL,

    -- Indexes for performance
    INDEX idx_household_church_id (church_id),
    INDEX idx_household_name (household_name),
    INDEX idx_household_head (household_head_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add household_id column to members table
ALTER TABLE members
ADD COLUMN household_id BIGINT AFTER location_id,
ADD CONSTRAINT fk_members_household FOREIGN KEY (household_id) REFERENCES households(id) ON DELETE SET NULL,
ADD INDEX idx_members_household (household_id);
