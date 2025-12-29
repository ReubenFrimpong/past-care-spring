package com.reuben.pastcare_spring.dtos;

import com.reuben.pastcare_spring.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating/updating dashboard templates
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardTemplateRequest {

    @NotBlank(message = "Template name is required")
    private String templateName;

    private String description;

    @NotNull(message = "Role is required")
    private Role role;

    @NotBlank(message = "Layout configuration is required")
    private String layoutConfig; // JSON string

    private Boolean isDefault;

    private String previewImageUrl;
}
