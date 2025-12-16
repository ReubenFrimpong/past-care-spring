package com.reuben.pastcare_spring.dtos;

import java.time.LocalDate;
import java.util.List;

import com.reuben.pastcare_spring.models.Church;
import com.reuben.pastcare_spring.models.Fellowship;

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

  String phoneNumber,

  String whatsappNumber,

  String otherPhoneNumber,

  String areaOfResidence,

  String address,

  String profileImageUrl,

  String maritalStatus,

  String spouseName,

  String occupation,

  LocalDate memberSince,

  String emergencyContactName,

  String emergencyContactNumber,

  String notes,

  Boolean isVerified
) {

}
