package com.reuben.pastcare_spring.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SmsTemplateRequest {

    @NotBlank(message = "Template name is required")
    @Size(max = 200, message = "Name too long")
    private String name;

    @Size(max = 500, message = "Description too long")
    private String description;

    @NotBlank(message = "Template content is required")
    private String template;

    @Size(max = 100, message = "Category too long")
    private String category;

    private Boolean isActive = true;

    private Boolean isDefault = false;
}
