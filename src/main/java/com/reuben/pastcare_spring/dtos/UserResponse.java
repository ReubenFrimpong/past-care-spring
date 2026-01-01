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
  /**
   * Convenience method to get the church ID directly.
   * This makes it easier for the frontend which often needs just the ID.
   */
  public Long churchId() {
    return church != null ? church.getId() : null;
  }

  /**
   * Convenience method to get the church name directly.
   */
  public String churchName() {
    return church != null ? church.getName() : null;
  }
}
