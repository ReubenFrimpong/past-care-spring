package com.reuben.pastcare_spring.models;

public enum CareNeedType {
    HOSPITAL_VISIT("Hospital Visit"),
    BEREAVEMENT("Bereavement Support"),
    COUNSELING("Counseling"),
    PRAYER("Prayer Request"),
    FINANCIAL_ASSISTANCE("Financial Assistance"),
    SPIRITUAL_GUIDANCE("Spiritual Guidance"),
    MARRIAGE_SUPPORT("Marriage Support"),
    FAMILY_CRISIS("Family Crisis"),
    UNEMPLOYMENT("Unemployment Support"),
    ADDICTION_RECOVERY("Addiction Recovery"),
    MENTAL_HEALTH("Mental Health Support"),
    ELDERLY_CARE("Elderly Care"),
    CHILD_CARE("Child Care Assistance"),
    MEDICAL_EMERGENCY("Medical Emergency"),
    HOUSING_ASSISTANCE("Housing Assistance"),
    OTHER("Other");

    private final String displayName;

    CareNeedType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
