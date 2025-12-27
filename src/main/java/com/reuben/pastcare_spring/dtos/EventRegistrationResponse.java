package com.reuben.pastcare_spring.dtos;

import com.reuben.pastcare_spring.models.EventRegistration;
import com.reuben.pastcare_spring.models.RegistrationStatus;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Response DTO for EventRegistration entity.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRegistrationResponse {

    private Long id;
    private Long churchId;

    // Event details
    private Long eventId;
    private String eventName;
    private LocalDateTime eventStartDate;
    private LocalDateTime eventEndDate;

    // Registrant details
    private Long memberId;
    private String memberName;
    private Boolean isGuest;
    private String guestName;
    private String guestEmail;
    private String guestPhone;

    // Registration info
    private RegistrationStatus status;
    private String statusDisplay;
    private LocalDateTime registrationDate;

    // Guest count
    private Integer numberOfGuests;
    private String guestNames;
    private Integer totalAttendeeCount;

    // Approval workflow
    private Long approvedById;
    private String approvedByName;
    private LocalDateTime approvedAt;
    private String rejectionReason;

    // Attendance
    private Boolean attended;
    private Long attendanceRecordId;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;

    // Waitlist
    private Boolean isOnWaitlist;
    private Integer waitlistPosition;
    private LocalDateTime promotedFromWaitlistAt;

    // Communication
    private Boolean confirmationSent;
    private Boolean reminderSent;
    private String ticketCode;

    // Cancellation
    private Boolean isCancelled;
    private String cancellationReason;
    private LocalDateTime cancelledAt;

    // Notes
    private String notes;
    private String specialRequirements;

    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdByName;
    private String updatedByName;

    /**
     * Convert EventRegistration entity to EventRegistrationResponse DTO
     */
    public static EventRegistrationResponse fromEntity(EventRegistration registration) {
        if (registration == null) {
            return null;
        }

        return EventRegistrationResponse.builder()
            .id(registration.getId())
            .churchId(registration.getChurch() != null ? registration.getChurch().getId() : null)
            .eventId(registration.getEvent() != null ? registration.getEvent().getId() : null)
            .eventName(registration.getEvent() != null ? registration.getEvent().getName() : null)
            .eventStartDate(registration.getEvent() != null ? registration.getEvent().getStartDate() : null)
            .eventEndDate(registration.getEvent() != null ? registration.getEvent().getEndDate() : null)
            .memberId(registration.getMember() != null ? registration.getMember().getId() : null)
            .memberName(registration.getRegistrantName())
            .isGuest(registration.getIsGuest())
            .guestName(registration.getGuestName())
            .guestEmail(registration.getGuestEmail())
            .guestPhone(registration.getGuestPhone())
            .status(registration.getStatus())
            .statusDisplay(registration.getStatus() != null ? registration.getStatus().getDisplayName() : null)
            .registrationDate(registration.getRegistrationDate())
            .numberOfGuests(registration.getNumberOfGuests())
            .guestNames(registration.getGuestNames())
            .totalAttendeeCount(registration.getTotalAttendeeCount())
            .approvedById(registration.getApprovedBy() != null ? registration.getApprovedBy().getId() : null)
            .approvedByName(registration.getApprovedBy() != null ?
                registration.getApprovedBy().getName() : null)
            .approvedAt(registration.getApprovedAt())
            .rejectionReason(registration.getRejectionReason())
            .attended(registration.getAttended())
            .attendanceRecordId(registration.getAttendanceRecordId())
            .checkInTime(registration.getCheckInTime())
            .checkOutTime(registration.getCheckOutTime())
            .isOnWaitlist(registration.getIsOnWaitlist())
            .waitlistPosition(registration.getWaitlistPosition())
            .promotedFromWaitlistAt(registration.getPromotedFromWaitlistAt())
            .confirmationSent(registration.getConfirmationSent())
            .reminderSent(registration.getReminderSent())
            .ticketCode(registration.getTicketCode())
            .isCancelled(registration.getIsCancelled())
            .cancellationReason(registration.getCancellationReason())
            .cancelledAt(registration.getCancelledAt())
            .notes(registration.getNotes())
            .specialRequirements(registration.getSpecialRequirements())
            .createdAt(registration.getCreatedAt())
            .updatedAt(registration.getUpdatedAt())
            .createdByName(registration.getCreatedBy() != null ?
                registration.getCreatedBy().getName() : null)
            .updatedByName(registration.getUpdatedBy() != null ?
                registration.getUpdatedBy().getName() : null)
            .build();
    }
}
