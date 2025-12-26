package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.Church;
import com.reuben.pastcare_spring.models.Member;
import com.reuben.pastcare_spring.models.PaymentTransaction;
import com.reuben.pastcare_spring.models.PaymentTransactionStatus;
import com.reuben.pastcare_spring.models.RecurringDonation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    // Find by church and status
    Page<PaymentTransaction> findByChurchAndStatus(Church church, PaymentTransactionStatus status, Pageable pageable);

    // Find by church
    Page<PaymentTransaction> findByChurch(Church church, Pageable pageable);

    // Find by member
    Page<PaymentTransaction> findByChurchAndMember(Church church, Member member, Pageable pageable);

    // Find by payment reference
    Optional<PaymentTransaction> findByPaymentReference(String paymentReference);

    // Find by Paystack reference
    Optional<PaymentTransaction> findByPaystackReference(String paystackReference);

    // Find by recurring donation
    Page<PaymentTransaction> findByChurchAndRecurringDonation(Church church, RecurringDonation recurringDonation, Pageable pageable);

    // Find failed transactions ready for retry
    @Query("SELECT t FROM PaymentTransaction t WHERE t.church.id = :churchId " +
           "AND t.status = 'FAILED' " +
           "AND t.nextRetryAt IS NOT NULL " +
           "AND t.nextRetryAt <= :now " +
           "AND t.retryCount < :maxRetries")
    List<PaymentTransaction> findReadyForRetry(@Param("churchId") Long churchId,
                                                @Param("now") LocalDateTime now,
                                                @Param("maxRetries") Integer maxRetries);

    // Count transactions by status
    Long countByChurchAndStatus(Church church, PaymentTransactionStatus status);

    // Find transactions in date range
    @Query("SELECT t FROM PaymentTransaction t WHERE t.church.id = :churchId " +
           "AND t.createdAt BETWEEN :startDate AND :endDate")
    List<PaymentTransaction> findByDateRange(@Param("churchId") Long churchId,
                                             @Param("startDate") Instant startDate,
                                             @Param("endDate") Instant endDate);
}
