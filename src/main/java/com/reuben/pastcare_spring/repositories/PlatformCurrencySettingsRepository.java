package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.PlatformCurrencySettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for {@link PlatformCurrencySettings} entities.
 *
 * <p>Typically only one record exists in this table for platform-wide settings.
 *
 * @since 2026-01-01
 */
@Repository
public interface PlatformCurrencySettingsRepository extends JpaRepository<PlatformCurrencySettings, Long> {

    /**
     * Get the current platform currency settings.
     *
     * <p>Returns the most recently updated settings record.
     *
     * @return Optional containing current settings
     */
    @Query("""
        SELECT pcs FROM PlatformCurrencySettings pcs
        ORDER BY pcs.updatedAt DESC
        LIMIT 1
        """)
    Optional<PlatformCurrencySettings> findCurrentSettings();

    /**
     * Get settings for a specific currency pair.
     *
     * @param baseCurrency Base currency code (e.g., "USD")
     * @param displayCurrency Display currency code (e.g., "GHS")
     * @return Optional containing matching settings
     */
    Optional<PlatformCurrencySettings> findByBaseCurrencyAndDisplayCurrency(
            String baseCurrency,
            String displayCurrency
    );
}
