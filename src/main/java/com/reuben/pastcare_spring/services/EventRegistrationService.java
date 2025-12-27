package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.dtos.EventRegistrationRequest;
import com.reuben.pastcare_spring.dtos.EventRegistrationResponse;
import com.reuben.pastcare_spring.models.*;
import com.reuben.pastcare_spring.repositories.*;
import com.reuben.pastcare_spring.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing event registrations.
 * Handles registration creation, approval workflow, waitlist management, and attendance tracking.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventRegistrationService {

    private final EventRegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final MemberRepository memberRepository;
    private final ChurchRepository churchRepository;
    private final UserRepository userRepository;
    private final QRCodeService qrCodeService;
    private final EmailService emailService;

    /**
     * Register for an event
     */
    @Transactional
    public EventRegistrationResponse registerForEvent(EventRegistrationRequest request, Long userId) {
        Long churchId = TenantContext.getCurrentChurchId();
        log.info("Registering for event {} in church {}", request.getEventId(), churchId);

        // Get event
        Event event = eventRepository.findByIdAndChurchIdAndDeletedAtIsNull(request.getEventId(), churchId)
            .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        // Validate event is open for registration
        if (!event.isRegistrationOpen()) {
            throw new IllegalStateException("Event registration is closed");
        }

        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new IllegalArgumentException("Church not found"));

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Build registration
        EventRegistration registration = EventRegistration.builder()
            .church(church)
            .event(event)
            .isGuest(request.getIsGuest() != null ? request.getIsGuest() : false)
            .numberOfGuests(request.getNumberOfGuests() != null ? request.getNumberOfGuests() : 0)
            .guestNames(request.getGuestNames())
            .notes(request.getNotes())
            .specialRequirements(request.getSpecialRequirements())
            .createdBy(user)
            .build();

        // Set member or guest details
        if (request.getIsGuest() != null && request.getIsGuest()) {
            registration.setGuestName(request.getGuestName());
            registration.setGuestEmail(request.getGuestEmail());
            registration.setGuestPhone(request.getGuestPhone());
        } else {
            Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

            // Check if already registered
            if (registrationRepository.existsByEventAndMemberAndIsCancelledFalseAndDeletedAtIsNull(event, member)) {
                throw new IllegalStateException("Member is already registered for this event");
            }

            registration.setMember(member);
        }

        // Determine status and waitlist
        if (event.getAutoApproveRegistrations()) {
            registration.setStatus(RegistrationStatus.APPROVED);
        } else {
            registration.setStatus(RegistrationStatus.PENDING);
        }

        // Check capacity and waitlist
        if (event.isAtCapacity()) {
            if (event.getAllowWaitlist()) {
                registration.setIsOnWaitlist(true);
                Long currentWaitlistCount = registrationRepository.countWaitlist(event.getId());
                registration.setWaitlistPosition((int) (currentWaitlistCount + 1));
                log.info("Added to waitlist at position {}", registration.getWaitlistPosition());
            } else {
                throw new IllegalStateException("Event is at full capacity and waitlist is not allowed");
            }
        }

        // Save registration
        registration = registrationRepository.save(registration);

        // Update event registration count if not on waitlist
        if (!registration.getIsOnWaitlist()) {
            event.incrementRegistrations();
            eventRepository.save(event);
        }

        log.info("Registration created with ID: {}", registration.getId());
        return EventRegistrationResponse.fromEntity(registration);
    }

    /**
     * Approve a registration
     */
    @Transactional
    public EventRegistrationResponse approveRegistration(Long registrationId, Long userId) {
        Long churchId = TenantContext.getCurrentChurchId();
        log.info("Approving registration {} in church {}", registrationId, churchId);

        EventRegistration registration = registrationRepository.findByIdAndChurchIdAndDeletedAtIsNull(registrationId, churchId)
            .orElseThrow(() -> new IllegalArgumentException("Registration not found"));

        if (registration.getStatus() != RegistrationStatus.PENDING) {
            throw new IllegalStateException("Only pending registrations can be approved");
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        registration.approve(user);
        registration = registrationRepository.save(registration);

        log.info("Registration {} approved", registrationId);
        return EventRegistrationResponse.fromEntity(registration);
    }

    /**
     * Reject a registration
     */
    @Transactional
    public EventRegistrationResponse rejectRegistration(Long registrationId, String reason, Long userId) {
        Long churchId = TenantContext.getCurrentChurchId();
        log.info("Rejecting registration {} in church {}", registrationId, churchId);

        EventRegistration registration = registrationRepository.findByIdAndChurchIdAndDeletedAtIsNull(registrationId, churchId)
            .orElseThrow(() -> new IllegalArgumentException("Registration not found"));

        if (registration.getStatus() != RegistrationStatus.PENDING) {
            throw new IllegalStateException("Only pending registrations can be rejected");
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        registration.reject(reason, user);
        registration = registrationRepository.save(registration);

        // Update event registration count
        Event event = registration.getEvent();
        event.decrementRegistrations();
        eventRepository.save(event);

        log.info("Registration {} rejected", registrationId);
        return EventRegistrationResponse.fromEntity(registration);
    }


    /**
     * Mark registration as attended
     */
    @Transactional
    public EventRegistrationResponse markAsAttended(Long registrationId) {
        Long churchId = TenantContext.getCurrentChurchId();

        EventRegistration registration = registrationRepository.findByIdAndChurchIdAndDeletedAtIsNull(registrationId, churchId)
            .orElseThrow(() -> new IllegalArgumentException("Registration not found"));

        registration.markAsAttended();
        registration = registrationRepository.save(registration);

        log.info("Registration {} marked as attended", registrationId);
        return EventRegistrationResponse.fromEntity(registration);
    }

    /**
     * Mark registration as no-show
     */
    @Transactional
    public EventRegistrationResponse markAsNoShow(Long registrationId) {
        Long churchId = TenantContext.getCurrentChurchId();

        EventRegistration registration = registrationRepository.findByIdAndChurchIdAndDeletedAtIsNull(registrationId, churchId)
            .orElseThrow(() -> new IllegalArgumentException("Registration not found"));

        registration.markAsNoShow();
        registration = registrationRepository.save(registration);

        log.info("Registration {} marked as no-show", registrationId);
        return EventRegistrationResponse.fromEntity(registration);
    }

    /**
     * Promote from waitlist
     */
    @Transactional
    public void promoteFromWaitlistIfNeeded(Long eventId) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        // Check if there's available capacity
        if (!event.isAtCapacity()) {
            List<EventRegistration> waitlist = registrationRepository.findWaitlist(eventId);

            if (!waitlist.isEmpty()) {
                EventRegistration firstInWaitlist = waitlist.get(0);
                firstInWaitlist.promoteFromWaitlist();
                registrationRepository.save(firstInWaitlist);

                // Update event count
                event.incrementRegistrations();
                eventRepository.save(event);

                // Update positions for remaining waitlist
                for (int i = 1; i < waitlist.size(); i++) {
                    EventRegistration reg = waitlist.get(i);
                    reg.setWaitlistPosition(i);
                    registrationRepository.save(reg);
                }

                log.info("Promoted registration {} from waitlist for event {}", firstInWaitlist.getId(), eventId);
            }
        }
    }

    /**
     * Get registration by ID
     */
    @Transactional(readOnly = true)
    public EventRegistrationResponse getRegistration(Long registrationId) {
        Long churchId = TenantContext.getCurrentChurchId();
        EventRegistration registration = registrationRepository.findByIdAndChurchIdAndDeletedAtIsNull(registrationId, churchId)
            .orElseThrow(() -> new IllegalArgumentException("Registration not found"));
        return EventRegistrationResponse.fromEntity(registration);
    }

    /**
     * Get all registrations for an event
     */
    @Transactional(readOnly = true)
    public Page<EventRegistrationResponse> getEventRegistrations(Long eventId, Pageable pageable) {
        return registrationRepository.findByEventId(eventId, pageable)
            .map(EventRegistrationResponse::fromEntity);
    }

    /**
     * Get registrations for a member
     */
    @Transactional(readOnly = true)
    public Page<EventRegistrationResponse> getMemberRegistrations(Long memberId, Pageable pageable) {
        return registrationRepository.findByMemberId(memberId, pageable)
            .map(EventRegistrationResponse::fromEntity);
    }

    /**
     * Get pending approvals
     */
    @Transactional(readOnly = true)
    public Page<EventRegistrationResponse> getPendingApprovals(Pageable pageable) {
        Long churchId = TenantContext.getCurrentChurchId();
        return registrationRepository.findPendingApprovals(churchId, pageable)
            .map(EventRegistrationResponse::fromEntity);
    }

    /**
     * Get waitlist for an event
     */
    @Transactional(readOnly = true)
    public List<EventRegistrationResponse> getEventWaitlist(Long eventId) {
        return registrationRepository.findWaitlist(eventId).stream()
            .map(EventRegistrationResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Get attendees for an event
     */
    @Transactional(readOnly = true)
    public List<EventRegistrationResponse> getEventAttendees(Long eventId) {
        return registrationRepository.findAttendeesForEvent(eventId).stream()
            .map(EventRegistrationResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Filter registrations
     */
    @Transactional(readOnly = true)
    public Page<EventRegistrationResponse> filterRegistrations(
        Long eventId,
        Long memberId,
        RegistrationStatus status,
        Boolean isOnWaitlist,
        Boolean attended,
        Pageable pageable
    ) {
        Long churchId = TenantContext.getCurrentChurchId();
        return registrationRepository.findRegistrationsWithFilters(
            churchId, eventId, memberId, status, isOnWaitlist, attended, pageable
        ).map(EventRegistrationResponse::fromEntity);
    }

    /**
     * Generate QR code ticket for a registration
     */
    @Transactional
    public String generateTicketCode(Long registrationId) {
        Long churchId = TenantContext.getCurrentChurchId();
        log.info("Generating ticket code for registration {} in church {}", registrationId, churchId);

        EventRegistration registration = registrationRepository.findByIdAndChurchIdAndDeletedAtIsNull(registrationId, churchId)
            .orElseThrow(() -> new IllegalArgumentException("Registration not found"));

        // Generate encrypted ticket code using registration ID and event end date
        Event event = registration.getEvent();
        String ticketCode = qrCodeService.generateQRCodeData(registrationId, event.getEndDate());

        // Save ticket code
        registration.setTicketCode(ticketCode);
        registrationRepository.save(registration);

        log.info("Ticket code generated for registration {}", registrationId);
        return ticketCode;
    }

    /**
     * Send confirmation email for a registration
     */
    @Transactional
    public void sendConfirmationEmail(Long registrationId) {
        Long churchId = TenantContext.getCurrentChurchId();
        log.info("Sending confirmation email for registration {} in church {}", registrationId, churchId);

        EventRegistration registration = registrationRepository.findByIdAndChurchIdAndDeletedAtIsNull(registrationId, churchId)
            .orElseThrow(() -> new IllegalArgumentException("Registration not found"));

        // Don't send if already cancelled
        if (registration.getIsCancelled()) {
            throw new IllegalStateException("Cannot send confirmation for cancelled registration");
        }

        Event event = registration.getEvent();
        String recipientEmail;
        String recipientName;

        // Get email and name based on member or guest
        if (registration.getIsGuest()) {
            recipientEmail = registration.getGuestEmail();
            recipientName = registration.getGuestName();
        } else {
            Member member = registration.getMember();
            recipientEmail = member.getEmail();
            recipientName = member.getFirstName() + " " + member.getLastName();
        }

        // Validate email exists
        if (recipientEmail == null || recipientEmail.trim().isEmpty()) {
            log.warn("Cannot send confirmation email - no email address for registration {}", registrationId);
            return;
        }

        // Build email content
        String subject = "Registration Confirmation - " + event.getName();
        String body = buildConfirmationEmailBody(recipientName, event, registration);

        // Send email
        emailService.sendEmail(recipientEmail, subject, body);

        // Mark confirmation as sent
        registration.setConfirmationSent(true);
        registrationRepository.save(registration);

        log.info("Confirmation email sent to {} for registration {}", recipientEmail, registrationId);
    }

    /**
     * Build email body for registration confirmation
     */
    private String buildConfirmationEmailBody(String recipientName, Event event, EventRegistration registration) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' h:mm a");

        StringBuilder body = new StringBuilder();
        body.append("Dear ").append(recipientName).append(",\n\n");

        body.append("Thank you for registering for ").append(event.getName()).append("!\n\n");

        body.append("Event Details:\n");
        body.append("- Event: ").append(event.getName()).append("\n");
        body.append("- Date: ").append(event.getStartDate().format(formatter)).append("\n");

        if (event.getLocationType() == EventLocationType.PHYSICAL || event.getLocationType() == EventLocationType.HYBRID) {
            body.append("- Location: ").append(event.getPhysicalLocation() != null ? event.getPhysicalLocation() : "TBA").append("\n");
        }

        if (event.getLocationType() == EventLocationType.VIRTUAL || event.getLocationType() == EventLocationType.HYBRID) {
            body.append("- Virtual Link: ").append(event.getVirtualLink() != null ? event.getVirtualLink() : "Will be provided later").append("\n");
            if (event.getVirtualPlatform() != null) {
                body.append("- Platform: ").append(event.getVirtualPlatform()).append("\n");
            }
        }

        body.append("\nRegistration Status: ");
        if (registration.getIsOnWaitlist()) {
            body.append("On Waitlist (Position #").append(registration.getWaitlistPosition()).append(")\n");
            body.append("We'll notify you if a spot becomes available.\n");
        } else if (registration.getStatus() == RegistrationStatus.PENDING) {
            body.append("Pending Approval\n");
            body.append("Your registration is pending approval. We'll notify you once it's confirmed.\n");
        } else if (registration.getStatus() == RegistrationStatus.APPROVED) {
            body.append("Confirmed\n");
            body.append("Your registration is confirmed! We look forward to seeing you there.\n");
        }

        if (registration.getNumberOfGuests() != null && registration.getNumberOfGuests() > 0) {
            body.append("\nNumber of Guests: ").append(registration.getNumberOfGuests()).append("\n");
        }

        if (registration.getSpecialRequirements() != null && !registration.getSpecialRequirements().trim().isEmpty()) {
            body.append("\nSpecial Requirements: ").append(registration.getSpecialRequirements()).append("\n");
        }

        body.append("\nIf you need to cancel your registration, please contact us as soon as possible.\n\n");

        body.append("Best regards,\n");
        body.append(event.getChurch().getName());

        return body.toString();
    }

    /**
     * Cancel a registration by the attendee
     */
    @Transactional
    public EventRegistrationResponse cancelRegistration(Long registrationId, String reason) {
        Long churchId = TenantContext.getCurrentChurchId();
        log.info("Cancelling registration {} in church {}: {}", registrationId, churchId, reason);

        EventRegistration registration = registrationRepository.findByIdAndChurchIdAndDeletedAtIsNull(registrationId, churchId)
            .orElseThrow(() -> new IllegalArgumentException("Registration not found"));

        // Prevent cancelling already cancelled registrations
        if (registration.getIsCancelled()) {
            throw new IllegalStateException("Registration is already cancelled");
        }

        // Cancel registration
        registration.setIsCancelled(true);
        registration.setCancellationReason(reason);
        registration.setCancelledAt(LocalDateTime.now());

        // If approved, decrement event registration count
        if (registration.getStatus() == RegistrationStatus.APPROVED) {
            Event event = registration.getEvent();
            event.decrementRegistrations();
            eventRepository.save(event);
        }

        registration = registrationRepository.save(registration);
        log.info("Registration {} cancelled successfully", registrationId);

        return EventRegistrationResponse.fromEntity(registration);
    }
}
