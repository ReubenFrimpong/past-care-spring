package com.reuben.pastcare_spring.dtos;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Request DTO for adding or updating an event organizer.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventOrganizerRequest {

    @NotNull(message = "Member ID is required")
    private Long memberId;

    private Boolean isPrimary;

    @Size(max = 100, message = "Role must not exceed 100 characters")
    private String role;

    private Boolean isContactPerson;

    @Email(message = "Contact email must be valid")
    @Size(max = 200, message = "Contact email must not exceed 200 characters")
    private String contactEmail;

    @Size(max = 20, message = "Contact phone must not exceed 20 characters")
    private String contactPhone;

    @Size(max = 5000, message = "Responsibilities must not exceed 5000 characters")
    private String responsibilities;
}
