package com.reuben.pastcare_spring.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reuben.pastcare_spring.models.PlatformCurrencySettings;
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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for CurrencySettingsController.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("CurrencySettingsController Tests")
class CurrencySettingsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CurrencyConversionService currencyService;

    private PlatformCurrencySettings settings;

    @BeforeEach
    void setUp() {
        settings = PlatformCurrencySettings.builder()
                .id(1L)
                .baseCurrency("USD")
                .displayCurrency("GHS")
                .exchangeRate(new BigDecimal("12.0000"))
                .previousRate(new BigDecimal("11.5000"))
                .showBothCurrencies(true)
                .primaryDisplayCurrency("GHS")
                .lastUpdatedAt(LocalDateTime.now())
                .lastUpdatedBy(1L)
                .rateHistory("[]")
                .build();
    }

    // ==================== PUBLIC ENDPOINTS TESTS ====================

    @Test
    @DisplayName("GET /api/platform/currency/settings - Should return currency settings without auth")
    void getCurrentSettings_shouldReturnSettings() throws Exception {
        // Given
        when(currencyService.getCurrentSettings()).thenReturn(settings);

        // When & Then
        mockMvc.perform(get("/api/platform/currency/settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.baseCurrency", is("USD")))
                .andExpect(jsonPath("$.displayCurrency", is("GHS")))
                .andExpect(jsonPath("$.exchangeRate", is(12.0)))
                .andExpect(jsonPath("$.showBothCurrencies", is(true)))
                .andExpect(jsonPath("$.primaryDisplayCurrency", is("GHS")));

        verify(currencyService).getCurrentSettings();
    }

    @Test
    @DisplayName("GET /api/platform/currency/convert - Should convert USD to GHS")
    void convertUsdToGhs_shouldReturnConversion() throws Exception {
        // Given
        BigDecimal usdAmount = new BigDecimal("5.99");
        BigDecimal ghsAmount = new BigDecimal("71.88");
        BigDecimal exchangeRate = new BigDecimal("12.0");

        when(currencyService.convertUsdToGhs(usdAmount)).thenReturn(ghsAmount);
        when(currencyService.getCurrentExchangeRate()).thenReturn(exchangeRate);
        when(currencyService.formatUsdOnly(usdAmount)).thenReturn("$5.99");
        when(currencyService.formatGhsOnly(usdAmount)).thenReturn("GHS 71.88");
        when(currencyService.formatDualCurrency(usdAmount)).thenReturn("GHS 71.88 ($5.99)");

        // When & Then
        mockMvc.perform(get("/api/platform/currency/convert")
                        .param("usdAmount", "5.99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usdAmount", is(5.99)))
                .andExpect(jsonPath("$.ghsAmount", is(71.88)))
                .andExpect(jsonPath("$.exchangeRate", is(12.0)))
                .andExpect(jsonPath("$.usdFormatted", is("$5.99")))
                .andExpect(jsonPath("$.ghsFormatted", is("GHS 71.88")))
                .andExpect(jsonPath("$.dualCurrencyFormatted", is("GHS 71.88 ($5.99)")));

        verify(currencyService).convertUsdToGhs(usdAmount);
    }

    @Test
    @DisplayName("GET /api/platform/currency/convert - Should fail with invalid amount")
    void convertUsdToGhs_withInvalidAmount_shouldFail() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/platform/currency/convert")
                        .param("usdAmount", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/platform/currency/format - Should format dual currency")
    void formatDualCurrency_shouldReturnFormatted() throws Exception {
        // Given
        BigDecimal usdAmount = new BigDecimal("9.99");
        when(currencyService.formatDualCurrency(usdAmount)).thenReturn("GHS 119.88 ($9.99)");
        when(currencyService.formatUsdOnly(usdAmount)).thenReturn("$9.99");
        when(currencyService.formatGhsOnly(usdAmount)).thenReturn("GHS 119.88");

        // When & Then
        mockMvc.perform(get("/api/platform/currency/format")
                        .param("usdAmount", "9.99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usdAmount", is("9.99")))
                .andExpect(jsonPath("$.formatted", is("GHS 119.88 ($9.99)")))
                .andExpect(jsonPath("$.usdOnly", is("$9.99")))
                .andExpect(jsonPath("$.ghsOnly", is("GHS 119.88")));

        verify(currencyService).formatDualCurrency(usdAmount);
    }

    // ==================== SUPERADMIN ENDPOINTS TESTS ====================

    @Test
    @WithMockUser(authorities = "SUPERADMIN")
    @DisplayName("PUT /api/platform/currency/exchange-rate - SUPERADMIN should update rate")
    void updateExchangeRate_asSuperadmin_shouldUpdateRate() throws Exception {
        // Given
        CurrencySettingsController.ExchangeRateUpdateRequest request =
                new CurrencySettingsController.ExchangeRateUpdateRequest(
                        new BigDecimal("12.50"),
                        1L
                );

        PlatformCurrencySettings updatedSettings = PlatformCurrencySettings.builder()
                .id(1L)
                .baseCurrency("USD")
                .displayCurrency("GHS")
                .exchangeRate(new BigDecimal("12.50"))
                .previousRate(new BigDecimal("12.0"))
                .build();

        when(currencyService.getCurrentExchangeRate()).thenReturn(new BigDecimal("12.0"));
        when(currencyService.updateExchangeRate(
                eq(new BigDecimal("12.50")),
                eq(1L)
        )).thenReturn(updatedSettings);

        // When & Then
        mockMvc.perform(put("/api/platform/currency/exchange-rate")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exchangeRate", is(12.50)))
                .andExpect(jsonPath("$.previousRate", is(12.0)));

        verify(currencyService).updateExchangeRate(new BigDecimal("12.50"), 1L);
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    @DisplayName("PUT /api/platform/currency/exchange-rate - ADMIN should be denied")
    void updateExchangeRate_asAdmin_shouldBeDenied() throws Exception {
        // Given
        CurrencySettingsController.ExchangeRateUpdateRequest request =
                new CurrencySettingsController.ExchangeRateUpdateRequest(
                        new BigDecimal("12.50"),
                        1L
                );

        // When & Then
        mockMvc.perform(put("/api/platform/currency/exchange-rate")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /api/platform/currency/exchange-rate - Should fail without auth")
    void updateExchangeRate_withoutAuth_shouldFail() throws Exception {
        // Given
        CurrencySettingsController.ExchangeRateUpdateRequest request =
                new CurrencySettingsController.ExchangeRateUpdateRequest(
                        new BigDecimal("12.50"),
                        1L
                );

        // When & Then
        mockMvc.perform(put("/api/platform/currency/exchange-rate")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "SUPERADMIN")
    @DisplayName("PUT /api/platform/currency/display-preferences - SUPERADMIN should update preferences")
    void updateDisplayPreferences_asSuperadmin_shouldUpdatePreferences() throws Exception {
        // Given
        CurrencySettingsController.DisplayPreferencesUpdateRequest request =
                new CurrencySettingsController.DisplayPreferencesUpdateRequest(
                        false,
                        "USD"
                );

        PlatformCurrencySettings updatedSettings = PlatformCurrencySettings.builder()
                .showBothCurrencies(false)
                .primaryDisplayCurrency("USD")
                .build();

        when(currencyService.updateDisplayPreferences(false, "USD"))
                .thenReturn(updatedSettings);

        // When & Then
        mockMvc.perform(put("/api/platform/currency/display-preferences")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.showBothCurrencies", is(false)))
                .andExpect(jsonPath("$.primaryDisplayCurrency", is("USD")));

        verify(currencyService).updateDisplayPreferences(false, "USD");
    }

    @Test
    @WithMockUser(authorities = "SUPERADMIN")
    @DisplayName("GET /api/platform/currency/rate-history - SUPERADMIN should get history")
    void getRateHistory_asSuperadmin_shouldReturnHistory() throws Exception {
        // Given
        List<CurrencyConversionService.RateHistoryEntry> history = Arrays.asList(
                new CurrencyConversionService.RateHistoryEntry(
                        new BigDecimal("11.5"),
                        LocalDateTime.now().minusDays(30),
                        1L
                ),
                new CurrencyConversionService.RateHistoryEntry(
                        new BigDecimal("12.0"),
                        LocalDateTime.now().minusDays(1),
                        1L
                )
        );

        when(currencyService.getRateHistory()).thenReturn(history);

        // When & Then
        mockMvc.perform(get("/api/platform/currency/rate-history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].rate", is(11.5)))
                .andExpect(jsonPath("$[1].rate", is(12.0)));

        verify(currencyService).getRateHistory();
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    @DisplayName("GET /api/platform/currency/rate-history - ADMIN should be denied")
    void getRateHistory_asAdmin_shouldBeDenied() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/platform/currency/rate-history"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "SUPERADMIN")
    @DisplayName("GET /api/platform/currency/stats - SUPERADMIN should get stats")
    void getExchangeRateStats_asSuperadmin_shouldReturnStats() throws Exception {
        // Given
        CurrencyConversionService.ExchangeRateStats stats =
                new CurrencyConversionService.ExchangeRateStats(
                        new BigDecimal("12.0"),
                        new BigDecimal("11.5"),
                        new BigDecimal("4.35"),
                        new BigDecimal("11.0"),
                        new BigDecimal("13.0"),
                        5,
                        LocalDateTime.now().minusMonths(6),
                        LocalDateTime.now()
                );

        when(currencyService.getExchangeRateStats()).thenReturn(stats);

        // When & Then
        mockMvc.perform(get("/api/platform/currency/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentRate", is(12.0)))
                .andExpect(jsonPath("$.previousRate", is(11.5)))
                .andExpect(jsonPath("$.changePercentage", is(4.35)))
                .andExpect(jsonPath("$.minHistoricalRate", is(11.0)))
                .andExpect(jsonPath("$.maxHistoricalRate", is(13.0)))
                .andExpect(jsonPath("$.totalUpdates", is(5)));

        verify(currencyService).getExchangeRateStats();
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    @DisplayName("GET /api/platform/currency/stats - ADMIN should be denied")
    void getExchangeRateStats_asAdmin_shouldBeDenied() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/platform/currency/stats"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "SUPERADMIN")
    @DisplayName("PUT /api/platform/currency/exchange-rate - Should validate positive rate")
    void updateExchangeRate_withNegativeRate_shouldFail() throws Exception {
        // Given
        CurrencySettingsController.ExchangeRateUpdateRequest request =
                new CurrencySettingsController.ExchangeRateUpdateRequest(
                        new BigDecimal("-1.0"),
                        1L
                );

        // When & Then
        mockMvc.perform(put("/api/platform/currency/exchange-rate")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
