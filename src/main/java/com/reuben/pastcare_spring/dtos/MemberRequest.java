package com.reuben.pastcare_spring.dtos;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import com.reuben.pastcare_spring.annotations.Unique;
import com.reuben.pastcare_spring.validators.InternationalPhoneNumber;

import jakarta.validation.constraints.NotBlank;

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
  @InternationalPhoneNumber
  @Unique(table = "member", column = "phone_number", message = "Phone number already taken")
  String phoneNumber,

  @InternationalPhoneNumber
  String whatsappNumber,

  @InternationalPhoneNumber
  String otherPhoneNumber,

  // GPS coordinates as "latitude,longitude" string
  String coordinates,

  // Optional: Full Nominatim address data for location creation
  Map<String, Object> nominatimAddress,

  String profileImageUrl,

  @NotBlank(message = "Marital status is required")
  String maritalStatus,

  String spouseName,

  String occupation,

  YearMonth memberSince,

  String emergencyContactName,

  @InternationalPhoneNumber
  String emergencyContactNumber,

  String notes

) {

}
