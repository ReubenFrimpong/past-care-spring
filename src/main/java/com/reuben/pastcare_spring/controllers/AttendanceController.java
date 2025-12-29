package com.reuben.pastcare_spring.controllers;

import com.reuben.pastcare_spring.annotations.RequirePermission;
import com.reuben.pastcare_spring.enums.Permission;

import java.time.LocalDate;
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

import com.reuben.pastcare_spring.dtos.AttendanceRequest;
import com.reuben.pastcare_spring.dtos.AttendanceResponse;
import com.reuben.pastcare_spring.dtos.AttendanceSessionRequest;
import com.reuben.pastcare_spring.dtos.AttendanceSessionResponse;
import com.reuben.pastcare_spring.dtos.BulkAttendanceRequest;
import com.reuben.pastcare_spring.dtos.QRCodeResponse;
import com.reuben.pastcare_spring.services.AttendanceService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

  private final AttendanceService attendanceService;

  public AttendanceController(AttendanceService attendanceService) {
    this.attendanceService = attendanceService;
  }

    @RequirePermission(Permission.ATTENDANCE_MARK)
  @PostMapping("/sessions")
  public ResponseEntity<AttendanceSessionResponse> createAttendanceSession(
      @Valid @RequestBody AttendanceSessionRequest request) {
    return ResponseEntity.ok(attendanceService.createAttendanceSession(request));
  }

    @RequirePermission(Permission.ATTENDANCE_VIEW)
  @GetMapping("/sessions")
  public ResponseEntity<List<AttendanceSessionResponse>> getAllAttendanceSessions() {
    return ResponseEntity.ok(attendanceService.getAllAttendanceSessions());
  }

    @RequirePermission(Permission.ATTENDANCE_VIEW)
  @GetMapping("/sessions/{id}")
  public ResponseEntity<AttendanceSessionResponse> getAttendanceSession(@PathVariable Long id) {
    return ResponseEntity.ok(attendanceService.getAttendanceSession(id));
  }

    @RequirePermission(Permission.ATTENDANCE_VIEW)
  @GetMapping("/sessions/church/{churchId}")
  public ResponseEntity<List<AttendanceSessionResponse>> getAttendanceSessionsByChurch(
      @PathVariable Long churchId) {
    return ResponseEntity.ok(attendanceService.getAttendanceSessionsByChurch(churchId));
  }

    @RequirePermission(Permission.ATTENDANCE_VIEW)
  @GetMapping("/sessions/fellowship/{fellowshipId}")
  public ResponseEntity<List<AttendanceSessionResponse>> getAttendanceSessionsByFellowship(
      @PathVariable Long fellowshipId) {
    return ResponseEntity.ok(attendanceService.getAttendanceSessionsByFellowship(fellowshipId));
  }

    @RequirePermission(Permission.ATTENDANCE_VIEW)
  @GetMapping("/sessions/date-range")
  public ResponseEntity<List<AttendanceSessionResponse>> getAttendanceSessionsByDateRange(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
    return ResponseEntity.ok(attendanceService.getAttendanceSessionsByDateRange(startDate, endDate));
  }

    @RequirePermission(Permission.ATTENDANCE_MARK)
  @PutMapping("/sessions/{id}")
  public ResponseEntity<AttendanceSessionResponse> updateAttendanceSession(
      @PathVariable Long id,
      @Valid @RequestBody AttendanceSessionRequest request) {
    return ResponseEntity.ok(attendanceService.updateAttendanceSession(id, request));
  }

    @RequirePermission(Permission.ATTENDANCE_MARK)
  @PutMapping("/sessions/{id}/complete")
  public ResponseEntity<AttendanceSessionResponse> completeAttendanceSession(@PathVariable Long id) {
    return ResponseEntity.ok(attendanceService.completeAttendanceSession(id));
  }

    @RequirePermission(Permission.ATTENDANCE_MARK)
  @PostMapping("/sessions/{id}/qr-code")
  public ResponseEntity<QRCodeResponse> generateQRCode(@PathVariable Long id) {
    return ResponseEntity.ok(attendanceService.generateQRCodeForSession(id));
  }

    @RequirePermission(Permission.ATTENDANCE_MARK)
  @DeleteMapping("/sessions/{id}")
  public ResponseEntity<?> deleteAttendanceSession(@PathVariable Long id) {
    attendanceService.deleteAttendanceSession(id);
    return ResponseEntity.noContent().build();
  }

    @RequirePermission(Permission.ATTENDANCE_MARK)
  @PostMapping("/mark")
  public ResponseEntity<AttendanceResponse> markAttendance(@Valid @RequestBody AttendanceRequest request) {
    return ResponseEntity.ok(attendanceService.markAttendance(request));
  }

    @RequirePermission(Permission.ATTENDANCE_MARK)
  @PostMapping("/mark-bulk")
  public ResponseEntity<List<AttendanceResponse>> markBulkAttendance(
      @Valid @RequestBody BulkAttendanceRequest request) {
    return ResponseEntity.ok(attendanceService.markBulkAttendance(request));
  }

    @RequirePermission(Permission.ATTENDANCE_VIEW)
  @GetMapping("/session/{sessionId}")
  public ResponseEntity<List<AttendanceResponse>> getAttendancesBySession(@PathVariable Long sessionId) {
    return ResponseEntity.ok(attendanceService.getAttendancesBySession(sessionId));
  }

    @RequirePermission(Permission.ATTENDANCE_VIEW)
  @GetMapping("/member/{memberId}")
  public ResponseEntity<List<AttendanceResponse>> getAttendancesByMember(@PathVariable Long memberId) {
    return ResponseEntity.ok(attendanceService.getAttendancesByMember(memberId));
  }
}
