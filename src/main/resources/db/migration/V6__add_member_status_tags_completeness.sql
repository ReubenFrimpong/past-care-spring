-- Add member status, profile completeness, and tags support for Phase 2 features
-- Enables quick add workflow, bulk management, and member lifecycle tracking

-- Add status field (enum stored as VARCHAR)
ALTER TABLE member ADD COLUMN status VARCHAR(30) DEFAULT 'MEMBER';

-- Add profile completeness field (0-100 percentage)
ALTER TABLE member ADD COLUMN profile_completeness INT DEFAULT 0;

-- Create member_tags table for custom categorization
CREATE TABLE member_tags (
    member_id BIGINT NOT NULL,
    tag VARCHAR(255) NOT NULL,
    FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE,
    INDEX idx_member_tags_member_id (member_id),
    INDEX idx_member_tags_tag (tag)
);

-- Update existing members to have default values
UPDATE member SET status = 'MEMBER' WHERE status IS NULL;
UPDATE member SET profile_completeness = 0 WHERE profile_completeness IS NULL;

-- Add comments for documentation
ALTER TABLE member MODIFY COLUMN status VARCHAR(30)
    COMMENT 'Member lifecycle status: VISITOR, FIRST_TIMER, REGULAR, MEMBER, LEADER, INACTIVE';

ALTER TABLE member MODIFY COLUMN profile_completeness INT
    COMMENT 'Profile completeness percentage (0-100). Quick add members start at ~25%';
