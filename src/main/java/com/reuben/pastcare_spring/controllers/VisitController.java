package com.reuben.pastcare_spring.controllers;

import com.reuben.pastcare_spring.annotations.RequirePermission;
import com.reuben.pastcare_spring.enums.Permission;

import com.reuben.pastcare_spring.dtos.VisitRequest;
import com.reuben.pastcare_spring.dtos.VisitResponse;
import com.reuben.pastcare_spring.services.VisitService;
import com.reuben.pastcare_spring.util.RequestContextUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Pastoral Care Module Phase 1: Visit Management
 * REST API for managing pastoral visits
 */
@RestController
@RequestMapping("/api/visits")
@RequiredArgsConstructor
@Tag(name = "Visits", description = "Pastoral visit management endpoints")
public class VisitController {

    private final VisitService visitService;
    private final RequestContextUtil requestContextUtil;

    /**
     * Get all visits for the current church
     */
    @GetMapping
    @RequirePermission(Permission.VISIT_VIEW_ALL)
    @Operation(summary = "Get all visits", description = "Returns all visits for the current church")
    public ResponseEntity<List<VisitResponse>> getAllVisits(HttpServletRequest request) {
        Long churchId = requestContextUtil.extractChurchId(request);
        List<VisitResponse> visits = visitService.getAllVisits(churchId);
        return ResponseEntity.ok(visits);
    }

    /**
     * Get visit by ID
     */
    @GetMapping("/{id}")
    @RequirePermission(Permission.VISIT_VIEW_ALL)
    @Operation(summary = "Get visit by ID", description = "Returns a single visit by ID")
    public ResponseEntity<VisitResponse> getVisitById(@PathVariable Long id) {
        VisitResponse visit = visitService.getVisitById(id);
        return ResponseEntity.ok(visit);
    }

    /**
     * Create a new visit
     */
    @PostMapping
    @RequirePermission(Permission.VISIT_CREATE)
    @Operation(summary = "Create visit", description = "Creates a new pastoral visit")
    public ResponseEntity<VisitResponse> createVisit(
        @Valid @RequestBody VisitRequest request,
        HttpServletRequest httpRequest
    ) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        Long userId = requestContextUtil.extractUserId(httpRequest);
        VisitResponse createdVisit = visitService.createVisit(churchId, request, userId);
        return ResponseEntity.ok(createdVisit);
    }

    /**
     * Update an existing visit
     */
    @PutMapping("/{id}")
    @RequirePermission(Permission.VISIT_EDIT)
    @Operation(summary = "Update visit", description = "Updates an existing visit")
    public ResponseEntity<VisitResponse> updateVisit(
        @PathVariable Long id,
        @Valid @RequestBody VisitRequest request
    ) {
        VisitResponse updatedVisit = visitService.updateVisit(id, request);
        return ResponseEntity.ok(updatedVisit);
    }

    /**
     * Delete a visit
     */
    @DeleteMapping("/{id}")
    @RequirePermission(Permission.VISIT_EDIT)
    @Operation(summary = "Delete visit", description = "Deletes a visit")
    public ResponseEntity<Void> deleteVisit(@PathVariable Long id) {
        visitService.deleteVisit(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get visits by member
     */
    @GetMapping("/member/{memberId}")
    @RequirePermission(Permission.VISIT_VIEW_ALL)
    @Operation(summary = "Get visits by member", description = "Returns all visits for a specific member")
    public ResponseEntity<List<VisitResponse>> getVisitsByMember(
        @PathVariable Long memberId,
        HttpServletRequest request
    ) {
        Long churchId = requestContextUtil.extractChurchId(request);
        List<VisitResponse> visits = visitService.findByMember(churchId, memberId);
        return ResponseEntity.ok(visits);
    }

    /**
     * Get visits by care need
     */
    @GetMapping("/care-need/{careNeedId}")
    @RequirePermission(Permission.VISIT_VIEW_ALL)
    @Operation(summary = "Get visits by care need", description = "Returns all visits for a specific care need")
    public ResponseEntity<List<VisitResponse>> getVisitsByCareNeed(
        @PathVariable Long careNeedId,
        HttpServletRequest request
    ) {
        Long churchId = requestContextUtil.extractChurchId(request);
        List<VisitResponse> visits = visitService.findByCareNeed(churchId, careNeedId);
        return ResponseEntity.ok(visits);
    }

    /**
     * Get upcoming visits
     */
    @GetMapping("/upcoming")
    @RequirePermission(Permission.VISIT_VIEW_ALL)
    @Operation(summary = "Get upcoming visits", description = "Returns all upcoming visits")
    public ResponseEntity<List<VisitResponse>> getUpcomingVisits(HttpServletRequest request) {
        Long churchId = requestContextUtil.extractChurchId(request);
        List<VisitResponse> visits = visitService.findUpcoming(churchId);
        return ResponseEntity.ok(visits);
    }

    /**
     * Get past visits
     */
    @GetMapping("/past")
    @RequirePermission(Permission.VISIT_VIEW_ALL)
    @Operation(summary = "Get past visits", description = "Returns all past visits")
    public ResponseEntity<List<VisitResponse>> getPastVisits(HttpServletRequest request) {
        Long churchId = requestContextUtil.extractChurchId(request);
        List<VisitResponse> visits = visitService.findPast(churchId);
        return ResponseEntity.ok(visits);
    }

    /**
     * Get today's visits
     */
    @GetMapping("/today")
    @RequirePermission(Permission.VISIT_VIEW_ALL)
    @Operation(summary = "Get today's visits", description = "Returns all visits scheduled for today")
    public ResponseEntity<List<VisitResponse>> getTodaysVisits(HttpServletRequest request) {
        Long churchId = requestContextUtil.extractChurchId(request);
        List<VisitResponse> visits = visitService.findTodaysVisits(churchId);
        return ResponseEntity.ok(visits);
    }

    /**
     * Get visits by date range
     */
    @GetMapping("/date-range")
    @RequirePermission(Permission.VISIT_VIEW_ALL)
    @Operation(summary = "Get visits by date range", description = "Returns visits within a date range")
    public ResponseEntity<List<VisitResponse>> getVisitsByDateRange(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        HttpServletRequest request
    ) {
        Long churchId = requestContextUtil.extractChurchId(request);
        List<VisitResponse> visits = visitService.findByDateRange(churchId, startDate, endDate);
        return ResponseEntity.ok(visits);
    }

    /**
     * Mark a visit as completed
     */
    @PutMapping("/{id}/complete")
    @RequirePermission(Permission.VISIT_EDIT)
    @Operation(summary = "Mark visit as completed", description = "Marks a visit as completed with optional outcomes")
    public ResponseEntity<VisitResponse> completeVisit(
        @PathVariable Long id,
        @RequestBody(required = false) Map<String, String> payload
    ) {
        String outcomes = payload != null ? payload.get("outcomes") : null;
        VisitResponse visit = visitService.markAsCompleted(id, outcomes);
        return ResponseEntity.ok(visit);
    }

    /**
     * Get visits requiring follow-up
     */
    @GetMapping("/follow-up")
    @RequirePermission(Permission.VISIT_VIEW_ALL)
    @Operation(summary = "Get visits requiring follow-up", description = "Returns all visits that require follow-up")
    public ResponseEntity<List<VisitResponse>> getVisitsRequiringFollowUp(HttpServletRequest request) {
        Long churchId = requestContextUtil.extractChurchId(request);
        List<VisitResponse> visits = visitService.findRequiringFollowUp(churchId);
        return ResponseEntity.ok(visits);
    }
}
