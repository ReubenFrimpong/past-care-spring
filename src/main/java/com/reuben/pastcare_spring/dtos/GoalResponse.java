package com.reuben.pastcare_spring.dtos;

import com.reuben.pastcare_spring.enums.GoalStatus;
import com.reuben.pastcare_spring.enums.GoalType;
import com.reuben.pastcare_spring.models.Goal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Response DTO for goals.
 * Dashboard Phase 2.3: Goal Tracking
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalResponse {

    private Long id;
    private Long churchId;
    private GoalType goalType;
    private BigDecimal targetValue;
    private BigDecimal currentValue;
    private LocalDate startDate;
    private LocalDate endDate;
    private GoalStatus status;
    private String title;
    private String description;
    private Long createdBy;
    private String createdByName;
    private Instant createdAt;
    private Instant updatedAt;

    // Calculated fields
    private Double progressPercentage;
    private Boolean isAchieved;
    private Boolean isActive;
    private Boolean isExpired;
    private Long daysRemaining;
    private Long totalDays;

    /**
     * Create GoalResponse from Goal entity
     */
    public static GoalResponse fromEntity(Goal goal) {
        return GoalResponse.builder()
            .id(goal.getId())
            .churchId(goal.getChurchId())
            .goalType(goal.getGoalType())
            .targetValue(goal.getTargetValue())
            .currentValue(goal.getCurrentValue())
            .startDate(goal.getStartDate())
            .endDate(goal.getEndDate())
            .status(goal.getStatus())
            .title(goal.getTitle())
            .description(goal.getDescription())
            .createdBy(goal.getCreatedBy() != null ? goal.getCreatedBy().getId() : null)
            .createdByName(goal.getCreatedBy() != null ? goal.getCreatedBy().getName() : null)
            .createdAt(goal.getCreatedAt())
            .updatedAt(goal.getUpdatedAt())
            .progressPercentage(goal.getProgressPercentage())
            .isAchieved(goal.isAchieved())
            .isActive(goal.isActive())
            .isExpired(goal.isExpired())
            .daysRemaining(goal.getDaysRemaining())
            .totalDays(goal.getTotalDays())
            .build();
    }
}
