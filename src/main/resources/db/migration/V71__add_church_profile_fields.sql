-- Add additional profile fields to church table including logo support

ALTER TABLE church
ADD COLUMN pastor VARCHAR(255),
ADD COLUMN denomination VARCHAR(255),
ADD COLUMN founded_year INT,
ADD COLUMN number_of_members INT,
ADD COLUMN logo_url VARCHAR(500);

-- Add index for logo lookup
CREATE INDEX idx_church_logo ON church(logo_url);
