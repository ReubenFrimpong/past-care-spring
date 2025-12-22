package com.reuben.pastcare_spring.dtos;

import java.util.List;

public record ProfileCompletenessResponse(
    int completeness,
    List<String> missingFields,
    List<String> suggestions
) {
}
