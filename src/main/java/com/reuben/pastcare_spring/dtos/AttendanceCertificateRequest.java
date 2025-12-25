package com.reuben.pastcare_spring.dtos;

import java.time.LocalDate;

/**
 * Request DTO for generating attendance certificate
 */
public record AttendanceCertificateRequest(
    Long memberId,
    LocalDate startDate,
    LocalDate endDate,
    String certificateType, // ATTENDANCE, PERFECT_ATTENDANCE, PARTICIPATION
    String templateType // STANDARD, FORMAL, MODERN
) {
    public AttendanceCertificateRequest {
        if (certificateType == null) {
            certificateType = "ATTENDANCE";
        }
        if (templateType == null) {
            templateType = "STANDARD";
        }
    }
}
