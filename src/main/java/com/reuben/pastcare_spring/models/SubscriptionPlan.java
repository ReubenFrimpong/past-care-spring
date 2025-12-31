package com.reuben.pastcare_spring.models;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Subscription plan entity defining pricing tiers.
 *
 * <p>Plans:
 * <ul>
 *   <li>STARTER (Free) - 2GB storage, 5 users</li>
 *   <li>PROFESSIONAL ($50/month) - 10GB storage, 50 users</li>
 *   <li>ENTERPRISE ($150/month) - 50GB storage, unlimited users</li>
 * </ul>
 */
@Entity
@Table(name = "subscription_plans")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Plan name: STARTER, PROFESSIONAL, ENTERPRISE
     */
    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    /**
     * Display name for UI: "Starter Plan", "Professional Plan", "Enterprise Plan"
     */
    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    /**
     * Plan description
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Monthly price in USD.
     * 0.00 for Starter (free), 50.00 for Professional, 150.00 for Enterprise
     */
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * Billing interval: MONTHLY, YEARLY
     */
    @Column(name = "billing_interval", nullable = false, length = 20)
    private String billingInterval = "MONTHLY";

    /**
     * Storage limit in MB.
     * 2048 MB (2GB) for Starter, 10240 MB (10GB) for Professional, 51200 MB (50GB) for Enterprise
     */
    @Column(name = "storage_limit_mb", nullable = false)
    private Long storageLimitMb;

    /**
     * User limit.
     * 5 for Starter, 50 for Professional, -1 for unlimited (Enterprise)
     */
    @Column(name = "user_limit", nullable = false)
    private Integer userLimit;

    /**
     * Whether this is the default free plan
     */
    @Column(name = "is_free", nullable = false)
    private Boolean isFree = false;

    /**
     * Whether this plan is currently active/available
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * Paystack plan code (if integrated with Paystack)
     */
    @Column(name = "paystack_plan_code", length = 100)
    private String paystackPlanCode;

    /**
     * Features included in this plan (JSON array or comma-separated)
     * Stored as JSON string in database, exposed as List<String> to API
     */
    @Column(name = "features", columnDefinition = "TEXT")
    @lombok.Getter(lombok.AccessLevel.NONE)
    @lombok.Setter(lombok.AccessLevel.NONE)
    private String features;

    /**
     * Display order for UI
     */
    @Column(name = "display_order")
    private Integer displayOrder;

    /**
     * When this plan was created
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * When this plan was last updated
     */
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

    /**
     * Check if user limit is unlimited
     */
    public boolean isUnlimitedUsers() {
        return userLimit == -1;
    }

    /**
     * Get storage limit in GB for display
     */
    public double getStorageLimitGb() {
        return storageLimitMb / 1024.0;
    }

    /**
     * Get features as a List<String> for JSON serialization.
     * Converts the JSON string stored in database to an array.
     */
    @JsonGetter("features")
    public List<String> getFeaturesList() {
        if (features == null || features.trim().isEmpty()) {
            return new ArrayList<>();
        }

        try {
            // Try to parse as JSON array
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(features, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            // If not JSON, split by comma (legacy support)
            return List.of(features.split(","));
        }
    }

    /**
     * Set features from a List<String>.
     * Converts the list to a JSON string for database storage.
     */
    @JsonSetter("features")
    public void setFeaturesList(List<String> featuresList) {
        if (featuresList == null || featuresList.isEmpty()) {
            this.features = "[]";
            return;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            this.features = mapper.writeValueAsString(featuresList);
        } catch (Exception e) {
            // Fallback to comma-separated
            this.features = String.join(",", featuresList);
        }
    }

    /**
     * Internal getter for features string (for JPA).
     * Not exposed to JSON serialization due to @JsonIgnore annotation.
     */
    @JsonIgnore
    public String getFeatures() {
        return features;
    }

    /**
     * Internal setter for features string (for JPA).
     * Not exposed to JSON serialization due to @JsonIgnore on field.
     */
    public void setFeatures(String features) {
        this.features = features;
    }
}
