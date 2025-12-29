-- Add paystack_authorization_code column to church_subscriptions table
ALTER TABLE church_subscriptions
ADD COLUMN paystack_authorization_code VARCHAR(100) AFTER paystack_email_token;

-- Create index for authorization code lookups
CREATE INDEX idx_church_subscriptions_auth_code ON church_subscriptions(paystack_authorization_code);
