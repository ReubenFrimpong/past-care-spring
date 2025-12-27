package com.reuben.pastcare_spring.dtos;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Request DTO for adding a tag to an event.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventTagRequest {

    @NotBlank(message = "Tag is required")
    @Size(max = 100, message = "Tag must not exceed 100 characters")
    private String tag;

    @Size(max = 20, message = "Tag color must not exceed 20 characters")
    private String tagColor;
}
