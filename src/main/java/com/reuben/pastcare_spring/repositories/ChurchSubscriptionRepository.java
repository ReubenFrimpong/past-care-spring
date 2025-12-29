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
}
