package com.reuben.pastcare_spring.services;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reuben.pastcare_spring.models.AttendanceSession;
import com.reuben.pastcare_spring.repositories.AttendanceSessionRepository;

/**
 * Service for automatically generating attendance sessions from recurring patterns.
 * Phase 1: Enhanced Attendance Tracking - Recurring Services
 *
 * Runs daily at midnight to check for sessions that need to be generated.
 * Supports DAILY, WEEKLY, MONTHLY, and custom recurrence patterns.
 *
 * @author Claude Sonnet 4.5
 * @version 1.0
 * @since 2025-12-24
 */
@Service
public class RecurringSessionService {

  private static final Logger logger = LoggerFactory.getLogger(RecurringSessionService.class);

  private final AttendanceSessionRepository sessionRepository;

  public RecurringSessionService(AttendanceSessionRepository sessionRepository) {
    this.sessionRepository = sessionRepository;
  }

  /**
   * Scheduled job that runs daily at midnight to generate sessions for recurring patterns.
   * Generates sessions 7 days in advance.
   */
  @Scheduled(cron = "0 0 0 * * *") // Run at midnight every day
  @Transactional
  public void generateRecurringSessions() {
    logger.info("Starting recurring session generation job...");

    try {
      // Get all recurring session templates
      List<AttendanceSession> recurringTemplates = sessionRepository.findByIsRecurringTrue();

      if (recurringTemplates.isEmpty()) {
        logger.info("No recurring session templates found");
        return;
      }

      logger.info("Found {} recurring session templates", recurringTemplates.size());

      int totalGenerated = 0;
      LocalDate today = LocalDate.now();
      LocalDate endDate = today.plusDays(7); // Generate 7 days ahead

      for (AttendanceSession template : recurringTemplates) {
        try {
          int generated = generateSessionsFromTemplate(template, today, endDate);
          totalGenerated += generated;
          logger.info("Generated {} sessions from template: {}", generated, template.getSessionName());
        } catch (Exception e) {
          logger.error("Error generating sessions from template {}: {}",
              template.getId(), e.getMessage(), e);
        }
      }

      logger.info("Recurring session generation completed. Total sessions generated: {}", totalGenerated);

    } catch (Exception e) {
      logger.error("Error in recurring session generation job: {}", e.getMessage(), e);
    }
  }

  /**
   * Generate sessions from a recurring template for the specified date range.
   *
   * @param template The recurring session template
   * @param startDate Start date for generation
   * @param endDate End date for generation
   * @return Number of sessions generated
   */
  @Transactional
  public int generateSessionsFromTemplate(AttendanceSession template, LocalDate startDate, LocalDate endDate) {
    if (template.getRecurrencePattern() == null || template.getRecurrencePattern().isBlank()) {
      logger.warn("Template {} has no recurrence pattern", template.getId());
      return 0;
    }

    List<LocalDate> dates = calculateRecurrenceDates(
        template.getRecurrencePattern(),
        startDate,
        endDate,
        template.getSessionDate()
    );

    int generatedCount = 0;

    for (LocalDate date : dates) {
      // Check if session already exists for this date
      boolean exists = sessionRepository.existsByChurch_IdAndSessionDateAndSessionNameAndIsRecurringFalse(
          template.getChurch().getId(),
          date,
          template.getSessionName()
      );

      if (!exists) {
        AttendanceSession newSession = createSessionFromTemplate(template, date);
        sessionRepository.save(newSession);
        generatedCount++;
        logger.debug("Created session for {} on {}", template.getSessionName(), date);
      }
    }

    return generatedCount;
  }

  /**
   * Calculate dates based on recurrence pattern.
   *
   * Pattern format examples:
   * - "DAILY" - Every day
   * - "WEEKLY:SUNDAY" - Every Sunday
   * - "WEEKLY:WEDNESDAY" - Every Wednesday
   * - "MONTHLY:1" - First day of every month
   * - "MONTHLY:LAST" - Last day of every month
   * - "CUSTOM:SUNDAY,WEDNESDAY" - Every Sunday and Wednesday
   *
   * @param pattern Recurrence pattern string
   * @param startDate Start date for calculation
   * @param endDate End date for calculation
   * @param templateDate Original template date (for reference)
   * @return List of dates matching the pattern
   */
  private List<LocalDate> calculateRecurrenceDates(
      String pattern,
      LocalDate startDate,
      LocalDate endDate,
      LocalDate templateDate) {

    List<LocalDate> dates = new ArrayList<>();

    String[] parts = pattern.toUpperCase().split(":");
    String type = parts[0];

    switch (type) {
      case "DAILY":
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
          dates.add(date);
        }
        break;

      case "WEEKLY":
        if (parts.length > 1) {
          DayOfWeek targetDay = DayOfWeek.valueOf(parts[1]);
          LocalDate date = startDate.with(TemporalAdjusters.nextOrSame(targetDay));
          while (!date.isAfter(endDate)) {
            if (!date.isBefore(startDate)) {
              dates.add(date);
            }
            date = date.plusWeeks(1);
          }
        } else {
          // Use template date's day of week
          DayOfWeek templateDay = templateDate.getDayOfWeek();
          LocalDate date = startDate.with(TemporalAdjusters.nextOrSame(templateDay));
          while (!date.isAfter(endDate)) {
            if (!date.isBefore(startDate)) {
              dates.add(date);
            }
            date = date.plusWeeks(1);
          }
        }
        break;

      case "MONTHLY":
        if (parts.length > 1) {
          if (parts[1].equals("LAST")) {
            // Last day of month
            for (LocalDate month = startDate.withDayOfMonth(1);
                 !month.isAfter(endDate);
                 month = month.plusMonths(1)) {
              LocalDate lastDay = month.with(TemporalAdjusters.lastDayOfMonth());
              if (!lastDay.isBefore(startDate) && !lastDay.isAfter(endDate)) {
                dates.add(lastDay);
              }
            }
          } else {
            // Specific day of month
            int dayOfMonth = Integer.parseInt(parts[1]);
            for (LocalDate month = startDate.withDayOfMonth(1);
                 !month.isAfter(endDate);
                 month = month.plusMonths(1)) {
              try {
                LocalDate date = month.withDayOfMonth(dayOfMonth);
                if (!date.isBefore(startDate) && !date.isAfter(endDate)) {
                  dates.add(date);
                }
              } catch (Exception e) {
                // Day doesn't exist in this month (e.g., Feb 31)
                logger.debug("Day {} doesn't exist in month {}", dayOfMonth, month.getMonth());
              }
            }
          }
        } else {
          // Use template date's day
          int dayOfMonth = templateDate.getDayOfMonth();
          for (LocalDate month = startDate.withDayOfMonth(1);
               !month.isAfter(endDate);
               month = month.plusMonths(1)) {
            try {
              LocalDate date = month.withDayOfMonth(dayOfMonth);
              if (!date.isBefore(startDate) && !date.isAfter(endDate)) {
                dates.add(date);
              }
            } catch (Exception e) {
              logger.debug("Day {} doesn't exist in month {}", dayOfMonth, month.getMonth());
            }
          }
        }
        break;

      case "CUSTOM":
        // Custom pattern: CUSTOM:SUNDAY,WEDNESDAY
        if (parts.length > 1) {
          String[] days = parts[1].split(",");
          List<DayOfWeek> targetDays = new ArrayList<>();
          for (String day : days) {
            try {
              targetDays.add(DayOfWeek.valueOf(day.trim()));
            } catch (Exception e) {
              logger.warn("Invalid day name: {}", day);
            }
          }

          for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            if (targetDays.contains(date.getDayOfWeek())) {
              dates.add(date);
            }
          }
        }
        break;

      default:
        logger.warn("Unknown recurrence pattern type: {}", type);
    }

    return dates;
  }

  /**
   * Create a new session from a template.
   *
   * @param template The template session
   * @param date The date for the new session
   * @return New session instance
   */
  private AttendanceSession createSessionFromTemplate(AttendanceSession template, LocalDate date) {
    AttendanceSession newSession = new AttendanceSession();

    newSession.setChurch(template.getChurch());
    newSession.setFellowship(template.getFellowship());
    newSession.setSessionName(template.getSessionName());
    newSession.setSessionDate(date);
    newSession.setSessionTime(template.getSessionTime());
    newSession.setServiceType(template.getServiceType());
    newSession.setNotes(template.getNotes());

    // Copy geofencing settings
    newSession.setGeofenceLatitude(template.getGeofenceLatitude());
    newSession.setGeofenceLongitude(template.getGeofenceLongitude());
    newSession.setGeofenceRadiusMeters(template.getGeofenceRadiusMeters());

    // Copy check-in settings
    newSession.setAllowLateCheckin(template.getAllowLateCheckin());
    newSession.setLateCutoffMinutes(template.getLateCutoffMinutes());
    newSession.setMaxCapacity(template.getMaxCapacity());

    // Adjust check-in windows if template has them
    if (template.getCheckInOpensAt() != null) {
      LocalTime openTime = template.getCheckInOpensAt().toLocalTime();
      newSession.setCheckInOpensAt(LocalDateTime.of(date, openTime));
    }

    if (template.getCheckInClosesAt() != null) {
      LocalTime closeTime = template.getCheckInClosesAt().toLocalTime();
      newSession.setCheckInClosesAt(LocalDateTime.of(date, closeTime));
    }

    // This is a generated session, not a recurring template
    newSession.setIsRecurring(false);
    newSession.setRecurrencePattern(null);

    // Not completed by default
    newSession.setIsCompleted(false);

    return newSession;
  }

  /**
   * Manually trigger session generation for a specific template.
   * Useful for testing or on-demand generation.
   *
   * @param templateId Template session ID
   * @param daysAhead Number of days to generate ahead
   * @return Number of sessions generated
   */
  @Transactional
  public int generateSessionsNow(Long templateId, int daysAhead) {
    AttendanceSession template = sessionRepository.findById(templateId)
        .orElseThrow(() -> new IllegalArgumentException("Template session not found: " + templateId));

    if (!template.getIsRecurring()) {
      throw new IllegalArgumentException("Session is not a recurring template: " + templateId);
    }

    LocalDate today = LocalDate.now();
    LocalDate endDate = today.plusDays(daysAhead);

    return generateSessionsFromTemplate(template, today, endDate);
  }
}
