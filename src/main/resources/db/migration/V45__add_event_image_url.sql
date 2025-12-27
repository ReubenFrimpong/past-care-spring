-- Add image_url column to events table for event flyers/images
ALTER TABLE events
ADD COLUMN image_url VARCHAR(500);

COMMENT ON COLUMN events.image_url IS 'Relative path to uploaded event image/flyer';
