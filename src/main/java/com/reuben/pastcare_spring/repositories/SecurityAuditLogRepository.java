package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.SecurityAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for security audit log operations.
 */
@Repository
public interface SecurityAuditLogRepository extends JpaRepository<SecurityAuditLog, Long> {

    /**
     * Count violations by user after a specific timestamp.
     */
    long countByUserIdAndTimestampAfter(Long userId, LocalDateTime timestamp);

    /**
     * Count violations after a specific timestamp.
     */
    long countByTimestampAfter(LocalDateTime timestamp);

    /**
     * Find all violations for a specific user.
     */
    List<SecurityAuditLog> findByUserIdOrderByTimestampDesc(Long userId);

    /**
     * Find all violations for a specific church.
     */
    List<SecurityAuditLog> findByActualChurchIdOrderByTimestampDesc(Long churchId);

    /**
     * Find all violations after a specific timestamp.
     */
    List<SecurityAuditLog> findByTimestampAfterOrderByTimestampDesc(LocalDateTime timestamp);

    /**
     * Find unreviewed violations.
     */
    List<SecurityAuditLog> findByReviewedFalseOrderByTimestampDesc();

    /**
     * Find high severity violations.
     */
    List<SecurityAuditLog> findBySeverityOrderByTimestampDesc(String severity);
}
