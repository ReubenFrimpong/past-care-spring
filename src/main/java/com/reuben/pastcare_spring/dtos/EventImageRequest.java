package com.reuben.pastcare_spring.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventImageRequest {

    @NotBlank(message = "Image URL is required")
    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    private String imageUrl;

    @Size(max = 500, message = "Caption must not exceed 500 characters")
    private String caption;

    private Integer displayOrder;

    private Boolean isCoverImage;
}
