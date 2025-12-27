package com.reuben.pastcare_spring.dtos;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SmsTemplateResponse {

    private Long id;
    private String name;
    private String description;
    private String template;
    private String category;
    private Boolean isActive;
    private Boolean isDefault;
    private Integer usageCount;
    private Long createdById;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
