-- Add maxUsesPerChurch field to partnership_codes table
ALTER TABLE partnership_codes
ADD COLUMN max_uses_per_church INTEGER NULL;

-- Create partnership_code_usage table to track per-church usage
CREATE TABLE partnership_code_usage (
    id BIGSERIAL PRIMARY KEY,
    partnership_code_id BIGINT NOT NULL,
    church_id BIGINT NOT NULL,
    used_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    grace_period_days_granted INTEGER NOT NULL,
    CONSTRAINT fk_partnership_code FOREIGN KEY (partnership_code_id)
        REFERENCES partnership_codes(id) ON DELETE CASCADE,
    CONSTRAINT fk_church FOREIGN KEY (church_id)
        REFERENCES churches(id) ON DELETE CASCADE,
    CONSTRAINT unique_code_church UNIQUE (partnership_code_id, church_id)
);

-- Create indexes for performance
CREATE INDEX idx_partnership_code_usage_code_id ON partnership_code_usage(partnership_code_id);
CREATE INDEX idx_partnership_code_usage_church_id ON partnership_code_usage(church_id);
CREATE INDEX idx_partnership_code_usage_used_at ON partnership_code_usage(used_at);

-- Add comments
COMMENT ON COLUMN partnership_codes.max_uses_per_church IS 'Maximum number of times a single church can use this code (NULL = unlimited per church)';
COMMENT ON TABLE partnership_code_usage IS 'Tracks which churches have used which partnership codes to enforce per-church limits';
COMMENT ON COLUMN partnership_code_usage.grace_period_days_granted IS 'Number of grace period days granted when this code was used';
