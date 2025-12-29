package com.reuben.pastcare_spring.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for security violations with enriched data.
 * Includes user and church names for frontend display.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityViolationResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private Long churchId;
    private String churchName;
    private Long attemptedChurchId;
    private String attemptedChurchName;
    private String violationType;
    private String violationMessage;
    private String endpoint;
    private String httpMethod;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime timestamp;
    private String severity;
}
