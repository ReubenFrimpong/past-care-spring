package com.reuben.pastcare_spring.mapper;

import com.reuben.pastcare_spring.Dtos.UserDto;
import com.reuben.pastcare_spring.models.User;

public class UserMapper {
    public static UserDto toDto(User user) {
        return new UserDto(
            user.getName(),
            user.getEmail(),
            user.getPhoneNumber(),
            user.getTitle(),
            user.getChapel() != null ? user.getChapel().getId() : null,
            user.getPrimaryService(),
            user.getDesignation()
        );
    }
}
