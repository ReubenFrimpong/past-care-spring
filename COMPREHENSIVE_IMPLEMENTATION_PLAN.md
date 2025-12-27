# Comprehensive Implementation Plan - PastCare Application

**Created**: 2025-12-27
**Status**: Ready for Implementation
**Estimated Total Duration**: 10 weeks

---

## Overview

This document provides a detailed, step-by-step implementation plan for completing all remaining modules in the PastCare application, with special focus on:
1. Church-level SMS credits migration
2. Robust failure and recovery mechanisms
3. Giving Module Phase 4 (Financial Reporting)
4. Dashboard Module Phase 2 (Analytics)
5. Events Module (All Phases)

---

## 🏗️ PHASE 0: SMS Credits Architecture Migration (Week 1)

**Priority**: CRITICAL - Must complete before automated messaging
**Duration**: 5-7 hours
**Dependencies**: None

### Step 0.1: Database Schema Changes (1-2 hours)

#### 0.1.1: Create Church-Level Credits Table
```sql
-- Migration: V34__migrate_to_church_level_credits.sql

-- Step 1: Create new church_sms_credits table
CREATE TABLE church_sms_credits (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    church_id BIGINT NOT NULL,
    balance DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total_purchased DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total_used DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    last_purchase_at TIMESTAMP NULL,
    low_balance_alert_sent BOOLEAN DEFAULT FALSE,
    low_balance_threshold DECIMAL(10,2) DEFAULT 50.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (church_id) REFERENCES churches(id) ON DELETE CASCADE,
    UNIQUE KEY uk_church_credits (church_id),
    INDEX idx_church_balance (church_id, balance)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

#### 0.1.2: Migrate Existing User Credits
```sql
-- Step 2: Aggregate user credits per church
INSERT INTO church_sms_credits (church_id, balance, total_purchased, total_used, created_at)
SELECT
    u.church_id,
    COALESCE(SUM(sc.balance), 0.00) as balance,
    COALESCE(SUM(sc.total_purchased), 0.00) as total_purchased,
    COALESCE(SUM(sc.total_used), 0.00) as total_used,
    MIN(sc.created_at) as created_at
FROM churches c
LEFT JOIN users u ON u.church_id = c.id
LEFT JOIN sms_credits sc ON sc.user_id = u.id
GROUP BY c.id;

-- Step 3: Verify migration
SELECT
    c.name as church_name,
    csc.balance as new_church_balance,
    (SELECT COALESCE(SUM(balance), 0) FROM sms_credits sc2
     JOIN users u2 ON sc2.user_id = u2.id
     WHERE u2.church_id = c.id) as old_user_totals
FROM churches c
LEFT JOIN church_sms_credits csc ON csc.church_id = c.id;
```

#### 0.1.3: Update Transactions Table
```sql
-- Step 4: Add church_id to sms_transactions
ALTER TABLE sms_transactions
    ADD COLUMN church_id BIGINT AFTER id,
    ADD INDEX idx_church_transactions (church_id, created_at);

-- Step 5: Populate church_id from user's church
UPDATE sms_transactions st
JOIN users u ON st.user_id = u.id
SET st.church_id = u.church_id
WHERE st.church_id IS NULL;

-- Step 6: Make church_id required
ALTER TABLE sms_transactions
    MODIFY COLUMN church_id BIGINT NOT NULL,
    ADD FOREIGN KEY fk_transaction_church (church_id) REFERENCES churches(id);

-- Step 7: Add performed_by_user_id to track who did what
ALTER TABLE sms_transactions
    CHANGE COLUMN user_id performed_by_user_id BIGINT NULL,
    ADD INDEX idx_performed_by (performed_by_user_id);
```

#### 0.1.4: Backup and Archive Old Table
```sql
-- Step 8: Create backup of old user credits
CREATE TABLE sms_credits_backup_20251227 AS SELECT * FROM sms_credits;

-- Step 9: Drop old sms_credits table (after verification!)
-- IMPORTANT: Only execute after confirming migration success
-- DROP TABLE sms_credits;
```

**Verification Checklist**:
- [ ] All churches have a church_sms_credits record
- [ ] Total balance matches sum of old user credits
- [ ] sms_transactions all have church_id populated
- [ ] Backup table created successfully

---

### Step 0.2: Backend Entity and Repository Changes (1 hour)

#### 0.2.1: Create ChurchSmsCredit Entity
```java
// File: src/main/java/com/reuben/pastcare_spring/models/ChurchSmsCredit.java
package com.reuben.pastcare_spring.models;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "church_sms_credits")
@Data
public class ChurchSmsCredit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "church_id", nullable = false, unique = true)
    private Church church;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "total_purchased", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPurchased = BigDecimal.ZERO;

    @Column(name = "total_used", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalUsed = BigDecimal.ZERO;

    @Column(name = "last_purchase_at")
    private LocalDateTime lastPurchaseAt;

    @Column(name = "low_balance_alert_sent")
    private Boolean lowBalanceAlertSent = false;

    @Column(name = "low_balance_threshold", precision = 10, scale = 2)
    private BigDecimal lowBalanceThreshold = new BigDecimal("50.00");

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public boolean hasLowBalance() {
        return balance.compareTo(lowBalanceThreshold) < 0;
    }

    public boolean hasSufficientBalance(BigDecimal required) {
        return balance.compareTo(required) >= 0;
    }
}
```

#### 0.2.2: Update SmsTransaction Entity
```java
// File: src/main/java/com/reuben/pastcare_spring/models/SmsTransaction.java
// Add church reference, rename user_id to performed_by_user_id

@Entity
@Table(name = "sms_transactions")
@Data
public class SmsTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "church_id", nullable = false)
    private Church church;  // NEW: Church that owns the credits

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by_user_id")
    private User performedBy;  // RENAMED: Who performed the action

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "balance_before", nullable = false, precision = 10, scale = 2)
    private BigDecimal balanceBefore;

    @Column(name = "balance_after", nullable = false, precision = 10, scale = 2)
    private BigDecimal balanceAfter;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(name = "reference_id", length = 100)
    private String referenceId;

    @Column(name = "payment_reference", length = 100)
    private String paymentReference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
```

#### 0.2.3: Create ChurchSmsCreditRepository
```java
// File: src/main/java/com/reuben/pastcare_spring/repositories/ChurchSmsCreditRepository.java
package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.ChurchSmsCredit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChurchSmsCreditRepository extends JpaRepository<ChurchSmsCredit, Long> {

    Optional<ChurchSmsCredit> findByChurchId(Long churchId);

    @Query("SELECT c FROM ChurchSmsCredit c WHERE c.balance < c.lowBalanceThreshold AND c.lowBalanceAlertSent = false")
    List<ChurchSmsCredit> findChurchesWithLowBalance();

    @Query("SELECT c FROM ChurchSmsCredit c WHERE c.church.id = :churchId")
    Optional<ChurchSmsCredit> findByChurchIdWithLock(Long churchId);
}
```

#### 0.2.4: Update SmsTransactionRepository
```java
// File: src/main/java/com/reuben/pastcare_spring/repositories/SmsTransactionRepository.java
// Update to use church_id instead of user_id

@Repository
public interface SmsTransactionRepository extends JpaRepository<SmsTransaction, Long> {

    // OLD: findByUserId(Long userId)
    // NEW: findByChurchId(Long churchId)
    Page<SmsTransaction> findByChurchId(Long churchId, Pageable pageable);

    Page<SmsTransaction> findByChurchIdAndType(Long churchId, TransactionType type, Pageable pageable);

    @Query("SELECT SUM(t.amount) FROM SmsTransaction t WHERE t.church.id = :churchId AND t.type = :type AND t.status = 'COMPLETED'")
    BigDecimal sumAmountByChurchIdAndType(Long churchId, TransactionType type);
}
```

---

### Step 0.3: Update SmsCreditService (2-3 hours)

#### 0.3.1: Refactor Core Methods
```java
// File: src/main/java/com/reuben/pastcare_spring/services/SmsCreditService.java

@Service
@Slf4j
public class SmsCreditService {

    private final ChurchSmsCreditRepository churchCreditRepository;
    private final SmsTransactionRepository transactionRepository;
    private final PhoneNumberService phoneNumberService;

    // NEW: Get church balance
    public BigDecimal getChurchBalance(Long churchId) {
        return getOrCreateChurchCredit(churchId).getBalance();
    }

    // NEW: Check if church has sufficient credits
    public boolean hasChurchCredits(Long churchId, BigDecimal required) {
        ChurchSmsCredit credit = getOrCreateChurchCredit(churchId);
        return credit.hasSufficientBalance(required);
    }

    // NEW: Get or create church credit account
    @Transactional
    public ChurchSmsCredit getOrCreateChurchCredit(Long churchId) {
        return churchCreditRepository.findByChurchId(churchId)
            .orElseGet(() -> {
                ChurchSmsCredit newCredit = new ChurchSmsCredit();
                newCredit.setChurch(new Church(churchId));
                newCredit.setBalance(BigDecimal.ZERO);
                newCredit.setTotalPurchased(BigDecimal.ZERO);
                newCredit.setTotalUsed(BigDecimal.ZERO);
                return churchCreditRepository.save(newCredit);
            });
    }

    // NEW: Deduct church credits
    @Transactional
    public void deductChurchCredits(
        Long churchId,
        BigDecimal amount,
        String description,
        Long performedByUserId,
        String referenceId
    ) {
        ChurchSmsCredit credit = getOrCreateChurchCredit(churchId);

        if (!credit.hasSufficientBalance(amount)) {
            throw new IllegalStateException(
                String.format("Insufficient church SMS credits. Required: %.2f, Available: %.2f",
                    amount, credit.getBalance())
            );
        }

        BigDecimal balanceBefore = credit.getBalance();
        credit.setBalance(credit.getBalance().subtract(amount));
        credit.setTotalUsed(credit.getTotalUsed().add(amount));
        churchCreditRepository.save(credit);

        // Log transaction
        logTransaction(
            churchId,
            performedByUserId,
            TransactionType.DEDUCTION,
            amount,
            balanceBefore,
            credit.getBalance(),
            description,
            referenceId,
            null
        );

        // Check for low balance
        checkLowBalance(credit);
    }

    // NEW: Purchase church credits
    @Transactional
    public SmsTransaction purchaseChurchCredits(
        Long churchId,
        Long purchasedByUserId,
        BigDecimal amount,
        String paymentReference
    ) {
        ChurchSmsCredit credit = getOrCreateChurchCredit(churchId);

        BigDecimal balanceBefore = credit.getBalance();
        credit.setBalance(credit.getBalance().add(amount));
        credit.setTotalPurchased(credit.getTotalPurchased().add(amount));
        credit.setLastPurchaseAt(LocalDateTime.now());
        credit.setLowBalanceAlertSent(false); // Reset alert flag
        churchCreditRepository.save(credit);

        String description = "Credit purchase via " +
            (paymentReference != null ? paymentReference : "manual");

        return logTransaction(
            churchId,
            purchasedByUserId,
            TransactionType.PURCHASE,
            amount,
            balanceBefore,
            credit.getBalance(),
            description,
            "PURCHASE-" + System.currentTimeMillis(),
            paymentReference
        );
    }

    // NEW: Refund church credits
    @Transactional
    public void refundChurchCredits(
        Long churchId,
        BigDecimal amount,
        String description,
        Long performedByUserId,
        String referenceId
    ) {
        ChurchSmsCredit credit = getOrCreateChurchCredit(churchId);

        BigDecimal balanceBefore = credit.getBalance();
        credit.setBalance(credit.getBalance().add(amount));
        credit.setTotalUsed(credit.getTotalUsed().subtract(amount));
        churchCreditRepository.save(credit);

        logTransaction(
            churchId,
            performedByUserId,
            TransactionType.REFUND,
            amount,
            balanceBefore,
            credit.getBalance(),
            description,
            referenceId,
            null
        );
    }

    // Helper: Log transaction
    private SmsTransaction logTransaction(
        Long churchId,
        Long performedByUserId,
        TransactionType type,
        BigDecimal amount,
        BigDecimal balanceBefore,
        BigDecimal balanceAfter,
        String description,
        String referenceId,
        String paymentReference
    ) {
        SmsTransaction transaction = new SmsTransaction();
        transaction.setChurch(new Church(churchId));
        if (performedByUserId != null) {
            transaction.setPerformedBy(new User(performedByUserId));
        }
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setDescription(description);
        transaction.setReferenceId(referenceId);
        transaction.setPaymentReference(paymentReference);
        transaction.setStatus(TransactionStatus.COMPLETED);
        return transactionRepository.save(transaction);
    }

    // Helper: Check low balance and alert
    private void checkLowBalance(ChurchSmsCredit credit) {
        if (credit.hasLowBalance() && !credit.getLowBalanceAlertSent()) {
            // TODO: Send low balance alert to church admins
            log.warn("Church {} has low SMS balance: {}",
                credit.getChurch().getId(), credit.getBalance());
            credit.setLowBalanceAlertSent(true);
            churchCreditRepository.save(credit);
        }
    }

    // Keep existing calculateSmsCost method
    public BigDecimal calculateSmsCost(String phoneNumber, String message) {
        // Existing implementation stays the same
        // ...
    }
}
```

---

### Step 0.4: Update SmsService (1 hour)

#### 0.4.1: Refactor sendSms Method
```java
// File: src/main/java/com/reuben/pastcare_spring/services/SmsService.java

@Service
@Slf4j
public class SmsService {

    @Transactional
    public SmsMessage sendSms(
        User sender,
        Church church,
        String recipientPhone,
        String recipientName,
        String message,
        Member member,
        LocalDateTime scheduledTime
    ) {
        // Validate phone number
        if (!phoneNumberService.isValidPhoneNumber(recipientPhone)) {
            throw new IllegalArgumentException("Invalid phone number: " + recipientPhone);
        }

        String normalizedPhone = phoneNumberService.normalizePhoneNumber(recipientPhone);

        // Calculate cost
        BigDecimal cost = smsCreditService.calculateSmsCost(normalizedPhone, message);
        int messageCount = phoneNumberService.calculateMessageCount(message);

        // Check CHURCH credits (not user credits)
        if (!smsCreditService.hasChurchCredits(church.getId(), cost)) {
            BigDecimal currentBalance = smsCreditService.getChurchBalance(church.getId());
            throw new IllegalStateException(
                String.format("Insufficient church SMS credits. Required: %.2f, Current balance: %.2f",
                    cost, currentBalance)
            );
        }

        // Create SMS message record
        SmsMessage smsMessage = new SmsMessage();
        smsMessage.setSender(sender);
        smsMessage.setChurch(church);
        smsMessage.setRecipient(member);
        smsMessage.setRecipientPhone(normalizedPhone);
        smsMessage.setRecipientName(recipientName);
        smsMessage.setMessage(message);
        smsMessage.setMessageCount(messageCount);
        smsMessage.setCost(cost);
        smsMessage.setScheduledTime(scheduledTime);
        smsMessage.setStatus(scheduledTime != null ? SmsStatus.SCHEDULED : SmsStatus.PENDING);

        smsMessage = smsMessageRepository.save(smsMessage);

        // If not scheduled, send immediately
        if (scheduledTime == null) {
            sendImmediately(smsMessage, sender, church);
        }

        return smsMessage;
    }

    @Async
    @Transactional
    public void sendImmediately(SmsMessage smsMessage, User sender, Church church) {
        try {
            smsMessage.setStatus(SmsStatus.SENDING);
            smsMessageRepository.save(smsMessage);

            // Deduct CHURCH credits (not user credits)
            String referenceId = "SMS-" + smsMessage.getId();
            smsCreditService.deductChurchCredits(
                church.getId(),
                smsMessage.getCost(),
                "SMS to " + smsMessage.getRecipientPhone(),
                sender.getId(),
                referenceId
            );

            // Send through gateway
            SmsGatewayService.SmsGatewayResponse response = smsGatewayRouter.sendSms(
                smsMessage.getRecipientPhone(),
                smsMessage.getMessage(),
                Map.of("sms_id", smsMessage.getId().toString())
            );

            // Update message status
            if (response.isSuccess()) {
                smsMessage.setStatus(SmsStatus.SENT);
                smsMessage.setGatewayMessageId(response.getMessageId());
                smsMessage.setSentAt(LocalDateTime.now());
            } else {
                smsMessage.setStatus(SmsStatus.FAILED);
                // Refund church credits on failure
                smsCreditService.refundChurchCredits(
                    church.getId(),
                    smsMessage.getCost(),
                    "Refund for failed SMS",
                    sender.getId(),
                    referenceId
                );
            }

            smsMessage.setGatewayResponse(response.getErrorMessage());
            smsMessageRepository.save(smsMessage);

            log.info("SMS sent: {} - Status: {}", smsMessage.getId(), smsMessage.getStatus());

        } catch (Exception e) {
            log.error("Error sending SMS {}: {}", smsMessage.getId(), e.getMessage(), e);
            smsMessage.setStatus(SmsStatus.FAILED);
            smsMessage.setGatewayResponse(e.getMessage());
            smsMessageRepository.save(smsMessage);

            // Refund church credits on exception
            try {
                smsCreditService.refundChurchCredits(
                    church.getId(),
                    smsMessage.getCost(),
                    "Refund for failed SMS",
                    sender.getId(),
                    "SMS-" + smsMessage.getId()
                );
            } catch (Exception refundError) {
                log.error("Error refunding credits: {}", refundError.getMessage());
            }
        }
    }

    // Update getSmsStats to return church balance
    public Map<String, Object> getSmsStats(Long senderId, Long churchId) {
        long total = smsMessageRepository.countBySenderIdAndChurchId(senderId, churchId);
        long sent = smsMessageRepository.countBySenderIdAndChurchIdAndStatus(senderId, churchId, SmsStatus.SENT);
        long delivered = smsMessageRepository.countBySenderIdAndChurchIdAndStatus(senderId, churchId, SmsStatus.DELIVERED);
        long failed = smsMessageRepository.countBySenderIdAndChurchIdAndStatus(senderId, churchId, SmsStatus.FAILED);

        Double totalCost = smsMessageRepository.sumCostBySenderIdAndChurchIdAndStatusIn(
            senderId, churchId, List.of(SmsStatus.SENT, SmsStatus.DELIVERED));

        return Map.of(
            "total", total,
            "sent", sent,
            "delivered", delivered,
            "failed", failed,
            "totalCost", totalCost != null ? totalCost : 0.0,
            "currentBalance", smsCreditService.getChurchBalance(churchId)  // Church balance
        );
    }
}
```

---

### Step 0.5: Update Controllers (30 minutes)

#### 0.5.1: Update SmsCreditController
```java
// File: src/main/java/com/reuben/pastcare_spring/controllers/SmsCreditController.java

@RestController
@RequestMapping("/api/sms-credits")
@Slf4j
public class SmsCreditController {

    private final SmsCreditService smsCreditService;
    private final UserRepository userRepository;
    private final ChurchRepository churchRepository;

    /**
     * Get church credit balance
     */
    @GetMapping("/balance")
    public ResponseEntity<Map<String, Object>> getChurchBalance(Authentication authentication) {
        try {
            Long churchId = TenantContext.getCurrentChurchId();
            BigDecimal balance = smsCreditService.getChurchBalance(churchId);

            return ResponseEntity.ok(Map.of(
                "churchId", churchId,
                "balance", balance,
                "balanceType", "CHURCH_LEVEL"
            ));
        } catch (Exception e) {
            log.error("Error fetching church balance: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Purchase credits for church
     */
    @PostMapping("/purchase")
    public ResponseEntity<SmsTransactionResponse> purchaseCredits(
        @Valid @RequestBody PurchaseCreditsRequest request,
        Authentication authentication
    ) {
        try {
            User user = getUserFromAuth(authentication);
            Church church = getChurch();

            SmsTransaction transaction = smsCreditService.purchaseChurchCredits(
                church.getId(),
                user.getId(),
                request.getAmount(),
                request.getPaymentReference()
            );

            return ResponseEntity.ok(mapToResponse(transaction));

        } catch (Exception e) {
            log.error("Error purchasing credits: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get church transaction history
     */
    @GetMapping("/transactions")
    public ResponseEntity<Page<SmsTransactionResponse>> getTransactions(
        Authentication authentication,
        Pageable pageable
    ) {
        try {
            Long churchId = TenantContext.getCurrentChurchId();
            Page<SmsTransaction> transactions = smsCreditService.getChurchTransactions(churchId, pageable);
            Page<SmsTransactionResponse> responses = transactions.map(this::mapToResponse);

            return ResponseEntity.ok(responses);

        } catch (Exception e) {
            log.error("Error fetching transactions: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Helper methods...
}
```

---

### Step 0.6: Frontend Updates (1 hour)

#### 0.6.1: Update SMS Stats Display
```typescript
// File: src/app/sms-page/sms-page.html
// Update labels to indicate church-level balance

<div class="stat-card">
  <div class="stat-icon">
    <i class="pi pi-wallet"></i>
  </div>
  <div class="stat-content">
    <div class="stat-label">Church SMS Balance</div>
    <div class="stat-value">GHS {{ stats().currentBalance | number:'1.2-2' }}</div>
    <small class="stat-hint">Shared across all users</small>
  </div>
</div>
```

#### 0.6.2: Update Purchase Dialog
```html
<!-- Update purchase dialog copy -->
<div class="dialog-content">
  <div class="alert alert-info">
    <i class="pi pi-info-circle"></i>
    <strong>Church SMS Credit Pool</strong>
    <p>Credits purchased will be available to all authorized users in your church.</p>
    Current Balance: <strong>GHS {{ stats().currentBalance | number:'1.2-2' }}</strong>
  </div>

  <!-- Rest of purchase form... -->
</div>
```

---

### Step 0.7: Testing and Verification (1 hour)

**Test Cases**:
1. [ ] Purchase credits - verify church balance increases
2. [ ] Send SMS - verify church balance decreases
3. [ ] Failed SMS - verify credits are refunded
4. [ ] Multiple users can send SMS from shared pool
5. [ ] Insufficient balance error message is clear
6. [ ] Transaction history shows all church activity
7. [ ] Low balance alert triggers correctly

---

## 🚨 PHASE 1: Failure & Recovery System (Week 1)

**Priority**: CRITICAL for Production
**Duration**: 1 week
**Dependencies**: Phase 0 complete

### Step 1.1: Automated SMS Failure Handling (2 days)

#### 1.1.1: Create SmsFailureLog Entity
```java
// File: src/main/java/com/reuben/pastcare_spring/models/SmsFailureLog.java

@Entity
@Table(name = "sms_failure_logs")
@Data
public class SmsFailureLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sms_message_id")
    private SmsMessage smsMessage;

    @ManyToOne
    @JoinColumn(name = "church_id", nullable = false)
    private Church church;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SmsFailureType failureType;

    @Column(nullable = false, length = 1000)
    private String errorMessage;

    @Column(name = "retry_count")
    private Integer retryCount = 0;

    @Column(name = "max_retries")
    private Integer maxRetries = 3;

    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FailureResolutionStatus status = FailureResolutionStatus.PENDING;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "resolution_notes", length = 1000)
    private String resolutionNotes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

public enum SmsFailureType {
    INSUFFICIENT_BALANCE,
    GATEWAY_ERROR,
    INVALID_PHONE_NUMBER,
    NETWORK_ERROR,
    RATE_LIMIT_EXCEEDED,
    UNKNOWN_ERROR
}

public enum FailureResolutionStatus {
    PENDING,           // Waiting for retry
    RETRYING,          // Currently retrying
    RESOLVED,          // Successfully resolved
    FAILED_PERMANENTLY, // Exhausted all retries
    MANUAL_INTERVENTION_REQUIRED // Needs admin action
}
```

#### 1.1.2: Create Migration
```sql
-- File: src/main/resources/db/migration/V35__create_sms_failure_logs.sql

CREATE TABLE sms_failure_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sms_message_id BIGINT,
    church_id BIGINT NOT NULL,
    failure_type VARCHAR(50) NOT NULL,
    error_message VARCHAR(1000) NOT NULL,
    retry_count INT DEFAULT 0,
    max_retries INT DEFAULT 3,
    next_retry_at TIMESTAMP NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    resolved_at TIMESTAMP NULL,
    resolution_notes VARCHAR(1000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sms_message_id) REFERENCES sms_messages(id) ON DELETE SET NULL,
    FOREIGN KEY (church_id) REFERENCES churches(id) ON DELETE CASCADE,
    INDEX idx_status_retry (status, next_retry_at),
    INDEX idx_church_status (church_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

#### 1.1.3: Create SmsFailureService
```java
// File: src/main/java/com/reuben/pastcare_spring/services/SmsFailureService.java

@Service
@Slf4j
public class SmsFailureService {

    private final SmsFailureLogRepository failureLogRepository;
    private final SmsCreditService smsCreditService;
    private final SmsService smsService;

    /**
     * Log SMS failure
     */
    @Transactional
    public SmsFailureLog logFailure(
        SmsMessage smsMessage,
        Church church,
        SmsFailureType failureType,
        String errorMessage
    ) {
        SmsFailureLog log = new SmsFailureLog();
        log.setSmsMessage(smsMessage);
        log.setChurch(church);
        log.setFailureType(failureType);
        log.setErrorMessage(errorMessage);
        log.setRetryCount(0);
        log.setMaxRetries(determineMaxRetries(failureType));
        log.setNextRetryAt(calculateNextRetry(0, failureType));
        log.setStatus(FailureResolutionStatus.PENDING);

        return failureLogRepository.save(log);
    }

    /**
     * Determine max retries based on failure type
     */
    private Integer determineMaxRetries(SmsFailureType failureType) {
        return switch (failureType) {
            case INSUFFICIENT_BALANCE -> 5; // Retry more times, balance might be topped up
            case GATEWAY_ERROR, NETWORK_ERROR -> 3; // Transient errors
            case RATE_LIMIT_EXCEEDED -> 2; // Wait and retry
            case INVALID_PHONE_NUMBER -> 0; // Don't retry
            case UNKNOWN_ERROR -> 2;
        };
    }

    /**
     * Calculate next retry time (exponential backoff)
     */
    private LocalDateTime calculateNextRetry(int retryCount, SmsFailureType failureType) {
        int baseDelayMinutes = switch (failureType) {
            case INSUFFICIENT_BALANCE -> 30; // Wait 30 min for balance top-up
            case GATEWAY_ERROR -> 5;
            case NETWORK_ERROR -> 10;
            case RATE_LIMIT_EXCEEDED -> 60; // Wait 1 hour
            case INVALID_PHONE_NUMBER -> 0; // Never retry
            case UNKNOWN_ERROR -> 15;
        };

        // Exponential backoff: baseDelay * (2 ^ retryCount)
        int delayMinutes = baseDelayMinutes * (int) Math.pow(2, retryCount);
        return LocalDateTime.now().plusMinutes(delayMinutes);
    }

    /**
     * Scheduled job to retry failed messages
     */
    @Scheduled(fixedDelay = 300000) // Run every 5 minutes
    @Transactional
    public void retryFailedMessages() {
        log.info("Starting retry process for failed SMS messages");

        LocalDateTime now = LocalDateTime.now();
        List<SmsFailureLog> pendingRetries = failureLogRepository
            .findByStatusAndNextRetryAtBefore(FailureResolutionStatus.PENDING, now);

        for (SmsFailureLog failureLog : pendingRetries) {
            retryFailedMessage(failureLog);
        }

        log.info("Completed retry process. Processed {} messages", pendingRetries.size());
    }

    /**
     * Retry a single failed message
     */
    @Transactional
    public void retryFailedMessage(SmsFailureLog failureLog) {
        try {
            SmsMessage smsMessage = failureLog.getSmsMessage();
            Church church = failureLog.getChurch();

            // Check if we've exceeded max retries
            if (failureLog.getRetryCount() >= failureLog.getMaxRetries()) {
                failureLog.setStatus(FailureResolutionStatus.FAILED_PERMANENTLY);
                failureLog.setResolutionNotes("Exceeded maximum retry attempts");
                failureLogRepository.save(failureLog);
                log.warn("SMS {} failed permanently after {} retries",
                    smsMessage.getId(), failureLog.getRetryCount());
                return;
            }

            // Update retry status
            failureLog.setStatus(FailureResolutionStatus.RETRYING);
            failureLog.setRetryCount(failureLog.getRetryCount() + 1);
            failureLogRepository.save(failureLog);

            // Check failure type and handle accordingly
            switch (failureLog.getFailureType()) {
                case INSUFFICIENT_BALANCE:
                    retryInsufficientBalance(smsMessage, church, failureLog);
                    break;

                case GATEWAY_ERROR:
                case NETWORK_ERROR:
                case UNKNOWN_ERROR:
                    retryGatewayError(smsMessage, church, failureLog);
                    break;

                case RATE_LIMIT_EXCEEDED:
                    retryRateLimitExceeded(smsMessage, church, failureLog);
                    break;

                case INVALID_PHONE_NUMBER:
                    // Don't retry, mark as requiring manual intervention
                    failureLog.setStatus(FailureResolutionStatus.MANUAL_INTERVENTION_REQUIRED);
                    failureLog.setResolutionNotes("Invalid phone number - requires correction");
                    failureLogRepository.save(failureLog);
                    break;
            }

        } catch (Exception e) {
            log.error("Error retrying failed message {}: {}",
                failureLog.getId(), e.getMessage(), e);

            // Schedule next retry
            failureLog.setStatus(FailureResolutionStatus.PENDING);
            failureLog.setNextRetryAt(calculateNextRetry(failureLog.getRetryCount(),
                failureLog.getFailureType()));
            failureLogRepository.save(failureLog);
        }
    }

    /**
     * Retry message that failed due to insufficient balance
     */
    private void retryInsufficientBalance(
        SmsMessage smsMessage,
        Church church,
        SmsFailureLog failureLog
    ) {
        // Check if church now has sufficient balance
        if (smsCreditService.hasChurchCredits(church.getId(), smsMessage.getCost())) {
            // Retry sending
            try {
                smsService.sendImmediately(smsMessage, smsMessage.getSender(), church);

                // Mark as resolved
                failureLog.setStatus(FailureResolutionStatus.RESOLVED);
                failureLog.setResolvedAt(LocalDateTime.now());
                failureLog.setResolutionNotes("Successfully sent after balance top-up");
                failureLogRepository.save(failureLog);

                log.info("Successfully retried SMS {} after balance top-up", smsMessage.getId());

            } catch (Exception e) {
                // Still failing, schedule next retry
                failureLog.setStatus(FailureResolutionStatus.PENDING);
                failureLog.setNextRetryAt(calculateNextRetry(failureLog.getRetryCount(),
                    failureLog.getFailureType()));
                failureLog.setResolutionNotes("Retry failed: " + e.getMessage());
                failureLogRepository.save(failureLog);
            }
        } else {
            // Still insufficient balance
            BigDecimal currentBalance = smsCreditService.getChurchBalance(church.getId());
            BigDecimal required = smsMessage.getCost();

            failureLog.setStatus(FailureResolutionStatus.PENDING);
            failureLog.setNextRetryAt(calculateNextRetry(failureLog.getRetryCount(),
                failureLog.getFailureType()));
            failureLog.setResolutionNotes(
                String.format("Still insufficient balance. Required: %.2f, Available: %.2f",
                    required, currentBalance));
            failureLogRepository.save(failureLog);

            log.warn("Cannot retry SMS {} - insufficient balance. Required: {}, Available: {}",
                smsMessage.getId(), required, currentBalance);
        }
    }

    /**
     * Retry message that failed due to gateway error
     */
    private void retryGatewayError(
        SmsMessage smsMessage,
        Church church,
        SmsFailureLog failureLog
    ) {
        try {
            smsService.sendImmediately(smsMessage, smsMessage.getSender(), church);

            // Mark as resolved
            failureLog.setStatus(FailureResolutionStatus.RESOLVED);
            failureLog.setResolvedAt(LocalDateTime.now());
            failureLog.setResolutionNotes("Successfully sent on retry #" + failureLog.getRetryCount());
            failureLogRepository.save(failureLog);

            log.info("Successfully retried SMS {} after gateway error", smsMessage.getId());

        } catch (Exception e) {
            // Still failing, schedule next retry
            failureLog.setStatus(FailureResolutionStatus.PENDING);
            failureLog.setNextRetryAt(calculateNextRetry(failureLog.getRetryCount(),
                failureLog.getFailureType()));
            failureLog.setResolutionNotes("Retry #" + failureLog.getRetryCount() +
                " failed: " + e.getMessage());
            failureLogRepository.save(failureLog);
        }
    }

    /**
     * Retry message that failed due to rate limiting
     */
    private void retryRateLimitExceeded(
        SmsMessage smsMessage,
        Church church,
        SmsFailureLog failureLog
    ) {
        // Similar to retryGatewayError but with longer backoff
        retryGatewayError(smsMessage, church, failureLog);
    }

    /**
     * Get failed messages requiring manual intervention
     */
    public List<SmsFailureLog> getMessagesRequiringIntervention(Long churchId) {
        return failureLogRepository.findByChurchIdAndStatus(
            churchId,
            FailureResolutionStatus.MANUAL_INTERVENTION_REQUIRED
        );
    }

    /**
     * Get permanently failed messages
     */
    public List<SmsFailureLog> getPermanentlyFailedMessages(Long churchId) {
        return failureLogRepository.findByChurchIdAndStatus(
            churchId,
            FailureResolutionStatus.FAILED_PERMANENTLY
        );
    }
}
```

---

### Step 1.2: Low Balance Alert System (1 day)

#### 1.2.1: Create LowBalanceAlertService
```java
// File: src/main/java/com/reuben/pastcare_spring/services/LowBalanceAlertService.java

@Service
@Slf4j
public class LowBalanceAlertService {

    private final ChurchSmsCreditRepository churchCreditRepository;
    private final UserRepository userRepository;
    private final SmsService smsService;

    /**
     * Scheduled job to check for low balances
     */
    @Scheduled(cron = "0 0 9,15 * * *") // Run at 9 AM and 3 PM daily
    @Transactional
    public void checkLowBalances() {
        log.info("Checking for churches with low SMS balance");

        List<ChurchSmsCredit> lowBalanceChurches = churchCreditRepository
            .findChurchesWithLowBalance();

        for (ChurchSmsCredit credit : lowBalanceChurches) {
            sendLowBalanceAlert(credit);
        }

        log.info("Low balance check complete. Alerted {} churches", lowBalanceChurches.size());
    }

    /**
     * Send low balance alert to church admins
     */
    @Transactional
    public void sendLowBalanceAlert(ChurchSmsCredit credit) {
        try {
            Church church = credit.getChurch();

            // Get church admin users
            List<User> admins = userRepository.findByChurchIdAndRole(
                church.getId(),
                UserRole.ADMIN
            );

            if (admins.isEmpty()) {
                log.warn("No admin users found for church {}", church.getId());
                return;
            }

            String message = String.format(
                "ALERT: Your church SMS balance is low. Current balance: GHS %.2f. " +
                "Please top up to continue sending messages. - PastCare",
                credit.getBalance()
            );

            // Send SMS to all admins
            for (User admin : admins) {
                if (admin.getPhoneNumber() != null && !admin.getPhoneNumber().isEmpty()) {
                    try {
                        smsService.sendSms(
                            admin,
                            church,
                            admin.getPhoneNumber(),
                            admin.getName(),
                            message,
                            null,
                            null
                        );
                    } catch (IllegalStateException e) {
                        // Insufficient balance to send alert - log only
                        log.error("Cannot send low balance alert - insufficient credits");
                        break;
                    }
                }
            }

            // Mark alert as sent
            credit.setLowBalanceAlertSent(true);
            churchCreditRepository.save(credit);

            log.info("Sent low balance alert for church {}", church.getId());

        } catch (Exception e) {
            log.error("Error sending low balance alert: {}", e.getMessage(), e);
        }
    }
}
```

---

### Step 1.3: Automated Message Queuing System (2 days)

#### 1.3.1: Create SmsQueue Entity
```java
// File: src/main/java/com/reuben/pastcare_spring/models/SmsQueue.java

@Entity
@Table(name = "sms_queue")
@Data
public class SmsQueue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "church_id", nullable = false)
    private Church church;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SmsQueueType queueType;

    @Column(name = "recipient_phone", nullable = false)
    private String recipientPhone;

    @Column(name = "recipient_name")
    private String recipientName;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal estimatedCost;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SmsQueueStatus status = SmsQueueStatus.PENDING;

    @Column(name = "scheduled_for")
    private LocalDateTime scheduledFor;

    @Column(name = "priority")
    private Integer priority = 5; // 1 = highest, 10 = lowest

    @Column(name = "attempts")
    private Integer attempts = 0;

    @Column(name = "max_attempts")
    private Integer maxAttempts = 3;

    @Column(name = "last_attempt_at")
    private LocalDateTime lastAttemptAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @ManyToOne
    @JoinColumn(name = "sms_message_id")
    private SmsMessage smsMessage; // Link to sent message

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (scheduledFor == null) {
            scheduledFor = LocalDateTime.now();
        }
    }
}

public enum SmsQueueType {
    MANUAL,              // User-initiated
    AUTOMATED_THANK_YOU, // Donation thank you
    AUTOMATED_REMINDER,  // Event/attendance reminder
    AUTOMATED_BIRTHDAY,  // Birthday wish
    AUTOMATED_FOLLOWUP,  // Visitor followup
    SYSTEM_ALERT        // System notifications
}

public enum SmsQueueStatus {
    PENDING,    // Waiting to be processed
    PROCESSING, // Currently being sent
    COMPLETED,  // Successfully sent
    FAILED,     // Failed permanently
    DEFERRED    // Deferred due to insufficient balance
}
```

#### 1.3.2: Create Migration
```sql
-- File: src/main/resources/db/migration/V36__create_sms_queue.sql

CREATE TABLE sms_queue (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    church_id BIGINT NOT NULL,
    queue_type VARCHAR(50) NOT NULL,
    recipient_phone VARCHAR(20) NOT NULL,
    recipient_name VARCHAR(255),
    member_id BIGINT,
    message TEXT NOT NULL,
    estimated_cost DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    scheduled_for TIMESTAMP NOT NULL,
    priority INT DEFAULT 5,
    attempts INT DEFAULT 0,
    max_attempts INT DEFAULT 3,
    last_attempt_at TIMESTAMP NULL,
    processed_at TIMESTAMP NULL,
    error_message VARCHAR(1000),
    sms_message_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (church_id) REFERENCES churches(id) ON DELETE CASCADE,
    FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE SET NULL,
    FOREIGN KEY (sms_message_id) REFERENCES sms_messages(id) ON DELETE SET NULL,
    INDEX idx_status_scheduled (status, scheduled_for),
    INDEX idx_church_status (church_id, status),
    INDEX idx_priority (priority, scheduled_for)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

#### 1.3.3: Create SmsQueueService
```java
// File: src/main/java/com/reuben/pastcare_spring/services/SmsQueueService.java

@Service
@Slf4j
public class SmsQueueService {

    private final SmsQueueRepository queueRepository;
    private final SmsService smsService;
    private final SmsCreditService smsCreditService;
    private final SmsFailureService failureService;

    /**
     * Queue an SMS for sending
     */
    @Transactional
    public SmsQueue queueSms(
        Church church,
        String recipientPhone,
        String recipientName,
        Member member,
        String message,
        SmsQueueType queueType,
        LocalDateTime scheduledFor,
        Integer priority
    ) {
        BigDecimal estimatedCost = smsCreditService.calculateSmsCost(recipientPhone, message);

        SmsQueue queue = new SmsQueue();
        queue.setChurch(church);
        queue.setQueueType(queueType);
        queue.setRecipientPhone(recipientPhone);
        queue.setRecipientName(recipientName);
        queue.setMember(member);
        queue.setMessage(message);
        queue.setEstimatedCost(estimatedCost);
        queue.setStatus(SmsQueueStatus.PENDING);
        queue.setScheduledFor(scheduledFor != null ? scheduledFor : LocalDateTime.now());
        queue.setPriority(priority != null ? priority : 5);

        return queueRepository.save(queue);
    }

    /**
     * Process SMS queue (scheduled job)
     */
    @Scheduled(fixedDelay = 60000) // Run every 1 minute
    @Transactional
    public void processQueue() {
        LocalDateTime now = LocalDateTime.now();

        // Get pending messages scheduled for now or earlier, ordered by priority
        List<SmsQueue> pendingMessages = queueRepository
            .findByStatusAndScheduledForBeforeOrderByPriorityAscScheduledForAsc(
                SmsQueueStatus.PENDING,
                now
            );

        log.info("Processing SMS queue - {} messages pending", pendingMessages.size());

        for (SmsQueue queueItem : pendingMessages) {
            processQueueItem(queueItem);
        }
    }

    /**
     * Process a single queue item
     */
    @Transactional
    public void processQueueItem(SmsQueue queueItem) {
        try {
            Church church = queueItem.getChurch();

            // Check if church has sufficient balance
            if (!smsCreditService.hasChurchCredits(church.getId(), queueItem.getEstimatedCost())) {
                handleInsufficientBalance(queueItem);
                return;
            }

            // Mark as processing
            queueItem.setStatus(SmsQueueStatus.PROCESSING);
            queueItem.setAttempts(queueItem.getAttempts() + 1);
            queueItem.setLastAttemptAt(LocalDateTime.now());
            queueRepository.save(queueItem);

            // Get sender (use first admin or system user)
            User sender = getSystemUser(church);

            // Send SMS
            SmsMessage sentMessage = smsService.sendSms(
                sender,
                church,
                queueItem.getRecipientPhone(),
                queueItem.getRecipientName(),
                queueItem.getMessage(),
                queueItem.getMember(),
                null // Send immediately
            );

            // Mark as completed
            queueItem.setStatus(SmsQueueStatus.COMPLETED);
            queueItem.setProcessedAt(LocalDateTime.now());
            queueItem.setSmsMessage(sentMessage);
            queueRepository.save(queueItem);

            log.info("Successfully processed queue item {}", queueItem.getId());

        } catch (IllegalStateException e) {
            // Insufficient balance
            handleInsufficientBalance(queueItem);

        } catch (Exception e) {
            handleProcessingError(queueItem, e);
        }
    }

    /**
     * Handle insufficient balance
     */
    private void handleInsufficientBalance(SmsQueue queueItem) {
        log.warn("Insufficient balance for queue item {}", queueItem.getId());

        // If we've tried too many times, mark as failed
        if (queueItem.getAttempts() >= queueItem.getMaxAttempts()) {
            queueItem.setStatus(SmsQueueStatus.FAILED);
            queueItem.setErrorMessage("Insufficient balance after " +
                queueItem.getAttempts() + " attempts");
            queueItem.setProcessedAt(LocalDateTime.now());

            // Create failure log for tracking
            failureService.logFailure(
                null,
                queueItem.getChurch(),
                SmsFailureType.INSUFFICIENT_BALANCE,
                "Queue item " + queueItem.getId() + " failed - insufficient balance"
            );
        } else {
            // Defer processing (will retry in next cycle)
            queueItem.setStatus(SmsQueueStatus.DEFERRED);
            queueItem.setScheduledFor(LocalDateTime.now().plusMinutes(30)); // Retry in 30 min
            queueItem.setErrorMessage("Deferred due to insufficient balance");
        }

        queueRepository.save(queueItem);
    }

    /**
     * Handle processing error
     */
    private void handleProcessingError(SmsQueue queueItem, Exception e) {
        log.error("Error processing queue item {}: {}",
            queueItem.getId(), e.getMessage(), e);

        queueItem.setErrorMessage(e.getMessage());

        if (queueItem.getAttempts() >= queueItem.getMaxAttempts()) {
            queueItem.setStatus(SmsQueueStatus.FAILED);
            queueItem.setProcessedAt(LocalDateTime.now());

            // Create failure log
            failureService.logFailure(
                null,
                queueItem.getChurch(),
                SmsFailureType.UNKNOWN_ERROR,
                "Queue item " + queueItem.getId() + " failed: " + e.getMessage()
            );
        } else {
            // Retry later
            queueItem.setStatus(SmsQueueStatus.PENDING);
            queueItem.setScheduledFor(LocalDateTime.now().plusMinutes(15));
        }

        queueRepository.save(queueItem);
    }

    /**
     * Get system user for church (for automated messages)
     */
    private User getSystemUser(Church church) {
        // Get first admin user
        return userRepository.findByChurchIdAndRole(church.getId(), UserRole.ADMIN)
            .stream()
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No admin user found for church"));
    }
}
```

---

**[TRUNCATED DUE TO LENGTH - This is a comprehensive 10-week plan]**

**Continuing in next response with:**
- Phase 2: Giving Module Phase 4 (Weeks 2-3)
- Phase 3: Dashboard Module Phase 2 (Week 4)
- Phase 4: Events Module (Weeks 5-7)
- Phase 5: Testing & Polish (Weeks 8-10)

Should I continue with the complete implementation plan?
