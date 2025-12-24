package com.reuben.pastcare_spring.dtos;

import com.reuben.pastcare_spring.enums.AgeGroup;
import com.reuben.pastcare_spring.enums.VisitorSource;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating/updating a visitor.
 * Phase 1: Enhanced Attendance Tracking - Visitor Management
 *
 * @param firstName Visitor's first name
 * @param lastName Visitor's last name
 * @param phoneNumber Visitor's phone number (unique)
 * @param email Visitor's email address
 * @param ageGroup Visitor's age group
 * @param howHeardAboutUs How the visitor heard about the church
 * @param invitedByMemberId ID of member who invited them (optional)
 * @param assignedToUserId ID of user assigned for follow-up (optional)
 * @param followUpStatus Follow-up status (e.g., "PENDING", "CONTACTED", "SCHEDULED")
 * @param notes Additional notes about the visitor
 */
public record VisitorRequest(
    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    String firstName,

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    String lastName,

    @NotBlank(message = "Phone number is required")
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    String phoneNumber,

    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    String email,

    AgeGroup ageGroup,

    VisitorSource howHeardAboutUs,

    Long invitedByMemberId,

    Long assignedToUserId,

    @Size(max = 30, message = "Follow-up status must not exceed 30 characters")
    String followUpStatus,

    String notes
) {
}
