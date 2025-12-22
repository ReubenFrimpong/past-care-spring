package com.reuben.pastcare_spring.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reuben.pastcare_spring.dtos.AdvancedSearchResponse;
import com.reuben.pastcare_spring.dtos.SavedSearchRequest;
import com.reuben.pastcare_spring.dtos.SavedSearchResponse;
import com.reuben.pastcare_spring.services.SavedSearchService;
import com.reuben.pastcare_spring.util.RequestContextUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for SavedSearchController.
 * Tests REST API endpoints for saved searches.
 */
@WebMvcTest(controllers = SavedSearchController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Saved Search Controller Tests")
class SavedSearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SavedSearchService savedSearchService;

    @MockBean
    private RequestContextUtil requestContextUtil;

    private SavedSearchRequest savedSearchRequest;
    private SavedSearchResponse savedSearchResponse;

    @BeforeEach
    void setUp() {
        // Setup test data
        savedSearchRequest = new SavedSearchRequest(
            "Active Members",
            "{\"filterGroups\":[]}",
            true,
            false,
            "Search for all active members"
        );

        SavedSearchResponse.CreatorInfo creatorInfo = new SavedSearchResponse.CreatorInfo(
            1L,
            "John Doe",
            "john@example.com"
        );

        savedSearchResponse = new SavedSearchResponse(
            1L,
            "Active Members",
            "{\"filterGroups\":[]}",
            true,
            false,
            "Search for all active members",
            LocalDateTime.now().minusDays(2),
            50L,
            "2 days ago",
            creatorInfo,
            LocalDateTime.now().minusDays(7),
            LocalDateTime.now().minusDays(7),
            true,
            true
        );

        // Mock request context
        when(requestContextUtil.extractChurchId(any(HttpServletRequest.class))).thenReturn(1L);
        when(requestContextUtil.extractUserId(any(HttpServletRequest.class))).thenReturn(1L);
    }

    // ========== Create Saved Search ==========

    @Test
    @DisplayName("Should create saved search successfully")
    void testCreateSavedSearch() throws Exception {
        // Given
        when(savedSearchService.createSavedSearch(any(SavedSearchRequest.class), eq(1L), eq(1L)))
            .thenReturn(savedSearchResponse);

        // When & Then
        mockMvc.perform(post("/api/members/saved-searches")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(savedSearchRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.searchName").value("Active Members"))
            .andExpect(jsonPath("$.isPublic").value(true))
            .andExpect(jsonPath("$.canEdit").value(true));

        verify(savedSearchService).createSavedSearch(any(SavedSearchRequest.class), eq(1L), eq(1L));
    }

    @Test
    @DisplayName("Should return 400 when search name is blank")
    void testCreateSavedSearch_BlankName() throws Exception {
        // Given
        SavedSearchRequest invalidRequest = new SavedSearchRequest(
            "",
            "{\"filterGroups\":[]}",
            false,
            false,
            null
        );

        // When & Then
        mockMvc.perform(post("/api/members/saved-searches")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when search criteria is blank")
    void testCreateSavedSearch_BlankCriteria() throws Exception {
        // Given
        SavedSearchRequest invalidRequest = new SavedSearchRequest(
            "Test Search",
            "",
            false,
            false,
            null
        );

        // When & Then
        mockMvc.perform(post("/api/members/saved-searches")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    // ========== Get All Saved Searches ==========

    @Test
    @DisplayName("Should get all accessible saved searches")
    void testGetAllSavedSearches() throws Exception {
        // Given
        Page<SavedSearchResponse> searchPage = new PageImpl<>(List.of(savedSearchResponse));
        when(savedSearchService.getAccessibleSearches(eq(1L), eq(1L), any()))
            .thenReturn(searchPage);

        // When & Then
        mockMvc.perform(get("/api/members/saved-searches")
                .param("page", "0")
                .param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].searchName").value("Active Members"))
            .andExpect(jsonPath("$.content[0].isPublic").value(true))
            .andExpect(jsonPath("$.totalElements").value(1));

        verify(savedSearchService).getAccessibleSearches(eq(1L), eq(1L), any());
    }

    // ========== Get Saved Search By ID ==========

    @Test
    @DisplayName("Should get saved search by ID")
    void testGetSavedSearchById() throws Exception {
        // Given
        when(savedSearchService.getSavedSearchById(1L, 1L, 1L))
            .thenReturn(savedSearchResponse);

        // When & Then
        mockMvc.perform(get("/api/members/saved-searches/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.searchName").value("Active Members"))
            .andExpect(jsonPath("$.lastExecutedAgo").value("2 days ago"));

        verify(savedSearchService).getSavedSearchById(1L, 1L, 1L);
    }

    // ========== Update Saved Search ==========

    @Test
    @DisplayName("Should update saved search successfully")
    void testUpdateSavedSearch() throws Exception {
        // Given
        SavedSearchRequest updateRequest = new SavedSearchRequest(
            "Updated Search Name",
            "{\"filterGroups\":[]}",
            false,
            true,
            "Updated description"
        );

        SavedSearchResponse updatedResponse = new SavedSearchResponse(
            1L,
            "Updated Search Name",
            "{\"filterGroups\":[]}",
            false,
            true,
            "Updated description",
            null,
            null,
            "Never",
            savedSearchResponse.createdBy(),
            savedSearchResponse.createdAt(),
            LocalDateTime.now(),
            true,
            true
        );

        when(savedSearchService.updateSavedSearch(eq(1L), any(SavedSearchRequest.class), eq(1L), eq(1L)))
            .thenReturn(updatedResponse);

        // When & Then
        mockMvc.perform(put("/api/members/saved-searches/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.searchName").value("Updated Search Name"))
            .andExpect(jsonPath("$.isDynamic").value(true));

        verify(savedSearchService).updateSavedSearch(eq(1L), any(SavedSearchRequest.class), eq(1L), eq(1L));
    }

    // ========== Delete Saved Search ==========

    @Test
    @DisplayName("Should delete saved search successfully")
    void testDeleteSavedSearch() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/members/saved-searches/1"))
            .andExpect(status().isNoContent());

        verify(savedSearchService).deleteSavedSearch(1L, 1L, 1L);
    }

    // ========== Execute Saved Search ==========

    @Test
    @DisplayName("Should execute saved search successfully")
    void testExecuteSavedSearch() throws Exception {
        // Given
        AdvancedSearchResponse.SearchMetadata metadata = new AdvancedSearchResponse.SearchMetadata(
            2,
            150L,
            "Advanced search query"
        );
        AdvancedSearchResponse searchResponse = new AdvancedSearchResponse(
            new PageImpl<>(List.of()),
            metadata
        );

        when(savedSearchService.executeSavedSearch(eq(1L), eq(1L), eq(1L), any()))
            .thenReturn(searchResponse);

        // When & Then
        mockMvc.perform(post("/api/members/saved-searches/1/execute")
                .param("page", "0")
                .param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.metadata.totalFiltersApplied").value(2))
            .andExpect(jsonPath("$.metadata.executionTimeMs").value(150));

        verify(savedSearchService).executeSavedSearch(eq(1L), eq(1L), eq(1L), any());
    }

    // ========== Duplicate Saved Search ==========

    @Test
    @DisplayName("Should duplicate saved search successfully")
    void testDuplicateSavedSearch() throws Exception {
        // Given
        SavedSearchResponse duplicatedResponse = new SavedSearchResponse(
            2L,
            "Active Members (Copy)",
            "{\"filterGroups\":[]}",
            false,
            false,
            "Search for all active members",
            null,
            null,
            "Never",
            savedSearchResponse.createdBy(),
            LocalDateTime.now(),
            LocalDateTime.now(),
            true,
            true
        );

        when(savedSearchService.duplicateSavedSearch(1L, 1L, 1L))
            .thenReturn(duplicatedResponse);

        // When & Then
        mockMvc.perform(post("/api/members/saved-searches/1/duplicate"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(2))
            .andExpect(jsonPath("$.searchName").value("Active Members (Copy)"))
            .andExpect(jsonPath("$.isPublic").value(false));

        verify(savedSearchService).duplicateSavedSearch(1L, 1L, 1L);
    }
}
