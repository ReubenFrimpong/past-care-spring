package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.Church;
import com.reuben.pastcare_spring.models.Pledge;
import com.reuben.pastcare_spring.models.PledgePayment;
import com.reuben.pastcare_spring.models.PledgePaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Giving Module Phase 3: Pledge & Campaign Management
 * Repository for pledge payment data access
 */
@Repository
public interface PledgePaymentRepository extends JpaRepository<PledgePayment, Long> {

  /**
   * Find all pledge payments for a church
   */
  List<PledgePayment> findByChurch(Church church);

  /**
   * Find pledge payment by ID and church (multi-tenant safety)
   */
  Optional<PledgePayment> findByIdAndChurch(Long id, Church church);

  /**
   * Find all payments for a pledge
   */
  List<PledgePayment> findByPledge(Pledge pledge);

  /**
   * Find payments by pledge and church
   */
  List<PledgePayment> findByPledgeAndChurch(Pledge pledge, Church church);

  /**
   * Find payments by status
   */
  List<PledgePayment> findByChurchAndStatus(Church church, PledgePaymentStatus status);

  /**
   * Find pending payments
   */
  @Query("SELECT pp FROM PledgePayment pp WHERE pp.church = :church AND pp.status = 'PENDING' ORDER BY pp.dueDate ASC")
  List<PledgePayment> findPendingPayments(@Param("church") Church church);

  /**
   * Find paid payments for a pledge
   */
  @Query("SELECT pp FROM PledgePayment pp WHERE pp.pledge = :pledge AND pp.status = 'PAID' ORDER BY pp.paymentDate DESC")
  List<PledgePayment> findPaidPaymentsByPledge(@Param("pledge") Pledge pledge);

  /**
   * Find overdue payments
   */
  @Query("SELECT pp FROM PledgePayment pp WHERE pp.church = :church AND pp.status IN ('PENDING', 'LATE') AND pp.dueDate < :currentDate ORDER BY pp.dueDate ASC")
  List<PledgePayment> findOverduePayments(
      @Param("church") Church church,
      @Param("currentDate") LocalDate currentDate
  );

  /**
   * Find payments due within date range
   */
  @Query("SELECT pp FROM PledgePayment pp WHERE pp.church = :church AND pp.status = 'PENDING' AND pp.dueDate BETWEEN :startDate AND :endDate ORDER BY pp.dueDate ASC")
  List<PledgePayment> findPaymentsDueInRange(
      @Param("church") Church church,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate
  );

  /**
   * Count payments by pledge
   */
  long countByPledge(Pledge pledge);

  /**
   * Count paid payments by pledge
   */
  @Query("SELECT COUNT(pp) FROM PledgePayment pp WHERE pp.pledge = :pledge AND pp.status = 'PAID'")
  long countPaidPaymentsByPledge(@Param("pledge") Pledge pledge);

  /**
   * Count pending payments by pledge
   */
  @Query("SELECT COUNT(pp) FROM PledgePayment pp WHERE pp.pledge = :pledge AND pp.status = 'PENDING'")
  long countPendingPaymentsByPledge(@Param("pledge") Pledge pledge);

  /**
   * Find next payment for a pledge
   */
  @Query("SELECT pp FROM PledgePayment pp WHERE pp.pledge = :pledge AND pp.status = 'PENDING' ORDER BY pp.dueDate ASC LIMIT 1")
  Optional<PledgePayment> findNextPaymentByPledge(@Param("pledge") Pledge pledge);

  /**
   * Find last paid payment for a pledge
   */
  @Query("SELECT pp FROM PledgePayment pp WHERE pp.pledge = :pledge AND pp.status = 'PAID' ORDER BY pp.paymentDate DESC LIMIT 1")
  Optional<PledgePayment> findLastPaidPaymentByPledge(@Param("pledge") Pledge pledge);
}
