package com.reuben.pastcare_spring.services;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reuben.pastcare_spring.dtos.CheckInRequest;
import com.reuben.pastcare_spring.dtos.CheckInResponse;
import com.reuben.pastcare_spring.dtos.NearbySessionResponse;
import com.reuben.pastcare_spring.enums.CheckInMethod;
import com.reuben.pastcare_spring.models.Attendance;
import com.reuben.pastcare_spring.models.AttendanceSession;
import com.reuben.pastcare_spring.models.Member;
import com.reuben.pastcare_spring.models.Visitor;
import com.reuben.pastcare_spring.repositories.AttendanceRepository;
import com.reuben.pastcare_spring.repositories.AttendanceSessionRepository;
import com.reuben.pastcare_spring.repositories.MemberRepository;
import com.reuben.pastcare_spring.repositories.VisitorRepository;

/**
 * Service for handling check-in operations across all methods.
 * Phase 1: Enhanced Attendance Tracking
 *
 * This is the main orchestrator service that coordinates:
 * - QR code check-in validation
 * - Geofence-based check-in
 * - Manual check-in
 * - Late arrival tracking
 * - Duplicate check-in prevention
 *
 * @author Claude Sonnet 4.5
 * @version 1.0
 * @since 2025-12-24
 */
@Service
public class CheckInService {

  private final AttendanceSessionRepository attendanceSessionRepository;
  private final AttendanceRepository attendanceRepository;
  private final MemberRepository memberRepository;
  private final VisitorRepository visitorRepository;
  private final QRCodeService qrCodeService;
  private final GeofenceService geofenceService;

  public CheckInService(
      AttendanceSessionRepository attendanceSessionRepository,
      AttendanceRepository attendanceRepository,
      MemberRepository memberRepository,
      VisitorRepository visitorRepository,
      QRCodeService qrCodeService,
      GeofenceService geofenceService) {
    this.attendanceSessionRepository = attendanceSessionRepository;
    this.attendanceRepository = attendanceRepository;
    this.memberRepository = memberRepository;
    this.visitorRepository = visitorRepository;
    this.qrCodeService = qrCodeService;
    this.geofenceService = geofenceService;
  }

  /**
   * Process a check-in request for a member or visitor.
   *
   * @param request The check-in request
   * @return CheckInResponse with attendance details
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional
  public CheckInResponse checkIn(CheckInRequest request) {
    // Validate request
    request.validate();

    // Get session
    AttendanceSession session = attendanceSessionRepository.findById(request.sessionId())
        .orElseThrow(() -> new IllegalArgumentException("Attendance session not found with id: " + request.sessionId()));

    // Validate session is not completed
    if (session.getIsCompleted() != null && session.getIsCompleted()) {
      throw new IllegalArgumentException("Cannot check in to a completed session");
    }

    // Validate check-in window
    validateCheckInWindow(session);

    // Perform method-specific validation
    validateCheckInMethod(request, session);

    // Check for duplicate attendance
    checkDuplicateAttendance(request.sessionId(), request.memberId(), request.visitorId());

    // Create attendance record
    Attendance attendance = new Attendance();
    attendance.setAttendanceSession(session);
    attendance.setCheckInMethod(request.checkInMethod());
    attendance.setCheckInTime(LocalDateTime.now());
    attendance.setDeviceInfo(request.deviceInfo());

    // Set location data if provided
    if (request.latitude() != null && request.longitude() != null) {
      attendance.setCheckInLocationLat(request.latitude());
      attendance.setCheckInLocationLong(request.longitude());
    }

    // Set member or visitor
    String personName;
    Long personId;
    if (request.memberId() != null) {
      Member member = memberRepository.findById(request.memberId())
          .orElseThrow(() -> new IllegalArgumentException("Member not found with id: " + request.memberId()));
      attendance.setMember(member);
      personName = member.getFirstName() + " " + member.getLastName();
      personId = member.getId();
    } else {
      Visitor visitor = visitorRepository.findById(request.visitorId())
          .orElseThrow(() -> new IllegalArgumentException("Visitor not found with id: " + request.visitorId()));
      // Note: VisitorAttendance relationship will be handled separately in visitor service
      personName = visitor.getFirstName() + " " + visitor.getLastName();
      personId = visitor.getId();
    }

    // Calculate if late
    calculateLateArrival(attendance, session);

    // Save attendance
    Attendance savedAttendance = attendanceRepository.save(attendance);

    // Build response
    String message = buildCheckInMessage(savedAttendance, request.checkInMethod());

    return new CheckInResponse(
        savedAttendance.getId(),
        session.getId(),
        session.getSessionName(),
        request.memberId(),
        request.memberId() != null ? personName : null,
        request.visitorId(),
        request.visitorId() != null ? personName : null,
        savedAttendance.getCheckInMethod(),
        savedAttendance.getCheckInTime(),
        savedAttendance.getIsLate(),
        savedAttendance.getMinutesLate(),
        message
    );
  }

  /**
   * Find nearby sessions based on user's location.
   *
   * @param latitude User's latitude
   * @param longitude User's longitude
   * @param maxDistanceMeters Maximum search distance (default 5000m)
   * @return List of nearby sessions with distance info
   */
  public List<NearbySessionResponse> findNearbySessions(double latitude, double longitude, Integer maxDistanceMeters) {
    List<AttendanceSession> nearbySessions = geofenceService.findNearbySessions(latitude, longitude, maxDistanceMeters);

    return nearbySessions.stream()
        .map(session -> {
          double distance = geofenceService.getDistanceToSession(session.getId(), latitude, longitude);
          boolean isWithin = distance <= session.getGeofenceRadiusMeters();

          return new NearbySessionResponse(
              session.getId(),
              session.getSessionName(),
              session.getSessionDate(),
              session.getSessionTime(),
              session.getServiceType(),
              session.getFellowship() != null ? session.getFellowship().getId() : null,
              session.getFellowship() != null ? session.getFellowship().getName() : null,
              Math.round(distance * 100.0) / 100.0, // Round to 2 decimal places
              session.getGeofenceRadiusMeters(),
              isWithin,
              session.getIsCompleted()
          );
        })
        .collect(Collectors.toList());
  }

  /**
   * Validate check-in window based on session configuration.
   */
  private void validateCheckInWindow(AttendanceSession session) {
    LocalDateTime now = LocalDateTime.now();

    if (session.getCheckInOpensAt() != null && now.isBefore(session.getCheckInOpensAt())) {
      throw new IllegalArgumentException("Check-in has not opened yet for this session");
    }

    if (session.getCheckInClosesAt() != null && now.isAfter(session.getCheckInClosesAt())) {
      throw new IllegalArgumentException("Check-in window has closed for this session");
    }
  }

  /**
   * Perform method-specific validation.
   */
  private void validateCheckInMethod(CheckInRequest request, AttendanceSession session) {
    switch (request.checkInMethod()) {
      case QR_CODE:
        validateQRCodeCheckIn(request.qrCodeData(), request.sessionId());
        break;
      case GEOFENCE:
        validateGeofenceCheckIn(request.latitude(), request.longitude(), session);
        break;
      case MANUAL:
      case SELF_CHECKIN:
      case MOBILE_APP:
        // No additional validation needed
        break;
      default:
        throw new IllegalArgumentException("Unsupported check-in method: " + request.checkInMethod());
    }
  }

  /**
   * Validate QR code check-in.
   */
  private void validateQRCodeCheckIn(String qrCodeData, Long expectedSessionId) {
    Map<String, Object> validation = qrCodeService.validateQRCode(qrCodeData);

    if (!(Boolean) validation.get("valid")) {
      throw new IllegalArgumentException((String) validation.get("message"));
    }

    Long sessionId = (Long) validation.get("sessionId");
    if (!sessionId.equals(expectedSessionId)) {
      throw new IllegalArgumentException("QR code is not valid for this session");
    }
  }

  /**
   * Validate geofence check-in.
   */
  private void validateGeofenceCheckIn(Double latitude, Double longitude, AttendanceSession session) {
    if (!geofenceService.isWithinGeofence(session.getId(), latitude, longitude)) {
      double distance = geofenceService.getDistanceToSession(session.getId(), latitude, longitude);
      throw new IllegalArgumentException(
          String.format("You are not within the geofence radius. Distance: %.2f meters", distance)
      );
    }
  }

  /**
   * Check for duplicate attendance.
   */
  private void checkDuplicateAttendance(Long sessionId, Long memberId, Long visitorId) {
    if (memberId != null) {
      boolean exists = attendanceRepository.existsByAttendanceSessionIdAndMemberId(sessionId, memberId);
      if (exists) {
        throw new IllegalArgumentException("Member has already checked in to this session");
      }
    }
    // Note: Visitor duplicate checking would need VisitorAttendance repository
  }

  /**
   * Calculate if check-in is late.
   */
  private void calculateLateArrival(Attendance attendance, AttendanceSession session) {
    if (session.getSessionTime() == null) {
      attendance.setIsLate(false);
      return;
    }

    LocalDateTime sessionStartDateTime = LocalDateTime.of(session.getSessionDate(), session.getSessionTime());
    LocalDateTime checkInTime = attendance.getCheckInTime();

    if (checkInTime.isAfter(sessionStartDateTime)) {
      long minutesLate = ChronoUnit.MINUTES.between(sessionStartDateTime, checkInTime);

      if (session.getAllowLateCheckin() && minutesLate <= session.getLateCutoffMinutes()) {
        attendance.setIsLate(true);
        attendance.setMinutesLate((int) minutesLate);
      } else if (!session.getAllowLateCheckin()) {
        throw new IllegalArgumentException("Late check-in is not allowed for this session");
      } else {
        throw new IllegalArgumentException(
            String.format("Check-in cutoff time exceeded. Maximum allowed: %d minutes", session.getLateCutoffMinutes())
        );
      }
    } else {
      attendance.setIsLate(false);
    }
  }

  /**
   * Build check-in success message.
   */
  private String buildCheckInMessage(Attendance attendance, CheckInMethod method) {
    StringBuilder message = new StringBuilder("Check-in successful");

    if (attendance.getIsLate() != null && attendance.getIsLate()) {
      message.append(String.format(" (late by %d minutes)", attendance.getMinutesLate()));
    }

    message.append(" via ").append(method.name().replace("_", " ").toLowerCase());

    return message.toString();
  }
}
