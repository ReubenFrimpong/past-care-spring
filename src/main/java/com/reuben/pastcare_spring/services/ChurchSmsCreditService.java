package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.models.*;
import com.reuben.pastcare_spring.repositories.ChurchSmsCreditRepository;
import com.reuben.pastcare_spring.repositories.ChurchRepository;
import com.reuben.pastcare_spring.repositories.SmsTransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing church-level SMS credits
 * Replaces individual user credit management with shared church wallet
 */
@Service
@Slf4j
public class ChurchSmsCreditService {

    private final ChurchSmsCreditRepository churchSmsCreditRepository;
    private final SmsTransactionRepository smsTransactionRepository;
    private final ChurchRepository churchRepository;

    public ChurchSmsCreditService(
        ChurchSmsCreditRepository churchSmsCreditRepository,
        SmsTransactionRepository smsTransactionRepository,
        ChurchRepository churchRepository
    ) {
        this.churchSmsCreditRepository = churchSmsCreditRepository;
        this.smsTransactionRepository = smsTransactionRepository;
        this.churchRepository = churchRepository;
    }

    /**
     * Get or create SMS credit wallet for church
     */
    @Transactional
    public ChurchSmsCredit getOrCreateWallet(Long churchId) {
        return churchSmsCreditRepository.findByChurchId(churchId)
            .orElseGet(() -> {
                Church church = churchRepository.findById(churchId)
                    .orElseThrow(() -> new IllegalArgumentException("Church not found: " + churchId));

                ChurchSmsCredit credit = new ChurchSmsCredit();
                credit.setChurch(church);
                credit.setBalance(BigDecimal.ZERO);
                credit.setTotalPurchased(BigDecimal.ZERO);
                credit.setTotalUsed(BigDecimal.ZERO);
                credit.setLowBalanceThreshold(new BigDecimal("50.00"));
                credit.setLowBalanceAlertSent(false);
                credit.setMigrationCompleted(true);

                log.info("Creating new SMS wallet for church: {}", church.getName());
                return churchSmsCreditRepository.save(credit);
            });
    }

    /**
     * Get church's current balance
     */
    public BigDecimal getBalance(Long churchId) {
        return churchSmsCreditRepository.findByChurchId(churchId)
            .map(ChurchSmsCredit::getBalance)
            .orElse(BigDecimal.ZERO);
    }

    /**
     * Check if church has sufficient credits
     */
    public boolean hasSufficientCredits(Long churchId, BigDecimal requiredAmount) {
        ChurchSmsCredit wallet = churchSmsCreditRepository.findByChurchId(churchId)
            .orElse(null);

        return wallet != null && wallet.hasSufficientBalance(requiredAmount);
    }

    /**
     * Purchase credits (called after successful payment)
     * @param churchId Church purchasing credits
     * @param performedByUserId User who made the purchase
     * @param amount Amount of credits to add
     * @param paymentReference Payment gateway reference
     */
    @Transactional
    public SmsTransaction purchaseCredits(
        Long churchId,
        Long performedByUserId,
        BigDecimal amount,
        String paymentReference
    ) {
        ChurchSmsCredit wallet = getOrCreateWallet(churchId);
        Church church = wallet.getChurch();

        BigDecimal balanceBefore = wallet.getBalance();

        // Add credits using entity method
        wallet.addCredits(amount);

        BigDecimal balanceAfter = wallet.getBalance();
        churchSmsCreditRepository.save(wallet);

        // Create transaction record
        SmsTransaction transaction = new SmsTransaction();
        transaction.setChurch(church);
        if (performedByUserId != null) {
            // Set performedBy if user reference is available
            User performedBy = new User();
            performedBy.setId(performedByUserId);
            transaction.setPerformedBy(performedBy);
        }
        transaction.setType(TransactionType.PURCHASE);
        transaction.setAmount(amount);
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setDescription("Credit purchase - " + amount + " credits added");
        transaction.setReferenceId(UUID.randomUUID().toString());
        transaction.setPaymentReference(paymentReference);
        transaction.setStatus(TransactionStatus.COMPLETED);

        SmsTransaction saved = smsTransactionRepository.save(transaction);
        log.info("Church {} purchased {} credits. New balance: {}", church.getName(), amount, balanceAfter);

        return saved;
    }

    /**
     * Deduct credits for SMS
     * @param churchId Church sending SMS
     * @param performedByUserId User who triggered the send
     * @param amount Cost of SMS
     * @param description Description of transaction
     * @param referenceId Reference to SMS message
     */
    @Transactional
    public SmsTransaction deductCredits(
        Long churchId,
        Long performedByUserId,
        BigDecimal amount,
        String description,
        String referenceId
    ) {
        ChurchSmsCredit wallet = churchSmsCreditRepository.findByChurchId(churchId)
            .orElseThrow(() -> new IllegalStateException("SMS wallet not found for church: " + churchId));

        Church church = wallet.getChurch();
        BigDecimal balanceBefore = wallet.getBalance();

        // Deduct credits using entity method (throws exception if insufficient)
        wallet.deductCredits(amount);

        BigDecimal balanceAfter = wallet.getBalance();
        churchSmsCreditRepository.save(wallet);

        // Create transaction record
        SmsTransaction transaction = new SmsTransaction();
        transaction.setChurch(church);
        if (performedByUserId != null) {
            User performedBy = new User();
            performedBy.setId(performedByUserId);
            transaction.setPerformedBy(performedBy);
        }
        transaction.setType(TransactionType.DEDUCTION);
        transaction.setAmount(amount);
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setDescription(description);
        transaction.setReferenceId(referenceId);
        transaction.setStatus(TransactionStatus.COMPLETED);

        SmsTransaction saved = smsTransactionRepository.save(transaction);
        log.info("Church {} deducted {} credits. New balance: {}", church.getName(), amount, balanceAfter);

        return saved;
    }

    /**
     * Refund credits (if SMS fails)
     * @param churchId Church to refund
     * @param performedByUserId User (nullable)
     * @param amount Amount to refund
     * @param description Reason for refund
     * @param referenceId Reference to original transaction
     */
    @Transactional
    public SmsTransaction refundCredits(
        Long churchId,
        Long performedByUserId,
        BigDecimal amount,
        String description,
        String referenceId
    ) {
        ChurchSmsCredit wallet = getOrCreateWallet(churchId);
        Church church = wallet.getChurch();

        BigDecimal balanceBefore = wallet.getBalance();

        // Refund credits using entity method
        wallet.refundCredits(amount);

        BigDecimal balanceAfter = wallet.getBalance();
        churchSmsCreditRepository.save(wallet);

        // Create transaction record
        SmsTransaction transaction = new SmsTransaction();
        transaction.setChurch(church);
        if (performedByUserId != null) {
            User performedBy = new User();
            performedBy.setId(performedByUserId);
            transaction.setPerformedBy(performedBy);
        }
        transaction.setType(TransactionType.REFUND);
        transaction.setAmount(amount);
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setDescription(description);
        transaction.setReferenceId(referenceId);
        transaction.setStatus(TransactionStatus.COMPLETED);

        SmsTransaction saved = smsTransactionRepository.save(transaction);
        log.info("Church {} refunded {} credits. New balance: {}", church.getName(), amount, balanceAfter);

        return saved;
    }

    /**
     * Get transaction history for church
     */
    public Page<SmsTransaction> getTransactionHistory(Long churchId, Pageable pageable) {
        return smsTransactionRepository.findByChurchId(churchId, pageable);
    }

    /**
     * Get all transactions for church (no pagination)
     */
    public List<SmsTransaction> getAllTransactions(Long churchId) {
        return smsTransactionRepository.findByChurchIdOrderByCreatedAtDesc(churchId);
    }

    /**
     * Get wallet for church
     */
    public ChurchSmsCredit getWallet(Long churchId) {
        return churchSmsCreditRepository.findByChurchId(churchId)
            .orElseThrow(() -> new IllegalStateException("Wallet not found for church: " + churchId));
    }

    /**
     * Get churches with low balance
     */
    public List<ChurchSmsCredit> getChurchesWithLowBalance() {
        return churchSmsCreditRepository.findChurchesWithLowBalance();
    }

    /**
     * Get churches needing low balance alert
     */
    public List<ChurchSmsCredit> getChurchesNeedingLowBalanceAlert() {
        return churchSmsCreditRepository.findChurchesNeedingLowBalanceAlert();
    }

    /**
     * Mark low balance alert as sent
     */
    @Transactional
    public void markLowBalanceAlertSent(Long churchId) {
        ChurchSmsCredit wallet = getWallet(churchId);
        wallet.setLowBalanceAlertSent(true);
        churchSmsCreditRepository.save(wallet);
    }

    /**
     * Update low balance threshold
     */
    @Transactional
    public void updateLowBalanceThreshold(Long churchId, BigDecimal threshold) {
        ChurchSmsCredit wallet = getWallet(churchId);
        wallet.setLowBalanceThreshold(threshold);
        wallet.setLowBalanceAlertSent(false); // Reset alert flag
        churchSmsCreditRepository.save(wallet);
    }

    /**
     * Get statistics across all churches (for super admin)
     */
    public ChurchSmsCreditStats getGlobalStats() {
        BigDecimal totalCredits = churchSmsCreditRepository.getTotalCreditsAcrossAllChurches();
        BigDecimal totalPurchased = churchSmsCreditRepository.getTotalPurchasedAcrossAllChurches();
        BigDecimal totalUsed = churchSmsCreditRepository.getTotalUsedAcrossAllChurches();
        long churchesWithZeroBalance = churchSmsCreditRepository.countChurchesWithZeroBalance();

        return new ChurchSmsCreditStats(totalCredits, totalPurchased, totalUsed, churchesWithZeroBalance);
    }

    /**
     * Inner class for global statistics
     */
    public record ChurchSmsCreditStats(
        BigDecimal totalCredits,
        BigDecimal totalPurchased,
        BigDecimal totalUsed,
        long churchesWithZeroBalance
    ) {}
}
