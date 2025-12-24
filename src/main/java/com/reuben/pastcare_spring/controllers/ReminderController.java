package com.reuben.pastcare_spring.controllers;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.reuben.pastcare_spring.dtos.RecipientResponse;
import com.reuben.pastcare_spring.dtos.ReminderRequest;
import com.reuben.pastcare_spring.dtos.ReminderResponse;
import com.reuben.pastcare_spring.enums.ReminderStatus;
import com.reuben.pastcare_spring.services.AttendanceReminderService;

import jakarta.validation.Valid;

/**
 * REST controller for attendance reminders.
 * Phase 1: Enhanced Attendance Tracking - Attendance Reminders
 *
 * Endpoints:
 * - POST /api/reminders - Create new reminder
 * - GET /api/reminders - Get all reminders
 * - GET /api/reminders/{id} - Get reminder by ID
 * - GET /api/reminders/{id}/recipients - Get reminder recipients
 * - GET /api/reminders/status/{status} - Get reminders by status
 * - GET /api/reminders/date-range - Get reminders by date range
 * - POST /api/reminders/{id}/send - Send reminder immediately
 * - PUT /api/reminders/{id}/cancel - Cancel reminder
 * - DELETE /api/reminders/{id} - Delete reminder
 *
 * Note: This implementation provides the framework for reminder management.
 * Actual SMS/Email/WhatsApp delivery requires integration with external services.
 *
 * @author Claude Sonnet 4.5
 * @version 1.0
 * @since 2025-12-24
 */
@RestController
@RequestMapping("/api/reminders")
public class ReminderController {

  private final AttendanceReminderService reminderService;

  public ReminderController(AttendanceReminderService reminderService) {
    this.reminderService = reminderService;
  }

  /**
   * Create a new attendance reminder.
   *
   * @param request Reminder creation data
   * @param createdByUserId User creating the reminder (from auth context)
   * @return Created reminder
   */
  @PostMapping
  public ResponseEntity<ReminderResponse> createReminder(
      @Valid @RequestBody ReminderRequest request,
      @RequestParam Long createdByUserId) {
    return ResponseEntity.ok(reminderService.createReminder(request, createdByUserId));
  }

  /**
   * Get all reminders.
   */
  @GetMapping
  public ResponseEntity<List<ReminderResponse>> getAllReminders() {
    return ResponseEntity.ok(reminderService.getAllReminders());
  }

  /**
   * Get reminder by ID.
   */
  @GetMapping("/{id}")
  public ResponseEntity<ReminderResponse> getReminderById(@PathVariable Long id) {
    return ResponseEntity.ok(reminderService.getReminderById(id));
  }

  /**
   * Get all recipients for a reminder.
   */
  @GetMapping("/{id}/recipients")
  public ResponseEntity<List<RecipientResponse>> getReminderRecipients(@PathVariable Long id) {
    return ResponseEntity.ok(reminderService.getReminderRecipients(id));
  }

  /**
   * Get reminders by status.
   */
  @GetMapping("/status/{status}")
  public ResponseEntity<List<ReminderResponse>> getRemindersByStatus(@PathVariable ReminderStatus status) {
    return ResponseEntity.ok(reminderService.getRemindersByStatus(status));
  }

  /**
   * Get reminders scheduled within a date range.
   */
  @GetMapping("/date-range")
  public ResponseEntity<List<ReminderResponse>> getRemindersByDateRange(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
    return ResponseEntity.ok(reminderService.getRemindersByDateRange(start, end));
  }

  /**
   * Send a reminder immediately (bypasses schedule).
   */
  @PostMapping("/{id}/send")
  public ResponseEntity<ReminderResponse> sendReminderNow(@PathVariable Long id) {
    return ResponseEntity.ok(reminderService.sendReminderNow(id));
  }

  /**
   * Cancel a reminder.
   */
  @PutMapping("/{id}/cancel")
  public ResponseEntity<ReminderResponse> cancelReminder(
      @PathVariable Long id,
      @RequestParam Long cancelledByUserId) {
    return ResponseEntity.ok(reminderService.cancelReminder(id, cancelledByUserId));
  }

  /**
   * Delete a reminder.
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteReminder(@PathVariable Long id) {
    reminderService.deleteReminder(id);
    return ResponseEntity.noContent().build();
  }
}
