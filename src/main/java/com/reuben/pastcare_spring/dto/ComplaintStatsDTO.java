package com.reuben.pastcare_spring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for complaint statistics.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintStatsDTO {

    private Long total;
    private Long submitted;
    private Long underReview;
    private Long inProgress;
    private Long resolved;
    private Long urgent;
    private Long pendingResponse;
    private Long closed;
    private Long escalated;

    // Average resolution time in hours
    private Double avgResolutionTimeHours;

    // Complaints by category
    private Long general;
    private Long service;
    private Long facility;
    private Long staff;
    private Long financial;
    private Long ministry;
    private Long safeguarding;
    private Long discrimination;
    private Long other;
}
