package com.reuben.pastcare_spring.models;

public enum SmsStatus {
    PENDING,
    SCHEDULED,
    SENDING,
    SENT,
    DELIVERED,
    FAILED,
    REJECTED,
    CANCELLED
}
