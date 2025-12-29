-- Create payments table
CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    church_id BIGINT NOT NULL,
    plan_id BIGINT,
    amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'USD',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    paystack_reference VARCHAR(100) NOT NULL UNIQUE,
    paystack_transaction_id VARCHAR(100),
    paystack_authorization_code VARCHAR(100),
    payment_method VARCHAR(50),
    card_last4 VARCHAR(4),
    card_brand VARCHAR(50),
    card_expiry VARCHAR(5),
    payment_type VARCHAR(20) DEFAULT 'SUBSCRIPTION',
    description TEXT,
    metadata TEXT,
    invoice_number VARCHAR(50),
    payment_date TIMESTAMP,
    refund_amount DECIMAL(10, 2),
    refund_date TIMESTAMP,
    refund_reason TEXT,
    failure_reason TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (church_id) REFERENCES churches(id) ON DELETE CASCADE,
    FOREIGN KEY (plan_id) REFERENCES subscription_plans(id) ON DELETE SET NULL
);

-- Create indexes for query performance
CREATE INDEX idx_payments_church ON payments(church_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_reference ON payments(paystack_reference);
CREATE INDEX idx_payments_transaction ON payments(paystack_transaction_id);
CREATE INDEX idx_payments_date ON payments(payment_date);
CREATE INDEX idx_payments_type ON payments(payment_type);
CREATE INDEX idx_payments_invoice ON payments(invoice_number);

-- Create index for reporting queries
CREATE INDEX idx_payments_church_date ON payments(church_id, payment_date);
CREATE INDEX idx_payments_church_status ON payments(church_id, status);
