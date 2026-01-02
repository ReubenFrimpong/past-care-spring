package com.reuben.pastcare_spring.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for recalculate all goals endpoint.
 * Dashboard Phase 2.3: Goal Tracking
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecalculateAllResponse {
    private String message;
    private int updatedGoals;
}
