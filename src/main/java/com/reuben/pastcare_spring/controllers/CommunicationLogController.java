package com.reuben.pastcare_spring.controllers;

import com.reuben.pastcare_spring.dtos.CommunicationLogRequest;
import com.reuben.pastcare_spring.dtos.CommunicationLogResponse;
import com.reuben.pastcare_spring.models.CommunicationType;
import com.reuben.pastcare_spring.models.FollowUpStatus;
import com.reuben.pastcare_spring.services.CommunicationLogService;
import com.reuben.pastcare_spring.util.RequestContextUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST Controller for managing communication logs.
 * Handles all communication interactions with members (calls, emails, visits, etc.).
 *
 * Phase 4: Lifecycle & Communication Tracking
 */
@RestController
@RequestMapping("/api/communication-logs")
public class CommunicationLogController {

    @Autowired
    private CommunicationLogService communicationLogService;

    @Autowired
    private RequestContextUtil requestContextUtil;

    /**
     * Create a new communication log.
     */
    @PostMapping
    public ResponseEntity<CommunicationLogResponse> createCommunicationLog(
            @Valid @RequestBody CommunicationLogRequest request,
            HttpServletRequest httpRequest) {

        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        Long userId = requestContextUtil.extractUserId(httpRequest);

        CommunicationLogResponse response = communicationLogService.createCommunicationLog(churchId, request, userId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Update an existing communication log.
     */
    @PutMapping("/{logId}")
    public ResponseEntity<CommunicationLogResponse> updateCommunicationLog(
            @PathVariable Long logId,
            @Valid @RequestBody CommunicationLogRequest request,
            HttpServletRequest httpRequest) {

        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        Long userId = requestContextUtil.extractUserId(httpRequest);

        CommunicationLogResponse response = communicationLogService.updateCommunicationLog(churchId, logId, request, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get a single communication log by ID.
     */
    @GetMapping("/{logId}")
    public ResponseEntity<CommunicationLogResponse> getCommunicationLogById(
            @PathVariable Long logId,
            HttpServletRequest request) {

        Long churchId = requestContextUtil.extractChurchId(request);
        CommunicationLogResponse response = communicationLogService.getCommunicationLogById(churchId, logId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all communication logs for a member.
     */
    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<CommunicationLogResponse>> getMemberCommunicationLogs(
            @PathVariable Long memberId,
            HttpServletRequest request) {

        Long churchId = requestContextUtil.extractChurchId(request);
        List<CommunicationLogResponse> logs = communicationLogService.getMemberCommunicationLogs(churchId, memberId);
        return ResponseEntity.ok(logs);
    }

    /**
     * Get all communication logs for the church with pagination.
     */
    @GetMapping
    public ResponseEntity<Page<CommunicationLogResponse>> getChurchCommunicationLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "communicationDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            HttpServletRequest request) {

        Long churchId = requestContextUtil.extractChurchId(request);
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<CommunicationLogResponse> logs = communicationLogService.getChurchCommunicationLogs(churchId, pageable);
        return ResponseEntity.ok(logs);
    }

    /**
     * Get communication logs by type.
     */
    @GetMapping("/type/{communicationType}")
    public ResponseEntity<Page<CommunicationLogResponse>> getCommunicationLogsByType(
            @PathVariable CommunicationType communicationType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        Long churchId = requestContextUtil.extractChurchId(request);
        Pageable pageable = PageRequest.of(page, size);

        Page<CommunicationLogResponse> logs = communicationLogService.getCommunicationLogsByType(churchId, communicationType, pageable);
        return ResponseEntity.ok(logs);
    }

    /**
     * Get communication logs within a date range.
     */
    @GetMapping("/date-range")
    public ResponseEntity<List<CommunicationLogResponse>> getCommunicationLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            HttpServletRequest request) {

        Long churchId = requestContextUtil.extractChurchId(request);
        List<CommunicationLogResponse> logs = communicationLogService.getCommunicationLogsByDateRange(churchId, startDate, endDate);
        return ResponseEntity.ok(logs);
    }

    /**
     * Get communication logs requiring follow-up.
     */
    @GetMapping("/follow-up/required")
    public ResponseEntity<List<CommunicationLogResponse>> getFollowUpRequired(HttpServletRequest request) {
        Long churchId = requestContextUtil.extractChurchId(request);
        List<CommunicationLogResponse> logs = communicationLogService.getFollowUpRequired(churchId);
        return ResponseEntity.ok(logs);
    }

    /**
     * Get overdue follow-ups.
     */
    @GetMapping("/follow-up/overdue")
    public ResponseEntity<List<CommunicationLogResponse>> getOverdueFollowUps(HttpServletRequest request) {
        Long churchId = requestContextUtil.extractChurchId(request);
        List<CommunicationLogResponse> logs = communicationLogService.getOverdueFollowUps(churchId);
        return ResponseEntity.ok(logs);
    }

    /**
     * Update follow-up status.
     */
    @PatchMapping("/{logId}/follow-up-status")
    public ResponseEntity<CommunicationLogResponse> updateFollowUpStatus(
            @PathVariable Long logId,
            @RequestParam FollowUpStatus status,
            HttpServletRequest request) {

        Long churchId = requestContextUtil.extractChurchId(request);
        CommunicationLogResponse response = communicationLogService.updateFollowUpStatus(churchId, logId, status);
        return ResponseEntity.ok(response);
    }

    /**
     * Get recent communications for a member.
     */
    @GetMapping("/member/{memberId}/recent")
    public ResponseEntity<List<CommunicationLogResponse>> getRecentCommunications(
            @PathVariable Long memberId,
            @RequestParam(defaultValue = "30") int days,
            HttpServletRequest request) {

        Long churchId = requestContextUtil.extractChurchId(request);
        List<CommunicationLogResponse> logs = communicationLogService.getRecentCommunications(churchId, memberId, days);
        return ResponseEntity.ok(logs);
    }

    /**
     * Delete a communication log.
     */
    @DeleteMapping("/{logId}")
    public ResponseEntity<Void> deleteCommunicationLog(
            @PathVariable Long logId,
            HttpServletRequest request) {

        Long churchId = requestContextUtil.extractChurchId(request);
        communicationLogService.deleteCommunicationLog(churchId, logId);
        return ResponseEntity.noContent().build();
    }
}
