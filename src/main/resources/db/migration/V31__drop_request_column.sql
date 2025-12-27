-- Drop the legacy 'request' column from V14 schema
-- This column is replaced by 'title' and 'description' in V28 schema

ALTER TABLE prayer_requests DROP COLUMN request;
