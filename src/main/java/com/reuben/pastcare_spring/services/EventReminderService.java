package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.models.*;
import com.reuben.pastcare_spring.repositories.*;
import com.reuben.pastcare_spring.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service for managing event reminders and invitations.
 * Handles automated reminder sending and manual invitation distribution.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventReminderService {

    private final EventRepository eventRepository;
    private final EventRegistrationRepository registrationRepository;
    private final MemberRepository memberRepository;
    private final EmailService emailService;

    /**
     * Send reminders for upcoming events
     * Called by scheduler to send reminders based on reminderDaysBefore
     */
    @Transactional
    public void sendScheduledReminders(Long churchId) {
        log.info("Sending scheduled reminders for church {}", churchId);

        LocalDateTime now = LocalDateTime.now();

        // Find events that need reminders (1-7 days before, based on reminderDaysBefore)
        for (int days = 1; days <= 7; days++) {
            LocalDateTime reminderThreshold = now.plusDays(days);
            LocalDateTime startRange = reminderThreshold.minusHours(1);
            LocalDateTime endRange = reminderThreshold.plusHours(1);

            List<Event> events = eventRepository.findEventsNeedingReminders(
                churchId, now, endRange
            );

            for (Event event : events) {
                if (event.getReminderDaysBefore() != null &&
                    event.getReminderDaysBefore() == days &&
                    !event.getReminderSent()) {
                    sendEventReminders(event);
                }
            }
        }
    }

    /**
     * Send reminders for a specific event to all registered attendees
     */
    @Transactional
    public void sendEventReminders(Event event) {
        log.info("Sending reminders for event {} ({})", event.getId(), event.getName());

        if (event.getReminderSent()) {
            log.warn("Reminders already sent for event {}", event.getId());
            return;
        }

        // Get all approved registrations for the event
        List<EventRegistration> registrations = registrationRepository
            .findByEventIdAndStatus(event.getId(), RegistrationStatus.APPROVED);

        int emailsSent = 0;

        for (EventRegistration registration : registrations) {
            try {
                // Send email reminder
                if (sendEmailReminder(event, registration)) {
                    emailsSent++;
                }

                // Mark reminder as sent for this registration
                registration.setReminderSent(true);
                registrationRepository.save(registration);

            } catch (Exception e) {
                log.error("Error sending reminder for registration {}: {}",
                    registration.getId(), e.getMessage());
            }
        }

        // Mark event reminders as sent
        event.setReminderSent(true);
        eventRepository.save(event);

        log.info("Sent {} email reminders for event {}", emailsSent, event.getId());
    }

    /**
     * Send email reminder for an event registration
     */
    private boolean sendEmailReminder(Event event, EventRegistration registration) {
        String recipientEmail;
        String recipientName;

        if (registration.getIsGuest()) {
            recipientEmail = registration.getGuestEmail();
            recipientName = registration.getGuestName();
        } else {
            Member member = registration.getMember();
            recipientEmail = member.getEmail();
            recipientName = member.getFirstName() + " " + member.getLastName();
        }

        if (recipientEmail == null || recipientEmail.trim().isEmpty()) {
            log.warn("No email for registration {}", registration.getId());
            return false;
        }

        String subject = "Reminder: " + event.getName();
        String body = buildReminderEmailBody(recipientName, event, registration);

        emailService.sendEmail(recipientEmail, subject, body);
        log.info("Email reminder sent to {}", recipientEmail);
        return true;
    }


    /**
     * Build email body for event reminder
     */
    private String buildReminderEmailBody(String recipientName, Event event, EventRegistration registration) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' h:mm a");

        StringBuilder body = new StringBuilder();
        body.append("Dear ").append(recipientName).append(",\n\n");

        body.append("This is a friendly reminder about your upcoming event:\n\n");

        body.append("Event: ").append(event.getName()).append("\n");
        body.append("Date: ").append(event.getStartDate().format(formatter)).append("\n");

        // Location details
        if (event.getLocationType() == EventLocationType.PHYSICAL ||
            event.getLocationType() == EventLocationType.HYBRID) {
            body.append("Location: ").append(event.getPhysicalLocation()).append("\n");
        }

        if (event.getLocationType() == EventLocationType.VIRTUAL ||
            event.getLocationType() == EventLocationType.HYBRID) {
            body.append("Virtual Link: ").append(event.getVirtualLink()).append("\n");
            if (event.getVirtualPlatform() != null) {
                body.append("Platform: ").append(event.getVirtualPlatform()).append("\n");
            }
        }

        if (event.getDescription() != null && !event.getDescription().isEmpty()) {
            body.append("\nDescription:\n").append(event.getDescription()).append("\n");
        }

        if (registration.getNumberOfGuests() != null && registration.getNumberOfGuests() > 0) {
            body.append("\nYou registered with ").append(registration.getNumberOfGuests())
                .append(" guest(s)\n");
        }

        body.append("\nWe look forward to seeing you there!\n\n");

        body.append("If you need to cancel, please contact us as soon as possible.\n\n");

        body.append("Best regards,\n");
        body.append(event.getChurch().getName());

        return body.toString();
    }


    /**
     * Send invitation to specific members for an event
     */
    @Transactional
    public int sendEventInvitations(Long eventId, List<Long> memberIds, String personalMessage) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        int invitationsSent = 0;

        for (Long memberId : memberIds) {
            try {
                Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new IllegalArgumentException("Member not found"));

                String subject = "You're Invited: " + event.getName();
                String body = buildInvitationEmailBody(member, event, personalMessage);

                emailService.sendEmail(member.getEmail(), subject, body);
                invitationsSent++;

                log.info("Invitation sent to member {} for event {}", memberId, eventId);

            } catch (Exception e) {
                log.error("Error sending invitation to member {}: {}", memberId, e.getMessage());
            }
        }

        log.info("Sent {} invitations for event {}", invitationsSent, eventId);
        return invitationsSent;
    }

    /**
     * Send invitation to all members of a church
     */
    @Transactional
    public int sendEventInvitationsToAll(Long eventId, String personalMessage) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        Long churchId = event.getChurch().getId();
        List<Member> members = memberRepository.findByChurchId(churchId);

        int invitationsSent = 0;

        for (Member member : members) {
            try {
                if (member.getEmail() != null && !member.getEmail().trim().isEmpty()) {
                    String subject = "You're Invited: " + event.getName();
                    String body = buildInvitationEmailBody(member, event, personalMessage);

                    emailService.sendEmail(member.getEmail(), subject, body);
                    invitationsSent++;
                }
            } catch (Exception e) {
                log.error("Error sending invitation to member {}: {}",
                    member.getId(), e.getMessage());
            }
        }

        log.info("Sent {} invitations to all members for event {}", invitationsSent, eventId);
        return invitationsSent;
    }

    /**
     * Build invitation email body
     */
    private String buildInvitationEmailBody(Member member, Event event, String personalMessage) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' h:mm a");

        StringBuilder body = new StringBuilder();
        body.append("Dear ").append(member.getFirstName()).append(",\n\n");

        if (personalMessage != null && !personalMessage.trim().isEmpty()) {
            body.append(personalMessage).append("\n\n");
        } else {
            body.append("You are cordially invited to join us for an upcoming event!\n\n");
        }

        body.append("Event Details:\n");
        body.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        body.append("Event: ").append(event.getName()).append("\n");
        body.append("Date: ").append(event.getStartDate().format(formatter)).append("\n");

        if (event.getLocationType() == EventLocationType.PHYSICAL ||
            event.getLocationType() == EventLocationType.HYBRID) {
            body.append("Location: ").append(event.getPhysicalLocation()).append("\n");
        }

        if (event.getLocationType() == EventLocationType.VIRTUAL ||
            event.getLocationType() == EventLocationType.HYBRID) {
            body.append("Virtual Link: ").append(event.getVirtualLink()).append("\n");
        }

        if (event.getDescription() != null && !event.getDescription().isEmpty()) {
            body.append("\nAbout this event:\n");
            body.append(event.getDescription()).append("\n");
        }

        if (event.getRequiresRegistration()) {
            body.append("\n⚠ Registration Required\n");
            if (event.getMaxCapacity() != null) {
                body.append("Limited to ").append(event.getMaxCapacity()).append(" attendees\n");
            }
            if (event.getRegistrationDeadline() != null) {
                body.append("Register by: ")
                    .append(event.getRegistrationDeadline().format(formatter)).append("\n");
            }
        }

        body.append("\nWe would love to see you there!\n\n");

        body.append("Best regards,\n");
        body.append(event.getChurch().getName());

        return body.toString();
    }
}
