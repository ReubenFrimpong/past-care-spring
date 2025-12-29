package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Payment entity.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Find payment by Paystack reference.
     */
    Optional<Payment> findByPaystackReference(String paystackReference);

    /**
     * Find payment by Paystack transaction ID.
     */
    Optional<Payment> findByPaystackTransactionId(String paystackTransactionId);

    /**
     * Find all payments for a church.
     */
    List<Payment> findByChurchIdOrderByCreatedAtDesc(Long churchId);

    /**
     * Find all successful payments for a church.
     */
    List<Payment> findByChurchIdAndStatusOrderByPaymentDateDesc(Long churchId, String status);

    /**
     * Find payments by status.
     */
    List<Payment> findByStatus(String status);

    /**
     * Find payments within date range.
     */
    List<Payment> findByPaymentDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find payments by church and date range.
     */
    List<Payment> findByChurchIdAndPaymentDateBetween(Long churchId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Calculate total revenue.
     */
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'SUCCESS'")
    BigDecimal calculateTotalRevenue();

    /**
     * Calculate revenue for a church.
     */
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.churchId = :churchId AND p.status = 'SUCCESS'")
    BigDecimal calculateChurchRevenue(@Param("churchId") Long churchId);

    /**
     * Calculate revenue within date range.
     */
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'SUCCESS' AND p.paymentDate BETWEEN :startDate AND :endDate")
    BigDecimal calculateRevenueBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Count successful payments.
     */
    long countByStatus(String status);

    /**
     * Count payments for a church.
     */
    long countByChurchId(Long churchId);

    /**
     * Find recent payments (last N days).
     */
    @Query("SELECT p FROM Payment p WHERE p.paymentDate >= :fromDate ORDER BY p.paymentDate DESC")
    List<Payment> findRecentPayments(@Param("fromDate") LocalDateTime fromDate);
}
