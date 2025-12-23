package com.reuben.pastcare_spring.dtos;

import com.reuben.pastcare_spring.models.PrayerRequestCategory;
import com.reuben.pastcare_spring.models.PrayerRequestPriority;
import com.reuben.pastcare_spring.models.PrayerRequestStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for creating/updating prayer requests
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrayerRequestDto {

    @NotBlank(message = "Prayer request is required")
    private String request;

    @NotNull(message = "Category is required")
    private PrayerRequestCategory category;

    private PrayerRequestPriority priority = PrayerRequestPriority.NORMAL;

    private Boolean isAnonymous = false;

    private Boolean isUrgent = false;

    private Boolean isPublic = false;

    private PrayerRequestStatus status;

    private String testimony;

    private LocalDateTime expiresAt;
}
