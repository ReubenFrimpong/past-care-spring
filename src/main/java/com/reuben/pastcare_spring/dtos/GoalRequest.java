package com.reuben.pastcare_spring.dtos;

import com.reuben.pastcare_spring.enums.GoalType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO for creating/updating goals.
 * Dashboard Phase 2.3: Goal Tracking
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoalRequest {

    @NotNull(message = "Goal type is required")
    private GoalType goalType;

    @NotNull(message = "Target value is required")
    @Positive(message = "Target value must be positive")
    private BigDecimal targetValue;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;
}
