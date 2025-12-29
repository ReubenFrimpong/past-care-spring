package com.reuben.pastcare_spring.controllers;

import com.reuben.pastcare_spring.annotations.RequirePermission;
import com.reuben.pastcare_spring.enums.Permission;

import com.reuben.pastcare_spring.dtos.AdvancedSearchResponse;
import com.reuben.pastcare_spring.dtos.SavedSearchRequest;
import com.reuben.pastcare_spring.dtos.SavedSearchResponse;
import com.reuben.pastcare_spring.services.SavedSearchService;
import com.reuben.pastcare_spring.util.RequestContextUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing saved member searches.
 * Provides endpoints for creating, retrieving, executing, and managing saved searches.
 */
@RestController
@RequestMapping("/api/saved-searches")
public class SavedSearchController {

    private final SavedSearchService savedSearchService;
    private final RequestContextUtil requestContextUtil;

    public SavedSearchController(SavedSearchService savedSearchService, RequestContextUtil requestContextUtil) {
        this.savedSearchService = savedSearchService;
        this.requestContextUtil = requestContextUtil;
    }

    /**
     * Get all accessible saved searches (public + user's private).
     *
     * @param httpRequest HTTP request to extract church and user IDs
     * @param page        Page number (default: 0)
     * @param size        Page size (default: 20)
     * @return Page of saved search responses
     */
    @RequirePermission(Permission.MEMBER_VIEW_ALL)
    @GetMapping
    public ResponseEntity<Page<SavedSearchResponse>> getAccessibleSearches(
            HttpServletRequest httpRequest,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        Long userId = requestContextUtil.extractUserId(httpRequest);

        Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());

        return ResponseEntity.ok(savedSearchService.getAccessibleSearches(churchId, userId, pageable));
    }

    /**
     * Get a single saved search by ID.
     *
     * @param id          Search ID
     * @param httpRequest HTTP request to extract church and user IDs
     * @return Saved search response
     */
    @RequirePermission(Permission.MEMBER_VIEW_ALL)
    @GetMapping("/{id}")
    public ResponseEntity<SavedSearchResponse> getSavedSearchById(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {

        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        Long userId = requestContextUtil.extractUserId(httpRequest);

        return ResponseEntity.ok(savedSearchService.getSavedSearchById(id, churchId, userId));
    }

    /**
     * Create a new saved search.
     *
     * @param request     Saved search request
     * @param httpRequest HTTP request to extract church and user IDs
     * @return Created saved search response
     */
    @RequirePermission(Permission.MEMBER_EDIT_ALL)
    @PostMapping
    public ResponseEntity<SavedSearchResponse> createSavedSearch(
            @Valid @RequestBody SavedSearchRequest request,
            HttpServletRequest httpRequest) {

        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        Long userId = requestContextUtil.extractUserId(httpRequest);

        return ResponseEntity.ok(savedSearchService.createSavedSearch(request, churchId, userId));
    }

    /**
     * Update an existing saved search.
     *
     * @param id          Search ID
     * @param request     Updated search request
     * @param httpRequest HTTP request to extract church and user IDs
     * @return Updated saved search response
     */
    @RequirePermission(Permission.MEMBER_EDIT_ALL)
    @PutMapping("/{id}")
    public ResponseEntity<SavedSearchResponse> updateSavedSearch(
            @PathVariable Long id,
            @Valid @RequestBody SavedSearchRequest request,
            HttpServletRequest httpRequest) {

        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        Long userId = requestContextUtil.extractUserId(httpRequest);

        return ResponseEntity.ok(savedSearchService.updateSavedSearch(id, request, churchId, userId));
    }

    /**
     * Delete a saved search.
     *
     * @param id          Search ID
     * @param httpRequest HTTP request to extract church and user IDs
     * @return No content response
     */
    @RequirePermission(Permission.MEMBER_EDIT_ALL)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSavedSearch(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {

        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        Long userId = requestContextUtil.extractUserId(httpRequest);

        savedSearchService.deleteSavedSearch(id, churchId, userId);

        return ResponseEntity.noContent().build();
    }

    /**
     * Execute a saved search and return member results.
     *
     * @param id          Search ID
     * @param httpRequest HTTP request to extract church and user IDs
     * @param page        Page number (default: 0)
     * @param size        Page size (default: 20)
     * @return Advanced search response with member results
     */
    @RequirePermission(Permission.MEMBER_EDIT_ALL)
    @PostMapping("/{id}/execute")
    public ResponseEntity<AdvancedSearchResponse> executeSavedSearch(
            @PathVariable Long id,
            HttpServletRequest httpRequest,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        Long userId = requestContextUtil.extractUserId(httpRequest);

        Pageable pageable = PageRequest.of(page, size, Sort.by("firstName").ascending());

        return ResponseEntity.ok(savedSearchService.executeSavedSearch(id, churchId, userId, pageable));
    }

    /**
     * Duplicate a saved search for editing.
     *
     * @param id          Search ID to duplicate
     * @param httpRequest HTTP request to extract church and user IDs
     * @return Duplicated saved search response
     */
    @RequirePermission(Permission.MEMBER_EDIT_ALL)
    @PostMapping("/{id}/duplicate")
    public ResponseEntity<SavedSearchResponse> duplicateSavedSearch(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {

        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        Long userId = requestContextUtil.extractUserId(httpRequest);

        return ResponseEntity.ok(savedSearchService.duplicateSavedSearch(id, churchId, userId));
    }
}
