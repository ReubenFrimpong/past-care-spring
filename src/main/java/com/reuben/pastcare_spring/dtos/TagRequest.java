package com.reuben.pastcare_spring.dtos;

import java.util.Set;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record TagRequest(
    @NotNull(message = "Tags cannot be null")
    @NotEmpty(message = "At least one tag is required")
    Set<String> tags
) {
}
