package com.reuben.pastcare_spring.dtos;

public record CompletenessStatsResponse(
    double averageCompleteness,
    long totalMembers,
    long completeProfiles,      // 100%
    long nearlyComplete,        // 75-99%
    long incomplete,            // 0-74%
    Distribution distribution
) {
    public record Distribution(
        long range0to25,
        long range26to50,
        long range51to75,
        long range76to100
    ) {}
}
