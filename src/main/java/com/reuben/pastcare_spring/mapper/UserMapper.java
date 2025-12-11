package com.reuben.pastcare_spring.mapper;

import com.reuben.pastcare_spring.dtos.UserResponse;
import com.reuben.pastcare_spring.models.User;

public class UserMapper {
    public static UserResponse toUserResponse(User user) {
        return new UserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getPhoneNumber(),
            user.getTitle(),
            user.getChurch(),
            user.getFellowships(),
            user.getPrimaryService(),
            user.getRole()
        );
    }
}
