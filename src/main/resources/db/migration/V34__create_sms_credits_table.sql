-- SMS Credits Table (User Wallet)
CREATE TABLE sms_credits (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    church_id BIGINT NOT NULL,
    balance DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    total_purchased DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    total_used DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (church_id) REFERENCES churches(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_church (user_id, church_id),
    INDEX idx_church_balance (church_id, balance),
    INDEX idx_user_church (user_id, church_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
