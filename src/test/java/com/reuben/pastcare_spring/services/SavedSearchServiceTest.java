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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SavedSearchService.
 * Tests CRUD operations, permissions, and search execution.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Saved Search Service Tests")
class SavedSearchServiceTest {

    @Mock
    private SavedSearchRepository savedSearchRepository;

    @Mock
    private ChurchRepository churchRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MemberService memberService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private SavedSearchService savedSearchService;

    private Church church;
    private User user1;
    private User user2;
    private SavedSearch savedSearch;
    private SavedSearchRequest savedSearchRequest;

    @BeforeEach
    void setUp() {
        // Setup test data
        church = new Church();
        church.setId(1L);
        church.setName("Test Church");

        user1 = new User();
        user1.setId(1L);
        user1.setName("John Doe");
        user1.setEmail("john@example.com");
        user1.setChurch(church);

        user2 = new User();
        user2.setId(2L);
        user2.setName("Jane Smith");
        user2.setEmail("jane@example.com");
        user2.setChurch(church);

        String searchCriteria = "{\"filterGroups\":[]}";

        savedSearch = SavedSearch.builder()
            .id(1L)
            .church(church)
            .createdBy(user1)
            .searchName("Active Members")
            .searchCriteria(searchCriteria)
            .isPublic(true)
            .isDynamic(false)
            .description("Search for all active members")
            .build();
        savedSearch.setCreatedAt(LocalDateTime.now().minusDays(7));
        savedSearch.setUpdatedAt(LocalDateTime.now().minusDays(7));

        savedSearchRequest = new SavedSearchRequest(
            "Active Members",
            searchCriteria,
            true,
            false,
            "Search for all active members"
        );
    }

    // ========== Create Saved Search ==========

    @Test
    @DisplayName("Should create saved search successfully")
    void testCreateSavedSearch() throws JsonProcessingException {
        // Given
        when(churchRepository.findById(1L)).thenReturn(Optional.of(church));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(objectMapper.readValue(anyString(), eq(AdvancedSearchRequest.class)))
            .thenReturn(new AdvancedSearchRequest(new ArrayList<>(), AdvancedSearchRequest.LogicalOperator.AND));
        when(savedSearchRepository.save(any(SavedSearch.class))).thenReturn(savedSearch);

        // When
        SavedSearchResponse response = savedSearchService.createSavedSearch(savedSearchRequest, 1L, 1L);

        // Then
        assertNotNull(response);
        assertEquals("Active Members", response.searchName());
        assertEquals(true, response.isPublic());
        assertEquals(true, response.canEdit());
        assertEquals(true, response.canDelete());
        verify(savedSearchRepository).save(any(SavedSearch.class));
    }

    @Test
    @DisplayName("Should throw exception when church not found")
    void testCreateSavedSearch_ChurchNotFound() {
        // Given
        when(churchRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            savedSearchService.createSavedSearch(savedSearchRequest, 1L, 1L);
        });
        verify(savedSearchRepository, never()).save(any(SavedSearch.class));
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void testCreateSavedSearch_UserNotFound() {
        // Given
        when(churchRepository.findById(1L)).thenReturn(Optional.of(church));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            savedSearchService.createSavedSearch(savedSearchRequest, 1L, 1L);
        });
        verify(savedSearchRepository, never()).save(any(SavedSearch.class));
    }

    @Test
    @DisplayName("Should throw exception when search criteria is invalid JSON")
    void testCreateSavedSearch_InvalidSearchCriteria() throws JsonProcessingException {
        // Given
        when(churchRepository.findById(1L)).thenReturn(Optional.of(church));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(objectMapper.readValue(anyString(), eq(AdvancedSearchRequest.class)))
            .thenThrow(new JsonProcessingException("Invalid JSON") {});

        SavedSearchRequest invalidRequest = new SavedSearchRequest(
            "Invalid Search",
            "invalid json",
            false,
            false,
            null
        );

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            savedSearchService.createSavedSearch(invalidRequest, 1L, 1L);
        });
        verify(savedSearchRepository, never()).save(any(SavedSearch.class));
    }

    // ========== Get Accessible Searches ==========

    @Test
    @DisplayName("Should get accessible searches for user")
    void testGetAccessibleSearches() {
        // Given
        List<SavedSearch> searches = List.of(savedSearch);
        Page<SavedSearch> searchPage = new PageImpl<>(searches);
        Pageable pageable = PageRequest.of(0, 20);

        when(churchRepository.findById(1L)).thenReturn(Optional.of(church));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(savedSearchRepository.findAccessibleSearches(church, user1, pageable))
            .thenReturn(searchPage);

        // When
        Page<SavedSearchResponse> response = savedSearchService.getAccessibleSearches(1L, 1L, pageable);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals("Active Members", response.getContent().get(0).searchName());
        verify(savedSearchRepository).findAccessibleSearches(church, user1, pageable);
    }

    // ========== Get Saved Search By ID ==========

    @Test
    @DisplayName("Should get public saved search by ID")
    void testGetSavedSearchById_Public() {
        // Given
        when(churchRepository.findById(1L)).thenReturn(Optional.of(church));
        when(savedSearchRepository.findByIdAndChurch(1L, church))
            .thenReturn(Optional.of(savedSearch));

        // When
        SavedSearchResponse response = savedSearchService.getSavedSearchById(1L, 1L, 2L);

        // Then
        assertNotNull(response);
        assertEquals("Active Members", response.searchName());
        assertEquals(false, response.canEdit()); // user2 cannot edit user1's search
        assertEquals(false, response.canDelete()); // user2 cannot delete user1's search
    }

    @Test
    @DisplayName("Should get private saved search by creator")
    void testGetSavedSearchById_PrivateByCreator() {
        // Given
        savedSearch.setIsPublic(false);
        when(churchRepository.findById(1L)).thenReturn(Optional.of(church));
        when(savedSearchRepository.findByIdAndChurch(1L, church))
            .thenReturn(Optional.of(savedSearch));

        // When
        SavedSearchResponse response = savedSearchService.getSavedSearchById(1L, 1L, 1L);

        // Then
        assertNotNull(response);
        assertEquals("Active Members", response.searchName());
        assertEquals(true, response.canEdit()); // creator can edit
        assertEquals(true, response.canDelete()); // creator can delete
    }

    @Test
    @DisplayName("Should throw exception when non-creator tries to access private search")
    void testGetSavedSearchById_PrivateByNonCreator() {
        // Given
        savedSearch.setIsPublic(false);
        when(churchRepository.findById(1L)).thenReturn(Optional.of(church));
        when(savedSearchRepository.findByIdAndChurch(1L, church))
            .thenReturn(Optional.of(savedSearch));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            savedSearchService.getSavedSearchById(1L, 1L, 2L);
        });
    }

    @Test
    @DisplayName("Should throw exception when saved search not found")
    void testGetSavedSearchById_NotFound() {
        // Given
        when(churchRepository.findById(1L)).thenReturn(Optional.of(church));
        when(savedSearchRepository.findByIdAndChurch(1L, church))
            .thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            savedSearchService.getSavedSearchById(1L, 1L, 1L);
        });
    }

    // ========== Update Saved Search ==========

    @Test
    @DisplayName("Should update saved search successfully")
    void testUpdateSavedSearch() throws JsonProcessingException {
        // Given
        when(churchRepository.findById(1L)).thenReturn(Optional.of(church));
        when(savedSearchRepository.findByIdAndChurch(1L, church))
            .thenReturn(Optional.of(savedSearch));
        when(objectMapper.readValue(anyString(), eq(AdvancedSearchRequest.class)))
            .thenReturn(new AdvancedSearchRequest(new ArrayList<>(), AdvancedSearchRequest.LogicalOperator.AND));
        when(savedSearchRepository.save(any(SavedSearch.class))).thenReturn(savedSearch);

        SavedSearchRequest updateRequest = new SavedSearchRequest(
            "Updated Search Name",
            "{\"filterGroups\":[]}",
            false,
            true,
            "Updated description"
        );

        // When
        SavedSearchResponse response = savedSearchService.updateSavedSearch(1L, updateRequest, 1L, 1L);

        // Then
        assertNotNull(response);
        verify(savedSearchRepository).save(any(SavedSearch.class));
    }

    @Test
    @DisplayName("Should throw exception when non-creator tries to update")
    void testUpdateSavedSearch_NonCreator() {
        // Given
        when(churchRepository.findById(1L)).thenReturn(Optional.of(church));
        when(savedSearchRepository.findByIdAndChurch(1L, church))
            .thenReturn(Optional.of(savedSearch));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            savedSearchService.updateSavedSearch(1L, savedSearchRequest, 1L, 2L);
        });
        verify(savedSearchRepository, never()).save(any(SavedSearch.class));
    }

    // ========== Delete Saved Search ==========

    @Test
    @DisplayName("Should delete saved search successfully")
    void testDeleteSavedSearch() {
        // Given
        when(churchRepository.findById(1L)).thenReturn(Optional.of(church));
        when(savedSearchRepository.findByIdAndChurch(1L, church))
            .thenReturn(Optional.of(savedSearch));

        // When
        savedSearchService.deleteSavedSearch(1L, 1L, 1L);

        // Then
        verify(savedSearchRepository).delete(savedSearch);
    }

    @Test
    @DisplayName("Should throw exception when non-creator tries to delete")
    void testDeleteSavedSearch_NonCreator() {
        // Given
        when(churchRepository.findById(1L)).thenReturn(Optional.of(church));
        when(savedSearchRepository.findByIdAndChurch(1L, church))
            .thenReturn(Optional.of(savedSearch));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            savedSearchService.deleteSavedSearch(1L, 1L, 2L);
        });
        verify(savedSearchRepository, never()).delete(any(SavedSearch.class));
    }

    // ========== Execute Saved Search ==========

    @Test
    @DisplayName("Should execute saved search successfully")
    void testExecuteSavedSearch() throws JsonProcessingException {
        // Given
        AdvancedSearchRequest searchRequest = new AdvancedSearchRequest(
            new ArrayList<>(),
            AdvancedSearchRequest.LogicalOperator.AND
        );
        AdvancedSearchResponse.SearchMetadata metadata = new AdvancedSearchResponse.SearchMetadata(
            2,
            150L,
            "Advanced search query"
        );
        AdvancedSearchResponse searchResponse = new AdvancedSearchResponse(
            new PageImpl<>(new ArrayList<>()),
            metadata
        );
        Pageable pageable = PageRequest.of(0, 20);

        when(churchRepository.findById(1L)).thenReturn(Optional.of(church));
        when(savedSearchRepository.findByIdAndChurch(1L, church))
            .thenReturn(Optional.of(savedSearch));
        when(objectMapper.readValue(anyString(), eq(AdvancedSearchRequest.class)))
            .thenReturn(searchRequest);
        when(memberService.advancedSearch(searchRequest, 1L, pageable))
            .thenReturn(searchResponse);
        when(savedSearchRepository.save(any(SavedSearch.class))).thenReturn(savedSearch);

        // When
        AdvancedSearchResponse response = savedSearchService.executeSavedSearch(1L, 1L, 1L, pageable);

        // Then
        assertNotNull(response);
        assertEquals(2, response.metadata().totalFiltersApplied());
        verify(memberService).advancedSearch(searchRequest, 1L, pageable);
        verify(savedSearchRepository).save(any(SavedSearch.class)); // Updates lastExecuted
    }

    @Test
    @DisplayName("Should throw exception when executing private search by non-creator")
    void testExecuteSavedSearch_PrivateByNonCreator() {
        // Given
        savedSearch.setIsPublic(false);
        when(churchRepository.findById(1L)).thenReturn(Optional.of(church));
        when(savedSearchRepository.findByIdAndChurch(1L, church))
            .thenReturn(Optional.of(savedSearch));
        Pageable pageable = PageRequest.of(0, 20);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            savedSearchService.executeSavedSearch(1L, 1L, 2L, pageable);
        });
        verify(memberService, never()).advancedSearch(any(), anyLong(), any());
    }

    // ========== Duplicate Saved Search ==========

    @Test
    @DisplayName("Should duplicate saved search successfully")
    void testDuplicateSavedSearch() {
        // Given
        when(churchRepository.findById(1L)).thenReturn(Optional.of(church));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        when(savedSearchRepository.findByIdAndChurch(1L, church))
            .thenReturn(Optional.of(savedSearch));

        SavedSearch duplicatedSearch = SavedSearch.builder()
            .id(2L)
            .church(church)
            .createdBy(user2)
            .searchName("Active Members (Copy)")
            .searchCriteria(savedSearch.getSearchCriteria())
            .isPublic(false)
            .isDynamic(savedSearch.getIsDynamic())
            .description(savedSearch.getDescription())
            .build();

        when(savedSearchRepository.save(any(SavedSearch.class))).thenReturn(duplicatedSearch);

        // When
        SavedSearchResponse response = savedSearchService.duplicateSavedSearch(1L, 1L, 2L);

        // Then
        assertNotNull(response);
        assertTrue(response.searchName().contains("(Copy)"));
        assertEquals(false, response.isPublic()); // Always private
        assertEquals(user2.getId(), response.createdBy().id());
        verify(savedSearchRepository).save(any(SavedSearch.class));
    }

    @Test
    @DisplayName("Should throw exception when duplicating private search by non-creator")
    void testDuplicateSavedSearch_PrivateByNonCreator() {
        // Given
        savedSearch.setIsPublic(false);
        when(churchRepository.findById(1L)).thenReturn(Optional.of(church));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        when(savedSearchRepository.findByIdAndChurch(1L, church))
            .thenReturn(Optional.of(savedSearch));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            savedSearchService.duplicateSavedSearch(1L, 1L, 2L);
        });
        verify(savedSearchRepository, never()).save(any(SavedSearch.class));
    }

    // ========== Time Ago Calculation ==========

    @Test
    @DisplayName("Should calculate 'Never' for null lastExecuted")
    void testTimeAgo_Never() {
        // Given
        savedSearch.setLastExecuted(null);
        when(churchRepository.findById(1L)).thenReturn(Optional.of(church));
        when(savedSearchRepository.findByIdAndChurch(1L, church))
            .thenReturn(Optional.of(savedSearch));

        // When
        SavedSearchResponse response = savedSearchService.getSavedSearchById(1L, 1L, 1L);

        // Then
        assertEquals("Never", response.lastExecutedAgo());
    }

    @Test
    @DisplayName("Should calculate 'Just now' for recent execution")
    void testTimeAgo_JustNow() {
        // Given
        savedSearch.setLastExecuted(LocalDateTime.now().minusSeconds(30));
        when(churchRepository.findById(1L)).thenReturn(Optional.of(church));
        when(savedSearchRepository.findByIdAndChurch(1L, church))
            .thenReturn(Optional.of(savedSearch));

        // When
        SavedSearchResponse response = savedSearchService.getSavedSearchById(1L, 1L, 1L);

        // Then
        assertEquals("Just now", response.lastExecutedAgo());
    }

    @Test
    @DisplayName("Should calculate minutes ago")
    void testTimeAgo_Minutes() {
        // Given
        savedSearch.setLastExecuted(LocalDateTime.now().minusMinutes(5));
        when(churchRepository.findById(1L)).thenReturn(Optional.of(church));
        when(savedSearchRepository.findByIdAndChurch(1L, church))
            .thenReturn(Optional.of(savedSearch));

        // When
        SavedSearchResponse response = savedSearchService.getSavedSearchById(1L, 1L, 1L);

        // Then
        assertTrue(response.lastExecutedAgo().contains("minute"));
    }

    @Test
    @DisplayName("Should calculate days ago")
    void testTimeAgo_Days() {
        // Given
        savedSearch.setLastExecuted(LocalDateTime.now().minusDays(3));
        when(churchRepository.findById(1L)).thenReturn(Optional.of(church));
        when(savedSearchRepository.findByIdAndChurch(1L, church))
            .thenReturn(Optional.of(savedSearch));

        // When
        SavedSearchResponse response = savedSearchService.getSavedSearchById(1L, 1L, 1L);

        // Then
        assertTrue(response.lastExecutedAgo().contains("day"));
    }
}
