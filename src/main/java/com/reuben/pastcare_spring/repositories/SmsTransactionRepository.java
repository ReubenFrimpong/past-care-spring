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

    Page<SmsTransaction> findByUserIdAndChurchId(Long userId, Long churchId, Pageable pageable);

    List<SmsTransaction> findByUserIdAndChurchIdAndCreatedAtBetween(
        Long userId, Long churchId, LocalDateTime start, LocalDateTime end);

    Optional<SmsTransaction> findByReferenceId(String referenceId);

    Optional<SmsTransaction> findByPaymentReference(String paymentReference);

    Page<SmsTransaction> findByChurchIdAndType(Long churchId, TransactionType type, Pageable pageable);
}
