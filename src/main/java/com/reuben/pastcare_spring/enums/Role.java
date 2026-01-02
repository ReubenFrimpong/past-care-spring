package com.reuben.pastcare_spring.enums;

import java.util.Arrays;
import java.util.Set;
import java.util.EnumSet;

/**
 * User roles in the PastCare SaaS application.
 *
 * Each role has an associated set of permissions that define what actions
 * users with that role can perform.
 *
 * Hierarchy (authority level):
 * 1. SUPERADMIN (platform level)
 * 2. ADMIN (church level - full access)
 * 3. PASTOR, TREASURER, MEMBER_MANAGER (church level - departmental)
 * 4. FELLOWSHIP_LEADER (fellowship level)
 * 5. MEMBER (individual level)
 */
public enum Role {

    /**
     * Platform administrator - manages all churches and billing
     */
    SUPERADMIN(EnumSet.of(
        // Platform permissions (SUPERADMIN gets ALL permissions)
        Permission.PLATFORM_ACCESS,
        Permission.ALL_CHURCHES_VIEW,
        Permission.ALL_CHURCHES_MANAGE,
        Permission.BILLING_MANAGE,
        Permission.SYSTEM_CONFIG
        // Note: SUPERADMIN check bypasses individual permission checks
    )),

    /**
     * Church administrator - full access to church operations
     */
    ADMIN(EnumSet.of(
        // Member permissions
        Permission.MEMBER_VIEW_ALL,
        Permission.MEMBER_CREATE,
        Permission.MEMBER_EDIT_ALL,
        Permission.MEMBER_DELETE,
        Permission.MEMBER_EXPORT,
        Permission.MEMBER_IMPORT,

        // Household permissions
        Permission.HOUSEHOLD_VIEW,
        Permission.HOUSEHOLD_CREATE,
        Permission.HOUSEHOLD_EDIT,
        Permission.HOUSEHOLD_DELETE,
        Permission.HOUSEHOLD_MANAGE,

        // Fellowship permissions
        Permission.FELLOWSHIP_VIEW_ALL,
        Permission.FELLOWSHIP_CREATE,
        Permission.FELLOWSHIP_EDIT_ALL,
        Permission.FELLOWSHIP_DELETE,
        Permission.FELLOWSHIP_MANAGE,
        Permission.FELLOWSHIP_MANAGE_MEMBERS,

        // Financial permissions (full access)
        Permission.DONATION_VIEW_ALL,
        Permission.DONATION_CREATE,
        Permission.DONATION_EDIT,
        Permission.DONATION_DELETE,
        Permission.DONATION_EXPORT,
        Permission.CAMPAIGN_VIEW,
        Permission.CAMPAIGN_MANAGE,
        Permission.PLEDGE_VIEW_ALL,
        Permission.PLEDGE_MANAGE,
        Permission.RECEIPT_ISSUE,

        // Event permissions
        Permission.EVENT_VIEW_ALL,
        Permission.EVENT_CREATE,
        Permission.EVENT_EDIT_ALL,
        Permission.EVENT_EDIT,
        Permission.EVENT_DELETE,
        Permission.EVENT_REGISTER,
        Permission.EVENT_MANAGE_REGISTRATIONS,

        // Attendance permissions
        Permission.ATTENDANCE_VIEW_ALL,
        Permission.ATTENDANCE_VIEW,
        Permission.ATTENDANCE_RECORD,
        Permission.ATTENDANCE_MARK,
        Permission.ATTENDANCE_EDIT,

        // Visitor permissions
        Permission.VISITOR_VIEW,
        Permission.VISITOR_MANAGE,

        // Pastoral care permissions
        Permission.CARE_NEED_VIEW_ALL,
        Permission.CARE_NEED_CREATE,
        Permission.CARE_NEED_EDIT,
        Permission.CARE_NEED_ASSIGN,
        Permission.VISIT_VIEW_ALL,
        Permission.VISIT_CREATE,
        Permission.VISIT_EDIT,
        Permission.PRAYER_REQUEST_VIEW_ALL,
        Permission.PRAYER_REQUEST_CREATE,
        Permission.PRAYER_REQUEST_EDIT,

        // Communication permissions
        Permission.SMS_SEND,
        Permission.EMAIL_SEND,
        Permission.BULK_MESSAGE_SEND,

        // Report permissions
        Permission.REPORT_VIEW,
        Permission.REPORT_GENERATE,
        Permission.REPORT_MEMBER,
        Permission.REPORT_FINANCIAL,
        Permission.REPORT_ATTENDANCE,
        Permission.REPORT_ANALYTICS,
        Permission.REPORT_EXPORT,

        // Admin permissions
        Permission.USER_VIEW,
        Permission.USER_CREATE,
        Permission.USER_EDIT,
        Permission.USER_DELETE,
        Permission.USER_MANAGE,
        Permission.USER_MANAGE_ROLES,
        Permission.CHURCH_SETTINGS_VIEW,
        Permission.CHURCH_SETTINGS_EDIT,
        Permission.SUBSCRIPTION_VIEW,
        Permission.SUBSCRIPTION_MANAGE,
        Permission.BILLING_VIEW
    )),

    /**
     * Pastor - focus on pastoral care and member oversight
     */
    PASTOR(EnumSet.of(
        // Member permissions
        Permission.MEMBER_VIEW_ALL,
        Permission.MEMBER_EDIT_PASTORAL,

        // Household permissions
        Permission.HOUSEHOLD_VIEW,

        // Fellowship permissions
        Permission.FELLOWSHIP_VIEW_ALL,

        // Event permissions
        Permission.EVENT_VIEW_ALL,
        Permission.EVENT_CREATE,
        Permission.EVENT_EDIT_OWN,
        Permission.EVENT_REGISTER,

        // Attendance permissions
        Permission.ATTENDANCE_VIEW_ALL,
        Permission.ATTENDANCE_RECORD,

        // Pastoral care permissions
        Permission.CARE_NEED_VIEW_ALL,
        Permission.CARE_NEED_CREATE,
        Permission.CARE_NEED_EDIT,
        Permission.CARE_NEED_ASSIGN,
        Permission.VISIT_VIEW_ALL,
        Permission.VISIT_CREATE,
        Permission.VISIT_EDIT,
        Permission.PRAYER_REQUEST_VIEW_ALL,
        Permission.PRAYER_REQUEST_CREATE,
        Permission.PRAYER_REQUEST_EDIT,

        // Communication permissions
        Permission.SMS_SEND,
        Permission.EMAIL_SEND,

        // Report permissions
        Permission.REPORT_MEMBER,
        Permission.REPORT_ATTENDANCE,
        Permission.REPORT_ANALYTICS
    )),

    /**
     * Treasurer - focus on financial operations
     */
    TREASURER(EnumSet.of(
        // Member permissions (view only for donor management)
        Permission.MEMBER_VIEW_ALL,

        // Financial permissions
        Permission.DONATION_VIEW_ALL,
        Permission.DONATION_CREATE,
        Permission.DONATION_EDIT,
        Permission.DONATION_DELETE,
        Permission.DONATION_EXPORT,
        Permission.CAMPAIGN_VIEW,
        Permission.CAMPAIGN_MANAGE,
        Permission.PLEDGE_VIEW_ALL,
        Permission.PLEDGE_MANAGE,
        Permission.RECEIPT_ISSUE,

        // Report permissions
        Permission.REPORT_FINANCIAL,
        Permission.REPORT_EXPORT
    )),

    /**
     * Fellowship Leader - manages specific fellowship(s)
     *
     * @deprecated Use FELLOWSHIP_LEADER instead
     */
    @Deprecated
    FELLOWSHIP_HEAD(EnumSet.of(
        // Member permissions (fellowship-scoped)
        Permission.MEMBER_VIEW_FELLOWSHIP,

        // Fellowship permissions (own fellowship only)
        Permission.FELLOWSHIP_VIEW_OWN,
        Permission.FELLOWSHIP_EDIT_OWN,

        // Event permissions
        Permission.EVENT_VIEW_ALL,
        Permission.EVENT_CREATE,
        Permission.EVENT_EDIT_OWN,

        // Attendance permissions (fellowship-scoped)
        Permission.ATTENDANCE_VIEW_FELLOWSHIP,
        Permission.ATTENDANCE_RECORD,

        // Communication permissions (fellowship-scoped)
        Permission.SMS_SEND_FELLOWSHIP,
        Permission.EMAIL_SEND
    )),

    /**
     * Fellowship Leader - manages specific fellowship(s)
     */
    FELLOWSHIP_LEADER(EnumSet.of(
        // Member permissions (fellowship-scoped)
        Permission.MEMBER_VIEW_FELLOWSHIP,

        // Fellowship permissions (own fellowship only)
        Permission.FELLOWSHIP_VIEW_OWN,
        Permission.FELLOWSHIP_EDIT_OWN,

        // Event permissions
        Permission.EVENT_VIEW_ALL,
        Permission.EVENT_CREATE,
        Permission.EVENT_EDIT_OWN,

        // Attendance permissions (fellowship-scoped)
        Permission.ATTENDANCE_VIEW_FELLOWSHIP,
        Permission.ATTENDANCE_RECORD,

        // Communication permissions (fellowship-scoped)
        Permission.SMS_SEND_FELLOWSHIP,
        Permission.EMAIL_SEND
    )),

    /**
     * Member Manager - focus on member data management
     */
    MEMBER_MANAGER(EnumSet.of(
        // Member permissions
        Permission.MEMBER_VIEW_ALL,
        Permission.MEMBER_CREATE,
        Permission.MEMBER_EDIT_ALL,
        Permission.MEMBER_DELETE,
        Permission.MEMBER_EXPORT,
        Permission.MEMBER_IMPORT,

        // Household permissions
        Permission.HOUSEHOLD_VIEW,
        Permission.HOUSEHOLD_CREATE,
        Permission.HOUSEHOLD_EDIT,
        Permission.HOUSEHOLD_DELETE,

        // Report permissions
        Permission.REPORT_MEMBER,
        Permission.REPORT_ANALYTICS,
        Permission.REPORT_EXPORT
    )),

    /**
     * Regular church member - limited personal access
     */
    MEMBER(EnumSet.of(
        // Member permissions (own profile only)
        Permission.MEMBER_VIEW_OWN,
        Permission.MEMBER_EDIT_OWN,

        // Fellowship permissions (view own membership)
        Permission.FELLOWSHIP_VIEW_OWN,

        // Financial permissions (own giving history)
        Permission.DONATION_VIEW_OWN,
        Permission.PLEDGE_VIEW_OWN,

        // Event permissions
        Permission.EVENT_VIEW_PUBLIC,
        Permission.EVENT_REGISTER,

        // Pastoral care permissions
        Permission.PRAYER_REQUEST_CREATE
    ));

    private final Set<Permission> permissions;

    Role(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    /**
     * Get all permissions for this role
     */
    public Set<Permission> getPermissions() {
        return permissions;
    }

    /**
     * Check if this role has a specific permission
     */
    public boolean hasPermission(Permission permission) {
        // SUPERADMIN has all permissions
        if (this == SUPERADMIN) {
            return true;
        }
        return permissions.contains(permission);
    }

    /**
     * Check if this role has ANY of the specified permissions
     */
    public boolean hasAnyPermission(Permission... permissions) {
        return Arrays.stream(permissions)
            .anyMatch(this::hasPermission);
    }

    /**
     * Check if this role has ALL of the specified permissions
     */
    public boolean hasAllPermissions(Permission... permissions) {
        return Arrays.stream(permissions)
            .allMatch(this::hasPermission);
    }

    /**
     * Check if this is a platform-level role
     */
    public boolean isPlatformRole() {
        return this == SUPERADMIN;
    }

    /**
     * Check if this is a church-level admin role
     */
    public boolean isChurchAdmin() {
        return this == ADMIN;
    }

    /**
     * Check if this is a departmental role (Pastor, Treasurer, etc.)
     */
    public boolean isDepartmentalRole() {
        return this == PASTOR || this == TREASURER || this == MEMBER_MANAGER;
    }

    /**
     * Check if this is a fellowship-level role
     */
    public boolean isFellowshipRole() {
        return this == FELLOWSHIP_LEADER || this == FELLOWSHIP_HEAD;
    }

    /**
     * Get display name for role (for UI)
     */
    public String getDisplayName() {
        return switch (this) {
            case SUPERADMIN -> "Super Administrator";
            case ADMIN -> "Administrator";
            case PASTOR -> "Pastor";
            case TREASURER -> "Treasurer";
            case FELLOWSHIP_HEAD -> "Fellowship Head (deprecated - use Fellowship Leader)";
            case FELLOWSHIP_LEADER -> "Fellowship Leader";
            case MEMBER_MANAGER -> "Member Manager";
            case MEMBER -> "Member";
        };
    }

    /**
     * Get description for role
     */
    public String getDescription() {
        return switch (this) {
            case SUPERADMIN -> "Platform administrator with full access to all churches";
            case ADMIN -> "Church administrator with full access to church operations";
            case PASTOR -> "Pastor with access to member oversight and pastoral care";
            case TREASURER -> "Financial officer with access to donations and campaigns";
            case FELLOWSHIP_HEAD -> "Fellowship leader (deprecated role)";
            case FELLOWSHIP_LEADER -> "Fellowship leader with access to own fellowship management";
            case MEMBER_MANAGER -> "Member data manager with access to member operations";
            case MEMBER -> "Regular church member with limited personal access";
        };
    }
}
