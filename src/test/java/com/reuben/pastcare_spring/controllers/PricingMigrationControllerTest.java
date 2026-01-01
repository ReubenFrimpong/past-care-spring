package com.reuben.pastcare_spring.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reuben.pastcare_spring.services.PricingMigrationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for PricingMigrationController.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("PricingMigrationController Tests")
class PricingMigrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PricingMigrationService migrationService;

    // ==================== SUPERADMIN ENDPOINTS TESTS ====================

    @Test
    @WithMockUser(authorities = "SUPERADMIN")
    @DisplayName("POST /api/platform/pricing-migration/migrate-church - Should migrate single church")
    void migrateChurch_asSuperadmin_shouldMigrateChurch() throws Exception {
        // Given
        PricingMigrationController.ChurchMigrationRequest request =
                new PricingMigrationController.ChurchMigrationRequest(42L, 1L);

        PricingMigrationService.MigrationResult result =
                new PricingMigrationService.MigrationResult(
                        42L,
                        "Grace Community Church",
                        true,
                        "STANDARD",
                        "TIER_2",
                        350,
                        new BigDecimal("150.00"),
                        new BigDecimal("9.99"),
                        null
                );

        when(migrationService.migrateChurch(42L, 1L)).thenReturn(result);

        // When & Then
        mockMvc.perform(post("/api/platform/pricing-migration/migrate-church")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.churchId", is(42)))
                .andExpect(jsonPath("$.churchName", is("Grace Community Church")))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.oldPlanName", is("STANDARD")))
                .andExpect(jsonPath("$.newTierName", is("TIER_2")))
                .andExpect(jsonPath("$.memberCount", is(350)))
                .andExpect(jsonPath("$.oldMonthlyPrice", is(150.00)))
                .andExpect(jsonPath("$.newMonthlyPrice", is(9.99)));

        verify(migrationService).migrateChurch(42L, 1L);
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    @DisplayName("POST /api/platform/pricing-migration/migrate-church - ADMIN should be denied")
    void migrateChurch_asAdmin_shouldBeDenied() throws Exception {
        // Given
        PricingMigrationController.ChurchMigrationRequest request =
                new PricingMigrationController.ChurchMigrationRequest(42L, 1L);

        // When & Then
        mockMvc.perform(post("/api/platform/pricing-migration/migrate-church")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(migrationService);
    }

    @Test
    @DisplayName("POST /api/platform/pricing-migration/migrate-church - Should fail without auth")
    void migrateChurch_withoutAuth_shouldFail() throws Exception {
        // Given
        PricingMigrationController.ChurchMigrationRequest request =
                new PricingMigrationController.ChurchMigrationRequest(42L, 1L);

        // When & Then
        mockMvc.perform(post("/api/platform/pricing-migration/migrate-church")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(migrationService);
    }

    @Test
    @WithMockUser(authorities = "SUPERADMIN")
    @DisplayName("POST /api/platform/pricing-migration/bulk-migrate - Should migrate all churches")
    void bulkMigrateAllChurches_asSuperadmin_shouldMigrateAll() throws Exception {
        // Given
        PricingMigrationController.BulkMigrationRequest request =
                new PricingMigrationController.BulkMigrationRequest(1L);

        PricingMigrationService.MigrationResult result1 =
                new PricingMigrationService.MigrationResult(
                        1L, "Church 1", true, "STANDARD", "TIER_1",
                        150, new BigDecimal("150"), new BigDecimal("5.99"), null
                );

        PricingMigrationService.MigrationResult result2 =
                new PricingMigrationService.MigrationResult(
                        2L, "Church 2", true, "STANDARD", "TIER_2",
                        350, new BigDecimal("150"), new BigDecimal("9.99"), null
                );

        PricingMigrationService.MigrationSummary summary =
                new PricingMigrationService.MigrationSummary(
                        150,
                        148,
                        2,
                        Arrays.asList(result1, result2),
                        Arrays.asList("Church 15: No subscription found"),
                        45230L
                );

        when(migrationService.bulkMigrateAllChurches(1L)).thenReturn(summary);

        // When & Then
        mockMvc.perform(post("/api/platform/pricing-migration/bulk-migrate")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalChurches", is(150)))
                .andExpect(jsonPath("$.successCount", is(148)))
                .andExpect(jsonPath("$.failureCount", is(2)))
                .andExpect(jsonPath("$.results", hasSize(2)))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.durationMs", is(45230)));

        verify(migrationService).bulkMigrateAllChurches(1L);
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    @DisplayName("POST /api/platform/pricing-migration/bulk-migrate - ADMIN should be denied")
    void bulkMigrateAllChurches_asAdmin_shouldBeDenied() throws Exception {
        // Given
        PricingMigrationController.BulkMigrationRequest request =
                new PricingMigrationController.BulkMigrationRequest(1L);

        // When & Then
        mockMvc.perform(post("/api/platform/pricing-migration/bulk-migrate")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(migrationService);
    }

    @Test
    @WithMockUser(authorities = "SUPERADMIN")
    @DisplayName("POST /api/platform/pricing-migration/rollback - Should rollback migration")
    void rollbackMigration_asSuperadmin_shouldRollback() throws Exception {
        // Given
        PricingMigrationController.RollbackRequest request =
                new PricingMigrationController.RollbackRequest(
                        42L,
                        1L,
                        "Congregation pricing causing issues"
                );

        doNothing().when(migrationService).rollbackMigration(
                42L,
                1L,
                "Congregation pricing causing issues"
        );

        // When & Then
        mockMvc.perform(post("/api/platform/pricing-migration/rollback")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", containsString("rolled back successfully")))
                .andExpect(jsonPath("$.churchId", is(42)))
                .andExpect(jsonPath("$.reason", is("Congregation pricing causing issues")));

        verify(migrationService).rollbackMigration(
                42L,
                1L,
                "Congregation pricing causing issues"
        );
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    @DisplayName("POST /api/platform/pricing-migration/rollback - ADMIN should be denied")
    void rollbackMigration_asAdmin_shouldBeDenied() throws Exception {
        // Given
        PricingMigrationController.RollbackRequest request =
                new PricingMigrationController.RollbackRequest(
                        42L, 1L, "Reason"
                );

        // When & Then
        mockMvc.perform(post("/api/platform/pricing-migration/rollback")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(migrationService);
    }

    @Test
    @WithMockUser(authorities = "SUPERADMIN")
    @DisplayName("GET /api/platform/pricing-migration/status - Should return migration status")
    void getMigrationStatus_asSuperadmin_shouldReturnStatus() throws Exception {
        // Given
        Map<Long, Long> tierDistribution = new HashMap<>();
        tierDistribution.put(1L, 50L);
        tierDistribution.put(2L, 60L);
        tierDistribution.put(3L, 30L);

        PricingMigrationService.MigrationStatusReport report =
                new PricingMigrationService.MigrationStatusReport(
                        150L,
                        148L,
                        1L,
                        1L,
                        new BigDecimal("-140.01"),
                        tierDistribution
                );

        when(migrationService.getMigrationStatus()).thenReturn(report);

        // When & Then
        mockMvc.perform(get("/api/platform/pricing-migration/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalMigrations", is(150)))
                .andExpect(jsonPath("$.completed", is(148)))
                .andExpect(jsonPath("$.rolledBack", is(1)))
                .andExpect(jsonPath("$.failed", is(1)))
                .andExpect(jsonPath("$.avgPriceChange", is(-140.01)))
                .andExpect(jsonPath("$.tierDistribution.1", is(50)))
                .andExpect(jsonPath("$.tierDistribution.2", is(60)))
                .andExpect(jsonPath("$.tierDistribution.3", is(30)));

        verify(migrationService).getMigrationStatus();
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    @DisplayName("GET /api/platform/pricing-migration/status - ADMIN should be denied")
    void getMigrationStatus_asAdmin_shouldBeDenied() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/platform/pricing-migration/status"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(migrationService);
    }

    @Test
    @WithMockUser(authorities = "SUPERADMIN")
    @DisplayName("GET /api/platform/pricing-migration/can-migrate/{churchId} - Should check eligibility")
    void canMigrate_asSuperadmin_shouldCheckEligibility() throws Exception {
        // Given
        when(migrationService.canMigrate(42L)).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/platform/pricing-migration/can-migrate/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.churchId", is(42)))
                .andExpect(jsonPath("$.canMigrate", is(true)))
                .andExpect(jsonPath("$.reason", containsString("eligible")));

        verify(migrationService).canMigrate(42L);
    }

    @Test
    @WithMockUser(authorities = "SUPERADMIN")
    @DisplayName("GET /api/platform/pricing-migration/can-migrate/{churchId} - Should return false for ineligible church")
    void canMigrate_ineligibleChurch_shouldReturnFalse() throws Exception {
        // Given
        when(migrationService.canMigrate(99L)).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/platform/pricing-migration/can-migrate/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.churchId", is(99)))
                .andExpect(jsonPath("$.canMigrate", is(false)))
                .andExpect(jsonPath("$.reason", containsString("not eligible")));

        verify(migrationService).canMigrate(99L);
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    @DisplayName("GET /api/platform/pricing-migration/can-migrate/{churchId} - ADMIN should be denied")
    void canMigrate_asAdmin_shouldBeDenied() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/platform/pricing-migration/can-migrate/42"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(migrationService);
    }

    @Test
    @WithMockUser(authorities = "SUPERADMIN")
    @DisplayName("POST /api/platform/pricing-migration/migrate-church - Should validate request")
    void migrateChurch_withInvalidRequest_shouldFail() throws Exception {
        // Given - missing performedBy
        String invalidRequest = "{\"churchId\": 42}";

        // When & Then
        mockMvc.perform(post("/api/platform/pricing-migration/migrate-church")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(migrationService);
    }

    @Test
    @WithMockUser(authorities = "SUPERADMIN")
    @DisplayName("POST /api/platform/pricing-migration/rollback - Should validate reason is provided")
    void rollbackMigration_withoutReason_shouldFail() throws Exception {
        // Given - empty reason
        PricingMigrationController.RollbackRequest request =
                new PricingMigrationController.RollbackRequest(42L, 1L, "");

        // When & Then
        mockMvc.perform(post("/api/platform/pricing-migration/rollback")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(migrationService);
    }
}
