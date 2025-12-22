package com.reuben.pastcare_spring.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reuben.pastcare_spring.dtos.AdvancedSearchRequest;
import com.reuben.pastcare_spring.dtos.AdvancedSearchResponse;
import com.reuben.pastcare_spring.dtos.SavedSearchRequest;
import com.reuben.pastcare_spring.dtos.SavedSearchResponse;
import com.reuben.pastcare_spring.models.Church;
import com.reuben.pastcare_spring.models.SavedSearch;
import com.reuben.pastcare_spring.models.User;
import com.reuben.pastcare_spring.repositories.ChurchRepository;
import com.reuben.pastcare_spring.repositories.SavedSearchRepository;
import com.reuben.pastcare_spring.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Service for managing saved member searches.
 * Handles creation, retrieval, execution, and deletion of saved searches.
 */
@Service
public class SavedSearchService {

    @Autowired
    private SavedSearchRepository savedSearchRepository;

    @Autowired
    private ChurchRepository churchRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MemberService memberService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Create a new saved search.
     *
     * @param request  Saved search request
     * @param churchId Church ID
     * @param userId   User ID (creator)
     * @return Created saved search response
     */
    @Transactional
    public SavedSearchResponse createSavedSearch(SavedSearchRequest request, Long churchId, Long userId) {
        // Validate church and user
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new IllegalArgumentException("Invalid church ID"));

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));

        // Validate search criteria JSON
        validateSearchCriteria(request.searchCriteria());

        // Create saved search entity
        SavedSearch savedSearch = SavedSearch.builder()
            .church(church)
            .createdBy(user)
            .searchName(request.searchName())
            .searchCriteria(request.searchCriteria())
            .isPublic(request.isPublic())
            .isDynamic(request.isDynamic())
            .description(request.description())
            .build();

        savedSearch = savedSearchRepository.save(savedSearch);

        return toResponse(savedSearch, userId);
    }

    /**
     * Get all accessible saved searches for a user (public + their private).
     *
     * @param churchId Church ID
     * @param userId   User ID
     * @param pageable Pagination parameters
     * @return Page of saved search responses
     */
    public Page<SavedSearchResponse> getAccessibleSearches(Long churchId, Long userId, Pageable pageable) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new IllegalArgumentException("Invalid church ID"));

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));

        Page<SavedSearch> searches = savedSearchRepository.findAccessibleSearches(church, user, pageable);

        return searches.map(search -> toResponse(search, userId));
    }

    /**
     * Get a single saved search by ID.
     *
     * @param id       Search ID
     * @param churchId Church ID (for security)
     * @param userId   User ID (for permission checks)
     * @return Saved search response
     */
    public SavedSearchResponse getSavedSearchById(Long id, Long churchId, Long userId) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new IllegalArgumentException("Invalid church ID"));

        SavedSearch search = savedSearchRepository.findByIdAndChurch(id, church)
            .orElseThrow(() -> new IllegalArgumentException("Saved search not found"));

        // Check if user has access to this search
        if (!search.getIsPublic() && !search.getCreatedBy().getId().equals(userId)) {
            throw new IllegalArgumentException("You do not have permission to access this search");
        }

        return toResponse(search, userId);
    }

    /**
     * Update an existing saved search.
     *
     * @param id       Search ID
     * @param request  Updated search request
     * @param churchId Church ID
     * @param userId   User ID (must be creator)
     * @return Updated saved search response
     */
    @Transactional
    public SavedSearchResponse updateSavedSearch(Long id, SavedSearchRequest request, Long churchId, Long userId) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new IllegalArgumentException("Invalid church ID"));

        SavedSearch search = savedSearchRepository.findByIdAndChurch(id, church)
            .orElseThrow(() -> new IllegalArgumentException("Saved search not found"));

        // Check if user is the creator
        if (!search.getCreatedBy().getId().equals(userId)) {
            throw new IllegalArgumentException("Only the creator can update this search");
        }

        // Validate search criteria JSON
        validateSearchCriteria(request.searchCriteria());

        // Update fields
        search.setSearchName(request.searchName());
        search.setSearchCriteria(request.searchCriteria());
        search.setIsPublic(request.isPublic());
        search.setIsDynamic(request.isDynamic());
        search.setDescription(request.description());

        search = savedSearchRepository.save(search);

        return toResponse(search, userId);
    }

    /**
     * Delete a saved search.
     *
     * @param id       Search ID
     * @param churchId Church ID
     * @param userId   User ID (must be creator or admin)
     */
    @Transactional
    public void deleteSavedSearch(Long id, Long churchId, Long userId) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new IllegalArgumentException("Invalid church ID"));

        SavedSearch search = savedSearchRepository.findByIdAndChurch(id, church)
            .orElseThrow(() -> new IllegalArgumentException("Saved search not found"));

        // Check if user is the creator
        if (!search.getCreatedBy().getId().equals(userId)) {
            // TODO: Add admin role check when roles are implemented
            throw new IllegalArgumentException("Only the creator can delete this search");
        }

        savedSearchRepository.delete(search);
    }

    /**
     * Execute a saved search and return results.
     *
     * @param id       Search ID
     * @param churchId Church ID
     * @param userId   User ID (for access check)
     * @param pageable Pagination parameters
     * @return Advanced search response with member results
     */
    @Transactional
    public AdvancedSearchResponse executeSavedSearch(Long id, Long churchId, Long userId, Pageable pageable) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new IllegalArgumentException("Invalid church ID"));

        SavedSearch search = savedSearchRepository.findByIdAndChurch(id, church)
            .orElseThrow(() -> new IllegalArgumentException("Saved search not found"));

        // Check if user has access to this search
        if (!search.getIsPublic() && !search.getCreatedBy().getId().equals(userId)) {
            throw new IllegalArgumentException("You do not have permission to execute this search");
        }

        // Parse search criteria JSON
        AdvancedSearchRequest searchRequest;
        try {
            searchRequest = objectMapper.readValue(search.getSearchCriteria(), AdvancedSearchRequest.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid search criteria format", e);
        }

        // Execute the search
        AdvancedSearchResponse response = memberService.advancedSearch(searchRequest, churchId, pageable);

        // Update last executed stats
        search.setLastExecuted(LocalDateTime.now());
        search.setLastResultCount(response.members().getTotalElements());
        savedSearchRepository.save(search);

        return response;
    }

    /**
     * Duplicate a saved search for editing.
     *
     * @param id       Search ID to duplicate
     * @param churchId Church ID
     * @param userId   User ID (creator of new search)
     * @return Duplicated saved search response
     */
    @Transactional
    public SavedSearchResponse duplicateSavedSearch(Long id, Long churchId, Long userId) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new IllegalArgumentException("Invalid church ID"));

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));

        SavedSearch original = savedSearchRepository.findByIdAndChurch(id, church)
            .orElseThrow(() -> new IllegalArgumentException("Saved search not found"));

        // Check if user has access to this search
        if (!original.getIsPublic() && !original.getCreatedBy().getId().equals(userId)) {
            throw new IllegalArgumentException("You do not have permission to duplicate this search");
        }

        // Create duplicate
        SavedSearch duplicate = SavedSearch.builder()
            .church(church)
            .createdBy(user)
            .searchName(original.getSearchName() + " (Copy)")
            .searchCriteria(original.getSearchCriteria())
            .isPublic(false) // Always create as private
            .isDynamic(original.getIsDynamic())
            .description(original.getDescription())
            .build();

        duplicate = savedSearchRepository.save(duplicate);

        return toResponse(duplicate, userId);
    }

    /**
     * Validate search criteria JSON format.
     *
     * @param searchCriteria JSON string to validate
     */
    private void validateSearchCriteria(String searchCriteria) {
        try {
            objectMapper.readValue(searchCriteria, AdvancedSearchRequest.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid search criteria format: " + e.getMessage());
        }
    }

    /**
     * Convert SavedSearch entity to response DTO.
     *
     * @param search SavedSearch entity
     * @param userId Current user ID (for permission checks)
     * @return SavedSearchResponse DTO
     */
    private SavedSearchResponse toResponse(SavedSearch search, Long userId) {
        User creator = search.getCreatedBy();

        // Calculate "time ago" string
        String lastExecutedAgo = calculateTimeAgo(search.getLastExecuted());

        // Determine permissions
        boolean canEdit = search.getCreatedBy().getId().equals(userId);
        boolean canDelete = search.getCreatedBy().getId().equals(userId);
        // TODO: Add admin check when roles are implemented

        return new SavedSearchResponse(
            search.getId(),
            search.getSearchName(),
            search.getSearchCriteria(),
            search.getIsPublic(),
            search.getIsDynamic(),
            search.getDescription(),
            search.getLastExecuted(),
            search.getLastResultCount(),
            lastExecutedAgo,
            new SavedSearchResponse.CreatorInfo(
                creator.getId(),
                creator.getName(),
                creator.getEmail()
            ),
            search.getCreatedAt(),
            search.getUpdatedAt(),
            canEdit,
            canDelete
        );
    }

    /**
     * Calculate human-readable time ago string.
     *
     * @param dateTime DateTime to calculate from (null = "Never")
     * @return Human-readable string like "2 days ago"
     */
    private String calculateTimeAgo(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "Never";
        }

        Duration duration = Duration.between(dateTime, LocalDateTime.now());
        long seconds = duration.getSeconds();

        if (seconds < 60) {
            return "Just now";
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
        } else if (seconds < 86400) {
            long hours = seconds / 3600;
            return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        } else if (seconds < 2592000) {
            long days = seconds / 86400;
            return days + " day" + (days > 1 ? "s" : "") + " ago";
        } else if (seconds < 31536000) {
            long months = seconds / 2592000;
            return months + " month" + (months > 1 ? "s" : "") + " ago";
        } else {
            long years = seconds / 31536000;
            return years + " year" + (years > 1 ? "s" : "") + " ago";
        }
    }
}
