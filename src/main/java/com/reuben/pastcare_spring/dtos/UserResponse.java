package com.reuben.pastcare_spring.dtos;

import java.time.LocalDateTime;
import java.util.List;

import com.reuben.pastcare_spring.enums.Role;
import com.reuben.pastcare_spring.models.Church;
import com.reuben.pastcare_spring.models.Fellowship;

public record UserResponse(
  Long id,
  String name,
  String email,
  String phoneNumber,
  String title,
  Church church,
  List<Fellowship> fellowships,
  Role role,
  boolean isActive,
  LocalDateTime lastLoginAt,
  boolean mustChangePassword
) {

}
