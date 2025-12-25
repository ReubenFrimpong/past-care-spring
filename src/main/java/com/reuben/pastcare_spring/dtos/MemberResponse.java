package com.reuben.pastcare_spring.dtos;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;

import com.reuben.pastcare_spring.models.MemberStatus;

public record MemberResponse(
  Long id,

  String firstName,

  String otherName,

  String lastName,

  String title,

  String sex,

  Long churchId,

  String churchName,

  List<FellowshipSummary> fellowships,

  LocalDate dob,

  String countryCode,

  String timezone,

  String phoneNumber,

  String email,

  String whatsappNumber,

  String otherPhoneNumber,

  LocationResponse location,

  String profileImageUrl,

  String maritalStatus,

  Long spouseId,  // Reference to linked spouse member

  String occupation,

  YearMonth memberSince,

  String emergencyContactName,

  String emergencyContactNumber,

  String notes,

  Boolean isVerified,

  // Phase 2 fields
  MemberStatus status,

  Integer profileCompleteness,

  Set<String> tags,

  // Phase 3.3: Parent-Child Relationships
  /**
   * List of parent IDs and names for this member (if they are a child).
   * Simplified representation to avoid deep nesting.
   */
  List<ParentInfo> parents,

  /**
   * List of children IDs and names for this member (if they are a parent).
   * Simplified representation to avoid deep nesting.
   */
  List<ChildInfo> children
) {

  /**
   * Simplified parent information to include in member response.
   */
  public record ParentInfo(Long id, String fullName) {}

  /**
   * Simplified child information to include in member response.
   */
  public record ChildInfo(Long id, String fullName) {}

  /**
   * Simplified fellowship information to avoid circular references.
   */
  public record FellowshipSummary(Long id, String name) {}
}
