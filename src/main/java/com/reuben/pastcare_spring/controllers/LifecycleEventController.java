package com.reuben.pastcare_spring.controllers;

import com.reuben.pastcare_spring.dtos.LifecycleEventRequest;
import com.reuben.pastcare_spring.dtos.LifecycleEventResponse;
import com.reuben.pastcare_spring.models.LifecycleEventType;
import com.reuben.pastcare_spring.services.LifecycleEventService;
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

import java.time.LocalDate;
import java.util.List;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;


/**
 * REST Controller for managing lifecycle events.
 * Handles baptisms, confirmations, memberships, and other spiritual milestones.
 *
 * Phase 4: Lifecycle & Communication Tracking
 */
@RestController
@RequestMapping("/api/lifecycle-events")
public class LifecycleEventController {

    @Autowired
    private LifecycleEventService lifecycleEventService;

    @Autowired
    private RequestContextUtil requestContextUtil;

    /**
     * Create a new lifecycle event.
     */
    @PostMapping
    public ResponseEntity<LifecycleEventResponse> createLifecycleEvent(
            @Valid @RequestBody LifecycleEventRequest request,
            HttpServletRequest httpRequest) {

        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        Long userId = requestContextUtil.extractUserId(httpRequest);

        LifecycleEventResponse response = lifecycleEventService.createLifecycleEvent(churchId, request, userId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Update an existing lifecycle event.
     */
    @PutMapping("/{eventId}")
    public ResponseEntity<LifecycleEventResponse> updateLifecycleEvent(
            @PathVariable Long eventId,
            @Valid @RequestBody LifecycleEventRequest request,
            HttpServletRequest httpRequest) {

        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        Long userId = requestContextUtil.extractUserId(httpRequest);

        LifecycleEventResponse response = lifecycleEventService.updateLifecycleEvent(churchId, eventId, request, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all lifecycle events for a member.
     */
    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<LifecycleEventResponse>> getMemberLifecycleEvents(
            @PathVariable Long memberId,
            HttpServletRequest request) {

        Long churchId = requestContextUtil.extractChurchId(request);
        List<LifecycleEventResponse> events = lifecycleEventService.getMemberLifecycleEvents(churchId, memberId);
        return ResponseEntity.ok(events);
    }

    /**
     * Get all lifecycle events for the church with pagination.
     */
    @GetMapping
    public ResponseEntity<Page<LifecycleEventResponse>> getChurchLifecycleEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "eventDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            HttpServletRequest request) {

        Long churchId = requestContextUtil.extractChurchId(request);
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<LifecycleEventResponse> events = lifecycleEventService.getChurchLifecycleEvents(churchId, pageable);
        return ResponseEntity.ok(events);
    }

    /**
     * Get lifecycle events by type.
     */
    @GetMapping("/type/{eventType}")
    public ResponseEntity<Page<LifecycleEventResponse>> getLifecycleEventsByType(
            @PathVariable LifecycleEventType eventType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        Long churchId = requestContextUtil.extractChurchId(request);
        Pageable pageable = PageRequest.of(page, size);

        Page<LifecycleEventResponse> events = lifecycleEventService.getLifecycleEventsByType(churchId, eventType, pageable);
        return ResponseEntity.ok(events);
    }

    /**
     * Get lifecycle events within a date range.
     */
    @GetMapping("/date-range")
    public ResponseEntity<List<LifecycleEventResponse>> getLifecycleEventsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletRequest request) {

        Long churchId = requestContextUtil.extractChurchId(request);
        List<LifecycleEventResponse> events = lifecycleEventService.getLifecycleEventsByDateRange(churchId, startDate, endDate);
        return ResponseEntity.ok(events);
    }

    /**
     * Verify a lifecycle event.
     */
    @PatchMapping("/{eventId}/verify")
    public ResponseEntity<LifecycleEventResponse> verifyLifecycleEvent(
            @PathVariable Long eventId,
            HttpServletRequest httpRequest) {

        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        Long userId = requestContextUtil.extractUserId(httpRequest);

        LifecycleEventResponse response = lifecycleEventService.verifyLifecycleEvent(churchId, eventId, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a lifecycle event.
     */
    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteLifecycleEvent(
            @PathVariable Long eventId,
            HttpServletRequest request) {

        Long churchId = requestContextUtil.extractChurchId(request);
        lifecycleEventService.deleteLifecycleEvent(churchId, eventId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Check if a member has a specific lifecycle event type.
     */
    @GetMapping("/member/{memberId}/has-event/{eventType}")
    public ResponseEntity<Boolean> memberHasLifecycleEvent(
            @PathVariable Long memberId,
            @PathVariable LifecycleEventType eventType,
            HttpServletRequest request) {

        Long churchId = requestContextUtil.extractChurchId(request);
        boolean hasEvent = lifecycleEventService.memberHasLifecycleEvent(churchId, memberId, eventType);
        return ResponseEntity.ok(hasEvent);
    }

    /**
     * Verify a lifecycle event
     */
    @PutMapping("/{eventId}/verify")
    public ResponseEntity<LifecycleEventResponse> verifyLifeCycleEvent(@PathVariable Long eventId, HttpServletRequest request) {
      Long churchId = requestContextUtil.extractChurchId(request);
      Long userId = requestContextUtil.extractUserId(request);
        
      return ResponseEntity.ok(lifecycleEventService.verifyLifecycleEvent(churchId, eventId, userId));
    }
}
