package com.reuben.pastcare_spring.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SendBulkSmsRequest {

    @NotEmpty(message = "At least one recipient is required")
    private List<String> recipientPhones;

    @NotBlank(message = "Message is required")
    @Size(max = 1600, message = "Message too long")
    private String message;

    private LocalDateTime scheduledTime;
}
