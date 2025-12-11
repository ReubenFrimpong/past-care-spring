package com.reuben.pastcare_spring.dtos;

import java.util.List;

import com.reuben.pastcare_spring.enums.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserCreateRequest(
  @NotBlank(message = "Name is required")
  String name,

  @NotBlank(message = "Email is required")
  @Email(message = "Email must be valid")
  String email,

  String phoneNumber,

  String title,

  Long churchId,

  List<Long> fellowshipIds,

  String password,

  String primaryService,
  Role role) {

}
