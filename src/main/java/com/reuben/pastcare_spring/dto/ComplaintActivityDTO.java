package com.reuben.pastcare_spring.dto;

import com.reuben.pastcare_spring.models.ComplaintActivity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for ComplaintActivity entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintActivityDTO {

    private Long id;
    private Long complaintId;
    private Long performedById;
    private String performedByName;
    private String activityType;
    private String oldValue;
    private String newValue;
    private String description;
    private LocalDateTime performedAt;
    private Boolean visibleToComplainant;

    /**
     * Convert entity to DTO.
     */
    public static ComplaintActivityDTO fromEntity(ComplaintActivity activity) {
        if (activity == null) {
            return null;
        }

        ComplaintActivityDTO dto = new ComplaintActivityDTO();
        dto.setId(activity.getId());
        dto.setComplaintId(activity.getComplaint().getId());
        dto.setPerformedById(activity.getPerformedBy().getId());
        dto.setPerformedByName(activity.getPerformedBy().getName());
        dto.setActivityType(activity.getActivityType().name());
        dto.setOldValue(activity.getOldValue());
        dto.setNewValue(activity.getNewValue());
        dto.setDescription(activity.getDescription());
        dto.setPerformedAt(activity.getPerformedAt());
        dto.setVisibleToComplainant(activity.getVisibleToComplainant());

        return dto;
    }
}
