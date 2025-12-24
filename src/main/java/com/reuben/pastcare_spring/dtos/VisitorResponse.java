package com.reuben.pastcare_spring.dtos;

import java.time.Instant;
import java.time.LocalDate;

import com.reuben.pastcare_spring.enums.AgeGroup;
import com.reuben.pastcare_spring.enums.VisitorSource;

/**
 * Response DTO for visitor details.
 * Phase 1: Enhanced Attendance Tracking - Visitor Management
 *
 * @param id Visitor ID
 * @param firstName Visitor's first name
 * @param lastName Visitor's last name
 * @param phoneNumber Visitor's phone number
 * @param email Visitor's email address
 * @param ageGroup Visitor's age group
 * @param howHeardAboutUs How the visitor heard about the church
 * @param invitedByMemberId ID of member who invited them
 * @param invitedByMemberName Name of member who invited them
 * @param isFirstTime Whether this is their first visit
 * @param visitCount Number of times visited
 * @param lastVisitDate Date of last visit
 * @param assignedToUserId ID of user assigned for follow-up
 * @param assignedToUserName Name of user assigned for follow-up
 * @param followUpStatus Follow-up status
 * @param convertedToMember Whether converted to member
 * @param convertedMemberId ID of converted member record
 * @param conversionDate Date of conversion to member
 * @param notes Additional notes
 * @param createdAt Record creation timestamp
 * @param updatedAt Last update timestamp
 */
public record VisitorResponse(
    Long id,
    String firstName,
    String lastName,
    String phoneNumber,
    String email,
    AgeGroup ageGroup,
    VisitorSource howHeardAboutUs,
    Long invitedByMemberId,
    String invitedByMemberName,
    Boolean isFirstTime,
    Integer visitCount,
    LocalDate lastVisitDate,
    Long assignedToUserId,
    String assignedToUserName,
    String followUpStatus,
    Boolean convertedToMember,
    Long convertedMemberId,
    LocalDate conversionDate,
    String notes,
    Instant createdAt,
    Instant updatedAt
) {
}
