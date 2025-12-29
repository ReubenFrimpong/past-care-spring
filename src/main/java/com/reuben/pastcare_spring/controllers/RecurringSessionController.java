package com.reuben.pastcare_spring.controllers;

import com.reuben.pastcare_spring.annotations.RequirePermission;
import com.reuben.pastcare_spring.enums.Permission;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.reuben.pastcare_spring.services.RecurringSessionService;

/**
 * REST controller for managing recurring session generation.
 * Phase 1: Enhanced Attendance Tracking - Recurring Services
 *
 * Endpoints:
 * - POST /api/recurring-sessions/generate-all - Manually trigger generation for all templates
 * - POST /api/recurring-sessions/{templateId}/generate - Generate sessions for specific template
 *
 * @author Claude Sonnet 4.5
 * @version 1.0
 * @since 2025-12-24
 */
@RestController
@RequestMapping("/api/recurring-sessions")
public class RecurringSessionController {

  private final RecurringSessionService recurringSessionService;

  public RecurringSessionController(RecurringSessionService recurringSessionService) {
    this.recurringSessionService = recurringSessionService;
  }

  /**
   * Manually trigger generation of all recurring sessions.
   * Useful for testing or immediate generation.
   *
   * @return Success message with count
   */
    @RequirePermission(Permission.EVENT_CREATE)
  @PostMapping("/generate-all")
  public ResponseEntity<String> generateAllRecurringSessions() {
    recurringSessionService.generateRecurringSessions();
    return ResponseEntity.ok("Recurring session generation triggered successfully");
  }

  /**
   * Generate sessions for a specific template.
   *
   * @param templateId The template session ID
   * @param daysAhead Number of days to generate ahead (default: 7)
   * @return Success message with count
   */
    @RequirePermission(Permission.EVENT_CREATE)
  @PostMapping("/{templateId}/generate")
  public ResponseEntity<String> generateSessionsForTemplate(
      @PathVariable Long templateId,
      @RequestParam(defaultValue = "7") int daysAhead) {

    int count = recurringSessionService.generateSessionsNow(templateId, daysAhead);
    return ResponseEntity.ok(
        String.format("Generated %d sessions for template %d", count, templateId)
    );
  }
}
