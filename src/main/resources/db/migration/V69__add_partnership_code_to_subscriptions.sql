-- Add partnership code tracking to church_subscriptions
ALTER TABLE church_subscriptions
ADD COLUMN partnership_code_id BIGINT NULL,
ADD COLUMN grace_period_end DATETIME NULL,
ADD CONSTRAINT fk_partnership_code FOREIGN KEY (partnership_code_id) REFERENCES partnership_codes(id);

CREATE INDEX idx_grace_period_end ON church_subscriptions(grace_period_end);
