-- Create subscription_plans table
CREATE TABLE subscription_plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    display_name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    billing_interval VARCHAR(20) NOT NULL DEFAULT 'MONTHLY',
    storage_limit_mb BIGINT NOT NULL,
    user_limit INT NOT NULL,
    is_free BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    paystack_plan_code VARCHAR(100),
    features TEXT,
    display_order INT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Insert default plans
INSERT INTO subscription_plans (name, display_name, description, price, storage_limit_mb, user_limit, is_free, display_order, features)
VALUES
    ('STARTER', 'Starter Plan', 'Perfect for small churches getting started', 0.00, 2048, 5, TRUE, 1,
     '["2 GB Storage", "Up to 5 users", "Basic features", "Community support"]'),
    ('PROFESSIONAL', 'Professional Plan', 'For growing churches with more needs', 50.00, 10240, 50, FALSE, 2,
     '["10 GB Storage", "Up to 50 users", "All features", "Priority support", "Custom branding"]'),
    ('ENTERPRISE', 'Enterprise Plan', 'For large churches with advanced requirements', 150.00, 51200, -1, FALSE, 3,
     '["50 GB Storage", "Unlimited users", "All features", "Premium support", "Custom branding", "API access", "Dedicated account manager"]');

-- Create index for active plans
CREATE INDEX idx_subscription_plans_active ON subscription_plans(is_active);
CREATE INDEX idx_subscription_plans_order ON subscription_plans(display_order);
