package com.reuben.pastcare_spring.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SendToAllMembersRequest {

    @NotBlank(message = "Message is required")
    @Size(max = 1600, message = "Message too long")
    private String message;

    private LocalDateTime scheduledTime;
}
