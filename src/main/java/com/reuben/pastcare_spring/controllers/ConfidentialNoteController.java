package com.reuben.pastcare_spring.controllers;

import com.reuben.pastcare_spring.dtos.ConfidentialNoteRequest;
import com.reuben.pastcare_spring.dtos.ConfidentialNoteResponse;
import com.reuben.pastcare_spring.models.ConfidentialNoteCategory;
import com.reuben.pastcare_spring.models.FollowUpStatus;
import com.reuben.pastcare_spring.services.ConfidentialNoteService;
import com.reuben.pastcare_spring.util.RequestContextUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing confidential notes about members.
 * Handles role-based access control for sensitive pastoral care information.
 *
 * Phase 4: Lifecycle & Communication Tracking
 */
@RestController
@RequestMapping("/api/confidential-notes")
public class ConfidentialNoteController {

    @Autowired
    private ConfidentialNoteService confidentialNoteService;

    @Autowired
    private RequestContextUtil requestContextUtil;

    /**
     * Create a new confidential note.
     */
    @PostMapping
    public ResponseEntity<ConfidentialNoteResponse> createConfidentialNote(
            @Valid @RequestBody ConfidentialNoteRequest request,
            HttpServletRequest httpRequest) {

        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        Long userId = requestContextUtil.extractUserId(httpRequest);

        ConfidentialNoteResponse response = confidentialNoteService.createConfidentialNote(churchId, request, userId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Update an existing confidential note.
     */
    @PutMapping("/{noteId}")
    public ResponseEntity<ConfidentialNoteResponse> updateConfidentialNote(
            @PathVariable Long noteId,
            @Valid @RequestBody ConfidentialNoteRequest request,
            HttpServletRequest httpRequest) {

        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        Long userId = requestContextUtil.extractUserId(httpRequest);

        ConfidentialNoteResponse response = confidentialNoteService.updateConfidentialNote(churchId, noteId, request, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get a single confidential note by ID.
     */
    @GetMapping("/{noteId}")
    public ResponseEntity<ConfidentialNoteResponse> getConfidentialNoteById(
            @PathVariable Long noteId,
            HttpServletRequest request) {

        Long churchId = requestContextUtil.extractChurchId(request);
        ConfidentialNoteResponse response = confidentialNoteService.getConfidentialNoteById(churchId, noteId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all confidential notes for a member (non-archived).
     */
    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<ConfidentialNoteResponse>> getMemberConfidentialNotes(
            @PathVariable Long memberId,
            HttpServletRequest request) {

        Long churchId = requestContextUtil.extractChurchId(request);
        List<ConfidentialNoteResponse> notes = confidentialNoteService.getMemberConfidentialNotes(churchId, memberId);
        return ResponseEntity.ok(notes);
    }

    /**
     * Get all confidential notes for the church with pagination.
     */
    @GetMapping
    public ResponseEntity<Page<ConfidentialNoteResponse>> getChurchConfidentialNotes(
            @RequestParam(required = false) Boolean includeArchived,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            HttpServletRequest request) {

        Long churchId = requestContextUtil.extractChurchId(request);
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ConfidentialNoteResponse> notes = confidentialNoteService.getChurchConfidentialNotes(churchId, includeArchived, pageable);
        return ResponseEntity.ok(notes);
    }

    /**
     * Get confidential notes by category.
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<Page<ConfidentialNoteResponse>> getConfidentialNotesByCategory(
            @PathVariable ConfidentialNoteCategory category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        Long churchId = requestContextUtil.extractChurchId(request);
        Pageable pageable = PageRequest.of(page, size);

        Page<ConfidentialNoteResponse> notes = confidentialNoteService.getConfidentialNotesByCategory(churchId, category, pageable);
        return ResponseEntity.ok(notes);
    }

    /**
     * Search confidential notes by subject or tags.
     */
    @GetMapping("/search")
    public ResponseEntity<Page<ConfidentialNoteResponse>> searchNotes(
            @RequestParam String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        Long churchId = requestContextUtil.extractChurchId(request);
        Pageable pageable = PageRequest.of(page, size);

        Page<ConfidentialNoteResponse> notes = confidentialNoteService.searchNotes(churchId, search, pageable);
        return ResponseEntity.ok(notes);
    }

    /**
     * Get confidential notes requiring follow-up.
     */
    @GetMapping("/follow-up/required")
    public ResponseEntity<List<ConfidentialNoteResponse>> getFollowUpRequired(HttpServletRequest request) {
        Long churchId = requestContextUtil.extractChurchId(request);
        List<ConfidentialNoteResponse> notes = confidentialNoteService.getFollowUpRequired(churchId);
        return ResponseEntity.ok(notes);
    }

    /**
     * Get overdue follow-ups for confidential notes.
     */
    @GetMapping("/follow-up/overdue")
    public ResponseEntity<List<ConfidentialNoteResponse>> getOverdueFollowUps(HttpServletRequest request) {
        Long churchId = requestContextUtil.extractChurchId(request);
        List<ConfidentialNoteResponse> notes = confidentialNoteService.getOverdueFollowUps(churchId);
        return ResponseEntity.ok(notes);
    }

    /**
     * Get high priority confidential notes.
     */
    @GetMapping("/priority/high")
    public ResponseEntity<List<ConfidentialNoteResponse>> getHighPriorityNotes(HttpServletRequest request) {
        Long churchId = requestContextUtil.extractChurchId(request);
        List<ConfidentialNoteResponse> notes = confidentialNoteService.getHighPriorityNotes(churchId);
        return ResponseEntity.ok(notes);
    }

    /**
     * Archive a confidential note.
     */
    @PatchMapping("/{noteId}/archive")
    public ResponseEntity<ConfidentialNoteResponse> archiveNote(
            @PathVariable Long noteId,
            HttpServletRequest request) {

        Long churchId = requestContextUtil.extractChurchId(request);
        ConfidentialNoteResponse response = confidentialNoteService.archiveNote(churchId, noteId);
        return ResponseEntity.ok(response);
    }

    /**
     * Unarchive a confidential note.
     */
    @PatchMapping("/{noteId}/unarchive")
    public ResponseEntity<ConfidentialNoteResponse> unarchiveNote(
            @PathVariable Long noteId,
            HttpServletRequest request) {

        Long churchId = requestContextUtil.extractChurchId(request);
        ConfidentialNoteResponse response = confidentialNoteService.unarchiveNote(churchId, noteId);
        return ResponseEntity.ok(response);
    }

    /**
     * Update follow-up status.
     */
    @PatchMapping("/{noteId}/follow-up-status")
    public ResponseEntity<ConfidentialNoteResponse> updateFollowUpStatus(
            @PathVariable Long noteId,
            @RequestParam FollowUpStatus status,
            HttpServletRequest request) {

        Long churchId = requestContextUtil.extractChurchId(request);
        ConfidentialNoteResponse response = confidentialNoteService.updateFollowUpStatus(churchId, noteId, status);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a confidential note (hard delete).
     */
    @DeleteMapping("/{noteId}")
    public ResponseEntity<Void> deleteConfidentialNote(
            @PathVariable Long noteId,
            HttpServletRequest request) {

        Long churchId = requestContextUtil.extractChurchId(request);
        confidentialNoteService.deleteConfidentialNote(churchId, noteId);
        return ResponseEntity.noContent().build();
    }
}
