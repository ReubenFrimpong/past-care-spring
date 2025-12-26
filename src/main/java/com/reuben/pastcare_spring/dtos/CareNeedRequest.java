package com.reuben.pastcare_spring.dtos;

import com.reuben.pastcare_spring.models.CareNeedPriority;
import com.reuben.pastcare_spring.models.CareNeedType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Request DTO for creating or updating a care need
 */
public record CareNeedRequest(
    @NotNull(message = "Member ID is required")
    Long memberId,

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    String title,

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    String description,

    @NotNull(message = "Type is required")
    CareNeedType type,

    CareNeedPriority priority,

    Long assignedToUserId,

    LocalDateTime dueDate,

    Boolean followUpRequired,

    LocalDateTime followUpDate,

    Set<String> tags
) {}
