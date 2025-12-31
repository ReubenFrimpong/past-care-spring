-- Create storage add-ons table for managing storage upgrades
-- These can be purchased by churches to expand their storage beyond the base 2GB

CREATE TABLE storage_addons (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    display_name VARCHAR(200) NOT NULL,
    description TEXT,
    storage_gb INT NOT NULL,                    -- Additional storage in GB (e.g., 3, 8, 18, 48)
    price DECIMAL(10, 2) NOT NULL,              -- Monthly price in GHC
    total_storage_gb INT NOT NULL,              -- Total storage with base (e.g., 5, 10, 20, 50)
    estimated_photos INT,                       -- Estimated number of photos
    estimated_documents INT,                    -- Estimated number of documents
    is_active BOOLEAN DEFAULT TRUE,
    is_recommended BOOLEAN DEFAULT FALSE,       -- Highlight as "Most Popular"
    display_order INT DEFAULT 0,                -- Order to display (ascending)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_active (is_active),
    INDEX idx_display_order (display_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default storage add-ons (prices in GHC)
-- Conversion rate used: 1 USD = 15 GHC
INSERT INTO storage_addons (
    name,
    display_name,
    description,
    storage_gb,
    price,
    total_storage_gb,
    estimated_photos,
    estimated_documents,
    is_recommended,
    display_order
) VALUES
    (
        '3GB_ADDON',
        '+3 GB Storage',
        'Perfect for small churches storing photos and documents',
        3,
        22.50,
        5,      -- 2GB base + 3GB addon
        1500,
        1000,
        FALSE,
        1
    ),
    (
        '8GB_ADDON',
        '+8 GB Storage',
        'Ideal for medium-sized churches with regular events',
        8,
        45.00,
        10,     -- 2GB base + 8GB addon
        4000,
        2500,
        TRUE,   -- Most popular
        2
    ),
    (
        '18GB_ADDON',
        '+18 GB Storage',
        'Great for large churches with extensive media libraries',
        18,
        90.00,
        20,     -- 2GB base + 18GB addon
        9000,
        6000,
        FALSE,
        3
    ),
    (
        '48GB_ADDON',
        '+48 GB Storage',
        'Maximum storage for mega churches and multi-campus ministries',
        48,
        180.00,
        50,     -- 2GB base + 48GB addon
        24000,
        15000,
        FALSE,
        4
    );

-- Verification query
-- SELECT
--     display_name,
--     storage_gb,
--     price,
--     total_storage_gb,
--     is_recommended
-- FROM storage_addons
-- WHERE is_active = TRUE
-- ORDER BY display_order;
