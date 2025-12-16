package com.reuben.pastcare_spring.dtos;
import jakarta.validation.constraints.NotBlank;

public record AuthLoginRequest(
  @NotBlank String email,
  @NotBlank String password,
  boolean rememberMe
) {

}
