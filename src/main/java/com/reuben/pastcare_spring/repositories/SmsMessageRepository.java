package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.SmsMessage;
import com.reuben.pastcare_spring.models.SmsStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SmsMessageRepository extends JpaRepository<SmsMessage, Long> {

    Page<SmsMessage> findBySenderIdAndChurchId(Long senderId, Long churchId, Pageable pageable);

    Page<SmsMessage> findByChurchId(Long churchId, Pageable pageable);

    List<SmsMessage> findByStatusAndScheduledTimeBefore(SmsStatus status, LocalDateTime time);

    Optional<SmsMessage> findByGatewayMessageId(String gatewayMessageId);

    @Query("SELECT COUNT(m) FROM SmsMessage m WHERE m.sender.id = :senderId AND m.church.id = :churchId")
    long countBySenderIdAndChurchId(@Param("senderId") Long senderId, @Param("churchId") Long churchId);

    @Query("SELECT COUNT(m) FROM SmsMessage m WHERE m.sender.id = :senderId AND m.church.id = :churchId AND m.status = :status")
    long countBySenderIdAndChurchIdAndStatus(
        @Param("senderId") Long senderId,
        @Param("churchId") Long churchId,
        @Param("status") SmsStatus status);

    @Query("SELECT SUM(m.cost) FROM SmsMessage m WHERE m.sender.id = :senderId AND m.church.id = :churchId AND m.status IN :statuses")
    Double sumCostBySenderIdAndChurchIdAndStatusIn(
        @Param("senderId") Long senderId,
        @Param("churchId") Long churchId,
        @Param("statuses") List<SmsStatus> statuses);

    /**
     * Find failed messages eligible for retry
     * Criteria:
     * - Status = FAILED
     * - Retry count < maxRetries
     * - Last retry was before the threshold (or never retried)
     */
    @Query("SELECT m FROM SmsMessage m WHERE m.status = 'FAILED' " +
           "AND m.retryCount < :maxRetries " +
           "AND (m.lastRetryAt IS NULL OR m.lastRetryAt < :retryThreshold)")
    List<SmsMessage> findFailedMessagesForRetry(
        @Param("maxRetries") int maxRetries,
        @Param("retryThreshold") LocalDateTime retryThreshold);
}
