package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.Church;
import com.reuben.pastcare_spring.models.Member;
import com.reuben.pastcare_spring.models.RecurringDonation;
import com.reuben.pastcare_spring.models.RecurringDonationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RecurringDonationRepository extends JpaRepository<RecurringDonation, Long> {

    // Find by church and status
    Page<RecurringDonation> findByChurchAndStatus(Church church, RecurringDonationStatus status, Pageable pageable);

    // Find by church
    Page<RecurringDonation> findByChurch(Church church, Pageable pageable);

    // Find by member
    Page<RecurringDonation> findByChurchAndMember(Church church, Member member, Pageable pageable);

    // Find active recurring donations by member
    List<RecurringDonation> findByChurchAndMemberAndStatus(Church church, Member member, RecurringDonationStatus status);

    // Find by ID and church
    Optional<RecurringDonation> findByIdAndChurch(Long id, Church church);

    // Find all active recurring donations due for charging
    @Query("SELECT r FROM RecurringDonation r WHERE r.church.id = :churchId " +
           "AND r.status = 'ACTIVE' " +
           "AND r.nextChargeDate <= :date")
    List<RecurringDonation> findDueForCharging(@Param("churchId") Long churchId, @Param("date") LocalDate date);

    // Find recurring donations with consecutive failures
    @Query("SELECT r FROM RecurringDonation r WHERE r.church.id = :churchId " +
           "AND r.consecutiveFailures >= :threshold")
    List<RecurringDonation> findWithConsecutiveFailures(@Param("churchId") Long churchId, @Param("threshold") Integer threshold);

    // Count active recurring donations by church
    Long countByChurchAndStatus(Church church, RecurringDonationStatus status);

    // Find recurring donations ending soon
    @Query("SELECT r FROM RecurringDonation r WHERE r.church.id = :churchId " +
           "AND r.status = 'ACTIVE' " +
           "AND r.endDate IS NOT NULL " +
           "AND r.endDate BETWEEN :startDate AND :endDate")
    List<RecurringDonation> findEndingSoon(@Param("churchId") Long churchId,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);

    // Find by Paystack authorization code
    Optional<RecurringDonation> findByChurchAndPaystackAuthorizationCode(Church church, String authorizationCode);

    // Find by Paystack customer code
    List<RecurringDonation> findByChurchAndPaystackCustomerCode(Church church, String customerCode);
}
