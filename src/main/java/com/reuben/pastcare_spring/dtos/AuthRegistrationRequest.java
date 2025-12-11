package com.reuben.pastcare_spring.dtos;

import com.reuben.pastcare_spring.enums.Role;

import jakarta.validation.constraints.NotBlank;

public record AuthRegistrationRequest(@NotBlank String name,@NotBlank String email, @NotBlank String password, Role role, String phoneNumber) {

}
