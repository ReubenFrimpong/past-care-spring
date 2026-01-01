package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.ChurchSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for ChurchSubscription entity.
 */
@Repository
public interface ChurchSubscriptionRepository extends JpaRepository<ChurchSubscription, Long> {

    /**
     * Find subscription by church ID.
     */
    Optional<ChurchSubscription> findByChurchId(Long churchId);

    /**
     * Find subscription by Paystack customer code.
     */
    Optional<ChurchSubscription> findByPaystackCustomerCode(String paystackCustomerCode);

    /**
     * Find subscription by Paystack subscription code.
     */
    Optional<ChurchSubscription> findByPaystackSubscriptionCode(String paystackSubscriptionCode);

    /**
     * Find all subscriptions by status.
     */
    List<ChurchSubscription> findByStatus(String status);

    /**
     * Find all subscriptions due for billing.
     */
    List<ChurchSubscription> findByNextBillingDateBeforeAndAutoRenewTrue(LocalDate date);

    /**
     * Find all past due subscriptions.
     */
    List<ChurchSubscription> findByStatusAndNextBillingDateBefore(String status, LocalDate date);

    /**
     * Count subscriptions by status.
     */
    long countByStatus(String status);

    /**
     * Count active subscriptions (ACTIVE only).
     */
    @Query("SELECT COUNT(cs) FROM ChurchSubscription cs WHERE cs.status = 'ACTIVE'")
    long countActiveSubscriptions();

    /**
     * Find subscriptions eligible for data deletion.
     * Eligible if: status = SUSPENDED AND data_retention_end_date <= today AND deletion_warning_sent_at is at least 7 days ago
     */
    @Query("SELECT cs FROM ChurchSubscription cs WHERE cs.status = 'SUSPENDED' " +
           "AND cs.dataRetentionEndDate IS NOT NULL " +
           "AND cs.dataRetentionEndDate <= :today " +
           "AND cs.deletionWarningSentAt IS NOT NULL " +
           "AND cs.deletionWarningSentAt <= :sevenDaysAgo")
    List<ChurchSubscription> findEligibleForDeletion(LocalDate today, java.time.LocalDateTime sevenDaysAgo);

    /**
     * Find subscriptions needing deletion warning emails.
     * Need warning if: status = SUSPENDED AND deletion warning not sent yet AND retention end date within 7 days
     */
    @Query("SELECT cs FROM ChurchSubscription cs WHERE cs.status = 'SUSPENDED' " +
           "AND cs.dataRetentionEndDate IS NOT NULL " +
           "AND cs.deletionWarningSentAt IS NULL " +
           "AND cs.dataRetentionEndDate <= :warningThreshold")
    List<ChurchSubscription> findNeedingDeletionWarning(LocalDate warningThreshold);

    /**
     * Delete subscription by church ID.
     */
    void deleteByChurchId(Long churchId);

    /**
     * Find active subscription by church ID.
     * Active means status = 'ACTIVE' (excludes PAST_DUE, CANCELED, SUSPENDED).
     */
    @Query("SELECT cs FROM ChurchSubscription cs WHERE cs.churchId = :churchId AND cs.status = 'ACTIVE'")
    Optional<ChurchSubscription> findActiveByChurchId(Long churchId);
}
