-- Create church_subscriptions table
CREATE TABLE church_subscriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    church_id BIGINT NOT NULL UNIQUE,
    plan_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'TRIALING',
    trial_end_date DATE,
    next_billing_date DATE,
    current_period_start DATE,
    current_period_end DATE,
    canceled_at TIMESTAMP,
    ends_at DATE,
    paystack_customer_code VARCHAR(100),
    paystack_subscription_code VARCHAR(100),
    paystack_email_token VARCHAR(100),
    payment_method_type VARCHAR(50),
    card_last4 VARCHAR(4),
    card_brand VARCHAR(50),
    auto_renew BOOLEAN NOT NULL DEFAULT TRUE,
    grace_period_days INT DEFAULT 7,
    failed_payment_attempts INT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (church_id) REFERENCES churches(id) ON DELETE CASCADE,
    FOREIGN KEY (plan_id) REFERENCES subscription_plans(id) ON DELETE RESTRICT
);

-- Create indexes for query performance
CREATE INDEX idx_church_subscriptions_church ON church_subscriptions(church_id);
CREATE INDEX idx_church_subscriptions_status ON church_subscriptions(status);
CREATE INDEX idx_church_subscriptions_trial_end ON church_subscriptions(trial_end_date);
CREATE INDEX idx_church_subscriptions_next_billing ON church_subscriptions(next_billing_date);
CREATE INDEX idx_church_subscriptions_paystack_customer ON church_subscriptions(paystack_customer_code);
CREATE INDEX idx_church_subscriptions_paystack_subscription ON church_subscriptions(paystack_subscription_code);

-- Create default subscriptions for existing churches (all start with STARTER plan on trial)
INSERT INTO church_subscriptions (church_id, plan_id, status, trial_end_date, auto_renew, grace_period_days, failed_payment_attempts)
SELECT
    c.id,
    (SELECT id FROM subscription_plans WHERE name = 'STARTER' LIMIT 1),
    'TRIALING',
    DATE_ADD(CURDATE(), INTERVAL 14 DAY),  -- 14 day trial
    TRUE,
    7,
    0
FROM churches c
WHERE NOT EXISTS (
    SELECT 1 FROM church_subscriptions cs WHERE cs.church_id = c.id
);
