-- V10: Add email field to Member table
-- This allows members to have email addresses for communication and account management

ALTER TABLE member ADD COLUMN email VARCHAR(100);

-- Create index on email for faster lookups
CREATE INDEX idx_member_email ON member(email);

-- Add comment explaining the column
ALTER TABLE member MODIFY COLUMN email VARCHAR(100) COMMENT 'Member email address for communication and account management';
