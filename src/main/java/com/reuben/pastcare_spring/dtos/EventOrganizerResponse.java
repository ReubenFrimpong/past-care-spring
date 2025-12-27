package com.reuben.pastcare_spring.dtos;

import com.reuben.pastcare_spring.models.EventOrganizer;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Response DTO for EventOrganizer entity.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventOrganizerResponse {

    private Long id;
    private Long churchId;

    // Event details
    private Long eventId;
    private String eventName;

    // Member details
    private Long memberId;
    private String memberName;
    private String memberEmail;
    private String memberPhone;

    // Organizer details
    private Boolean isPrimary;
    private String role;

    // Contact details
    private Boolean isContactPerson;
    private String contactEmail;
    private String contactPhone;
    private String effectiveContactEmail;
    private String effectiveContactPhone;

    // Responsibilities
    private String responsibilities;

    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdByName;

    /**
     * Convert EventOrganizer entity to EventOrganizerResponse DTO
     */
    public static EventOrganizerResponse fromEntity(EventOrganizer organizer) {
        if (organizer == null) {
            return null;
        }

        return EventOrganizerResponse.builder()
            .id(organizer.getId())
            .churchId(organizer.getChurch() != null ? organizer.getChurch().getId() : null)
            .eventId(organizer.getEvent() != null ? organizer.getEvent().getId() : null)
            .eventName(organizer.getEvent() != null ? organizer.getEvent().getName() : null)
            .memberId(organizer.getMember() != null ? organizer.getMember().getId() : null)
            .memberName(organizer.getOrganizerName())
            .memberEmail(organizer.getMember() != null ? organizer.getMember().getEmail() : null)
            .memberPhone(organizer.getMember() != null ? organizer.getMember().getPhoneNumber() : null)
            .isPrimary(organizer.getIsPrimary())
            .role(organizer.getRole())
            .isContactPerson(organizer.getIsContactPerson())
            .contactEmail(organizer.getContactEmail())
            .contactPhone(organizer.getContactPhone())
            .effectiveContactEmail(organizer.getEffectiveContactEmail())
            .effectiveContactPhone(organizer.getEffectiveContactPhone())
            .responsibilities(organizer.getResponsibilities())
            .createdAt(organizer.getCreatedAt())
            .updatedAt(organizer.getUpdatedAt())
            .createdByName(organizer.getCreatedBy() != null ?
                organizer.getCreatedBy().getName() : null)
            .build();
    }
}
