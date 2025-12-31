package com.reuben.pastcare_spring.dtos;

import com.reuben.pastcare_spring.models.PortalUserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for portal user response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PortalUserResponse {

    private Long id;
    private String email;
    private Long memberId;
    private String memberFirstName;
    private String memberLastName;
    private String profileImageUrl;
    private PortalUserStatus status;
    private LocalDateTime emailVerifiedAt;
    private LocalDateTime approvedAt;
    private String approvedByName;
    private String rejectionReason;
    private LocalDateTime lastLoginAt;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
