package com.reuben.pastcare_spring.dtos;

import java.time.LocalDate;
import java.util.List;

import com.reuben.pastcare_spring.annotations.Unique;
import com.reuben.pastcare_spring.models.Fellowship;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MemberRequest(
  
  Long id,
  
  @NotBlank(message = "Firstname is required")
  String firstName,
  
  String otherName,
  
  @NotBlank(message = "Lastname is required")
  String lastName,
  
  String title,
  
  @NotBlank(message = "Sex is required")
  String sex,

  Long churchId,
  
  List<Long> fellowshipIds,
  
  LocalDate dob,
  
  @NotBlank(message = "Phone number is required")
  @Unique(table = "member", column = "phone_number", message = "Phone number already taken")

  String phoneNumber,

  String whatsappNumber,

  String otherPhoneNumber,

  String areaOfResidence,

  String gpsAddress,

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
