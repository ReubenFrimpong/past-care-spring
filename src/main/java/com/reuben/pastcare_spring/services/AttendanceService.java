package com.reuben.pastcare_spring.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reuben.pastcare_spring.dtos.AttendanceRequest;
import com.reuben.pastcare_spring.dtos.AttendanceResponse;
import com.reuben.pastcare_spring.dtos.AttendanceSessionRequest;
import com.reuben.pastcare_spring.dtos.AttendanceSessionResponse;
import com.reuben.pastcare_spring.dtos.BulkAttendanceRequest;
import com.reuben.pastcare_spring.dtos.QRCodeResponse;
import com.reuben.pastcare_spring.mapper.AttendanceMapper;
import com.reuben.pastcare_spring.mapper.AttendanceSessionMapper;
import com.reuben.pastcare_spring.models.Attendance;
import com.reuben.pastcare_spring.models.AttendanceSession;
import com.reuben.pastcare_spring.models.Church;
import com.reuben.pastcare_spring.models.Event;
import com.reuben.pastcare_spring.models.Fellowship;
import com.reuben.pastcare_spring.models.Member;
import com.reuben.pastcare_spring.repositories.AttendanceRepository;
import com.reuben.pastcare_spring.repositories.AttendanceSessionRepository;
import com.reuben.pastcare_spring.repositories.ChurchRepository;
import com.reuben.pastcare_spring.repositories.EventRepository;
import com.reuben.pastcare_spring.repositories.FellowshipRepository;
import com.reuben.pastcare_spring.repositories.MemberRepository;

@Service
public class AttendanceService {

  private final AttendanceSessionRepository attendanceSessionRepository;
  private final AttendanceRepository attendanceRepository;
  private final ChurchRepository churchRepository;
  private final EventRepository eventRepository;
  private final FellowshipRepository fellowshipRepository;
  private final MemberRepository memberRepository;
  private final QRCodeService qrCodeService;
  private final TenantValidationService tenantValidationService;

  public AttendanceService(
      AttendanceSessionRepository attendanceSessionRepository,
      AttendanceRepository attendanceRepository,
      ChurchRepository churchRepository,
      EventRepository eventRepository,
      FellowshipRepository fellowshipRepository,
      MemberRepository memberRepository,
      QRCodeService qrCodeService,
      TenantValidationService tenantValidationService) {
    this.attendanceSessionRepository = attendanceSessionRepository;
    this.attendanceRepository = attendanceRepository;
    this.churchRepository = churchRepository;
    this.eventRepository = eventRepository;
    this.fellowshipRepository = fellowshipRepository;
    this.memberRepository = memberRepository;
    this.qrCodeService = qrCodeService;
    this.tenantValidationService = tenantValidationService;
  }

  @Transactional
  public AttendanceSessionResponse createAttendanceSession(AttendanceSessionRequest request) {
    AttendanceSession session = new AttendanceSession();
    session.setSessionName(request.sessionName());
    session.setSessionDate(request.sessionDate());
    session.setSessionTime(request.sessionTime());
    session.setNotes(request.notes());
    session.setIsCompleted(false);

    Church church = churchRepository.findById(request.churchId())
        .orElseThrow(() -> new IllegalArgumentException("Church not found"));
    session.setChurch(church);

    if (request.fellowshipId() != null) {
      Fellowship fellowship = fellowshipRepository.findById(request.fellowshipId())
          .orElseThrow(() -> new IllegalArgumentException("Fellowship not found"));
      session.setFellowship(fellowship);
    }

    if (request.eventId() != null) {
      Event event = eventRepository.findById(request.eventId())
          .orElseThrow(() -> new IllegalArgumentException("Event not found"));
      session.setEvent(event);
    }

    AttendanceSession savedSession = attendanceSessionRepository.save(session);
    return AttendanceSessionMapper.toAttendanceSessionResponse(savedSession);
  }

  @Transactional
  public AttendanceSessionResponse updateAttendanceSession(Long id, AttendanceSessionRequest request) {
    AttendanceSession session = attendanceSessionRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Attendance session not found"));

    // CRITICAL SECURITY: Validate attendance session belongs to current church
    tenantValidationService.validateAttendanceSessionAccess(session);

    session.setSessionName(request.sessionName());
    session.setSessionDate(request.sessionDate());
    session.setSessionTime(request.sessionTime());
    session.setNotes(request.notes());

    Church church = churchRepository.findById(request.churchId())
        .orElseThrow(() -> new IllegalArgumentException("Church not found"));
    session.setChurch(church);

    if (request.fellowshipId() != null) {
      Fellowship fellowship = fellowshipRepository.findById(request.fellowshipId())
          .orElseThrow(() -> new IllegalArgumentException("Fellowship not found"));
      session.setFellowship(fellowship);
    } else {
      session.setFellowship(null);
    }

    if (request.eventId() != null) {
      Event event = eventRepository.findById(request.eventId())
          .orElseThrow(() -> new IllegalArgumentException("Event not found"));
      session.setEvent(event);
    } else {
      session.setEvent(null);
    }

    AttendanceSession updatedSession = attendanceSessionRepository.save(session);
    return AttendanceSessionMapper.toAttendanceSessionResponse(updatedSession, true);
  }

  public AttendanceSessionResponse getAttendanceSession(Long id) {
    AttendanceSession session = attendanceSessionRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Attendance session not found"));

    // CRITICAL SECURITY: Validate attendance session belongs to current church
    tenantValidationService.validateAttendanceSessionAccess(session);

    return AttendanceSessionMapper.toAttendanceSessionResponse(session, true);
  }

  public List<AttendanceSessionResponse> getAllAttendanceSessions() {
    return attendanceSessionRepository.findAll().stream()
        .map(AttendanceSessionMapper::toAttendanceSessionResponse)
        .collect(Collectors.toList());
  }

  public List<AttendanceSessionResponse> getAttendanceSessionsByChurch(Long churchId) {
    return attendanceSessionRepository.findByChurch_Id(churchId).stream()
        .map(AttendanceSessionMapper::toAttendanceSessionResponse)
        .collect(Collectors.toList());
  }

  public List<AttendanceSessionResponse> getAttendanceSessionsByFellowship(Long fellowshipId) {
    return attendanceSessionRepository.findByFellowship_Id(fellowshipId).stream()
        .map(AttendanceSessionMapper::toAttendanceSessionResponse)
        .collect(Collectors.toList());
  }

  public List<AttendanceSessionResponse> getAttendanceSessionsByDateRange(LocalDate startDate, LocalDate endDate) {
    return attendanceSessionRepository.findBySessionDateBetween(startDate, endDate).stream()
        .map(AttendanceSessionMapper::toAttendanceSessionResponse)
        .collect(Collectors.toList());
  }

  @Transactional
  public void deleteAttendanceSession(Long id) {
    AttendanceSession session = attendanceSessionRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Attendance session not found"));

    // CRITICAL SECURITY: Validate attendance session belongs to current church
    tenantValidationService.validateAttendanceSessionAccess(session);

    attendanceSessionRepository.delete(session);
  }

  @Transactional
  public AttendanceResponse markAttendance(AttendanceRequest request) {
    Member member = memberRepository.findById(request.memberId())
        .orElseThrow(() -> new IllegalArgumentException("Member not found"));

    AttendanceSession session = attendanceSessionRepository.findById(request.attendanceSessionId())
        .orElseThrow(() -> new IllegalArgumentException("Attendance session not found"));

    Attendance attendance = attendanceRepository
        .findByMemberIdAndAttendanceSessionId(request.memberId(), request.attendanceSessionId())
        .orElse(new Attendance());

    attendance.setMember(member);
    attendance.setAttendanceSession(session);
    attendance.setStatus(request.status());
    attendance.setRemarks(request.remarks());

    Attendance savedAttendance = attendanceRepository.save(attendance);
    return AttendanceMapper.toAttendanceResponse(savedAttendance);
  }

  @Transactional
  public List<AttendanceResponse> markBulkAttendance(BulkAttendanceRequest request) {
    return request.attendances().stream()
        .map(this::markAttendance)
        .collect(Collectors.toList());
  }

  public List<AttendanceResponse> getAttendancesBySession(Long sessionId) {
    return attendanceRepository.findByAttendanceSessionId(sessionId).stream()
        .map(AttendanceMapper::toAttendanceResponse)
        .collect(Collectors.toList());
  }

  public List<AttendanceResponse> getAttendancesByMember(Long memberId) {
    return attendanceRepository.findByMemberId(memberId).stream()
        .map(AttendanceMapper::toAttendanceResponse)
        .collect(Collectors.toList());
  }

  @Transactional
  public AttendanceSessionResponse completeAttendanceSession(Long id) {
    AttendanceSession session = attendanceSessionRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Attendance session not found"));

    // CRITICAL SECURITY: Validate attendance session belongs to current church
    tenantValidationService.validateAttendanceSessionAccess(session);

    session.setIsCompleted(true);
    AttendanceSession updatedSession = attendanceSessionRepository.save(session);
    return AttendanceSessionMapper.toAttendanceSessionResponse(updatedSession, true);
  }

  /**
   * Generate QR code for an attendance session.
   * Phase 1: Enhanced Attendance Tracking
   *
   * @param sessionId The attendance session ID
   * @return QRCodeResponse with QR code data and image
   */
  @Transactional
  public QRCodeResponse generateQRCodeForSession(Long sessionId) {
    AttendanceSession session = attendanceSessionRepository.findById(sessionId)
        .orElseThrow(() -> new IllegalArgumentException("Attendance session not found with id: " + sessionId));

    // CRITICAL SECURITY: Validate attendance session belongs to current church
    tenantValidationService.validateAttendanceSessionAccess(session);

    // Generate check-in URL with encrypted session data
    String checkInUrl = qrCodeService.generateCheckInUrl(sessionId);

    // Generate QR code image containing the check-in URL
    String qrCodeImage = qrCodeService.generateQRCodeImage(checkInUrl);

    // Keep the encrypted data for validation (without URL wrapper)
    String qrCodeData = qrCodeService.generateQRCodeData(sessionId);

    // Set expiry time (24 hours from now by default)
    LocalDateTime expiresAt = LocalDateTime.now().plusHours(24);

    // Save QR code data to session
    session.setQrCodeData(qrCodeData);
    session.setQrCodeUrl(qrCodeImage);
    session.setQrCodeExpiresAt(expiresAt);
    attendanceSessionRepository.save(session);

    return new QRCodeResponse(
        sessionId,
        session.getSessionName(),
        qrCodeData,
        qrCodeImage,
        expiresAt,
        "QR code generated successfully. Scan to check in at: " + checkInUrl
    );
  }
}
