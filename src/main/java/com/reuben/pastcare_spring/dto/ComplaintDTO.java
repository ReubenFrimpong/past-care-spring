package com.reuben.pastcare_spring.dto;

import com.reuben.pastcare_spring.models.Complaint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for Complaint entity.
 * Used for API responses and requests.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintDTO {

    private Long id;
    private Long churchId;
    private String churchName;

    // Submitter information
    private Long submittedById;
    private String submittedByName;
    private String submittedByEmail;
    private Boolean isAnonymous;

    // Complaint details
    private String category;
    private String subject;
    private String description;
    private String status;
    private String priority;

    // Assignment
    private Long assignedToId;
    private String assignedToName;
    private String assignedToEmail;

    // Response
    private String adminResponse;

    // Contact information
    private String contactEmail;
    private String contactPhone;

    // Tags
    private String tags;
    private List<String> tagList;

    // Timestamps
    private LocalDateTime submittedAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;

    // Activity count
    private Integer activityCount;

    /**
     * Convert entity to DTO.
     */
    public static ComplaintDTO fromEntity(Complaint complaint) {
        if (complaint == null) {
            return null;
        }

        ComplaintDTO dto = new ComplaintDTO();
        dto.setId(complaint.getId());
        dto.setChurchId(complaint.getChurch().getId());
        dto.setChurchName(complaint.getChurch().getName());

        // Submitter info (respect anonymity)
        dto.setSubmittedById(complaint.getSubmittedBy().getId());
        dto.setIsAnonymous(complaint.getIsAnonymous());
        if (!complaint.getIsAnonymous()) {
            dto.setSubmittedByName(complaint.getSubmittedBy().getName());
            dto.setSubmittedByEmail(complaint.getSubmittedBy().getEmail());
        } else {
            dto.setSubmittedByName("Anonymous");
            dto.setSubmittedByEmail(null);
        }

        // Complaint details
        dto.setCategory(complaint.getCategory().name());
        dto.setSubject(complaint.getSubject());
        dto.setDescription(complaint.getDescription());
        dto.setStatus(complaint.getStatus().name());
        dto.setPriority(complaint.getPriority().name());

        // Assignment
        if (complaint.getAssignedTo() != null) {
            dto.setAssignedToId(complaint.getAssignedTo().getId());
            dto.setAssignedToName(complaint.getAssignedTo().getName());
            dto.setAssignedToEmail(complaint.getAssignedTo().getEmail());
        }

        // Response
        dto.setAdminResponse(complaint.getAdminResponse());

        // Contact info
        dto.setContactEmail(complaint.getContactEmail());
        dto.setContactPhone(complaint.getContactPhone());

        // Tags
        dto.setTags(complaint.getTags());
        if (complaint.getTags() != null && !complaint.getTags().isEmpty()) {
            dto.setTagList(List.of(complaint.getTags().split(",")));
        }

        // Timestamps
        dto.setSubmittedAt(complaint.getSubmittedAt());
        dto.setUpdatedAt(complaint.getUpdatedAt());
        dto.setResolvedAt(complaint.getResolvedAt());

        return dto;
    }

    /**
     * Convert entity to DTO with activity count.
     */
    public static ComplaintDTO fromEntity(Complaint complaint, Integer activityCount) {
        ComplaintDTO dto = fromEntity(complaint);
        if (dto != null) {
            dto.setActivityCount(activityCount);
        }
        return dto;
    }
}
