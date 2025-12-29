package com.reuben.pastcare_spring.dtos;

import com.reuben.pastcare_spring.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for dashboard templates
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardTemplateResponse {
    private Long id;
    private String templateName;
    private String description;
    private Role role;
    private String roleDisplayName;
    private String layoutConfig; // JSON string
    private Boolean isDefault;
    private String previewImageUrl;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
