package com.reuben.pastcare_spring.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reuben.pastcare_spring.dtos.RecipientResponse;
import com.reuben.pastcare_spring.dtos.ReminderRequest;
import com.reuben.pastcare_spring.dtos.ReminderResponse;
import com.reuben.pastcare_spring.enums.ReminderStatus;
import com.reuben.pastcare_spring.mapper.ReminderMapper;
import com.reuben.pastcare_spring.models.AttendanceReminder;
import com.reuben.pastcare_spring.models.Fellowship;
import com.reuben.pastcare_spring.models.Member;
import com.reuben.pastcare_spring.models.ReminderRecipient;
import com.reuben.pastcare_spring.models.User;
import com.reuben.pastcare_spring.repositories.AttendanceReminderRepository;
import com.reuben.pastcare_spring.repositories.FellowshipRepository;
import com.reuben.pastcare_spring.repositories.MemberRepository;
import com.reuben.pastcare_spring.repositories.ReminderRecipientRepository;
import com.reuben.pastcare_spring.repositories.UserRepository;

/**
 * Service for managing attendance reminders.
 * Phase 1: Enhanced Attendance Tracking - Attendance Reminders
 *
 * Handles:
 * - Creating and scheduling reminders
 * - Managing recipients
 * - Tracking delivery status
 * - Sending reminders (integration point for SMS/Email/WhatsApp services)
 *
 * Note: Actual SMS/Email/WhatsApp delivery requires external service integration
 * (e.g., Twilio for SMS, SendGrid for Email, WhatsApp Business API).
 * This implementation provides the framework with placeholder delivery methods.
 *
 * @author Claude Sonnet 4.5
 * @version 1.0
 * @since 2025-12-24
 */
@Service
public class AttendanceReminderService {

  private final AttendanceReminderRepository reminderRepository;
  private final ReminderRecipientRepository recipientRepository;
  private final MemberRepository memberRepository;
  private final FellowshipRepository fellowshipRepository;
  private final UserRepository userRepository;

  public AttendanceReminderService(
      AttendanceReminderRepository reminderRepository,
      ReminderRecipientRepository recipientRepository,
      MemberRepository memberRepository,
      FellowshipRepository fellowshipRepository,
      UserRepository userRepository) {
    this.reminderRepository = reminderRepository;
    this.recipientRepository = recipientRepository;
    this.memberRepository = memberRepository;
    this.fellowshipRepository = fellowshipRepository;
    this.userRepository = userRepository;
  }

  /**
   * Create a new attendance reminder.
   *
   * @param request Reminder creation data
   * @param createdByUserId ID of user creating the reminder
   * @return Created reminder
   */
  @Transactional
  public ReminderResponse createReminder(ReminderRequest request, Long createdByUserId) {
    // Validate request
    request.validate();

    User createdByUser = userRepository.findById(createdByUserId)
        .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + createdByUserId));

    AttendanceReminder reminder = new AttendanceReminder();
    reminder.setCreatedByUser(createdByUser);
    reminder.setMessage(request.message());
    reminder.setTargetGroup(request.targetGroup());
    reminder.setScheduledFor(request.scheduledFor());
    reminder.setSendViaSms(request.sendViaSms());
    reminder.setSendViaEmail(request.sendViaEmail());
    reminder.setSendViaWhatsapp(request.sendViaWhatsapp());
    reminder.setIsRecurring(request.isRecurring());
    reminder.setRecurrencePattern(request.recurrencePattern());
    reminder.setNotes(request.notes());
    reminder.setStatus(ReminderStatus.SCHEDULED);

    // Set fellowship if provided
    if (request.fellowshipId() != null) {
      Fellowship fellowship = fellowshipRepository.findById(request.fellowshipId())
          .orElseThrow(() -> new IllegalArgumentException("Fellowship not found"));
      reminder.setFellowship(fellowship);
    }

    // Save reminder first to get ID
    AttendanceReminder savedReminder = reminderRepository.save(reminder);

    // Determine recipients based on target group
    List<Member> recipients = determineRecipients(request);

    // Create recipient records
    int recipientCount = 0;
    for (Member member : recipients) {
      ReminderRecipient recipient = new ReminderRecipient();
      recipient.setReminder(savedReminder);
      recipient.setMember(member);
      recipient.setStatus(ReminderStatus.SCHEDULED);
      recipientRepository.save(recipient);
      recipientCount++;
    }

    // Update reminder with recipient count
    savedReminder.setRecipientCount(recipientCount);
    savedReminder = reminderRepository.save(savedReminder);

    return ReminderMapper.toReminderResponse(savedReminder);
  }

  /**
   * Get all reminders.
   *
   * @return List of all reminders
   */
  public List<ReminderResponse> getAllReminders() {
    return reminderRepository.findAll()
        .stream()
        .map(ReminderMapper::toReminderResponse)
        .collect(Collectors.toList());
  }

  /**
   * Get reminder by ID.
   *
   * @param id Reminder ID
   * @return Reminder details
   */
  public ReminderResponse getReminderById(Long id) {
    AttendanceReminder reminder = reminderRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Reminder not found with id: " + id));
    return ReminderMapper.toReminderResponse(reminder);
  }

  /**
   * Get all recipients for a reminder.
   *
   * @param reminderId Reminder ID
   * @return List of recipients
   */
  public List<RecipientResponse> getReminderRecipients(Long reminderId) {
    return recipientRepository.findByReminderId(reminderId)
        .stream()
        .map(ReminderMapper::toRecipientResponse)
        .collect(Collectors.toList());
  }

  /**
   * Get reminders by status.
   *
   * @param status Reminder status
   * @return List of reminders
   */
  public List<ReminderResponse> getRemindersByStatus(ReminderStatus status) {
    return reminderRepository.findByStatus(status)
        .stream()
        .map(ReminderMapper::toReminderResponse)
        .collect(Collectors.toList());
  }

  /**
   * Cancel a reminder.
   *
   * @param id Reminder ID
   * @param cancelledByUserId User cancelling the reminder
   * @return Updated reminder
   */
  @Transactional
  public ReminderResponse cancelReminder(Long id, Long cancelledByUserId) {
    AttendanceReminder reminder = reminderRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Reminder not found with id: " + id));

    if (reminder.getStatus() == ReminderStatus.SENT) {
      throw new IllegalArgumentException("Cannot cancel a reminder that has already been sent");
    }

    User cancelledByUser = userRepository.findById(cancelledByUserId)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));

    reminder.setStatus(ReminderStatus.CANCELLED);
    reminder.setCancelledAt(LocalDateTime.now());
    reminder.setCancelledByUser(cancelledByUser);

    // Update all recipients to cancelled
    List<ReminderRecipient> recipients = recipientRepository.findByReminderId(id);
    for (ReminderRecipient recipient : recipients) {
      if (recipient.getStatus() == ReminderStatus.SCHEDULED) {
        recipient.setStatus(ReminderStatus.CANCELLED);
        recipientRepository.save(recipient);
      }
    }

    AttendanceReminder updated = reminderRepository.save(reminder);
    return ReminderMapper.toReminderResponse(updated);
  }

  /**
   * Send a reminder immediately (bypasses schedule).
   * This is a simplified implementation - actual delivery would integrate with external services.
   *
   * @param id Reminder ID
   * @return Updated reminder
   */
  @Transactional
  public ReminderResponse sendReminderNow(Long id) {
    AttendanceReminder reminder = reminderRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Reminder not found with id: " + id));

    if (reminder.getStatus() == ReminderStatus.SENT) {
      throw new IllegalArgumentException("Reminder has already been sent");
    }

    if (reminder.getStatus() == ReminderStatus.CANCELLED) {
      throw new IllegalArgumentException("Cannot send a cancelled reminder");
    }

    // Get all scheduled recipients
    List<ReminderRecipient> recipients = recipientRepository.findByReminderIdAndStatus(id, ReminderStatus.SCHEDULED);

    int sentCount = 0;
    int deliveredCount = 0;
    int failedCount = 0;

    for (ReminderRecipient recipient : recipients) {
      boolean success = sendToRecipient(reminder, recipient);

      if (success) {
        recipient.setStatus(ReminderStatus.SENT);
        recipient.setSentAt(LocalDateTime.now());
        recipient.setDeliveredAt(LocalDateTime.now()); // Simplified - actual delivery confirmation would come asynchronously
        sentCount++;
        deliveredCount++;
      } else {
        recipient.setStatus(ReminderStatus.FAILED);
        recipient.setFailedAt(LocalDateTime.now());
        recipient.setFailureReason("Delivery failed (placeholder)");
        failedCount++;
      }

      recipientRepository.save(recipient);
    }

    // Update reminder status
    reminder.setStatus(ReminderStatus.SENT);
    reminder.setSentAt(LocalDateTime.now());
    reminder.setSentCount(sentCount);
    reminder.setDeliveredCount(deliveredCount);
    reminder.setFailedCount(failedCount);

    AttendanceReminder updated = reminderRepository.save(reminder);
    return ReminderMapper.toReminderResponse(updated);
  }

  /**
   * Delete a reminder.
   *
   * @param id Reminder ID
   */
  @Transactional
  public void deleteReminder(Long id) {
    if (!reminderRepository.existsById(id)) {
      throw new IllegalArgumentException("Reminder not found with id: " + id);
    }

    // Delete all recipients first
    List<ReminderRecipient> recipients = recipientRepository.findByReminderId(id);
    recipientRepository.deleteAll(recipients);

    // Delete reminder
    reminderRepository.deleteById(id);
  }

  /**
   * Get reminders scheduled for a specific date range.
   *
   * @param start Start date/time
   * @param end End date/time
   * @return List of reminders
   */
  public List<ReminderResponse> getRemindersByDateRange(LocalDateTime start, LocalDateTime end) {
    return reminderRepository.findByScheduledForBetween(start, end)
        .stream()
        .map(ReminderMapper::toReminderResponse)
        .collect(Collectors.toList());
  }

  /**
   * Determine recipients based on target group and request parameters.
   * This is a simplified implementation - actual logic would be more sophisticated.
   */
  private List<Member> determineRecipients(ReminderRequest request) {
    List<Member> recipients = new ArrayList<>();

    // If specific member IDs provided, use those
    if (request.specificMemberIds() != null && !request.specificMemberIds().isEmpty()) {
      recipients = memberRepository.findAllById(request.specificMemberIds());
    }
    // If fellowship specified, get fellowship members
    else if (request.fellowshipId() != null) {
      Fellowship fellowship = fellowshipRepository.findById(request.fellowshipId())
          .orElseThrow(() -> new IllegalArgumentException("Fellowship not found"));
      // This assumes Fellowship has a members relationship - adjust as needed
      recipients = memberRepository.findAll().stream()
          .filter(m -> m.getFellowships() != null && m.getFellowships().contains(fellowship))
          .collect(Collectors.toList());
    }
    // Otherwise get all members (or apply other filtering based on target group)
    else {
      recipients = memberRepository.findAll();
      // In a real implementation, you would filter based on targetGroup
      // For example: "IRREGULAR_ATTENDERS" would query attendance records
    }

    return recipients;
  }

  /**
   * Send reminder to a specific recipient.
   * This is a placeholder method - actual implementation would integrate with:
   * - SMS service (e.g., Twilio, AWS SNS)
   * - Email service (e.g., SendGrid, AWS SES)
   * - WhatsApp Business API
   *
   * @param reminder The reminder to send
   * @param recipient The recipient
   * @return true if successful, false otherwise
   */
  private boolean sendToRecipient(AttendanceReminder reminder, ReminderRecipient recipient) {
    // Placeholder implementation
    // In production, this would make actual API calls to SMS/Email/WhatsApp providers

    boolean success = true;

    if (reminder.getSendViaSms()) {
      // TODO: Integrate with SMS service (e.g., Twilio)
      // success = smsService.send(recipient.getMember().getPhoneNumber(), reminder.getMessage());
      recipient.setSmsSent(true);
    }

    if (reminder.getSendViaEmail()) {
      // TODO: Integrate with Email service (e.g., SendGrid)
      // Member doesn't have email field, would need to get from associated User or add email to Member
      recipient.setEmailSent(false); // Set to false since Member doesn't have email
    }

    if (reminder.getSendViaWhatsapp()) {
      // TODO: Integrate with WhatsApp Business API
      // success = whatsappService.send(recipient.getMember().getWhatsappNumber(), reminder.getMessage());
      if (recipient.getMember().getWhatsappNumber() != null) {
        recipient.setWhatsappSent(true);
      }
    }

    return success;
  }
}
