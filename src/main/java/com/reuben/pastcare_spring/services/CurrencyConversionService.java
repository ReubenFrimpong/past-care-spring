package com.reuben.pastcare_spring.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reuben.pastcare_spring.models.PlatformCurrencySettings;
import com.reuben.pastcare_spring.repositories.PlatformCurrencySettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for currency conversion and exchange rate management.
 *
 * <p>Manages platform-wide currency settings including:
 * <ul>
 *   <li>Exchange rate between USD (base) and GHS (display)</li>
 *   <li>Currency conversion calculations</li>
 *   <li>Dual currency formatting</li>
 *   <li>Exchange rate history tracking</li>
 *   <li>SUPERADMIN rate updates</li>
 * </ul>
 *
 * @since 2026-01-01
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CurrencyConversionService {

    private final PlatformCurrencySettingsRepository settingsRepository;
    private final ObjectMapper objectMapper;

    /**
     * Default exchange rate if none configured (GHS 12 = $1 USD).
     */
    private static final BigDecimal DEFAULT_EXCHANGE_RATE = new BigDecimal("12.0000");

    /**
     * Get current platform currency settings.
     *
     * <p>Cached for 5 minutes to reduce database queries.
     * Cache is automatically evicted when rate is updated.
     *
     * @return Current currency settings
     */
    @Cacheable(value = "currencySettings", unless = "#result == null")
    public PlatformCurrencySettings getCurrentSettings() {
        return settingsRepository.findCurrentSettings()
                .orElseGet(this::createDefaultSettings);
    }

    /**
     * Create default currency settings if none exist.
     *
     * @return Newly created default settings
     */
    @Transactional
    protected PlatformCurrencySettings createDefaultSettings() {
        log.info("No currency settings found. Creating default settings (GHS 12 = $1 USD)");

        PlatformCurrencySettings settings = PlatformCurrencySettings.builder()
                .baseCurrency("USD")
                .displayCurrency("GHS")
                .exchangeRate(DEFAULT_EXCHANGE_RATE)
                .showBothCurrencies(true)
                .primaryDisplayCurrency("GHS")
                .rateHistory("[]")
                .lastUpdatedAt(LocalDateTime.now())
                .build();

        return settingsRepository.save(settings);
    }

    /**
     * Get current exchange rate (GHS per 1 USD).
     *
     * @return Exchange rate
     */
    public BigDecimal getCurrentExchangeRate() {
        return getCurrentSettings().getExchangeRate();
    }

    /**
     * Convert USD amount to GHS (display currency).
     *
     * @param usdAmount Amount in USD
     * @return Amount in GHS, rounded to 2 decimal places
     */
    public BigDecimal convertUsdToGhs(BigDecimal usdAmount) {
        if (usdAmount == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal exchangeRate = getCurrentExchangeRate();
        return usdAmount.multiply(exchangeRate).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Convert GHS amount to USD (base currency).
     *
     * @param ghsAmount Amount in GHS
     * @return Amount in USD, rounded to 2 decimal places
     */
    public BigDecimal convertGhsToUsd(BigDecimal ghsAmount) {
        if (ghsAmount == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal exchangeRate = getCurrentExchangeRate();
        if (exchangeRate.compareTo(BigDecimal.ZERO) == 0) {
            log.error("Exchange rate is zero! Cannot convert GHS to USD");
            return BigDecimal.ZERO;
        }

        return ghsAmount.divide(exchangeRate, 2, RoundingMode.HALF_UP);
    }

    /**
     * Format amount for dual currency display.
     *
     * <p>Examples:
     * <ul>
     *   <li>Primary GHS: "GHS 75.00 ($5.99)"</li>
     *   <li>Primary USD: "$5.99 (GHS 75.00)"</li>
     *   <li>GHS only: "GHS 75.00"</li>
     * </ul>
     *
     * @param usdAmount Amount in USD
     * @return Formatted dual currency string
     */
    public String formatDualCurrency(BigDecimal usdAmount) {
        if (usdAmount == null) {
            return "GHS 0.00 ($0.00)";
        }

        PlatformCurrencySettings settings = getCurrentSettings();
        BigDecimal ghsAmount = convertUsdToGhs(usdAmount);

        String usdFormatted = String.format("$%.2f", usdAmount);
        String ghsFormatted = String.format("GHS %.2f", ghsAmount);

        if (!settings.getShowBothCurrencies()) {
            return "GHS".equals(settings.getPrimaryDisplayCurrency())
                    ? ghsFormatted
                    : usdFormatted;
        }

        if ("GHS".equals(settings.getPrimaryDisplayCurrency())) {
            return ghsFormatted + " (" + usdFormatted + ")";
        } else {
            return usdFormatted + " (" + ghsFormatted + ")";
        }
    }

    /**
     * Format amount in GHS only.
     *
     * @param usdAmount Amount in USD
     * @return "GHS 75.00"
     */
    public String formatGhsOnly(BigDecimal usdAmount) {
        BigDecimal ghsAmount = convertUsdToGhs(usdAmount);
        return String.format("GHS %.2f", ghsAmount);
    }

    /**
     * Format amount in USD only.
     *
     * @param usdAmount Amount in USD
     * @return "$5.99"
     */
    public String formatUsdOnly(BigDecimal usdAmount) {
        if (usdAmount == null) {
            return "$0.00";
        }
        return String.format("$%.2f", usdAmount);
    }

    /**
     * Update the exchange rate (SUPERADMIN only).
     *
     * <p>Automatically tracks:
     * <ul>
     *   <li>Previous rate</li>
     *   <li>Update timestamp</li>
     *   <li>Admin who performed update</li>
     *   <li>Rate history (for audit trail)</li>
     * </ul>
     *
     * @param newRate New exchange rate (GHS per 1 USD)
     * @param adminUserId SUPERADMIN user ID
     * @return Updated settings
     * @throws IllegalArgumentException if rate is invalid
     */
    @Transactional
    @CacheEvict(value = "currencySettings", allEntries = true)
    public PlatformCurrencySettings updateExchangeRate(BigDecimal newRate, Long adminUserId) {
        if (newRate == null || newRate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Exchange rate must be positive");
        }

        if (newRate.compareTo(new BigDecimal("100")) > 0) {
            log.warn("Exchange rate {} seems unusually high. Proceeding with caution.", newRate);
        }

        PlatformCurrencySettings settings = getCurrentSettings();
        BigDecimal oldRate = settings.getExchangeRate();

        // Update rate history
        List<RateHistoryEntry> history = getRateHistory();
        history.add(new RateHistoryEntry(
                oldRate,
                LocalDateTime.now(),
                adminUserId
        ));

        try {
            String historyJson = objectMapper.writeValueAsString(history);
            settings.setRateHistory(historyJson);
        } catch (Exception e) {
            log.error("Failed to serialize rate history", e);
            // Continue without history update
        }

        // Update rate
        settings.updateRate(newRate, adminUserId);

        PlatformCurrencySettings saved = settingsRepository.save(settings);

        log.info("Exchange rate updated from {} to {} by admin user {}. Change: {:.2f}%",
                oldRate, newRate, adminUserId, settings.getRateChangePercentage());

        return saved;
    }

    /**
     * Get exchange rate history.
     *
     * @return List of historical rate changes
     */
    public List<RateHistoryEntry> getRateHistory() {
        PlatformCurrencySettings settings = getCurrentSettings();
        String historyJson = settings.getRateHistory();

        if (historyJson == null || historyJson.isBlank() || "[]".equals(historyJson)) {
            return new ArrayList<>();
        }

        try {
            return objectMapper.readValue(
                    historyJson,
                    new TypeReference<List<RateHistoryEntry>>() {}
            );
        } catch (Exception e) {
            log.error("Failed to deserialize rate history", e);
            return new ArrayList<>();
        }
    }

    /**
     * Get statistics about exchange rate changes.
     *
     * @return Rate statistics
     */
    public ExchangeRateStats getExchangeRateStats() {
        PlatformCurrencySettings settings = getCurrentSettings();
        List<RateHistoryEntry> history = getRateHistory();

        BigDecimal currentRate = settings.getExchangeRate();
        BigDecimal previousRate = settings.getPreviousRate();
        BigDecimal changePercentage = settings.getRateChangePercentage();

        // Calculate min/max from history
        BigDecimal minRate = currentRate;
        BigDecimal maxRate = currentRate;
        LocalDateTime firstUpdate = settings.getLastUpdatedAt();

        for (RateHistoryEntry entry : history) {
            if (entry.rate().compareTo(minRate) < 0) {
                minRate = entry.rate();
            }
            if (entry.rate().compareTo(maxRate) > 0) {
                maxRate = entry.rate();
            }
            if (firstUpdate == null || entry.timestamp().isBefore(firstUpdate)) {
                firstUpdate = entry.timestamp();
            }
        }

        return new ExchangeRateStats(
                currentRate,
                previousRate,
                changePercentage,
                minRate,
                maxRate,
                history.size(),
                firstUpdate,
                settings.getLastUpdatedAt()
        );
    }

    /**
     * Update currency display preferences (SUPERADMIN only).
     *
     * @param showBothCurrencies Whether to show both USD and GHS
     * @param primaryDisplayCurrency Which currency to show first ("USD" or "GHS")
     * @return Updated settings
     */
    @Transactional
    @CacheEvict(value = "currencySettings", allEntries = true)
    public PlatformCurrencySettings updateDisplayPreferences(
            boolean showBothCurrencies,
            String primaryDisplayCurrency) {

        if (!"USD".equals(primaryDisplayCurrency) && !"GHS".equals(primaryDisplayCurrency)) {
            throw new IllegalArgumentException("Primary display currency must be USD or GHS");
        }

        PlatformCurrencySettings settings = getCurrentSettings();
        settings.setShowBothCurrencies(showBothCurrencies);
        settings.setPrimaryDisplayCurrency(primaryDisplayCurrency);

        log.info("Currency display preferences updated: showBoth={}, primary={}",
                showBothCurrencies, primaryDisplayCurrency);

        return settingsRepository.save(settings);
    }

    /**
     * Rate history entry for audit trail.
     */
    public record RateHistoryEntry(
            BigDecimal rate,
            LocalDateTime timestamp,
            Long updatedBy
    ) {}

    /**
     * Exchange rate statistics.
     */
    public record ExchangeRateStats(
            BigDecimal currentRate,
            BigDecimal previousRate,
            BigDecimal changePercentage,
            BigDecimal minHistoricalRate,
            BigDecimal maxHistoricalRate,
            int totalUpdates,
            LocalDateTime firstUpdate,
            LocalDateTime lastUpdate
    ) {
        public boolean hasIncreased() {
            return changePercentage != null && changePercentage.compareTo(BigDecimal.ZERO) > 0;
        }

        public boolean hasDecreased() {
            return changePercentage != null && changePercentage.compareTo(BigDecimal.ZERO) < 0;
        }

        public BigDecimal getVolatility() {
            if (minHistoricalRate.compareTo(BigDecimal.ZERO) == 0) {
                return BigDecimal.ZERO;
            }
            return maxHistoricalRate.subtract(minHistoricalRate)
                    .divide(minHistoricalRate, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }
    }
}
