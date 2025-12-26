package com.reuben.pastcare_spring.dtos;

import com.reuben.pastcare_spring.models.CareNeedPriority;
import com.reuben.pastcare_spring.models.CareNeedType;

/**
 * DTO for auto-detected care need suggestions
 */
public record AutoDetectedCareNeed(
    Long memberId,
    String memberName,
    String reason,
    CareNeedType suggestedType,
    CareNeedPriority suggestedPriority,
    String suggestedTitle,
    String suggestedDescription,
    Integer consecutiveAbsences
) {
}
