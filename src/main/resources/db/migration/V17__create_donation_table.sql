-- Giving Module Phase 1: Donation Recording
-- Create donation table for tracking all church donations

CREATE TABLE donation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    -- Tenant isolation
    church_id BIGINT NOT NULL,

    -- Member relationship (nullable for anonymous donations)
    member_id BIGINT,

    -- Donation details
    amount DECIMAL(10, 2) NOT NULL,
    donation_date DATE NOT NULL,
    donation_type VARCHAR(30) NOT NULL,
    payment_method VARCHAR(30) NOT NULL,

    -- Anonymous flag
    is_anonymous BOOLEAN NOT NULL DEFAULT FALSE,

    -- Reference and tracking
    reference_number VARCHAR(100),
    notes TEXT,
    campaign VARCHAR(100),

    -- Receipt tracking
    receipt_issued BOOLEAN NOT NULL DEFAULT FALSE,
    receipt_number VARCHAR(50),

    -- Currency
    currency VARCHAR(3) DEFAULT 'GHS',

    -- Audit fields
    recorded_by_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Foreign keys
    CONSTRAINT fk_donation_church FOREIGN KEY (church_id) REFERENCES church(id) ON DELETE CASCADE,
    CONSTRAINT fk_donation_member FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE SET NULL,
    CONSTRAINT fk_donation_recorded_by FOREIGN KEY (recorded_by_id) REFERENCES user(id) ON DELETE SET NULL,

    -- Indexes for performance
    INDEX idx_donation_member (member_id),
    INDEX idx_donation_date (donation_date),
    INDEX idx_donation_type (donation_type),
    INDEX idx_donation_payment_method (payment_method),
    INDEX idx_donation_church_date (church_id, donation_date)
);
