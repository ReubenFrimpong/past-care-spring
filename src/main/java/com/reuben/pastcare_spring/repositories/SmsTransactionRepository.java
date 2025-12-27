package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.SmsTransaction;
import com.reuben.pastcare_spring.models.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SmsTransactionRepository extends JpaRepository<SmsTransaction, Long> {

    // Legacy user-based methods (deprecated - will be removed after full migration)
    Page<SmsTransaction> findByPerformedByIdAndChurchId(Long userId, Long churchId, Pageable pageable);

    List<SmsTransaction> findByPerformedByIdAndChurchIdAndCreatedAtBetween(
        Long userId, Long churchId, LocalDateTime start, LocalDateTime end);

    // Church-level transaction methods
    Page<SmsTransaction> findByChurchId(Long churchId, Pageable pageable);

    List<SmsTransaction> findByChurchIdOrderByCreatedAtDesc(Long churchId);

    List<SmsTransaction> findByChurchIdAndCreatedAtBetween(
        Long churchId, LocalDateTime start, LocalDateTime end);

    // Reference lookups
    Optional<SmsTransaction> findByReferenceId(String referenceId);

    Optional<SmsTransaction> findByPaymentReference(String paymentReference);

    // Type-based queries
    Page<SmsTransaction> findByChurchIdAndType(Long churchId, TransactionType type, Pageable pageable);

    List<SmsTransaction> findByChurchIdAndTypeOrderByCreatedAtDesc(Long churchId, TransactionType type);
}
