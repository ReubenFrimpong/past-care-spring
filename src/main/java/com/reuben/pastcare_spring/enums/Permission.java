package com.reuben.pastcare_spring.enums;

/**
 * Granular permissions for Role-Based Access Control (RBAC).
 *
 * Permissions are grouped by category for organization:
 * - MEMBER: Member management operations
 * - HOUSEHOLD: Household management operations
 * - FELLOWSHIP: Fellowship management operations
 * - FINANCIAL: Donation, campaign, pledge operations
 * - EVENT: Event management operations
 * - ATTENDANCE: Attendance tracking operations
 * - PASTORAL_CARE: Care needs, visits, prayer requests
 * - COMMUNICATION: SMS, email, bulk messaging
 * - REPORT: Report generation and viewing
 * - ADMIN: User and church administration
 * - PLATFORM: Superadmin platform operations
 *
 * Usage:
 * - Roles have sets of permissions
 * - Controllers/services check permissions before operations
 * - Frontend hides UI elements based on permissions
 */
public enum Permission {

    // ========== MEMBER PERMISSIONS ==========

    /**
     * View all members in the church
     */
    MEMBER_VIEW_ALL,

    /**
     * View only own member profile
     */
    MEMBER_VIEW_OWN,

    /**
     * View members in own fellowship only (for fellowship leaders)
     */
    MEMBER_VIEW_FELLOWSHIP,

    /**
     * Create new members
     */
    MEMBER_CREATE,

    /**
     * Edit all member information
     */
    MEMBER_EDIT_ALL,

    /**
     * Edit only own member profile (limited fields)
     */
    MEMBER_EDIT_OWN,

    /**
     * Edit pastoral data (care needs, notes, visits)
     */
    MEMBER_EDIT_PASTORAL,

    /**
     * Delete members
     */
    MEMBER_DELETE,

    /**
     * Export member data (CSV, Excel)
     */
    MEMBER_EXPORT,

    /**
     * Import member data (CSV, Excel)
     */
    MEMBER_IMPORT,

    // ========== HOUSEHOLD PERMISSIONS ==========

    HOUSEHOLD_VIEW,
    HOUSEHOLD_CREATE,
    HOUSEHOLD_EDIT,
    HOUSEHOLD_DELETE,

    /**
     * Manage households (create, edit, delete)
     */
    HOUSEHOLD_MANAGE,

    // ========== FELLOWSHIP PERMISSIONS ==========

    /**
     * View all fellowships in church
     */
    FELLOWSHIP_VIEW_ALL,

    /**
     * View only fellowships user is a member of
     */
    FELLOWSHIP_VIEW_OWN,

    FELLOWSHIP_CREATE,

    /**
     * Edit all fellowships
     */
    FELLOWSHIP_EDIT_ALL,

    /**
     * Edit only fellowships where user is the leader
     */
    FELLOWSHIP_EDIT_OWN,

    FELLOWSHIP_DELETE,

    /**
     * Manage fellowships (create, edit, delete)
     */
    FELLOWSHIP_MANAGE,

    /**
     * Add or remove members from fellowships
     */
    FELLOWSHIP_MANAGE_MEMBERS,

    // ========== FINANCIAL PERMISSIONS ==========

    /**
     * View all donations in church
     */
    DONATION_VIEW_ALL,

    /**
     * View only own giving history
     */
    DONATION_VIEW_OWN,

    DONATION_CREATE,
    DONATION_EDIT,
    DONATION_DELETE,
    DONATION_EXPORT,

    /**
     * View campaigns
     */
    CAMPAIGN_VIEW,

    /**
     * Create, edit, delete campaigns
     */
    CAMPAIGN_MANAGE,

    /**
     * View all pledges
     */
    PLEDGE_VIEW_ALL,

    /**
     * View only own pledges
     */
    PLEDGE_VIEW_OWN,

    /**
     * Create, edit, delete pledges
     */
    PLEDGE_MANAGE,

    /**
     * Issue donation receipts
     */
    RECEIPT_ISSUE,

    // ========== EVENT PERMISSIONS ==========

    /**
     * View all events (including private)
     */
    EVENT_VIEW_ALL,

    /**
     * View only public events
     */
    EVENT_VIEW_PUBLIC,

    EVENT_CREATE,

    /**
     * Edit all events
     */
    EVENT_EDIT_ALL,

    /**
     * Edit events (alias for consistency)
     */
    EVENT_EDIT,

    /**
     * Edit only events created by user
     */
    EVENT_EDIT_OWN,

    EVENT_DELETE,

    /**
     * Register for events
     */
    EVENT_REGISTER,

    /**
     * Manage event registrations (approve, reject, view all)
     */
    EVENT_MANAGE_REGISTRATIONS,

    // ========== ATTENDANCE PERMISSIONS ==========

    /**
     * View all attendance records
     */
    ATTENDANCE_VIEW_ALL,

    /**
     * View attendance records (alias for consistency)
     */
    ATTENDANCE_VIEW,

    /**
     * View attendance for own fellowship only
     */
    ATTENDANCE_VIEW_FELLOWSHIP,

    /**
     * Record attendance (alias)
     */
    ATTENDANCE_RECORD,

    /**
     * Mark attendance
     */
    ATTENDANCE_MARK,

    /**
     * Edit attendance records
     */
    ATTENDANCE_EDIT,

    // ========== VISITOR PERMISSIONS ==========

    /**
     * View visitors
     */
    VISITOR_VIEW,

    /**
     * Create, edit, delete visitors
     */
    VISITOR_MANAGE,

    // ========== PASTORAL CARE PERMISSIONS ==========

    /**
     * View all care needs in church
     */
    CARE_NEED_VIEW_ALL,

    /**
     * View only care needs assigned to user
     */
    CARE_NEED_VIEW_ASSIGNED,

    CARE_NEED_CREATE,
    CARE_NEED_EDIT,

    /**
     * Assign care needs to users
     */
    CARE_NEED_ASSIGN,

    /**
     * View all visits
     */
    VISIT_VIEW_ALL,

    VISIT_CREATE,
    VISIT_EDIT,

    /**
     * View all prayer requests
     */
    PRAYER_REQUEST_VIEW_ALL,

    PRAYER_REQUEST_CREATE,
    PRAYER_REQUEST_EDIT,

    // ========== COMMUNICATION PERMISSIONS ==========

    /**
     * Send SMS to church members
     */
    SMS_SEND,

    /**
     * Send SMS to own fellowship members only
     */
    SMS_SEND_FELLOWSHIP,

    /**
     * Send emails
     */
    EMAIL_SEND,

    /**
     * Send bulk messages to multiple recipients
     */
    BULK_MESSAGE_SEND,

    // ========== REPORT PERMISSIONS ==========

    /**
     * View reports
     */
    REPORT_VIEW,

    /**
     * Generate reports
     */
    REPORT_GENERATE,

    /**
     * Generate and view member reports
     */
    REPORT_MEMBER,

    /**
     * Generate and view financial reports
     */
    REPORT_FINANCIAL,

    /**
     * Generate and view attendance reports
     */
    REPORT_ATTENDANCE,

    /**
     * Generate and view analytics reports
     */
    REPORT_ANALYTICS,

    /**
     * Export reports to PDF, Excel, CSV
     */
    REPORT_EXPORT,

    // ========== ADMIN PERMISSIONS ==========

    /**
     * View users
     */
    USER_VIEW,

    /**
     * Create new users
     */
    USER_CREATE,

    /**
     * Edit user information
     */
    USER_EDIT,

    /**
     * Delete users
     */
    USER_DELETE,

    /**
     * Manage users (create, edit, delete)
     */
    USER_MANAGE,

    /**
     * Assign roles to users
     */
    USER_MANAGE_ROLES,

    /**
     * View church settings
     */
    CHURCH_SETTINGS_VIEW,

    /**
     * Edit church settings
     */
    CHURCH_SETTINGS_EDIT,

    /**
     * View subscription and billing information
     */
    SUBSCRIPTION_VIEW,

    /**
     * Manage subscription (upgrade, downgrade, cancel)
     */
    SUBSCRIPTION_MANAGE,

    // ========== SUPERADMIN/PLATFORM PERMISSIONS ==========

    /**
     * Access platform admin panel
     */
    PLATFORM_ACCESS,

    /**
     * View all churches in the platform
     */
    ALL_CHURCHES_VIEW,

    /**
     * View all churches in the platform (alias for consistency)
     */
    PLATFORM_VIEW_ALL_CHURCHES,

    /**
     * Manage all churches (edit, delete, suspend)
     */
    ALL_CHURCHES_MANAGE,

    /**
     * Manage churches (suspend, activate, delete)
     */
    PLATFORM_MANAGE_CHURCHES,

    /**
     * View billing and subscription information (church-level)
     */
    BILLING_VIEW,

    /**
     * Manage billing and subscriptions for all churches (platform-level)
     */
    BILLING_MANAGE,

    /**
     * Configure system-wide settings
     */
    SYSTEM_CONFIG,

    /**
     * Full superadmin access to all platform features
     */
    SUPERADMIN_ACCESS;

    /**
     * Get display name for permission (for UI)
     */
    public String getDisplayName() {
        return name().replace('_', ' ').toLowerCase();
    }

    /**
     * Get category for permission
     */
    public String getCategory() {
        String name = name();
        if (name.startsWith("MEMBER_")) return "MEMBER";
        if (name.startsWith("HOUSEHOLD_")) return "HOUSEHOLD";
        if (name.startsWith("FELLOWSHIP_")) return "FELLOWSHIP";
        if (name.startsWith("DONATION_") || name.startsWith("CAMPAIGN_") ||
            name.startsWith("PLEDGE_") || name.startsWith("RECEIPT_")) return "FINANCIAL";
        if (name.startsWith("EVENT_")) return "EVENT";
        if (name.startsWith("ATTENDANCE_")) return "ATTENDANCE";
        if (name.startsWith("CARE_NEED_") || name.startsWith("VISIT_") ||
            name.startsWith("PRAYER_REQUEST_")) return "PASTORAL_CARE";
        if (name.startsWith("SMS_") || name.startsWith("EMAIL_") ||
            name.startsWith("BULK_MESSAGE_")) return "COMMUNICATION";
        if (name.startsWith("REPORT_")) return "REPORT";
        if (name.startsWith("USER_") || name.startsWith("CHURCH_SETTINGS_") ||
            name.startsWith("SUBSCRIPTION_")) return "ADMIN";
        if (name.startsWith("PLATFORM_") || name.startsWith("ALL_CHURCHES_") ||
            name.startsWith("BILLING_") || name.startsWith("SYSTEM_")) return "PLATFORM";
        return "OTHER";
    }

    /**
     * Check if this is a view-only permission
     */
    public boolean isViewPermission() {
        return name().contains("_VIEW");
    }

    /**
     * Check if this is a management permission (create, edit, delete)
     */
    public boolean isManagementPermission() {
        return name().contains("_CREATE") ||
               name().contains("_EDIT") ||
               name().contains("_DELETE") ||
               name().contains("_MANAGE");
    }
}
