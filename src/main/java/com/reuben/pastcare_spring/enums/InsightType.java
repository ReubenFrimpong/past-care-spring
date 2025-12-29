package com.reuben.pastcare_spring.enums;

/**
 * Types of insights that can be generated.
 * Dashboard Phase 2.4: Advanced Analytics
 */
public enum InsightType {
    ANOMALY,        // Unusual pattern detected
    TREND,          // Trend analysis (positive or negative)
    PREDICTION,     // Future prediction based on data
    RECOMMENDATION, // Suggested action
    WARNING         // Warning about potential issue
}
