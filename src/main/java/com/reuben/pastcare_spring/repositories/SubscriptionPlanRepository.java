package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for SubscriptionPlan entity.
 */
@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {

    /**
     * Find plan by name.
     */
    Optional<SubscriptionPlan> findByName(String name);

    /**
     * Find all active plans.
     */
    List<SubscriptionPlan> findByIsActiveTrueOrderByDisplayOrderAsc();

    /**
     * Find free plan.
     */
    Optional<SubscriptionPlan> findByIsFreeTrueAndIsActiveTrue();

    /**
     * Find plan by Paystack plan code.
     */
    Optional<SubscriptionPlan> findByPaystackPlanCode(String paystackPlanCode);
}
