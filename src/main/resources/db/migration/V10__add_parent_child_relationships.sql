-- Phase 3.3: Parent-Child Relationships
-- Create join table for many-to-many parent-child relationships
-- A child can have multiple parents (mother, father, guardians)
-- A parent can have multiple children

CREATE TABLE member_parents (
    child_id BIGINT NOT NULL,
    parent_id BIGINT NOT NULL,
    PRIMARY KEY (child_id, parent_id),
    FOREIGN KEY (child_id) REFERENCES member(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES member(id) ON DELETE CASCADE,
    -- Prevent self-parenting
    CONSTRAINT chk_not_self_parent CHECK (child_id <> parent_id)
);

-- Create indexes for better query performance
CREATE INDEX idx_member_parents_child ON member_parents(child_id);
CREATE INDEX idx_member_parents_parent ON member_parents(parent_id);

-- Add comment for documentation
COMMENT ON TABLE member_parents IS 'Junction table for parent-child relationships between members. Supports multiple parents per child (e.g., mother, father, guardians).';
COMMENT ON COLUMN member_parents.child_id IS 'Foreign key to the child member';
COMMENT ON COLUMN member_parents.parent_id IS 'Foreign key to the parent member';
