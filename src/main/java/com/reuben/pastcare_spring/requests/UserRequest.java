package com.reuben.pastcare_spring.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserRequest(
  @NotBlank(message = "Name is required")
  String name,

  @NotBlank(message = "Email is required")
  @Email(message = "Email must be valid")
  String email,

  String phoneNumber,
  String title,

  @NotNull(message = "Chapel is required")
  Integer chapelId,

  String password,

  String primaryService,
  String designation) {

}
