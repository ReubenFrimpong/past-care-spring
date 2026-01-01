package com.reuben.pastcare_spring.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Entity for platform-wide currency and exchange rate settings.
 *
 * <p>Manages the exchange rate between base currency (USD) and display currency (GHS).
 * SUPERADMIN can update the exchange rate, and history is tracked.
 *
 * @since 2026-01-01
 */
@Entity
@Table(name = "platform_currency_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlatformCurrencySettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Base currency for all pricing (always USD).
     */
    @Column(nullable = false, length = 3)
    @Builder.Default
    private String baseCurrency = "USD";

    /**
     * Display currency for customers (GHS for Ghana).
     */
    @Column(nullable = false, length = 3)
    @Builder.Default
    private String displayCurrency = "GHS";

    /**
     * Current exchange rate (GHS per 1 USD).
     * Example: 12.0000 means GHS 12 = $1 USD
     */
    @Column(nullable = false, precision = 10, scale = 4)
    @Builder.Default
    private BigDecimal exchangeRate = new BigDecimal("12.0000");

    /**
     * ID of SUPERADMIN user who last updated the rate.
     */
    @Column
    private Long lastUpdatedBy;

    /**
     * Timestamp of last rate update.
     */
    @Column
    private LocalDateTime lastUpdatedAt;

    /**
     * Previous exchange rate before last update.
     */
    @Column(precision = 10, scale = 4)
    private BigDecimal previousRate;

    /**
     * Historical rate changes as JSON array.
     * Format: [{"rate": 12.0, "date": "2026-01-01T10:00:00", "updatedBy": 1}]
     */
    @Column(columnDefinition = "TEXT")
    private String rateHistory;

    /**
     * Whether to show both currencies (USD and GHS) on frontend.
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean showBothCurrencies = true;

    /**
     * Which currency to display first/primarily (USD or GHS).
     */
    @Column(nullable = false, length = 3)
    @Builder.Default
    private String primaryDisplayCurrency = "GHS";

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Convert USD amount to display currency (GHS).
     *
     * @param usdAmount Amount in USD
     * @return Amount in GHS, rounded to 2 decimal places
     */
    public BigDecimal convertToDisplay(BigDecimal usdAmount) {
        if (usdAmount == null) {
            return BigDecimal.ZERO;
        }
        return usdAmount.multiply(exchangeRate).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Convert display currency (GHS) amount to base currency (USD).
     *
     * @param displayAmount Amount in GHS
     * @return Amount in USD, rounded to 2 decimal places
     */
    public BigDecimal convertToBase(BigDecimal displayAmount) {
        if (displayAmount == null || exchangeRate.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return displayAmount.divide(exchangeRate, 2, RoundingMode.HALF_UP);
    }

    /**
     * Format amount for dual currency display.
     *
     * @param usdAmount Amount in USD
     * @return Formatted string like "GHS 75 ($5.99)" or "$5.99 (GHS 75)"
     */
    public String formatDualCurrency(BigDecimal usdAmount) {
        if (usdAmount == null) {
            return "GHS 0.00 ($0.00)";
        }

        BigDecimal ghsAmount = convertToDisplay(usdAmount);
        String usdFormatted = String.format("$%.2f", usdAmount);
        String ghsFormatted = String.format("GHS %.2f", ghsAmount);

        if (!showBothCurrencies) {
            return "GHS".equals(primaryDisplayCurrency) ? ghsFormatted : usdFormatted;
        }

        if ("GHS".equals(primaryDisplayCurrency)) {
            return ghsFormatted + " (" + usdFormatted + ")";
        } else {
            return usdFormatted + " (" + ghsFormatted + ")";
        }
    }

    /**
     * Update the exchange rate and track history.
     *
     * @param newRate New exchange rate
     * @param updatedBy SUPERADMIN user ID
     */
    public void updateRate(BigDecimal newRate, Long updatedBy) {
        if (newRate == null || newRate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Exchange rate must be positive");
        }

        this.previousRate = this.exchangeRate;
        this.exchangeRate = newRate;
        this.lastUpdatedBy = updatedBy;
        this.lastUpdatedAt = LocalDateTime.now();

        // Rate history is managed by service layer
    }

    /**
     * Get the percentage change from previous rate.
     *
     * @return Percentage change (positive = rate increased, negative = rate decreased)
     */
    public BigDecimal getRateChangePercentage() {
        if (previousRate == null || previousRate.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal change = exchangeRate.subtract(previousRate);
        return change.divide(previousRate, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    @Override
    public String toString() {
        return "PlatformCurrencySettings{" +
                "id=" + id +
                ", exchangeRate=" + exchangeRate +
                " " + displayCurrency + "/" + baseCurrency +
                ", lastUpdatedAt=" + lastUpdatedAt +
                '}';
    }
}
