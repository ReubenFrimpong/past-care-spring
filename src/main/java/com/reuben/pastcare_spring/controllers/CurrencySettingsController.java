package com.reuben.pastcare_spring.controllers;

import com.reuben.pastcare_spring.models.PlatformCurrencySettings;
import com.reuben.pastcare_spring.services.CurrencyConversionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * REST controller for currency settings and exchange rate management.
 *
 * <p>Public endpoints:
 * <ul>
 *   <li>GET /api/platform/currency/settings - Get current currency settings</li>
 *   <li>GET /api/platform/currency/convert - Convert USD to GHS</li>
 *   <li>GET /api/platform/currency/format - Format amount in dual currency</li>
 * </ul>
 *
 * <p>SUPERADMIN endpoints:
 * <ul>
 *   <li>PUT /api/platform/currency/exchange-rate - Update exchange rate</li>
 *   <li>PUT /api/platform/currency/display-preferences - Update display preferences</li>
 *   <li>GET /api/platform/currency/rate-history - Get exchange rate history</li>
 *   <li>GET /api/platform/currency/stats - Get exchange rate statistics</li>
 * </ul>
 *
 * @since 2026-01-01
 */
@RestController
@RequestMapping("/api/platform/currency")
@RequiredArgsConstructor
@Slf4j
@Validated
public class CurrencySettingsController {

    private final CurrencyConversionService currencyService;

    // ==================== PUBLIC ENDPOINTS ====================

    /**
     * Get current platform currency settings.
     *
     * <p><b>Public endpoint</b> - No authentication required.
     * Used to display pricing in correct currencies on landing page.
     *
     * @return Current currency settings
     */
    @GetMapping("/settings")
    public ResponseEntity<CurrencySettingsResponse> getCurrentSettings() {
        log.debug("GET /api/platform/currency/settings - Fetching currency settings");

        PlatformCurrencySettings settings = currencyService.getCurrentSettings();

        CurrencySettingsResponse response = new CurrencySettingsResponse(
                settings.getBaseCurrency(),
                settings.getDisplayCurrency(),
                settings.getExchangeRate(),
                settings.getPreviousRate(),
                settings.getShowBothCurrencies(),
                settings.getPrimaryDisplayCurrency(),
                settings.getLastUpdatedAt()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Convert USD amount to GHS.
     *
     * <p><b>Public endpoint</b> - No authentication required.
     *
     * <p>Example: GET /api/platform/currency/convert?usdAmount=5.99
     * Returns: {"usdAmount": 5.99, "ghsAmount": 71.88, "exchangeRate": 12.0}
     *
     * @param usdAmount Amount in USD
     * @return Conversion result
     */
    @GetMapping("/convert")
    public ResponseEntity<ConversionResponse> convertUsdToGhs(
            @RequestParam @NotNull @DecimalMin("0.01") BigDecimal usdAmount) {

        log.debug("GET /api/platform/currency/convert - usdAmount={}", usdAmount);

        BigDecimal ghsAmount = currencyService.convertUsdToGhs(usdAmount);
        BigDecimal exchangeRate = currencyService.getCurrentExchangeRate();

        ConversionResponse response = new ConversionResponse(
                usdAmount,
                ghsAmount,
                exchangeRate,
                currencyService.formatUsdOnly(usdAmount),
                currencyService.formatGhsOnly(usdAmount),
                currencyService.formatDualCurrency(usdAmount)
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Format amount for dual currency display.
     *
     * <p><b>Public endpoint</b> - No authentication required.
     *
     * <p>Example: GET /api/platform/currency/format?usdAmount=5.99
     * Returns: {"formatted": "GHS 71.88 ($5.99)"}
     *
     * @param usdAmount Amount in USD
     * @return Formatted string
     */
    @GetMapping("/format")
    public ResponseEntity<Map<String, String>> formatDualCurrency(
            @RequestParam @NotNull @DecimalMin("0.01") BigDecimal usdAmount) {

        log.debug("GET /api/platform/currency/format - usdAmount={}", usdAmount);

        String formatted = currencyService.formatDualCurrency(usdAmount);

        return ResponseEntity.ok(Map.of(
                "usdAmount", usdAmount.toString(),
                "formatted", formatted,
                "usdOnly", currencyService.formatUsdOnly(usdAmount),
                "ghsOnly", currencyService.formatGhsOnly(usdAmount)
        ));
    }

    // ==================== SUPERADMIN ENDPOINTS ====================

    /**
     * Update the platform exchange rate.
     *
     * <p><b>SUPERADMIN only</b> - Requires SUPERADMIN authority.
     *
     * <p>Example: PUT /api/platform/currency/exchange-rate
     * Body: {"newRate": 12.50, "adminUserId": 1}
     *
     * @param request Exchange rate update request
     * @return Updated currency settings
     */
    @PutMapping("/exchange-rate")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<PlatformCurrencySettings> updateExchangeRate(
            @Valid @RequestBody ExchangeRateUpdateRequest request) {

        log.info("PUT /api/platform/currency/exchange-rate - Updating rate from {} to {} (admin user: {})",
                currencyService.getCurrentExchangeRate(),
                request.newRate(),
                request.adminUserId());

        PlatformCurrencySettings settings = currencyService.updateExchangeRate(
                request.newRate(),
                request.adminUserId()
        );

        return ResponseEntity.ok(settings);
    }

    /**
     * Update currency display preferences.
     *
     * <p><b>SUPERADMIN only</b> - Requires SUPERADMIN authority.
     *
     * <p>Controls whether to show both currencies or just one,
     * and which currency to show first.
     *
     * @param request Display preferences update request
     * @return Updated currency settings
     */
    @PutMapping("/display-preferences")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<PlatformCurrencySettings> updateDisplayPreferences(
            @Valid @RequestBody DisplayPreferencesUpdateRequest request) {

        log.info("PUT /api/platform/currency/display-preferences - showBoth={}, primary={}",
                request.showBothCurrencies(),
                request.primaryDisplayCurrency());

        PlatformCurrencySettings settings = currencyService.updateDisplayPreferences(
                request.showBothCurrencies(),
                request.primaryDisplayCurrency()
        );

        return ResponseEntity.ok(settings);
    }

    /**
     * Get exchange rate change history.
     *
     * <p><b>SUPERADMIN only</b> - Requires SUPERADMIN authority.
     *
     * <p>Returns audit trail of all exchange rate changes.
     *
     * @return List of rate history entries
     */
    @GetMapping("/rate-history")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<List<CurrencyConversionService.RateHistoryEntry>> getRateHistory() {
        log.debug("GET /api/platform/currency/rate-history - Fetching rate history");

        List<CurrencyConversionService.RateHistoryEntry> history = currencyService.getRateHistory();

        return ResponseEntity.ok(history);
    }

    /**
     * Get exchange rate statistics.
     *
     * <p><b>SUPERADMIN only</b> - Requires SUPERADMIN authority.
     *
     * <p>Returns statistics about rate changes including:
     * <ul>
     *   <li>Current and previous rates</li>
     *   <li>Min/max historical rates</li>
     *   <li>Number of updates</li>
     *   <li>Volatility percentage</li>
     * </ul>
     *
     * @return Exchange rate statistics
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<CurrencyConversionService.ExchangeRateStats> getExchangeRateStats() {
        log.debug("GET /api/platform/currency/stats - Fetching exchange rate statistics");

        CurrencyConversionService.ExchangeRateStats stats = currencyService.getExchangeRateStats();

        return ResponseEntity.ok(stats);
    }

    // ==================== DTOs ====================

    /**
     * Response for currency settings.
     */
    public record CurrencySettingsResponse(
            String baseCurrency,
            String displayCurrency,
            BigDecimal exchangeRate,
            BigDecimal previousRate,
            boolean showBothCurrencies,
            String primaryDisplayCurrency,
            java.time.LocalDateTime lastUpdatedAt
    ) {}

    /**
     * Response for currency conversion.
     */
    public record ConversionResponse(
            BigDecimal usdAmount,
            BigDecimal ghsAmount,
            BigDecimal exchangeRate,
            String usdFormatted,
            String ghsFormatted,
            String dualCurrencyFormatted
    ) {}

    /**
     * Request to update exchange rate.
     */
    public record ExchangeRateUpdateRequest(
            @NotNull
            @DecimalMin(value = "0.01", message = "Exchange rate must be positive")
            BigDecimal newRate,

            @NotNull
            Long adminUserId
    ) {}

    /**
     * Request to update display preferences.
     */
    public record DisplayPreferencesUpdateRequest(
            boolean showBothCurrencies,

            @NotBlank
            String primaryDisplayCurrency // "USD" or "GHS"
    ) {}
}
