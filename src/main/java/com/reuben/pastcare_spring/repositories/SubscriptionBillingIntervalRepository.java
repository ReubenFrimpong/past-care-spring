package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.SubscriptionBillingInterval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link SubscriptionBillingInterval} entities.
 *
 * @since 2026-01-01
 */
@Repository
public interface SubscriptionBillingIntervalRepository extends JpaRepository<SubscriptionBillingInterval, Long> {

    /**
     * Find interval by name.
     *
     * @param intervalName Interval name (e.g., "MONTHLY", "QUARTERLY")
     * @return Optional containing interval if found
     */
    Optional<SubscriptionBillingInterval> findByIntervalName(String intervalName);

    /**
     * Find all active intervals ordered by display order.
     *
     * @return List of active intervals
     */
    List<SubscriptionBillingInterval> findByIsActiveTrueOrderByDisplayOrderAsc();

    /**
     * Find interval by number of months.
     *
     * @param months Number of months (1, 3, 6, 12)
     * @return Optional containing interval if found
     */
    Optional<SubscriptionBillingInterval> findByMonths(Integer months);

    /**
     * Check if interval name exists.
     *
     * @param intervalName Interval name to check
     * @return true if exists, false otherwise
     */
    boolean existsByIntervalName(String intervalName);
}
