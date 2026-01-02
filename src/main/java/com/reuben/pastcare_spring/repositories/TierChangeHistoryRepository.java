package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.TierChangeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for tier change history operations.
 * Provides queries for finding tier changes by various criteria.
 */
@Repository
public interface TierChangeHistoryRepository extends JpaRepository<TierChangeHistory, Long> {

    /**
     * Find tier change history by payment reference (unique identifier)
     */
    Optional<TierChangeHistory> findByPaymentReference(String paymentReference);

    /**
     * Find all tier changes for a church, ordered by most recent first
     */
    @Query("SELECT t FROM TierChangeHistory t WHERE t.church.id = :churchId ORDER BY t.changeRequestedAt DESC")
    List<TierChangeHistory> findByChurchIdOrderByChangeRequestedAtDesc(@Param("churchId") Long churchId);

    /**
     * Count completed upgrades for a subscription
     */
    @Query("SELECT COUNT(t) FROM TierChangeHistory t WHERE t.subscription.id = :subscriptionId AND t.paymentStatus = 'COMPLETED'")
    int countCompletedUpgradesBySubscription(@Param("subscriptionId") Long subscriptionId);

    /**
     * Find pending tier change for a church (should only be one at a time)
     */
    @Query("SELECT t FROM TierChangeHistory t WHERE t.church.id = :churchId AND t.paymentStatus = 'PENDING'")
    Optional<TierChangeHistory> findPendingUpgradeByChurchId(@Param("churchId") Long churchId);

    /**
     * Check if subscription has been upgraded since a specific time
     */
    @Query("SELECT COUNT(t) > 0 FROM TierChangeHistory t WHERE t.subscription.id = :subscriptionId AND t.changeRequestedAt > :since AND t.paymentStatus = 'COMPLETED'")
    boolean hasUpgradedSince(@Param("subscriptionId") Long subscriptionId, @Param("since") LocalDateTime since);

    /**
     * Find all tier changes for a specific subscription
     */
    @Query("SELECT t FROM TierChangeHistory t WHERE t.subscription.id = :subscriptionId ORDER BY t.changeRequestedAt DESC")
    List<TierChangeHistory> findBySubscriptionIdOrderByChangeRequestedAtDesc(@Param("subscriptionId") Long subscriptionId);

    /**
     * Find failed tier changes that need cleanup
     */
    @Query("SELECT t FROM TierChangeHistory t WHERE t.paymentStatus = 'FAILED' AND t.changeRequestedAt < :before")
    List<TierChangeHistory> findFailedUpgradesBefore(@Param("before") LocalDateTime before);
}
