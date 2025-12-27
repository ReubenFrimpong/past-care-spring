-- Add ticket_code column to event_registrations table for QR code tickets
ALTER TABLE event_registrations
ADD COLUMN ticket_code VARCHAR(500) UNIQUE;

COMMENT ON COLUMN event_registrations.ticket_code IS 'Encrypted ticket code for QR code generation and check-in';
