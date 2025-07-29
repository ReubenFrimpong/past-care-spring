package com.reuben.pastcare_spring.Dtos;

public record UserDto(
  String name,
  String email,
  String phoneNumber,
  String title,
  Integer chapelId,
  String primaryService,
  String designation
) {

}
