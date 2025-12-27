package com.reuben.pastcare_spring.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO for cancelling a registration.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CancellationRequest {
    private String reason;
}
