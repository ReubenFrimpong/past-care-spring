package com.reuben.pastcare_spring.controllers;

import com.reuben.pastcare_spring.annotations.RequirePermission;
import com.reuben.pastcare_spring.dtos.*;
import com.reuben.pastcare_spring.enums.Permission;
import com.reuben.pastcare_spring.models.*;
import com.reuben.pastcare_spring.repositories.UserRepository;
import com.reuben.pastcare_spring.security.TenantContext;
import com.reuben.pastcare_spring.services.ChurchSmsCreditService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for managing church-level SMS credits
 */
@RestController
@RequestMapping("/api/church-sms-credits")
@Slf4j
public class ChurchSmsCreditController {

    private final ChurchSmsCreditService churchSmsCreditService;
    private final UserRepository userRepository;

    public ChurchSmsCreditController(
        ChurchSmsCreditService churchSmsCreditService,
        UserRepository userRepository
    ) {
        this.churchSmsCreditService = churchSmsCreditService;
        this.userRepository = userRepository;
    }

    /**
     * Get church SMS credit balance
     */
    @GetMapping("/balance")
    @RequirePermission(Permission.CHURCH_SETTINGS_VIEW)
    public ResponseEntity<ChurchSmsCreditResponse> getBalance() {
        Long churchId = TenantContext.getCurrentChurchId();
        ChurchSmsCredit credit = churchSmsCreditService.getOrCreateWallet(churchId);
        return ResponseEntity.ok(ChurchSmsCreditResponse.fromEntity(credit));
    }

    /**
     * Purchase SMS credits
     */
    @PostMapping("/purchase")
    @RequirePermission(Permission.CHURCH_SETTINGS_EDIT)
    public ResponseEntity<SmsTransactionResponse> purchaseCredits(
        @RequestBody PurchaseCreditsRequest request,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long churchId = TenantContext.getCurrentChurchId();
        User user = userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new IllegalStateException("User not found"));

        SmsTransaction transaction = churchSmsCreditService.purchaseCredits(
            churchId,
            user.getId(),
            request.getAmount(),
            request.getPaymentReference()
        );

        return ResponseEntity.ok(mapTransactionToResponse(transaction));
    }

    /**
     * Get transaction history for church
     */
    @GetMapping("/transactions")
    @RequirePermission(Permission.CHURCH_SETTINGS_VIEW)
    public ResponseEntity<Page<SmsTransactionResponse>> getTransactions(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        Long churchId = TenantContext.getCurrentChurchId();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<SmsTransaction> transactions = churchSmsCreditService.getTransactionHistory(churchId, pageable);
        Page<SmsTransactionResponse> response = transactions.map(this::mapTransactionToResponse);

        return ResponseEntity.ok(response);
    }

    /**
     * Get all transactions (no pagination) for export
     */
    @GetMapping("/transactions/all")
    @RequirePermission(Permission.CHURCH_SETTINGS_VIEW)
    public ResponseEntity<List<SmsTransactionResponse>> getAllTransactions() {
        Long churchId = TenantContext.getCurrentChurchId();
        List<SmsTransaction> transactions = churchSmsCreditService.getAllTransactions(churchId);
        List<SmsTransactionResponse> response = transactions.stream()
            .map(this::mapTransactionToResponse)
            .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Update low balance threshold
     */
    @PutMapping("/threshold")
    @RequirePermission(Permission.CHURCH_SETTINGS_EDIT)
    public ResponseEntity<ChurchSmsCreditResponse> updateThreshold(
        @RequestParam BigDecimal threshold
    ) {
        Long churchId = TenantContext.getCurrentChurchId();
        churchSmsCreditService.updateLowBalanceThreshold(churchId, threshold);
        ChurchSmsCredit credit = churchSmsCreditService.getWallet(churchId);

        return ResponseEntity.ok(ChurchSmsCreditResponse.fromEntity(credit));
    }

    /**
     * Check if church has sufficient credits
     */
    @GetMapping("/check-balance")
    @RequirePermission(Permission.CHURCH_SETTINGS_VIEW)
    public ResponseEntity<BalanceCheckResponse> checkBalance(
        @RequestParam BigDecimal requiredAmount
    ) {
        Long churchId = TenantContext.getCurrentChurchId();
        BigDecimal currentBalance = churchSmsCreditService.getBalance(churchId);
        boolean hasSufficient = churchSmsCreditService.hasSufficientCredits(churchId, requiredAmount);

        BalanceCheckResponse checkResponse = new BalanceCheckResponse();
        checkResponse.setCurrentBalance(currentBalance);
        checkResponse.setRequiredAmount(requiredAmount);
        checkResponse.setHasSufficientBalance(hasSufficient);
        checkResponse.setDifference(currentBalance.subtract(requiredAmount));

        return ResponseEntity.ok(checkResponse);
    }

    /**
     * Get churches with low balance (Super Admin only)
     */
    @GetMapping("/low-balance")
    @RequirePermission(Permission.PLATFORM_ACCESS)
    public ResponseEntity<List<ChurchSmsCreditResponse>> getChurchesWithLowBalance() {
        List<ChurchSmsCredit> churches = churchSmsCreditService.getChurchesWithLowBalance();
        List<ChurchSmsCreditResponse> response = churches.stream()
            .map(ChurchSmsCreditResponse::fromEntity)
            .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Get global SMS credit statistics (Super Admin only)
     */
    @GetMapping("/stats/global")
    @RequirePermission(Permission.PLATFORM_ACCESS)
    public ResponseEntity<ChurchSmsCreditService.ChurchSmsCreditStats> getGlobalStats() {
        return ResponseEntity.ok(churchSmsCreditService.getGlobalStats());
    }

    /**
     * Mark low balance alert as sent (internal use)
     */
    @PostMapping("/alert-sent")
    @RequirePermission(Permission.PLATFORM_ACCESS)
    public ResponseEntity<Void> markAlertSent(@RequestParam Long churchId) {
        churchSmsCreditService.markLowBalanceAlertSent(churchId);
        return ResponseEntity.ok().build();
    }

    // Helper methods

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

    /**
     * Inner class for balance check response
     */
    @Data
    public static class BalanceCheckResponse {
        private BigDecimal currentBalance;
        private BigDecimal requiredAmount;
        private boolean hasSufficientBalance;
        private BigDecimal difference;
    }
}
