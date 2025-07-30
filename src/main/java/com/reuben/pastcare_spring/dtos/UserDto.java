package com.reuben.pastcare_spring.dtos;

public record UserDto(
  Integer id,
  String name,
  String email,
  String phoneNumber,
  String title,
  Integer chapelId,
  String primaryService,
  String designation
) {

}
