-- Giving Module Phase 3: Pledge & Campaign Management
-- Create pledge_payment table for tracking individual pledge payments

CREATE TABLE pledge_payment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    -- Tenant isolation
    church_id BIGINT NOT NULL,

    -- Pledge relationship
    pledge_id BIGINT NOT NULL,

    -- Donation relationship (links to actual donation record)
    donation_id BIGINT,

    -- Payment details
    amount DECIMAL(10, 2) NOT NULL,
    payment_date DATE NOT NULL,
    due_date DATE,

    -- Status
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, PAID, LATE, MISSED, CANCELLED

    -- Notes
    notes TEXT,

    -- Audit fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Foreign keys
    CONSTRAINT fk_pledge_payment_church FOREIGN KEY (church_id) REFERENCES church(id) ON DELETE CASCADE,
    CONSTRAINT fk_pledge_payment_pledge FOREIGN KEY (pledge_id) REFERENCES pledge(id) ON DELETE CASCADE,
    CONSTRAINT fk_pledge_payment_donation FOREIGN KEY (donation_id) REFERENCES donation(id) ON DELETE SET NULL,

    -- Indexes for performance
    INDEX idx_pledge_payment_church (church_id),
    INDEX idx_pledge_payment_pledge (pledge_id),
    INDEX idx_pledge_payment_donation (donation_id),
    INDEX idx_pledge_payment_status (status),
    INDEX idx_pledge_payment_due_date (due_date),
    INDEX idx_pledge_payment_church_status (church_id, status)
);
