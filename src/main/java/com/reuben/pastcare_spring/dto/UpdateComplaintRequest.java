package com.reuben.pastcare_spring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating complaint details (admin).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateComplaintRequest {

    private String status;

    private String priority;

    private Long assignedToId;

    private String adminResponse;

    private String internalNotes;

    private String tags;
}
