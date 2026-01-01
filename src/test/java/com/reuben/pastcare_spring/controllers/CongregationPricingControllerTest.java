package com.reuben.pastcare_spring.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reuben.pastcare_spring.models.CongregationPricingTier;
import com.reuben.pastcare_spring.models.SubscriptionBillingInterval;
import com.reuben.pastcare_spring.services.CongregationPricingService;
import com.reuben.pastcare_spring.services.CurrencyConversionService;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for CongregationPricingController.
 *
 * <p>Tests all endpoints with proper security context and validation.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("CongregationPricingController Tests")
class CongregationPricingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CongregationPricingService pricingService;

    @MockBean
    private CurrencyConversionService currencyService;

    private CongregationPricingTier tier1;
    private CongregationPricingTier tier2;
    private SubscriptionBillingInterval monthlyInterval;

    @BeforeEach
    void setUp() {
        // Setup test data
        tier1 = CongregationPricingTier.builder()
                .id(1L)
                .tierName("TIER_1")
                .displayName("Small Church (1-200)")
                .description("Perfect for small congregations")
                .minMembers(1)
                .maxMembers(200)
                .monthlyPriceUsd(new BigDecimal("5.99"))
                .quarterlyPriceUsd(new BigDecimal("16.47"))
                .biannualPriceUsd(new BigDecimal("34.94"))
                .annualPriceUsd(new BigDecimal("69.88"))
                .quarterlyDiscountPct(new BigDecimal("8.00"))
                .biannualDiscountPct(new BigDecimal("3.00"))
                .annualDiscountPct(new BigDecimal("3.00"))
                .features("[\"Member Management\",\"Event Planning\"]")
                .isActive(true)
                .displayOrder(1)
                .build();

        tier2 = CongregationPricingTier.builder()
                .id(2L)
                .tierName("TIER_2")
                .displayName("Growing Church (201-500)")
                .description("Ideal for growing congregations")
                .minMembers(201)
                .maxMembers(500)
                .monthlyPriceUsd(new BigDecimal("9.99"))
                .quarterlyPriceUsd(new BigDecimal("28.97"))
                .biannualPriceUsd(new BigDecimal("57.94"))
                .annualPriceUsd(new BigDecimal("117.88"))
                .isActive(true)
                .displayOrder(2)
                .build();

        monthlyInterval = SubscriptionBillingInterval.builder()
                .id(1L)
                .intervalName("MONTHLY")
                .displayName("Monthly")
                .months(1)
                .isActive(true)
                .displayOrder(1)
                .build();
    }

    // ==================== PUBLIC ENDPOINTS TESTS ====================

    @Test
    @DisplayName("GET /api/pricing/tiers - Should return all active tiers without authentication")
    void getAllActiveTiers_shouldReturnTiers() throws Exception {
        // Given
        List<CongregationPricingTier> tiers = Arrays.asList(tier1, tier2);
        when(pricingService.getAllActiveTiers()).thenReturn(tiers);

        // When & Then
        mockMvc.perform(get("/api/pricing/tiers"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].tierName", is("TIER_1")))
                .andExpect(jsonPath("$[0].displayName", is("Small Church (1-200)")))
                .andExpect(jsonPath("$[0].monthlyPriceUsd", is(5.99)))
                .andExpect(jsonPath("$[1].tierName", is("TIER_2")))
                .andExpect(jsonPath("$[1].displayName", is("Growing Church (201-500)")));

        verify(pricingService).getAllActiveTiers();
    }

    @Test
    @DisplayName("GET /api/pricing/tiers/{tierId} - Should return specific tier")
    void getTierById_shouldReturnTier() throws Exception {
        // Given
        when(pricingService.getTierById(1L)).thenReturn(tier1);

        // When & Then
        mockMvc.perform(get("/api/pricing/tiers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tierName", is("TIER_1")))
                .andExpect(jsonPath("$.minMembers", is(1)))
                .andExpect(jsonPath("$.maxMembers", is(200)));

        verify(pricingService).getTierById(1L);
    }

    @Test
    @DisplayName("GET /api/pricing/billing-intervals - Should return all billing intervals")
    void getAllBillingIntervals_shouldReturnIntervals() throws Exception {
        // Given
        List<SubscriptionBillingInterval> intervals = Arrays.asList(monthlyInterval);
        when(pricingService.getAllBillingIntervals()).thenReturn(intervals);

        // When & Then
        mockMvc.perform(get("/api/pricing/billing-intervals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].intervalName", is("MONTHLY")))
                .andExpect(jsonPath("$[0].months", is(1)));

        verify(pricingService).getAllBillingIntervals();
    }

    @Test
    @DisplayName("GET /api/pricing/calculate - Should calculate pricing for member count and interval")
    void calculatePricing_shouldReturnCalculation() throws Exception {
        // Given
        when(pricingService.getPricingTierForMemberCount(150)).thenReturn(tier1);
        when(currencyService.convertUsdToGhs(new BigDecimal("5.99")))
                .thenReturn(new BigDecimal("71.88"));
        when(currencyService.formatDualCurrency(new BigDecimal("5.99")))
                .thenReturn("GHS 71.88 ($5.99)");

        // When & Then
        mockMvc.perform(get("/api/pricing/calculate")
                        .param("memberCount", "150")
                        .param("billingInterval", "MONTHLY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tierId", is(1)))
                .andExpect(jsonPath("$.tierName", is("TIER_1")))
                .andExpect(jsonPath("$.memberCount", is(150)))
                .andExpect(jsonPath("$.billingInterval", is("MONTHLY")))
                .andExpect(jsonPath("$.priceUsd", is(5.99)))
                .andExpect(jsonPath("$.priceGhs", is(71.88)))
                .andExpect(jsonPath("$.dualCurrencyDisplay", is("GHS 71.88 ($5.99)")));

        verify(pricingService).getPricingTierForMemberCount(150);
        verify(currencyService).convertUsdToGhs(new BigDecimal("5.99"));
    }

    @Test
    @DisplayName("GET /api/pricing/calculate - Should fail with invalid member count")
    void calculatePricing_shouldFailWithInvalidMemberCount() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/pricing/calculate")
                        .param("memberCount", "0")
                        .param("billingInterval", "MONTHLY"))
                .andExpect(status().isBadRequest());
    }

    // ==================== CHURCH-SPECIFIC ENDPOINTS TESTS ====================

    @Test
    @WithMockUser(authorities = "ADMIN")
    @DisplayName("GET /api/pricing/church/current - ADMIN should get current tier")
    void getCurrentTierForChurch_asAdmin_shouldReturnTier() throws Exception {
        // Given
        CongregationPricingService.ChurchTierInfo tierInfo = new CongregationPricingService.ChurchTierInfo(
                tier1,
                150,
                200,
                75.0,
                50,
                false,
                false
        );
        when(pricingService.getChurchTierInfo(42L)).thenReturn(tierInfo);

        // When & Then
        mockMvc.perform(get("/api/pricing/church/current")
                        .param("churchId", "42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentMemberCount", is(150)))
                .andExpect(jsonPath("$.tierMaxMembers", is(200)))
                .andExpect(jsonPath("$.percentageUsed", is(75.0)))
                .andExpect(jsonPath("$.membersRemaining", is(50)));

        verify(pricingService).getChurchTierInfo(42L);
    }

    @Test
    @WithMockUser(authorities = "MEMBER")
    @DisplayName("GET /api/pricing/church/current - MEMBER should get current tier")
    void getCurrentTierForChurch_asMember_shouldReturnTier() throws Exception {
        // Given
        CongregationPricingService.ChurchTierInfo tierInfo = new CongregationPricingService.ChurchTierInfo(
                tier1, 150, 200, 75.0, 50, false, false
        );
        when(pricingService.getChurchTierInfo(42L)).thenReturn(tierInfo);

        // When & Then
        mockMvc.perform(get("/api/pricing/church/current")
                        .param("churchId", "42"))
                .andExpect(status().isOk());

        verify(pricingService).getChurchTierInfo(42L);
    }

    @Test
    @DisplayName("GET /api/pricing/church/current - Should fail without authentication")
    void getCurrentTierForChurch_withoutAuth_shouldFail() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/pricing/church/current")
                        .param("churchId", "42"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    @DisplayName("GET /api/pricing/church/upgrade-options - ADMIN should get upgrade options")
    void getUpgradeOptions_asAdmin_shouldReturnOptions() throws Exception {
        // Given
        CongregationPricingService.TierUpgradeInfo upgradeInfo =
                new CongregationPricingService.TierUpgradeInfo(
                        tier1,
                        tier2,
                        tier2,
                        210,
                        true
                );
        when(pricingService.getTierUpgradeInfo(42L)).thenReturn(upgradeInfo);

        // When & Then
        mockMvc.perform(get("/api/pricing/church/upgrade-options")
                        .param("churchId", "42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentMemberCount", is(210)))
                .andExpect(jsonPath("$.upgradeRequired", is(true)));

        verify(pricingService).getTierUpgradeInfo(42L);
    }

    @Test
    @WithMockUser(authorities = "TREASURER")
    @DisplayName("GET /api/pricing/church/upgrade-options - TREASURER should get upgrade options")
    void getUpgradeOptions_asTreasurer_shouldReturnOptions() throws Exception {
        // Given
        CongregationPricingService.TierUpgradeInfo upgradeInfo =
                new CongregationPricingService.TierUpgradeInfo(
                        tier1, tier2, tier2, 210, true
                );
        when(pricingService.getTierUpgradeInfo(42L)).thenReturn(upgradeInfo);

        // When & Then
        mockMvc.perform(get("/api/pricing/church/upgrade-options")
                        .param("churchId", "42"))
                .andExpect(status().isOk());

        verify(pricingService).getTierUpgradeInfo(42L);
    }

    @Test
    @WithMockUser(authorities = "MEMBER")
    @DisplayName("GET /api/pricing/church/upgrade-options - MEMBER should be denied")
    void getUpgradeOptions_asMember_shouldBeDenied() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/pricing/church/upgrade-options")
                        .param("churchId", "42"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    @DisplayName("GET /api/pricing/church/tier-check - Should return tier check status")
    void checkTierUpgradeRequired_shouldReturnStatus() throws Exception {
        // Given
        when(pricingService.checkTierUpgradeRequired(42L)).thenReturn(true);
        CongregationPricingService.ChurchTierInfo tierInfo =
                new CongregationPricingService.ChurchTierInfo(
                        tier1, 210, 200, 105.0, -10, true, true
                );
        when(pricingService.getChurchTierInfo(42L)).thenReturn(tierInfo);

        // When & Then
        mockMvc.perform(get("/api/pricing/church/tier-check")
                        .param("churchId", "42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.upgradeRequired", is(true)))
                .andExpect(jsonPath("$.currentMemberCount", is(210)))
                .andExpect(jsonPath("$.tierMaxMembers", is(200)))
                .andExpect(jsonPath("$.message", containsString("Tier upgrade required")));

        verify(pricingService).checkTierUpgradeRequired(42L);
        verify(pricingService).getChurchTierInfo(42L);
    }

    // ==================== SUPERADMIN ENDPOINTS TESTS ====================

    @Test
    @WithMockUser(authorities = "SUPERADMIN")
    @DisplayName("POST /api/pricing/tiers - SUPERADMIN should create tier")
    void createTier_asSuperadmin_shouldCreateTier() throws Exception {
        // Given
        CongregationPricingController.TierCreateRequest request =
                new CongregationPricingController.TierCreateRequest(
                        "TIER_6",
                        "Mega Church (5001+)",
                        "For very large congregations",
                        5001,
                        null,
                        new BigDecimal("29.99"),
                        new BigDecimal("87.97"),
                        new BigDecimal("175.94"),
                        new BigDecimal("349.88"),
                        "[\"All Features\"]",
                        6
                );

        CongregationPricingTier createdTier = CongregationPricingTier.builder()
                .id(6L)
                .tierName("TIER_6")
                .displayName("Mega Church (5001+)")
                .build();

        when(pricingService.createTier(
                eq("TIER_6"),
                eq("Mega Church (5001+)"),
                eq("For very large congregations"),
                eq(5001),
                isNull(),
                any(BigDecimal.class),
                any(BigDecimal.class),
                any(BigDecimal.class),
                any(BigDecimal.class),
                eq("[\"All Features\"]"),
                eq(6)
        )).thenReturn(createdTier);

        // When & Then
        mockMvc.perform(post("/api/pricing/tiers")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tierName", is("TIER_6")))
                .andExpect(jsonPath("$.displayName", is("Mega Church (5001+)")));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    @DisplayName("POST /api/pricing/tiers - ADMIN should be denied")
    void createTier_asAdmin_shouldBeDenied() throws Exception {
        // Given
        CongregationPricingController.TierCreateRequest request =
                new CongregationPricingController.TierCreateRequest(
                        "TIER_6", "Mega Church", "Desc", 5001, null,
                        new BigDecimal("29.99"), new BigDecimal("87.97"),
                        new BigDecimal("175.94"), new BigDecimal("349.88"),
                        "[\"Features\"]", 6
                );

        // When & Then
        mockMvc.perform(post("/api/pricing/tiers")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "SUPERADMIN")
    @DisplayName("PUT /api/pricing/tiers/{id} - SUPERADMIN should update tier")
    void updateTier_asSuperadmin_shouldUpdateTier() throws Exception {
        // Given
        CongregationPricingController.TierUpdateRequest request =
                new CongregationPricingController.TierUpdateRequest(
                        "Updated Display Name",
                        "Updated description",
                        new BigDecimal("6.99"),
                        null,
                        null,
                        null,
                        null,
                        null
                );

        CongregationPricingTier updatedTier = CongregationPricingTier.builder()
                .id(1L)
                .tierName("TIER_1")
                .displayName("Updated Display Name")
                .monthlyPriceUsd(new BigDecimal("6.99"))
                .build();

        when(pricingService.updateTier(
                eq(1L),
                eq("Updated Display Name"),
                eq("Updated description"),
                any(BigDecimal.class),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull()
        )).thenReturn(updatedTier);

        // When & Then
        mockMvc.perform(put("/api/pricing/tiers/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName", is("Updated Display Name")));
    }

    @Test
    @WithMockUser(authorities = "SUPERADMIN")
    @DisplayName("DELETE /api/pricing/tiers/{id} - SUPERADMIN should deactivate tier")
    void deactivateTier_asSuperadmin_shouldDeactivateTier() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/pricing/tiers/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", containsString("deactivated successfully")))
                .andExpect(jsonPath("$.tierId", is(1)));

        verify(pricingService).deactivateTier(1L);
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    @DisplayName("DELETE /api/pricing/tiers/{id} - ADMIN should be denied")
    void deactivateTier_asAdmin_shouldBeDenied() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/pricing/tiers/1")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }
}
