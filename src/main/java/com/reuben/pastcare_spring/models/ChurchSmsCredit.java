package com.reuben.pastcare_spring.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ChurchSmsCredit entity for managing church-wide SMS credit pool
 * Replaces individual user SMS credits with a shared church wallet
 */
@Entity
@Table(name = "church_sms_credits", indexes = {
    @Index(name = "idx_church_balance", columnList = "church_id, balance")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_church_credits", columnNames = "church_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChurchSmsCredit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "church_id", nullable = false)
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

    @Column(name = "migration_completed")
    private Boolean migrationCompleted = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (balance == null) {
            balance = BigDecimal.ZERO;
        }
        if (totalPurchased == null) {
            totalPurchased = BigDecimal.ZERO;
        }
        if (totalUsed == null) {
            totalUsed = BigDecimal.ZERO;
        }
        if (lowBalanceAlertSent == null) {
            lowBalanceAlertSent = false;
        }
        if (lowBalanceThreshold == null) {
            lowBalanceThreshold = new BigDecimal("50.00");
        }
        if (migrationCompleted == null) {
            migrationCompleted = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Check if the church has a low balance (below threshold)
     */
    public boolean hasLowBalance() {
        return balance != null && lowBalanceThreshold != null
            && balance.compareTo(lowBalanceThreshold) < 0;
    }

    /**
     * Check if the church has sufficient balance for a transaction
     */
    public boolean hasSufficientBalance(BigDecimal required) {
        return balance != null && required != null
            && balance.compareTo(required) >= 0;
    }

    /**
     * Add credits to the church balance
     */
    public void addCredits(BigDecimal amount) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            this.balance = this.balance.add(amount);
            this.totalPurchased = this.totalPurchased.add(amount);
            this.lastPurchaseAt = LocalDateTime.now();
            this.lowBalanceAlertSent = false; // Reset alert flag
        }
    }

    /**
     * Deduct credits from the church balance
     */
    public void deductCredits(BigDecimal amount) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            if (!hasSufficientBalance(amount)) {
                throw new IllegalArgumentException(
                    "Insufficient balance. Required: " + amount + ", Available: " + balance
                );
            }
            this.balance = this.balance.subtract(amount);
            this.totalUsed = this.totalUsed.add(amount);
        }
    }

    /**
     * Refund credits to the church balance (e.g., failed SMS)
     */
    public void refundCredits(BigDecimal amount) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            this.balance = this.balance.add(amount);
            this.totalUsed = this.totalUsed.subtract(amount);
            this.lowBalanceAlertSent = false; // Reset alert flag
        }
    }
}
