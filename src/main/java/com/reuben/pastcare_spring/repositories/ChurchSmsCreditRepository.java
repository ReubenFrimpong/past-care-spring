package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.Church;
import com.reuben.pastcare_spring.models.ChurchSmsCredit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChurchSmsCreditRepository extends JpaRepository<ChurchSmsCredit, Long> {

    /**
     * Find SMS credit record by church
     */
    Optional<ChurchSmsCredit> findByChurch(Church church);

    /**
     * Find SMS credit record by church ID
     */
    @Query("SELECT c FROM ChurchSmsCredit c WHERE c.church.id = :churchId")
    Optional<ChurchSmsCredit> findByChurchId(@Param("churchId") Long churchId);

    /**
     * Check if a church has SMS credits set up
     */
    boolean existsByChurch(Church church);

    /**
     * Find churches with low balance (below threshold)
     */
    @Query("SELECT c FROM ChurchSmsCredit c WHERE c.balance < c.lowBalanceThreshold")
    List<ChurchSmsCredit> findChurchesWithLowBalance();

    /**
     * Find churches with low balance that haven't been alerted
     */
    @Query("SELECT c FROM ChurchSmsCredit c WHERE c.balance < c.lowBalanceThreshold AND c.lowBalanceAlertSent = false")
    List<ChurchSmsCredit> findChurchesNeedingLowBalanceAlert();

    /**
     * Find churches with balance above a certain amount
     */
    @Query("SELECT c FROM ChurchSmsCredit c WHERE c.balance >= :minBalance")
    List<ChurchSmsCredit> findChurchesWithMinBalance(@Param("minBalance") BigDecimal minBalance);

    /**
     * Get total credits across all churches
     */
    @Query("SELECT COALESCE(SUM(c.balance), 0) FROM ChurchSmsCredit c")
    BigDecimal getTotalCreditsAcrossAllChurches();

    /**
     * Get total purchased credits across all churches
     */
    @Query("SELECT COALESCE(SUM(c.totalPurchased), 0) FROM ChurchSmsCredit c")
    BigDecimal getTotalPurchasedAcrossAllChurches();

    /**
     * Get total used credits across all churches
     */
    @Query("SELECT COALESCE(SUM(c.totalUsed), 0) FROM ChurchSmsCredit c")
    BigDecimal getTotalUsedAcrossAllChurches();

    /**
     * Count churches with zero balance
     */
    @Query("SELECT COUNT(c) FROM ChurchSmsCredit c WHERE c.balance = 0")
    long countChurchesWithZeroBalance();

    /**
     * Find churches that have never purchased credits
     */
    @Query("SELECT c FROM ChurchSmsCredit c WHERE c.lastPurchaseAt IS NULL")
    List<ChurchSmsCredit> findChurchesWithNoPurchaseHistory();
}
