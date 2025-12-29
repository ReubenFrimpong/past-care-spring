package com.reuben.pastcare_spring.enums;

/**
 * Enumeration of available report types in the system.
 * Each report type represents a pre-built report with specific data and formatting.
 */
public enum ReportType {
    // Member Reports
    MEMBER_DIRECTORY("Member Directory", "Complete member listing with contact info", "MEMBERS"),
    BIRTHDAY_ANNIVERSARY_LIST("Birthday & Anniversary List", "Upcoming birthdays and anniversaries", "MEMBERS"),
    INACTIVE_MEMBERS("Inactive Members Report", "Members who haven't attended recently", "MEMBERS"),
    HOUSEHOLD_ROSTER("Household Roster", "Family groupings and household information", "MEMBERS"),

    // Attendance Reports
    ATTENDANCE_SUMMARY("Attendance Summary", "Attendance statistics by date range", "ATTENDANCE"),
    FIRST_TIME_VISITORS("First-Time Visitors", "New visitor tracking and follow-up", "ATTENDANCE"),

    // Giving Reports
    GIVING_SUMMARY("Giving Summary", "Donation statistics by date range", "GIVING"),
    TOP_DONORS("Top Donors Report", "Highest contributing members", "GIVING"),
    CAMPAIGN_PROGRESS("Campaign Progress Report", "Fundraising campaign statistics", "GIVING"),

    // Fellowship Reports
    FELLOWSHIP_ROSTER("Fellowship Roster", "Fellowship membership and leaders", "FELLOWSHIPS"),

    // Pastoral Care Reports
    PASTORAL_CARE_SUMMARY("Pastoral Care Summary", "Care needs, visits, and prayer requests", "PASTORAL_CARE"),

    // Events Reports
    EVENT_ATTENDANCE("Event Attendance Report", "Event participation statistics", "EVENTS"),

    // Growth Reports
    GROWTH_TREND("Growth Trend Report", "Membership and attendance trends", "ANALYTICS");

    private final String displayName;
    private final String description;
    private final String category;

    ReportType(String displayName, String description, String category) {
        this.displayName = displayName;
        this.description = description;
        this.category = category;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }
}
