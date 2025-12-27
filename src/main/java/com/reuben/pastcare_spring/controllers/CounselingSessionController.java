package com.reuben.pastcare_spring.controllers;

import com.reuben.pastcare_spring.dtos.CounselingSessionRequest;
import com.reuben.pastcare_spring.dtos.CounselingSessionResponse;
import com.reuben.pastcare_spring.dtos.CounselingSessionStatsResponse;
import com.reuben.pastcare_spring.models.CounselingStatus;
import com.reuben.pastcare_spring.models.CounselingType;
import com.reuben.pastcare_spring.models.SessionOutcome;
import com.reuben.pastcare_spring.services.CounselingSessionService;
import com.reuben.pastcare_spring.util.RequestContextUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST controller for counseling session management
 */
@RestController
@RequestMapping("/api/counseling-sessions")
@RequiredArgsConstructor
@Tag(name = "Counseling Sessions", description = "Counseling session management endpoints")
public class CounselingSessionController {

    private final CounselingSessionService counselingSessionService;
    private final RequestContextUtil requestContextUtil;

    /**
     * Create a new counseling session
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create counseling session", description = "Creates a new counseling session")
    public ResponseEntity<CounselingSessionResponse> createSession(
            @Valid @RequestBody CounselingSessionRequest request,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        CounselingSessionResponse session = counselingSessionService.createSession(churchId, request);
        return ResponseEntity.ok(session);
    }

    /**
     * Get counseling session by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get session by ID", description = "Returns a single counseling session by ID")
    public ResponseEntity<CounselingSessionResponse> getSessionById(@PathVariable Long id) {
        CounselingSessionResponse session = counselingSessionService.getSessionById(id);
        return ResponseEntity.ok(session);
    }

    /**
     * Get all counseling sessions with pagination
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all sessions", description = "Returns paginated list of counseling sessions")
    public ResponseEntity<Page<CounselingSessionResponse>> getSessions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "sessionDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<CounselingSessionResponse> sessions = counselingSessionService.getSessions(churchId, pageable);
        return ResponseEntity.ok(sessions);
    }

    /**
     * Update an existing counseling session
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update session", description = "Updates an existing counseling session")
    public ResponseEntity<CounselingSessionResponse> updateSession(
            @PathVariable Long id,
            @Valid @RequestBody CounselingSessionRequest request) {
        CounselingSessionResponse updated = counselingSessionService.updateSession(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete a counseling session
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete session", description = "Deletes a counseling session")
    public ResponseEntity<Void> deleteSession(@PathVariable Long id) {
        counselingSessionService.deleteSession(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Complete a counseling session
     */
    @PostMapping("/{id}/complete")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Complete session", description = "Marks a counseling session as completed with outcome")
    public ResponseEntity<CounselingSessionResponse> completeSession(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {
        String outcome = (String) payload.get("outcome");
        SessionOutcome sessionOutcome = payload.get("sessionOutcome") != null
            ? SessionOutcome.valueOf((String) payload.get("sessionOutcome"))
            : null;
        CounselingSessionResponse updated = counselingSessionService.completeSession(id, outcome, sessionOutcome);
        return ResponseEntity.ok(updated);
    }

    /**
     * Schedule follow-up for a session
     */
    @PostMapping("/{id}/follow-up")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Schedule follow-up", description = "Schedules a follow-up for a counseling session")
    public ResponseEntity<CounselingSessionResponse> scheduleFollowUp(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {
        LocalDateTime followUpDate = LocalDateTime.parse((String) payload.get("followUpDate"));
        String followUpNotes = (String) payload.get("followUpNotes");
        CounselingSessionResponse updated = counselingSessionService.scheduleFollowUp(id, followUpDate, followUpNotes);
        return ResponseEntity.ok(updated);
    }

    /**
     * Create referral for a session
     */
    @PostMapping("/{id}/referral")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create referral", description = "Creates a professional referral for a counseling session")
    public ResponseEntity<CounselingSessionResponse> createReferral(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        String referredTo = payload.get("referredTo");
        String referralOrganization = payload.get("referralOrganization");
        String referralPhone = payload.get("referralPhone");
        String referralNotes = payload.get("referralNotes");
        CounselingSessionResponse updated = counselingSessionService.createReferral(
            id, referredTo, referralOrganization, referralPhone, referralNotes);
        return ResponseEntity.ok(updated);
    }

    /**
     * Get counseling sessions for the current user (counselor)
     */
    @GetMapping("/my-sessions")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get my sessions", description = "Returns counseling sessions for the current user as counselor")
    public ResponseEntity<Page<CounselingSessionResponse>> getMySessions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        Long userId = requestContextUtil.extractUserId(httpRequest);
        Pageable pageable = PageRequest.of(page, size, Sort.by("sessionDate").ascending());
        Page<CounselingSessionResponse> sessions = counselingSessionService.getSessionsByCounselor(churchId, userId, pageable);
        return ResponseEntity.ok(sessions);
    }

    /**
     * Get sessions by counselor
     */
    @GetMapping("/counselor/{counselorId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get by counselor", description = "Returns sessions for a specific counselor")
    public ResponseEntity<Page<CounselingSessionResponse>> getSessionsByCounselor(
            @PathVariable Long counselorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        Pageable pageable = PageRequest.of(page, size, Sort.by("sessionDate").descending());
        Page<CounselingSessionResponse> sessions = counselingSessionService.getSessionsByCounselor(churchId, counselorId, pageable);
        return ResponseEntity.ok(sessions);
    }

    /**
     * Get sessions by status
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get by status", description = "Returns sessions filtered by status")
    public ResponseEntity<Page<CounselingSessionResponse>> getSessionsByStatus(
            @PathVariable CounselingStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        Pageable pageable = PageRequest.of(page, size, Sort.by("sessionDate").descending());
        Page<CounselingSessionResponse> sessions = counselingSessionService.getSessionsByStatus(churchId, status, pageable);
        return ResponseEntity.ok(sessions);
    }

    /**
     * Get sessions by type
     */
    @GetMapping("/type/{type}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get by type", description = "Returns sessions filtered by type")
    public ResponseEntity<Page<CounselingSessionResponse>> getSessionsByType(
            @PathVariable CounselingType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        Pageable pageable = PageRequest.of(page, size, Sort.by("sessionDate").descending());
        Page<CounselingSessionResponse> sessions = counselingSessionService.getSessionsByType(churchId, type, pageable);
        return ResponseEntity.ok(sessions);
    }

    /**
     * Get upcoming sessions
     */
    @GetMapping("/upcoming")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get upcoming sessions", description = "Returns upcoming scheduled sessions")
    public ResponseEntity<List<CounselingSessionResponse>> getUpcomingSessions(HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        List<CounselingSessionResponse> sessions = counselingSessionService.getUpcomingSessions(churchId);
        return ResponseEntity.ok(sessions);
    }

    /**
     * Get upcoming sessions for current user (counselor)
     */
    @GetMapping("/my-upcoming")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get my upcoming sessions", description = "Returns upcoming sessions for the current counselor")
    public ResponseEntity<List<CounselingSessionResponse>> getMyUpcomingSessions(HttpServletRequest httpRequest) {
        Long userId = requestContextUtil.extractUserId(httpRequest);
        List<CounselingSessionResponse> sessions = counselingSessionService.getUpcomingSessionsByCounselor(userId);
        return ResponseEntity.ok(sessions);
    }

    /**
     * Get sessions requiring follow-up
     */
    @GetMapping("/follow-ups")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get follow-ups needed", description = "Returns sessions requiring follow-up")
    public ResponseEntity<List<CounselingSessionResponse>> getSessionsRequiringFollowUp(HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        List<CounselingSessionResponse> sessions = counselingSessionService.getSessionsRequiringFollowUp(churchId);
        return ResponseEntity.ok(sessions);
    }

    /**
     * Get sessions by member
     */
    @GetMapping("/member/{memberId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get by member", description = "Returns sessions for a specific member")
    public ResponseEntity<List<CounselingSessionResponse>> getSessionsByMember(
            @PathVariable Long memberId,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        List<CounselingSessionResponse> sessions = counselingSessionService.getSessionsByMember(churchId, memberId);
        return ResponseEntity.ok(sessions);
    }

    /**
     * Get sessions by care need
     */
    @GetMapping("/care-need/{careNeedId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get by care need", description = "Returns sessions for a specific care need")
    public ResponseEntity<List<CounselingSessionResponse>> getSessionsByCareNeed(
            @PathVariable Long careNeedId,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        List<CounselingSessionResponse> sessions = counselingSessionService.getSessionsByCareNeed(churchId, careNeedId);
        return ResponseEntity.ok(sessions);
    }

    /**
     * Search sessions
     */
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Search sessions", description = "Searches sessions by title or notes")
    public ResponseEntity<Page<CounselingSessionResponse>> searchSessions(
            @RequestParam String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        Pageable pageable = PageRequest.of(page, size, Sort.by("sessionDate").descending());
        Page<CounselingSessionResponse> sessions = counselingSessionService.searchSessions(churchId, search, pageable);
        return ResponseEntity.ok(sessions);
    }

    /**
     * Get session statistics
     */
    @GetMapping("/stats")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get statistics", description = "Returns counseling session statistics")
    public ResponseEntity<CounselingSessionStatsResponse> getSessionStats(HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        CounselingSessionStatsResponse stats = counselingSessionService.getSessionStats(churchId);
        return ResponseEntity.ok(stats);
    }
}
