-- V56__add_tenant_isolation_indexes.sql
-- Performance indexes for tenant isolation and efficient queries
-- Created: 2025-12-29

-- =====================================================
-- TENANT FILTERING INDEXES
-- =====================================================
-- These indexes optimize the Hibernate filter WHERE church_id = ? clause

-- Members table
CREATE INDEX IF NOT EXISTS idx_members_church_id ON members(church_id);
CREATE INDEX IF NOT EXISTS idx_members_id_church_id ON members(id, church_id);

-- Donations table
CREATE INDEX IF NOT EXISTS idx_donations_church_id ON donations(church_id);
CREATE INDEX IF NOT EXISTS idx_donations_id_church_id ON donations(id, church_id);

-- Events table
CREATE INDEX IF NOT EXISTS idx_events_church_id ON events(church_id);
CREATE INDEX IF NOT EXISTS idx_events_id_church_id ON events(id, church_id);
CREATE INDEX IF NOT EXISTS idx_events_church_deleted ON events(church_id, deleted_at);

-- Visits table
CREATE INDEX IF NOT EXISTS idx_visits_church_id ON visits(church_id);
CREATE INDEX IF NOT EXISTS idx_visits_id_church_id ON visits(id, church_id);

-- Households table
CREATE INDEX IF NOT EXISTS idx_households_church_id ON households(church_id);
CREATE INDEX IF NOT EXISTS idx_households_id_church_id ON households(id, church_id);

-- Fellowships table
CREATE INDEX IF NOT EXISTS idx_fellowships_church_id ON fellowships(church_id);
CREATE INDEX IF NOT EXISTS idx_fellowships_id_church_id ON fellowships(id, church_id);

-- Campaigns table
CREATE INDEX IF NOT EXISTS idx_campaigns_church_id ON campaigns(church_id);
CREATE INDEX IF NOT EXISTS idx_campaigns_id_church_id ON campaigns(id, church_id);

-- Care Needs table
CREATE INDEX IF NOT EXISTS idx_care_needs_church_id ON care_needs(church_id);
CREATE INDEX IF NOT EXISTS idx_care_needs_id_church_id ON care_needs(id, church_id);

-- Prayer Requests table
CREATE INDEX IF NOT EXISTS idx_prayer_requests_church_id ON prayer_requests(church_id);
CREATE INDEX IF NOT EXISTS idx_prayer_requests_id_church_id ON prayer_requests(id, church_id);

-- Attendance Sessions table
CREATE INDEX IF NOT EXISTS idx_attendance_sessions_church_id ON attendance_sessions(church_id);
CREATE INDEX IF NOT EXISTS idx_attendance_sessions_id_church_id ON attendance_sessions(id, church_id);

-- Event Registrations table
CREATE INDEX IF NOT EXISTS idx_event_registrations_church_id ON event_registrations(church_id);
CREATE INDEX IF NOT EXISTS idx_event_registrations_event_id ON event_registrations(event_id);

-- Event Organizers table
CREATE INDEX IF NOT EXISTS idx_event_organizers_church_id ON event_organizers(church_id);
CREATE INDEX IF NOT EXISTS idx_event_organizers_event_id ON event_organizers(event_id);

-- Event Tags table
CREATE INDEX IF NOT EXISTS idx_event_tags_church_id ON event_tags(church_id);
CREATE INDEX IF NOT EXISTS idx_event_tags_event_id ON event_tags(event_id);

-- Pledges table
CREATE INDEX IF NOT EXISTS idx_pledges_church_id ON pledges(church_id);
CREATE INDEX IF NOT EXISTS idx_pledges_id_church_id ON pledges(id, church_id);

-- Visitors table
CREATE INDEX IF NOT EXISTS idx_visitors_church_id ON visitors(church_id);
CREATE INDEX IF NOT EXISTS idx_visitors_id_church_id ON visitors(id, church_id);

-- =====================================================
-- STORAGE USAGE INDEXES
-- =====================================================
-- Optimize storage usage queries for billing

-- Primary index for getting latest usage per church
CREATE INDEX IF NOT EXISTS idx_storage_usage_church_calculated
ON storage_usage(church_id, calculated_at DESC);

-- Index for date range queries
CREATE INDEX IF NOT EXISTS idx_storage_usage_calculated
ON storage_usage(calculated_at DESC);

-- =====================================================
-- COMMON QUERY PATTERNS
-- =====================================================

-- Members by location
CREATE INDEX IF NOT EXISTS idx_members_location_id ON members(location_id);

-- Donations by date
CREATE INDEX IF NOT EXISTS idx_donations_date ON donations(donation_date);
CREATE INDEX IF NOT EXISTS idx_donations_church_date ON donations(church_id, donation_date);

-- Events by date
CREATE INDEX IF NOT EXISTS idx_events_start_date ON events(start_date);
CREATE INDEX IF NOT EXISTS idx_events_church_start ON events(church_id, start_date);

-- Attendance by session
CREATE INDEX IF NOT EXISTS idx_attendance_records_session_id ON attendance_records(session_id);

-- Comments:
-- - IF NOT EXISTS prevents errors on re-run
-- - Composite indexes (id, church_id) optimize findById with tenant filter
-- - church_id indexes allow efficient filtering by tenant
-- - Date indexes optimize common date range queries
