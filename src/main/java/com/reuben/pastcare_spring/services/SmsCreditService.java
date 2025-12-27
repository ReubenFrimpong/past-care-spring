package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.models.*;
import com.reuben.pastcare_spring.repositories.SmsCreditRepository;
import com.reuben.pastcare_spring.repositories.SmsRateRepository;
import com.reuben.pastcare_spring.repositories.SmsTransactionRepository;
import com.reuben.pastcare_spring.security.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class SmsCreditService {

    private final SmsCreditRepository smsCreditRepository;
    private final SmsTransactionRepository smsTransactionRepository;
    private final SmsRateRepository smsRateRepository;
    private final PhoneNumberService phoneNumberService;

    public SmsCreditService(
        SmsCreditRepository smsCreditRepository,
        SmsTransactionRepository smsTransactionRepository,
        SmsRateRepository smsRateRepository,
        PhoneNumberService phoneNumberService
    ) {
        this.smsCreditRepository = smsCreditRepository;
        this.smsTransactionRepository = smsTransactionRepository;
        this.smsRateRepository = smsRateRepository;
        this.phoneNumberService = phoneNumberService;
    }

    /**
     * Get or create SMS credit wallet for user
     */
    @Transactional
    public SmsCredit getOrCreateWallet(User user, Church church) {
        return smsCreditRepository.findByUserIdAndChurchId(user.getId(), church.getId())
            .orElseGet(() -> {
                SmsCredit credit = new SmsCredit();
                credit.setUser(user);
                credit.setChurch(church);
                credit.setBalance(BigDecimal.ZERO);
                credit.setTotalPurchased(BigDecimal.ZERO);
                credit.setTotalUsed(BigDecimal.ZERO);
                return smsCreditRepository.save(credit);
            });
    }

    /**
     * Get user's current balance
     */
    public BigDecimal getBalance(Long userId, Long churchId) {
        return smsCreditRepository.findByUserIdAndChurchId(userId, churchId)
            .map(SmsCredit::getBalance)
            .orElse(BigDecimal.ZERO);
    }

    /**
     * Purchase credits (called after successful payment)
     */
    @Transactional
    public SmsTransaction purchaseCredits(
        User user,
        Church church,
        BigDecimal amount,
        String paymentReference
    ) {
        SmsCredit wallet = getOrCreateWallet(user, church);

        BigDecimal balanceBefore = wallet.getBalance();
        BigDecimal balanceAfter = balanceBefore.add(amount);

        // Update wallet
        wallet.setBalance(balanceAfter);
        wallet.setTotalPurchased(wallet.getTotalPurchased().add(amount));
        smsCreditRepository.save(wallet);

        // Create transaction record
        SmsTransaction transaction = new SmsTransaction();
        transaction.setUser(user);
        transaction.setChurch(church);
        transaction.setType(TransactionType.PURCHASE);
        transaction.setAmount(amount);
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setDescription("Credit purchase");
        transaction.setReferenceId(UUID.randomUUID().toString());
        transaction.setPaymentReference(paymentReference);
        transaction.setStatus(TransactionStatus.COMPLETED);

        return smsTransactionRepository.save(transaction);
    }

    /**
     * Deduct credits for SMS
     */
    @Transactional
    public SmsTransaction deductCredits(
        User user,
        Church church,
        BigDecimal amount,
        String description,
        String referenceId
    ) {
        SmsCredit wallet = smsCreditRepository.findByUserIdAndChurchId(user.getId(), church.getId())
            .orElseThrow(() -> new IllegalStateException("SMS wallet not found"));

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient SMS credits. Current balance: " +
                wallet.getBalance() + ", Required: " + amount);
        }

        BigDecimal balanceBefore = wallet.getBalance();
        BigDecimal balanceAfter = balanceBefore.subtract(amount);

        // Update wallet
        wallet.setBalance(balanceAfter);
        wallet.setTotalUsed(wallet.getTotalUsed().add(amount));
        smsCreditRepository.save(wallet);

        // Create transaction record
        SmsTransaction transaction = new SmsTransaction();
        transaction.setUser(user);
        transaction.setChurch(church);
        transaction.setType(TransactionType.DEDUCTION);
        transaction.setAmount(amount);
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setDescription(description);
        transaction.setReferenceId(referenceId);
        transaction.setStatus(TransactionStatus.COMPLETED);

        return smsTransactionRepository.save(transaction);
    }

    /**
     * Refund credits (if SMS fails)
     */
    @Transactional
    public SmsTransaction refundCredits(
        User user,
        Church church,
        BigDecimal amount,
        String description,
        String referenceId
    ) {
        SmsCredit wallet = getOrCreateWallet(user, church);

        BigDecimal balanceBefore = wallet.getBalance();
        BigDecimal balanceAfter = balanceBefore.add(amount);

        // Update wallet
        wallet.setBalance(balanceAfter);
        wallet.setTotalUsed(wallet.getTotalUsed().subtract(amount));
        smsCreditRepository.save(wallet);

        // Create transaction record
        SmsTransaction transaction = new SmsTransaction();
        transaction.setUser(user);
        transaction.setChurch(church);
        transaction.setType(TransactionType.REFUND);
        transaction.setAmount(amount);
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setDescription(description);
        transaction.setReferenceId(referenceId);
        transaction.setStatus(TransactionStatus.COMPLETED);

        return smsTransactionRepository.save(transaction);
    }

    /**
     * Calculate SMS cost based on destination and message length
     */
    public BigDecimal calculateSmsCost(String phoneNumber, String message) {
        // Get country code
        String countryCode = phoneNumberService.extractCountryCode(phoneNumber);

        // Get rate for country
        Long churchId = TenantContext.getCurrentChurchId();
        SmsRate rate = smsRateRepository.findRateForChurchAndCountry(churchId, countryCode)
            .or(() -> smsRateRepository.findByCountryCodeAndIsActive(countryCode, true))
            .or(() -> smsRateRepository.findDefaultRate())
            .orElseThrow(() -> new IllegalStateException("No SMS rate found for country: " + countryCode));

        // Calculate message count
        int messageCount = phoneNumberService.calculateMessageCount(message);

        // Calculate total cost
        return rate.getRatePerSms().multiply(BigDecimal.valueOf(messageCount));
    }

    /**
     * Check if user has sufficient credits
     */
    public boolean hasSufficientCredits(Long userId, Long churchId, BigDecimal requiredAmount) {
        BigDecimal balance = getBalance(userId, churchId);
        return balance.compareTo(requiredAmount) >= 0;
    }

    /**
     * Get transaction history
     */
    public Page<SmsTransaction> getTransactionHistory(Long userId, Long churchId, Pageable pageable) {
        return smsTransactionRepository.findByUserIdAndChurchId(userId, churchId, pageable);
    }

    /**
     * Get wallet for user
     */
    public SmsCredit getWallet(Long userId, Long churchId) {
        return smsCreditRepository.findByUserIdAndChurchId(userId, churchId)
            .orElseThrow(() -> new IllegalStateException("Wallet not found"));
    }
}
