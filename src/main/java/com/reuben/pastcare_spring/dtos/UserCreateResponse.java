package com.reuben.pastcare_spring.dtos;

import java.util.List;

import com.reuben.pastcare_spring.enums.Role;
import com.reuben.pastcare_spring.models.Church;
import com.reuben.pastcare_spring.models.Fellowship;

/**
 * Response DTO for user creation that includes the generated password.
 * This allows the admin to communicate the temporary password to the new user.
 */
public record UserCreateResponse(
  Long id,
  String name,
  String email,
  String phoneNumber,
  String title,
  Church church,
  List<Fellowship> fellowships,
  Role role,
  String temporaryPassword,  // Only included in creation response
  String message
) {

}
