package com.reuben.pastcare_spring.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SendSmsRequest {

    @NotBlank(message = "Recipient phone number is required")
    private String recipientPhone;

    private String recipientName;

    @NotBlank(message = "Message is required")
    @Size(max = 1600, message = "Message too long")
    private String message;

    private Long memberId;

    private LocalDateTime scheduledTime;
}
