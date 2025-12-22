package com.reuben.pastcare_spring.dtos;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;

import com.reuben.pastcare_spring.models.Church;
import com.reuben.pastcare_spring.models.Fellowship;
import com.reuben.pastcare_spring.models.MemberStatus;

public record MemberResponse(
  Long id,

  String firstName,

  String otherName,

  String lastName,

  String title,

  String sex,

  Church church,

  List<Fellowship> fellowships,

  LocalDate dob,

  String countryCode,

  String timezone,

  String phoneNumber,

  String whatsappNumber,

  String otherPhoneNumber,

  LocationResponse location,

  String profileImageUrl,

  String maritalStatus,

  String spouseName,

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

  Set<String> tags
) {

}
