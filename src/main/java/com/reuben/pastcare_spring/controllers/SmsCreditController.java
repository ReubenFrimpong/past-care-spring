package com.reuben.pastcare_spring.controllers;

import com.reuben.pastcare_spring.dtos.*;
import com.reuben.pastcare_spring.models.*;
import com.reuben.pastcare_spring.repositories.ChurchRepository;
import com.reuben.pastcare_spring.repositories.SmsRateRepository;
import com.reuben.pastcare_spring.repositories.UserRepository;
import com.reuben.pastcare_spring.security.TenantContext;
import com.reuben.pastcare_spring.services.PhoneNumberService;
import com.reuben.pastcare_spring.services.SmsCreditService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/sms/credits")
@Slf4j
public class SmsCreditController {

    private final SmsCreditService smsCreditService;
    private final UserRepository userRepository;
    private final ChurchRepository churchRepository;
    private final PhoneNumberService phoneNumberService;
    private final SmsRateRepository smsRateRepository;

    public SmsCreditController(
        SmsCreditService smsCreditService,
        UserRepository userRepository,
        ChurchRepository churchRepository,
        PhoneNumberService phoneNumberService,
        SmsRateRepository smsRateRepository
    ) {
        this.smsCreditService = smsCreditService;
        this.userRepository = userRepository;
        this.churchRepository = churchRepository;
        this.phoneNumberService = phoneNumberService;
        this.smsRateRepository = smsRateRepository;
    }

    /**
     * Get current balance
     */
    @GetMapping("/balance")
    public ResponseEntity<SmsCreditResponse> getBalance(Authentication authentication) {
        try {
            User user = getUserFromAuth(authentication);
            Church church = getChurch();

            SmsCredit wallet = smsCreditService.getWallet(user.getId(), church.getId());
            return ResponseEntity.ok(mapToResponse(wallet));

        } catch (IllegalStateException e) {
            // Wallet doesn't exist yet, create it
            User user = getUserFromAuth(authentication);
            Church church = getChurch();
            SmsCredit wallet = smsCreditService.getOrCreateWallet(user, church);
            return ResponseEntity.ok(mapToResponse(wallet));

        } catch (Exception e) {
            log.error("Error fetching balance: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Purchase credits (after payment verification)
     * Note: This should be called after payment gateway confirms payment
     */
    @PostMapping("/purchase")
    public ResponseEntity<SmsTransactionResponse> purchaseCredits(
        @Valid @RequestBody PurchaseCreditsRequest request,
        Authentication authentication
    ) {
        try {
            User user = getUserFromAuth(authentication);
            Church church = getChurch();

            // TODO: Verify payment with payment gateway before adding credits
            // For now, we're assuming payment is verified

            SmsTransaction transaction = smsCreditService.purchaseCredits(
                user,
                church,
                request.getAmount(),
                request.getPaymentReference()
            );

            return ResponseEntity.ok(mapTransactionToResponse(transaction));

        } catch (Exception e) {
            log.error("Error purchasing credits: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get transaction history
     */
    @GetMapping("/transactions")
    public ResponseEntity<Page<SmsTransactionResponse>> getTransactions(
        Authentication authentication,
        Pageable pageable
    ) {
        try {
            User user = getUserFromAuth(authentication);
            Long churchId = TenantContext.getCurrentChurchId();

            Page<SmsTransaction> transactions = smsCreditService.getTransactionHistory(
                user.getId(), churchId, pageable);
            Page<SmsTransactionResponse> responses = transactions.map(this::mapTransactionToResponse);

            return ResponseEntity.ok(responses);

        } catch (Exception e) {
            log.error("Error fetching transactions: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Calculate SMS cost
     */
    @PostMapping("/calculate-cost")
    public ResponseEntity<CalculateSmsCostResponse> calculateCost(
        @Valid @RequestBody CalculateSmsCostRequest request
    ) {
        try {
            String normalizedPhone = phoneNumberService.normalizePhoneNumber(request.getPhoneNumber());
            String countryCode = phoneNumberService.extractCountryCode(normalizedPhone);
            int messageCount = phoneNumberService.calculateMessageCount(request.getMessage());

            // Get rate
            Long churchId = TenantContext.getCurrentChurchId();
            SmsRate rate = smsRateRepository.findRateForChurchAndCountry(churchId, countryCode)
                .or(() -> smsRateRepository.findByCountryCodeAndIsActive(countryCode, true))
                .or(() -> smsRateRepository.findDefaultRate())
                .orElseThrow(() -> new IllegalStateException("No SMS rate found"));

            BigDecimal cost = rate.getRatePerSms().multiply(BigDecimal.valueOf(messageCount));

            CalculateSmsCostResponse response = new CalculateSmsCostResponse(
                cost,
                messageCount,
                countryCode,
                rate.getCountryName(),
                rate.getIsLocal(),
                rate.getRatePerSms()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error calculating cost: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Helper methods

    private User getUserFromAuth(Authentication authentication) {
        String username = authentication.getName();
        return userRepository.findByEmail(username)
            .orElseThrow(() -> new IllegalStateException("User not found"));
    }

    private Church getChurch() {
        Long churchId = TenantContext.getCurrentChurchId();
        return churchRepository.findById(churchId)
            .orElseThrow(() -> new IllegalStateException("Church not found"));
    }

    private SmsCreditResponse mapToResponse(SmsCredit credit) {
        SmsCreditResponse response = new SmsCreditResponse();
        response.setId(credit.getId());
        response.setUserId(credit.getUser().getId());
        response.setUserName(credit.getUser().getName());
        response.setBalance(credit.getBalance());
        response.setTotalPurchased(credit.getTotalPurchased());
        response.setTotalUsed(credit.getTotalUsed());
        response.setCreatedAt(credit.getCreatedAt());
        response.setUpdatedAt(credit.getUpdatedAt());
        return response;
    }

    private SmsTransactionResponse mapTransactionToResponse(SmsTransaction transaction) {
        SmsTransactionResponse response = new SmsTransactionResponse();
        response.setId(transaction.getId());
        response.setUserId(transaction.getPerformedBy() != null ? transaction.getPerformedBy().getId() : null);
        response.setUserName(transaction.getPerformedBy() != null ? transaction.getPerformedBy().getName() : "System");
        response.setType(transaction.getType());
        response.setAmount(transaction.getAmount());
        response.setBalanceBefore(transaction.getBalanceBefore());
        response.setBalanceAfter(transaction.getBalanceAfter());
        response.setDescription(transaction.getDescription());
        response.setReferenceId(transaction.getReferenceId());
        response.setPaymentReference(transaction.getPaymentReference());
        response.setStatus(transaction.getStatus());
        response.setCreatedAt(transaction.getCreatedAt());
        return response;
    }
}
