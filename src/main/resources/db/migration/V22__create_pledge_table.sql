-- Giving Module Phase 3: Pledge & Campaign Management
-- Create pledge table for tracking member pledge commitments

CREATE TABLE pledge (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    -- Tenant isolation
    church_id BIGINT NOT NULL,

    -- Member relationship
    member_id BIGINT NOT NULL,

    -- Campaign relationship (optional)
    campaign_id BIGINT,

    -- Pledge details
    total_amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'GHS',

    -- Payment schedule
    frequency VARCHAR(20) NOT NULL, -- ONE_TIME, WEEKLY, BIWEEKLY, MONTHLY, QUARTERLY, YEARLY
    installments INT, -- Number of installments (NULL for one-time)

    -- Timeline
    pledge_date DATE NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,

    -- Status and progress
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, COMPLETED, CANCELLED, DEFAULTED
    amount_paid DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    amount_remaining DECIMAL(10, 2) NOT NULL,
    payments_made INT NOT NULL DEFAULT 0,
    last_payment_date DATE,
    next_payment_date DATE,

    -- Notes and reminders
    notes TEXT,
    send_reminders BOOLEAN NOT NULL DEFAULT TRUE,
    reminder_days_before INT DEFAULT 7, -- Send reminder X days before payment due

    -- Audit fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Foreign keys
    CONSTRAINT fk_pledge_church FOREIGN KEY (church_id) REFERENCES church(id) ON DELETE CASCADE,
    CONSTRAINT fk_pledge_member FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE,
    CONSTRAINT fk_pledge_campaign FOREIGN KEY (campaign_id) REFERENCES campaign(id) ON DELETE SET NULL,

    -- Indexes for performance
    INDEX idx_pledge_church (church_id),
    INDEX idx_pledge_member (member_id),
    INDEX idx_pledge_campaign (campaign_id),
    INDEX idx_pledge_status (status),
    INDEX idx_pledge_next_payment (next_payment_date),
    INDEX idx_pledge_church_status (church_id, status)
);
