package com.reuben.pastcare_spring.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reuben.pastcare_spring.models.Church;
import com.reuben.pastcare_spring.models.ChurchSubscription;
import com.reuben.pastcare_spring.repositories.ChurchRepository;
import com.reuben.pastcare_spring.repositories.ChurchSubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for DataRetentionController
 * Tests SUPERADMIN data retention management endpoints
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class DataRetentionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ChurchRepository churchRepository;

    @Autowired
    private ChurchSubscriptionRepository subscriptionRepository;

    private Church testChurch;
    private ChurchSubscription testSubscription;

    @BeforeEach
    void setUp() {
        subscriptionRepository.deleteAll();
        churchRepository.deleteAll();

        // Create a test church
        testChurch = new Church();
        testChurch.setName("Test Church");
        testChurch.setAddress("123 Test St");
        testChurch.setPhoneNumber("1234567890");
        testChurch = churchRepository.save(testChurch);

        // Create a suspended subscription with pending deletion
        testSubscription = new ChurchSubscription();
        testSubscription.setChurchId(testChurch.getId());
        testSubscription.setStatus("SUSPENDED");
        testSubscription.setSuspendedAt(LocalDateTime.now().minusDays(60));
        testSubscription.setDataRetentionEndDate(LocalDate.now().plusDays(30));
        testSubscription.setDeletionWarningSentAt(null);
        testSubscription.setRetentionExtensionDays(0);
        testSubscription = subscriptionRepository.save(testSubscription);
    }

    // ==================== GET /api/platform/data-retention/pending-deletions ====================

    @Test
    @WithMockUser(roles = "SUPERADMIN")
    void getPendingDeletions_AsSuperAdmin_ReturnsPendingDeletions() throws Exception {
        mockMvc.perform(get("/api/platform/data-retention/pending-deletions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].churchId", is(testChurch.getId().intValue())))
            .andExpect(jsonPath("$[0].churchName", is("Test Church")))
            .andExpect(jsonPath("$[0].dataRetentionEndDate", notNullValue()))
            .andExpect(jsonPath("$[0].daysUntilDeletion", notNullValue()))
            .andExpect(jsonPath("$[0].urgencyLevel", notNullValue()))
            .andExpect(jsonPath("$[0].warningSent", is(false)));
    }

    @Test
    @WithMockUser(roles = "SUPERADMIN")
    void getPendingDeletions_WithWarningSet_ShowsWarningSent() throws Exception {
        testSubscription.setDeletionWarningSentAt(LocalDateTime.now().minusDays(5));
        subscriptionRepository.save(testSubscription);

        mockMvc.perform(get("/api/platform/data-retention/pending-deletions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].warningSent", is(true)));
    }

    @Test
    @WithMockUser(roles = "SUPERADMIN")
    void getPendingDeletions_CalculatesUrgencyLevels_Correctly() throws Exception {
        // Create churches with different urgency levels

        // CRITICAL (3 days)
        Church criticalChurch = createChurch("Critical Church");
        createSuspendedSubscription(criticalChurch, 3);

        // HIGH (7 days)
        Church highChurch = createChurch("High Church");
        createSuspendedSubscription(highChurch, 7);

        // MEDIUM (14 days)
        Church mediumChurch = createChurch("Medium Church");
        createSuspendedSubscription(mediumChurch, 14);

        // LOW (30 days)
        Church lowChurch = createChurch("Low Church");
        createSuspendedSubscription(lowChurch, 30);

        mockMvc.perform(get("/api/platform/data-retention/pending-deletions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(5))) // Including the setUp church
            .andExpect(jsonPath("$[?(@.churchName == 'Critical Church')].urgencyLevel", contains("CRITICAL")))
            .andExpect(jsonPath("$[?(@.churchName == 'High Church')].urgencyLevel", contains("HIGH")))
            .andExpect(jsonPath("$[?(@.churchName == 'Medium Church')].urgencyLevel", contains("MEDIUM")))
            .andExpect(jsonPath("$[?(@.churchName == 'Low Church')].urgencyLevel", contains("LOW")));
    }

    @Test
    @WithMockUser(roles = "SUPERADMIN")
    void getPendingDeletions_WhenNoPendingDeletions_ReturnsEmptyList() throws Exception {
        subscriptionRepository.deleteAll();

        mockMvc.perform(get("/api/platform/data-retention/pending-deletions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getPendingDeletions_AsAdmin_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/platform/data-retention/pending-deletions"))
            .andExpect(status().isForbidden());
    }

    @Test
    void getPendingDeletions_Unauthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/platform/data-retention/pending-deletions"))
            .andExpect(status().isUnauthorized());
    }

    // ==================== GET /api/platform/data-retention/pending-deletions/{churchId} ====================

    @Test
    @WithMockUser(roles = "SUPERADMIN")
    void getPendingDeletionById_AsSuperAdmin_ReturnsDetails() throws Exception {
        mockMvc.perform(get("/api/platform/data-retention/pending-deletions/" + testChurch.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.churchId", is(testChurch.getId().intValue())))
            .andExpect(jsonPath("$.churchName", is("Test Church")))
            .andExpect(jsonPath("$.daysUntilDeletion", greaterThan(0)))
            .andExpect(jsonPath("$.urgencyLevel", notNullValue()));
    }

    @Test
    @WithMockUser(roles = "SUPERADMIN")
    void getPendingDeletionById_NonExistentChurch_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/platform/data-retention/pending-deletions/999999"))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "SUPERADMIN")
    void getPendingDeletionById_ChurchNotSuspended_ReturnsNotFound() throws Exception {
        // Create an active church
        Church activeChurch = createChurch("Active Church");
        ChurchSubscription activeSubscription = new ChurchSubscription();
        activeSubscription.setChurchId(activeChurch.getId());
        activeSubscription.setStatus("ACTIVE");
        subscriptionRepository.save(activeSubscription);

        mockMvc.perform(get("/api/platform/data-retention/pending-deletions/" + activeChurch.getId()))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "PASTOR")
    void getPendingDeletionById_AsPastor_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/platform/data-retention/pending-deletions/" + testChurch.getId()))
            .andExpect(status().isForbidden());
    }

    // ==================== POST /api/platform/data-retention/{churchId}/extend ====================

    @Test
    @WithMockUser(roles = "SUPERADMIN")
    void extendRetention_AsSuperAdmin_ExtendsSuccessfully() throws Exception {
        Map<String, Object> request = Map.of(
            "extensionDays", 30,
            "note", "Extension granted due to payment processing delay"
        );

        LocalDate originalEndDate = testSubscription.getDataRetentionEndDate();

        mockMvc.perform(post("/api/platform/data-retention/" + testChurch.getId() + "/extend")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.churchId", is(testChurch.getId().intValue())))
            .andExpect(jsonPath("$.dataRetentionEndDate", notNullValue()))
            .andExpect(jsonPath("$.daysUntilDeletion", greaterThan(30)));

        // Verify the subscription was updated
        ChurchSubscription updated = subscriptionRepository.findById(testSubscription.getId()).orElseThrow();
        assert updated.getRetentionExtensionDays() == 30;
        assert updated.getDataRetentionEndDate().isAfter(originalEndDate);
        assert updated.getRetentionExtensionNote().equals("Extension granted due to payment processing delay");
    }

    @Test
    @WithMockUser(roles = "SUPERADMIN")
    void extendRetention_MultipleExtensions_AccumulatesCorrectly() throws Exception {
        // First extension
        Map<String, Object> firstExtension = Map.of(
            "extensionDays", 15,
            "note", "First extension"
        );
        mockMvc.perform(post("/api/platform/data-retention/" + testChurch.getId() + "/extend")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstExtension)))
            .andExpect(status().isOk());

        // Second extension
        Map<String, Object> secondExtension = Map.of(
            "extensionDays", 10,
            "note", "Second extension"
        );
        mockMvc.perform(post("/api/platform/data-retention/" + testChurch.getId() + "/extend")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(secondExtension)))
            .andExpect(status().isOk());

        // Verify total extension is 25 days
        ChurchSubscription updated = subscriptionRepository.findById(testSubscription.getId()).orElseThrow();
        assert updated.getRetentionExtensionDays() == 25;
    }

    @Test
    @WithMockUser(roles = "SUPERADMIN")
    void extendRetention_WithInvalidDays_ReturnsBadRequest() throws Exception {
        Map<String, Object> request = Map.of(
            "extensionDays", 0,
            "note", "Invalid extension"
        );

        mockMvc.perform(post("/api/platform/data-retention/" + testChurch.getId() + "/extend")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "SUPERADMIN")
    void extendRetention_WithBlankNote_ReturnsBadRequest() throws Exception {
        Map<String, Object> request = Map.of(
            "extensionDays", 30,
            "note", ""
        );

        mockMvc.perform(post("/api/platform/data-retention/" + testChurch.getId() + "/extend")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "SUPERADMIN")
    void extendRetention_NonExistentChurch_ReturnsNotFound() throws Exception {
        Map<String, Object> request = Map.of(
            "extensionDays", 30,
            "note", "Extension"
        );

        mockMvc.perform(post("/api/platform/data-retention/999999/extend")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "TREASURER")
    void extendRetention_AsTreasurer_ReturnsForbidden() throws Exception {
        Map<String, Object> request = Map.of(
            "extensionDays", 30,
            "note", "Extension"
        );

        mockMvc.perform(post("/api/platform/data-retention/" + testChurch.getId() + "/extend")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());
    }

    // ==================== DELETE /api/platform/data-retention/{churchId}/cancel-deletion ====================

    @Test
    @WithMockUser(roles = "SUPERADMIN")
    void cancelDeletion_AsSuperAdmin_CancelsSuccessfully() throws Exception {
        mockMvc.perform(delete("/api/platform/data-retention/" + testChurch.getId() + "/cancel-deletion"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("canceled")));

        // Verify the deletion was canceled
        ChurchSubscription updated = subscriptionRepository.findById(testSubscription.getId()).orElseThrow();
        assert updated.getDataRetentionEndDate() == null;
        assert updated.getRetentionExtensionDays() == 0;
        assert updated.getRetentionExtensionNote() == null;
    }

    @Test
    @WithMockUser(roles = "SUPERADMIN")
    void cancelDeletion_NonExistentChurch_ReturnsNotFound() throws Exception {
        mockMvc.perform(delete("/api/platform/data-retention/999999/cancel-deletion"))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "MEMBER_MANAGER")
    void cancelDeletion_AsMemberManager_ReturnsForbidden() throws Exception {
        mockMvc.perform(delete("/api/platform/data-retention/" + testChurch.getId() + "/cancel-deletion"))
            .andExpect(status().isForbidden());
    }

    // ==================== Authorization Tests for All Roles ====================

    @Test
    @WithMockUser(roles = "MEMBER")
    void allEndpoints_AsMember_ReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/platform/data-retention/pending-deletions"))
            .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/platform/data-retention/pending-deletions/" + testChurch.getId()))
            .andExpect(status().isForbidden());

        Map<String, Object> request = Map.of("extensionDays", 30, "note", "Test");
        mockMvc.perform(post("/api/platform/data-retention/" + testChurch.getId() + "/extend")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/platform/data-retention/" + testChurch.getId() + "/cancel-deletion"))
            .andExpect(status().isForbidden());
    }

    @Test
    void allEndpoints_Unauthenticated_ReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/platform/data-retention/pending-deletions"))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/api/platform/data-retention/" + testChurch.getId() + "/cancel-deletion"))
            .andExpect(status().isUnauthorized());
    }

    // ==================== Helper Methods ====================

    private Church createChurch(String name) {
        Church church = new Church();
        church.setName(name);
        church.setAddress("Test Address");
        church.setPhoneNumber("1234567890");
        return churchRepository.save(church);
    }

    private ChurchSubscription createSuspendedSubscription(Church church, int daysUntilDeletion) {
        ChurchSubscription subscription = new ChurchSubscription();
        subscription.setChurchId(church.getId());
        subscription.setStatus("SUSPENDED");
        subscription.setSuspendedAt(LocalDateTime.now().minusDays(90 - daysUntilDeletion));
        subscription.setDataRetentionEndDate(LocalDate.now().plusDays(daysUntilDeletion));
        subscription.setDeletionWarningSentAt(null);
        subscription.setRetentionExtensionDays(0);
        return subscriptionRepository.save(subscription);
    }
}
