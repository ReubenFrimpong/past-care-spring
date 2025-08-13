package com.reuben.pastcare_spring.requests;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MemberRequest(
  
  Integer id,
  
  @NotBlank(message = "Firstname is required")
  String firstName,
  
  String otherName,
  
  @NotBlank(message = "Lastname is required")
  String lastName,
  
  String title,
  
  @NotBlank(message = "Sex is required")
  String sex,
  
  @NotNull(message = "Chapel is required")
  Integer chapelId,
  
  LocalDate dob,
  
  @NotBlank(message = "Phone number is required")
  String phoneNumber,

  String whatsappNumber,

  String otherPhoneNumber,

  String areaOfResidence,

  String gpsAddress,

  @NotNull(message = "Bacenta is required")
  Integer bacentaId,

  String profileImageUrl,

  @NotBlank(message = "Marital status is required")
  String maritalStatus,

  String spouseName,

  String occupation,

  LocalDate memberSince,

  String emergencyContactName,

  String emergencyContactNumber,

  String notes

) {

}
