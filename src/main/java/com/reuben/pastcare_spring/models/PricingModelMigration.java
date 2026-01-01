package com.reuben.pastcare_spring.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity for tracking migration from storage-based to congregation-based pricing.
 *
 * <p>Maintains audit trail of pricing model migrations for each church.
 * Supports rollback if needed.
 *
 * @since 2026-01-01
 */
@Entity
@Table(name = "pricing_model_migrations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PricingModelMigration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Church being migrated.
     */
    @Column(nullable = false)
    private Long churchId;

    /**
     * Previous subscription plan ID (storage-based).
     */
    @Column
    private Long oldPlanId;

    /**
     * Previous storage limit in MB.
     */
    @Column
    private Long oldStorageLimitMb;

    /**
     * Previous monthly price in USD.
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal oldMonthlyPrice;

    /**
     * New pricing tier ID (congregation-based).
     */
    @Column(nullable = false)
    private Long newPricingTierId;

    /**
     * Member count at time of migration.
     */
    @Column(nullable = false)
    private Integer newMemberCount;

    /**
     * New monthly price in USD.
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal newMonthlyPrice;

    /**
     * When migration was performed.
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime migratedAt;

    /**
     * SUPERADMIN user who performed migration (null for automated).
     */
    @Column
    private Long migratedBy;

    /**
     * Notes about migration.
     */
    @Column(columnDefinition = "TEXT")
    private String migrationNotes;

    /**
     * Migration status.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private MigrationStatus migrationStatus = MigrationStatus.COMPLETED;

    public enum MigrationStatus {
        PENDING,
        COMPLETED,
        ROLLED_BACK,
        FAILED
    }

    /**
     * Calculate price change from old to new pricing.
     *
     * @return Price difference in USD (positive = increase, negative = decrease)
     */
    public BigDecimal getPriceChange() {
        if (oldMonthlyPrice == null || newMonthlyPrice == null) {
            return BigDecimal.ZERO;
        }
        return newMonthlyPrice.subtract(oldMonthlyPrice);
    }

    /**
     * Calculate percentage price change.
     *
     * @return Percentage change (positive = increase, negative = decrease)
     */
    public BigDecimal getPriceChangePercentage() {
        if (oldMonthlyPrice == null || oldMonthlyPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal change = getPriceChange();
        return change.divide(oldMonthlyPrice, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * Check if migration resulted in price increase.
     *
     * @return true if new price is higher than old price
     */
    public boolean isPriceIncrease() {
        return getPriceChange().compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Check if migration can be rolled back.
     *
     * @return true if migration is in COMPLETED status
     */
    public boolean canRollback() {
        return migrationStatus == MigrationStatus.COMPLETED;
    }

    @Override
    public String toString() {
        return "PricingModelMigration{" +
                "id=" + id +
                ", churchId=" + churchId +
                ", oldMonthlyPrice=" + oldMonthlyPrice +
                ", newMonthlyPrice=" + newMonthlyPrice +
                ", priceChange=" + getPriceChange() +
                ", migrationStatus=" + migrationStatus +
                ", migratedAt=" + migratedAt +
                '}';
    }
}
