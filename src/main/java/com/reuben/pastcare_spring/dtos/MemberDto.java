package com.reuben.pastcare_spring.dtos;

import java.time.LocalDate;

public record MemberDto(
  Integer id,

  String firstName,

  String otherName,

  String lastName,

  String title,

  String sex,

  Integer chapelId,

  LocalDate dob,

  String phoneNumber,

  String whatsappNumber,

  String otherPhoneNumber,

  String areaOfResidence,

  String gpsAddress,

  Integer bacentaId,

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
