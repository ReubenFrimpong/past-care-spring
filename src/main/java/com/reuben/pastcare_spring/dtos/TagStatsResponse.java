package com.reuben.pastcare_spring.dtos;

import java.util.Map;

public record TagStatsResponse(
    Map<String, Long> tags,
    int totalTags,
    long totalMembers
) {
}
