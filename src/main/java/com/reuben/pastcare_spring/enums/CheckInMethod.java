package com.reuben.pastcare_spring.enums;

/**
 * Methods used for checking in to an attendance session.
 *
 * Phase 1: Enhanced Attendance Tracking
 */
public enum CheckInMethod {
    /**
     * Manual check-in by staff member or admin.
     * Staff marks member as present using the admin interface.
     */
    MANUAL,

    /**
     * QR code scan check-in.
     * Member scans QR code displayed at venue using mobile app.
     */
    QR_CODE,

    /**
     * Geofence-based automatic check-in.
     * Member's device detects proximity to session location.
     */
    GEOFENCE,

    /**
     * Mobile app check-in.
     * Member manually checks in using mobile app.
     */
    MOBILE_APP,

    /**
     * Self check-in at kiosk or tablet.
     * Member checks in using on-site tablet/kiosk.
     */
    SELF_CHECKIN
}
