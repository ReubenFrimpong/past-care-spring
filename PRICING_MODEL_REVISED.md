# PastCare SaaS - Revised Pricing Model

**Date**: 2025-12-28
**Status**: REVISED
**Based on**: VPS storage costs and market analysis

---

## Market Analysis

### Typical VPS Storage Costs
- **DigitalOcean**: $0.10/GB/month (block storage)
- **Linode**: $0.10/GB/month
- **Vultr**: $0.10/GB/month
- **AWS EBS**: $0.10/GB/month (gp3)

**Conclusion**: 10 GB costs ~$1.00/month in infrastructure. For a $9.99 subscription, that's only 10% of revenue, which is actually **reasonable**. However, we can optimize further.

---

## Revised Pricing Model

### Option 1: Tiered Storage (Recommended)

```
┌──────────────────────────────────────────────────────────────┐
│              PASTCARE SAAS PRICING - TIERED                  │
├──────────────────────────────────────────────────────────────┤
│ Base Plan: USD 9.99/month                                   │
│ Includes: 2 GB storage                                       │
│                                                              │
│ Storage Add-ons:                                             │
│ - 5 GB:  +USD 1.00/month (Total: $10.99)                    │
│ - 10 GB: +USD 2.00/month (Total: $11.99)                    │
│ - 20 GB: +USD 3.00/month (Total: $12.99)                    │
│ - 50 GB: +USD 6.00/month (Total: $15.99)                    │
│                                                              │
│ Features: All features included (unlimited)                  │
│ - Unlimited members                                          │
│ - Unlimited fellowships, events, donations                   │
│ - Unlimited users                                            │
│ - Full feature access                                        │
│ - Email & priority support                                   │
│                                                              │
│ Trial: 14 days free (2 GB storage)                          │
│ Custom Incentives: Flexible trial extension (see below)     │
└──────────────────────────────────────────────────────────────┘
```

**Rationale**:
- **2 GB base** is sufficient for most small churches (100-200 members)
- Average member photo: ~200 KB = 10,000 photos in 2 GB
- Event images: ~500 KB = 4,000 event photos in 2 GB
- Allows upsell for growing churches
- Better profit margins (80-90% instead of 50-60%)

---

### Option 2: Single Price, Lower Storage (More Profitable)

```
┌──────────────────────────────────────────────────────────────┐
│              PASTCARE SAAS PRICING - SIMPLE                  │
├──────────────────────────────────────────────────────────────┤
│ Standard Plan: USD 9.99/month                               │
│ Includes: 1 GB storage                                       │
│                                                              │
│ Additional Storage:                                          │
│ - USD 1.00 per additional 1 GB/month                        │
│                                                              │
│ Features: All features included (unlimited)                  │
│                                                              │
│ Trial: 14 days free (1 GB storage)                          │
│ Custom Incentives: Up to 3 months free for institutions     │
└──────────────────────────────────────────────────────────────┘
```

**Rationale**:
- **1 GB base** is still generous for text-heavy data
- Most churches won't exceed 1 GB without extensive photo uploads
- Better profit margins (90%+)
- Simpler pricing: $1 = 1 GB

---

## Storage Usage Analysis

### Typical Church Data Sizes

**Small Church (50-100 members)**:
```
Member profiles:        100 members × 200 KB = 20 MB
Event images:          20 events × 500 KB = 10 MB
Documents/exports:     50 MB
Church assets:         20 MB
Total:                 ~100 MB (well under 1 GB)
```

**Medium Church (200-500 members)**:
```
Member profiles:        500 members × 200 KB = 100 MB
Event images:          100 events × 500 KB = 50 MB
Documents/exports:     150 MB
Church assets:         50 MB
Total:                 ~350 MB (still under 1 GB)
```

**Large Church (1000+ members, active media)**:
```
Member profiles:        1000 members × 200 KB = 200 MB
Event images:          500 events × 500 KB = 250 MB
Event videos (optional): N/A (use YouTube/Vimeo links)
Documents/exports:     300 MB
Church assets:         100 MB
Backups:              150 MB
Total:                 ~1 GB (would need 2-5 GB plan)
```

**Conclusion**:
- 1-2 GB is sufficient for 90% of churches
- Only mega-churches with extensive media libraries need 10+ GB
- Video hosting should be external (YouTube, Vimeo) to save costs

---

## Recommended Pricing Model (Hybrid)

### Base Offering

```
┌──────────────────────────────────────────────────────────────┐
│              PASTCARE SAAS - FINAL PRICING                   │
├──────────────────────────────────────────────────────────────┤
│ Monthly Plan: USD 9.99/month                                │
│ Includes:                                                    │
│   • 2 GB storage                                             │
│   • Unlimited members, fellowships, events                   │
│   • Unlimited users                                          │
│   • Full feature access                                      │
│   • Email support                                            │
│                                                              │
│ Yearly Plan: USD 99/year (2 months free)                    │
│ Includes: Same as monthly + priority support                 │
│                                                              │
│ Storage Add-ons (charged monthly):                           │
│   • +3 GB: $1.50/month                                       │
│   • +8 GB: $3.00/month (5 GB total = $12.99/month)          │
│   • +18 GB: $6.00/month (20 GB total = $15.99/month)         │
│   • +48 GB: $12.00/month (50 GB total = $21.99/month)        │
│                                                              │
│ Trial: 14 days free (2 GB storage limit)                    │
└──────────────────────────────────────────────────────────────┘
```

---

## Flexible Trial & Incentive System

### Standard Trial
- **Duration**: 14 days
- **Storage**: 2 GB
- **Features**: Full access
- **Conversion**: Auto-prompt to subscribe on day 13

### Custom Incentives (Admin-Controlled)

**Use Cases**:
1. **Partner Churches**: 1-3 months free
2. **Referral Program**: 1 month free per referral
3. **Annual Commitment**: 2 months free (via yearly pricing)
4. **Early Adopters**: Custom trial periods
5. **Non-Profits**: Extended trials or discounts

**Implementation**:
```java
public class ChurchSubscription {
    // ... existing fields

    // Custom trial extension
    private Integer bonusTrialDays;  // Additional days beyond standard 14
    private String incentiveCode;    // Tracking code for incentive
    private String incentiveReason;  // "PARTNER", "REFERRAL", "NONPROFIT", etc.

    // Discount management
    private BigDecimal discountPercentage;  // 0-100 (e.g., 20 = 20% off)
    private LocalDate discountExpiresAt;    // When discount ends
}
```

**Admin Interface**:
```typescript
// Superadmin can grant incentives
interface IncentiveGrant {
  churchId: number;
  type: 'EXTENDED_TRIAL' | 'DISCOUNT' | 'FREE_MONTHS';
  value: number;  // Days or percentage
  reason: string;
  expiresAt?: Date;
}
```

---

## Database Schema Update

```sql
-- Updated church_subscription table
CREATE TABLE church_subscription (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    church_id BIGINT NOT NULL UNIQUE,
    plan_id BIGINT NOT NULL,

    -- Status
    status VARCHAR(20) NOT NULL DEFAULT 'TRIAL',
    -- TRIAL, ACTIVE, PAST_DUE, CANCELLED, SUSPENDED

    -- Billing
    billing_cycle VARCHAR(20) DEFAULT 'MONTHLY',  -- MONTHLY, YEARLY
    current_period_start DATE NOT NULL,
    current_period_end DATE NOT NULL,
    next_billing_date DATE,

    -- Trial (Enhanced)
    trial_start DATE,
    trial_end DATE,
    is_trial BOOLEAN DEFAULT FALSE,
    bonus_trial_days INT DEFAULT 0,          -- NEW: Additional trial days
    incentive_code VARCHAR(50),              -- NEW: Tracking code
    incentive_reason VARCHAR(100),           -- NEW: Reason for incentive

    -- Storage
    storage_limit_gb INT NOT NULL DEFAULT 2,  -- CHANGED: From 10 to 2
    current_storage_gb DECIMAL(10, 2) DEFAULT 0.00,

    -- Pricing (Enhanced)
    base_price_usd DECIMAL(10, 2) NOT NULL DEFAULT 9.99,
    storage_addon_price_usd DECIMAL(10, 2) DEFAULT 0.00,
    discount_percentage DECIMAL(5, 2) DEFAULT 0.00,  -- NEW: 0-100
    discount_expires_at DATE,                        -- NEW: Discount expiry

    -- Payment
    last_payment_date DATE,
    last_payment_amount DECIMAL(10, 2),

    -- Cancellation
    cancel_at_period_end BOOLEAN DEFAULT FALSE,
    cancelled_at TIMESTAMP,
    cancellation_reason TEXT,

    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_church_subscription_church
        FOREIGN KEY (church_id) REFERENCES church(id) ON DELETE CASCADE,
    CONSTRAINT fk_church_subscription_plan
        FOREIGN KEY (plan_id) REFERENCES subscription_plan(id),

    INDEX idx_subscription_status (status),
    INDEX idx_subscription_billing_date (next_billing_date),
    INDEX idx_subscription_trial (is_trial, trial_end),
    INDEX idx_subscription_incentive (incentive_code)
);

-- Subscription plan (updated)
CREATE TABLE subscription_plan (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL DEFAULT 'STANDARD',
    display_name VARCHAR(100) NOT NULL DEFAULT 'PastCare Standard',

    -- Pricing
    base_price_monthly_usd DECIMAL(10, 2) NOT NULL DEFAULT 9.99,
    base_price_yearly_usd DECIMAL(10, 2) NOT NULL DEFAULT 99.00,  -- 2 months free
    base_storage_gb INT NOT NULL DEFAULT 2,  -- CHANGED: From 10 to 2

    -- Storage pricing tiers (JSON for flexibility)
    storage_tiers JSON,
    -- Example: [
    --   {"gb": 3, "price": 1.50},
    --   {"gb": 8, "price": 3.00},
    --   {"gb": 18, "price": 6.00},
    --   {"gb": 48, "price": 12.00}
    -- ]

    currency VARCHAR(3) DEFAULT 'USD',
    trial_days INT DEFAULT 14,  -- CHANGED: From 30 to 14
    is_active BOOLEAN DEFAULT TRUE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Incentive tracking table (for analytics)
CREATE TABLE incentive_grant (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    church_subscription_id BIGINT NOT NULL,

    -- Incentive details
    incentive_type VARCHAR(30) NOT NULL,  -- EXTENDED_TRIAL, DISCOUNT, FREE_MONTHS
    value_amount DECIMAL(10, 2),          -- Days or percentage
    incentive_code VARCHAR(50),
    reason VARCHAR(200),

    -- Granted by
    granted_by_user_id BIGINT,
    granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Expiration
    expires_at DATE,

    -- Status
    is_active BOOLEAN DEFAULT TRUE,
    redeemed_at TIMESTAMP,

    CONSTRAINT fk_incentive_subscription
        FOREIGN KEY (church_subscription_id)
        REFERENCES church_subscription(id) ON DELETE CASCADE,

    INDEX idx_incentive_code (incentive_code),
    INDEX idx_incentive_church (church_subscription_id)
);
```

---

## Pricing Calculation Examples

### Example 1: Small Church (Base Plan)
```
Monthly Plan:
- Base price:        $9.99
- Storage used:      0.8 GB (within 2 GB limit)
- Storage add-on:    $0.00
Total:              $9.99/month
```

### Example 2: Medium Church (Need 5 GB)
```
Monthly Plan:
- Base price:        $9.99
- Storage used:      4.5 GB
- Storage add-on:    +8 GB tier = $3.00
Total:              $12.99/month
```

### Example 3: Large Church (Need 20 GB)
```
Monthly Plan:
- Base price:        $9.99
- Storage used:      18 GB
- Storage add-on:    +18 GB tier = $6.00
Total:              $15.99/month
```

### Example 4: Partner Church (3 Months Free + Discount)
```
Incentive:
- Extended trial:    90 days (instead of 14)
- After trial:       20% discount for 12 months
- Ongoing price:     $7.99/month (20% off $9.99)
```

---

## Revenue Projections (Revised)

### Scenario: 100 Churches

**Base Plan Distribution** (2 GB):
- 70 churches: $9.99 × 70 = $699.30/month

**With Storage Add-ons**:
- 20 churches: $12.99 × 20 = $259.80/month (+8 GB)
- 8 churches: $15.99 × 8 = $127.92/month (+18 GB)
- 2 churches: $21.99 × 2 = $43.98/month (+48 GB)

**Total Monthly Revenue**: $1,131.00

**Infrastructure Costs** (100 churches):
- Average storage per church: ~5 GB
- Total storage: 500 GB × $0.10 = $50/month
- Database: $20/month
- Compute: $30/month
- Total costs: ~$100/month

**Profit Margin**: ~91% ($1,031 profit on $1,131 revenue)

---

## Recommendations

### Primary Recommendation
✅ **Use Tiered Storage Model**:
- **Base**: $9.99/month with 2 GB
- **Add-ons**: $1.50 to $12 for 3-48 GB tiers
- **Trial**: 14 days standard
- **Incentives**: Flexible admin-controlled system

### Why This Model Works
1. **Affordable Entry Point**: $9.99 is competitive
2. **Room for Growth**: Upsell storage as churches grow
3. **High Margins**: 85-90% profit margins
4. **Flexible Incentives**: Can grant extended trials without code changes
5. **Predictable Costs**: Storage is only real variable cost

### Implementation Priority
1. ✅ Update database schema (2 GB base, incentive fields)
2. ✅ Add storage tier pricing logic
3. ✅ Create admin interface for granting incentives
4. ✅ Build storage usage monitoring
5. ✅ Implement automatic storage tier upgrade prompts

---

## Next Steps

1. **Finalize pricing tiers** - Confirm the exact storage tiers
2. **Update database migrations** - Add new fields for incentives
3. **Build incentive management** - Admin UI for granting custom trials/discounts
4. **Create pricing calculator** - Help churches choose right tier
5. **Implement storage monitoring** - Alert when approaching limits

---

**Document Status**: Revised and Ready for Implementation
**Updated**: 2025-12-28
**Replaces**: Previous 10 GB base model
