-- V9: Add spouse relationship to members table
-- This enables bidirectional spouse linking between members

-- Add spouse_id column to members table
ALTER TABLE members ADD COLUMN spouse_id BIGINT;

-- Add foreign key constraint referencing members table (self-referencing)
ALTER TABLE members ADD CONSTRAINT fk_member_spouse
    FOREIGN KEY (spouse_id) REFERENCES members(id) ON DELETE SET NULL;

-- Create index for faster lookups
CREATE INDEX idx_member_spouse ON members(spouse_id);
