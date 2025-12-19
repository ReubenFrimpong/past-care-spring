package com.reuben.pastcare_spring.dtos;

public record MemberStatsResponse(
    long totalMembers,
    long newThisMonth,
    long verified,
    long unverified,
    int activeRate
) {
}
