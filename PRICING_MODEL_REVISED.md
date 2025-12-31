# PastCare SaaS - Final Pricing Model

**Date**: 2025-12-30
**Status**: FINALIZED
**Based on**: Business decision - No trial, admin-controlled grace periods

---

## Final Pricing Decision

### Key Pricing Principles

1. **No Trial Period** - Churches must subscribe from day 1
2. **Grace Period** - Administered by platform admin on case-by-case basis
3. **USD Pricing** - Pricing in United States Dollars ($)
4. **Simple Pricing** - Single base plan with storage add-ons

---

## Pricing Model

```
┌──────────────────────────────────────────────────────────────┐
│              PASTCARE SAAS - FINAL PRICING                   │
├──────────────────────────────────────────────────────────────┤
│ Monthly Plan: $9.99/month (USD)                             │
│ Includes:                                                    │
│   • 2 GB storage                                             │
│   • Unlimited members, fellowships, events                   │
│   • Unlimited users                                          │
│   • Full feature access                                      │
│   • Email support                                            │
│                                                              │
│ Storage Add-ons (charged monthly):                           │
│   • +3 GB:  $1.50/month (Total: $11.49/month)              │
│   • +8 GB:  $3.00/month (Total: $12.99/month)              │
│   • +18 GB: $6.00/month (Total: $15.99/month)              │
│   • +48 GB: $12.00/month (Total: $21.99/month)             │
│                                                              │
│ Trial: NONE - Subscription required from day 1              │
│ Grace Period: Admin-controlled on case-by-case basis        │
└──────────────────────────────────────────────────────────────┘
```

---

## Grace Period System

### How Grace Periods Work

**Default Behavior**:
- No automatic trial or grace period
- Churches must have an active subscription to use the system
- Payment is required before activation

**Admin-Controlled Grace Periods**:
Platform administrators can grant grace periods for:

1. **Partner Churches** - 1-3 months grace period
2. **Special Cases** - Financial hardship, natural disasters
3. **Promotional Campaigns** - Launch promotions, referral rewards
4. **Non-Profit Organizations** - Extended grace periods
5. **Early Adopters** - Initial launch incentives

**Grace Period Fields**:
```java
public class ChurchSubscription {
    // Grace period management
    private Integer gracePeriodDays;       // Admin-granted grace days
    private LocalDate gracePeriodStart;    // When grace period started
    private LocalDate gracePeriodEnd;      // When grace period ends
    private String gracePeriodReason;      // Reason for grace period
    private Long grantedByAdminId;         // Which admin granted it
}
```

**Implementation**:
- Grace periods are manually granted by SUPERADMIN users
- Can be extended, reduced, or revoked at any time
- Tracked in billing dashboard for monitoring
- Churches are notified before grace period expires

---

## Storage Usage Guidelines

### Typical Church Data Sizes

**Small Church (50-100 members)**:
```
Member profiles:        100 members × 200 KB = 20 MB
Event images:          20 events × 500 KB = 10 MB
Documents/exports:     50 MB
Church assets:         20 MB
Total:                 ~100 MB (well under 2 GB limit)
```

**Medium Church (200-500 members)**:
```
Member profiles:        500 members × 200 KB = 100 MB
Event images:          100 events × 500 KB = 50 MB
Documents/exports:     150 MB
Church assets:         50 MB
Total:                 ~350 MB (still under 2 GB limit)
```

**Large Church (1000+ members, active media)**:
```
Member profiles:        1000 members × 200 KB = 200 MB
Event images:          500 events × 500 KB = 250 MB
Documents/exports:     300 MB
Church assets:         100 MB
Backups:              150 MB
Total:                 ~1 GB (may need +3 GB add-on)
```

**Mega Church (2000+ members, extensive media)**:
```
Member profiles:        2000 members × 200 KB = 400 MB
Event images:          1000 events × 500 KB = 500 MB
Documents/exports:     500 MB
Church assets:         200 MB
Backups:              300 MB
Total:                 ~2 GB (may need +8 GB or +18 GB add-on)
```

**Storage Best Practices**:
- Videos should be hosted externally (YouTube, Vimeo)
- Regular cleanup of old/unused files
- Image optimization before upload
- Backup exports stored externally

---

## Database Schema

```sql
-- Updated church_subscription table
CREATE TABLE church_subscription (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    church_id BIGINT NOT NULL UNIQUE,
    plan_id BIGINT NOT NULL,

    -- Status
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    -- ACTIVE, PAST_DUE, CANCELLED, SUSPENDED, GRACE_PERIOD

    -- Billing
    billing_period_months INT NOT NULL DEFAULT 1,  -- 1=monthly, 12=yearly
    current_period_start DATE NOT NULL,
    current_period_end DATE NOT NULL,
    next_billing_date DATE,

    -- NO TRIAL FIELDS (removed)

    -- Grace Period (Admin-Controlled)
    grace_period_days INT DEFAULT 0,              -- NEW: Admin-granted grace days
    grace_period_start DATE,                      -- NEW: Grace period start
    grace_period_end DATE,                        -- NEW: Grace period end
    grace_period_reason VARCHAR(200),             -- NEW: Reason for grace period
    granted_by_admin_id BIGINT,                   -- NEW: Which admin granted it

    -- Storage
    storage_limit_mb INT NOT NULL DEFAULT 2048,   -- 2 GB in MB
    current_storage_mb DECIMAL(10, 2) DEFAULT 0.00,

    -- Payment Tracking
    last_payment_date DATE,
    last_payment_amount DECIMAL(10, 2),
    failed_payment_attempts INT DEFAULT 0,
    last_payment_error TEXT,

    -- Paystack Integration
    paystack_subscription_code VARCHAR(100),
    paystack_customer_code VARCHAR(100),
    payment_reference VARCHAR(100),
    payment_method VARCHAR(50),

    -- Promotional Credits
    promotional_credits_months INT DEFAULT 0,     -- Free months granted
    promotional_credits_remaining INT DEFAULT 0,  -- Free months left
    promotional_credits_reason VARCHAR(200),

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
    INDEX idx_subscription_grace_period (grace_period_end)
);

-- Subscription plan (simplified)
CREATE TABLE subscription_plan (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL DEFAULT 'STANDARD',
    display_name VARCHAR(100) NOT NULL DEFAULT 'PastCare Standard',

    -- Pricing (US Dollar)
    price DECIMAL(10, 2) NOT NULL DEFAULT 9.99,
    currency VARCHAR(3) DEFAULT 'USD',
    billing_interval VARCHAR(20) DEFAULT 'MONTHLY',

    -- Storage
    storage_limit_mb INT NOT NULL DEFAULT 2048,  -- 2 GB base

    -- Features (all included)
    user_limit INT,  -- NULL = unlimited
    features JSON,   -- Feature flags if needed

    -- Settings
    is_active BOOLEAN DEFAULT TRUE,
    is_free BOOLEAN DEFAULT FALSE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

---

## Pricing Calculation Examples

### Example 1: Small Church (Base Plan)
```
Monthly Subscription:
- Base price:        $9.99
- Storage used:      0.8 GB (within 2 GB limit)
- Storage add-on:    $0.00
Total:              $9.99/month
```

### Example 2: Medium Church (5 GB Storage)
```
Monthly Subscription:
- Base price:        $9.99
- Storage used:      4.5 GB
- Storage add-on:    +8 GB tier = $3.00
Total:              $12.99/month
```

### Example 3: Large Church (20 GB Storage)
```
Monthly Subscription:
- Base price:        $9.99
- Storage used:      18 GB
- Storage add-on:    +18 GB tier = $6.00
Total:              $15.99/month
```

### Example 4: Partner Church (Grace Period)
```
Grace Period (Admin-Granted):
- Grace period:      90 days
- Reason:           "Partner church"
- Granted by:       Platform Admin
- After grace:      $9.99/month (normal billing)
```

### Example 5: Church with Promotional Credits
```
Promotional Credits:
- Credits granted:   3 free months
- Months used:       1
- Months remaining:  2
- After credits:     $9.99/month (normal billing)
```

---

## Subscription Status Flow

```
┌─────────────────────────────────────────────────────────────┐
│                   SUBSCRIPTION STATUS FLOW                   │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  1. Church Signs Up                                         │
│     ↓                                                       │
│  2. Admin May Grant Grace Period (Optional)                │
│     ↓                                                       │
│  3. Status: GRACE_PERIOD (if granted) or ACTIVE (if paid)  │
│     ↓                                                       │
│  4. Grace Period Expires OR Payment Made                    │
│     ↓                                                       │
│  5. Status: ACTIVE (payment successful)                     │
│     ↓                                                       │
│  6. Monthly Billing Cycle                                   │
│     ↓                                                       │
│  7. Payment Failed?                                         │
│     ├─ Yes → Status: PAST_DUE (7 days grace)              │
│     │         ↓                                            │
│     │      Still Failed? → Status: SUSPENDED               │
│     │                                                       │
│     └─ No → Status: ACTIVE (continue)                      │
│                                                             │
│  8. Church Cancels                                          │
│     ↓                                                       │
│  9. Status: CANCELLED (end of current period)               │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## Revenue Projections

### Scenario: 100 Active Churches

**Base Plan Distribution** (2 GB):
- 70 churches: $9.99 × 70 = $699.30/month

**With Storage Add-ons**:
- 20 churches: $12.99 × 20 = $259.80/month (+8 GB)
- 8 churches: $15.99 × 8 = $127.92/month (+18 GB)
- 2 churches: $21.99 × 2 = $43.98/month (+48 GB)

**Total Monthly Revenue**: $1,131.00

**Annual Revenue**: $13,572.00

**Infrastructure Costs** (estimated):
- Storage: $50/month
- Database: $20/month
- Compute: $30/month
- Total: ~$100/month

**Profit Margin**: ~91% ($1,031 profit on $1,131 revenue)

---

## Admin Grace Period Management

### SUPERADMIN Dashboard Features

**Grant Grace Period**:
1. Select church from platform admin dashboard
2. Click "Grant Grace Period"
3. Enter:
   - Number of days (e.g., 30, 60, 90)
   - Reason (e.g., "Partner church", "Financial hardship")
4. Confirm and save
5. Church is notified via email

**Extend Grace Period**:
1. View church subscription details
2. Click "Extend Grace Period"
3. Add additional days
4. Update reason if needed

**Revoke Grace Period**:
1. View church subscription details
2. Click "End Grace Period Now"
3. Confirm action
4. Church status changes to PAST_DUE if no payment

**Monitor Grace Periods**:
- Dashboard shows all churches with active grace periods
- Expiration dates highlighted
- Automated reminders 7 days before expiration

---

## Payment Failure Handling

### Grace Period After Payment Failure

**Automatic Grace Period** (7 days):
- Payment fails → Status: PAST_DUE
- System grants automatic 7-day grace period
- Church can still access system during this time
- Daily email reminders sent

**After 7 Days**:
- Status: SUSPENDED
- Access to system blocked
- Data preserved for 30 days
- Can reactivate by paying outstanding balance

**Admin Can Override**:
- Extend grace period beyond 7 days
- Waive late fees
- Custom payment arrangements

---

## Implementation Checklist

### Database Updates
- [x] Remove trial-related fields from schema
- [x] Add grace period fields (days, start, end, reason, admin_id)
- [x] Update subscription status enum to include GRACE_PERIOD
- [x] Add promotional credits fields (already exists)

### Backend Services
- [ ] Remove trial logic from BillingService
- [ ] Add grace period management methods
- [ ] Add admin endpoints for granting/extending/revoking grace periods
- [ ] Update subscription status logic to handle grace periods
- [ ] Add grace period expiration job

### Frontend Components
- [ ] Remove trial-related UI elements
- [ ] Add grace period management UI (SUPERADMIN only)
- [ ] Add grace period display in church details
- [ ] Add grace period expiration warnings
- [ ] Update billing dashboard to show grace period stats

### Admin Features
- [ ] Grace period grant dialog
- [ ] Grace period extension dialog
- [ ] Grace period revocation confirmation
- [ ] Grace period list/filter in billing dashboard
- [ ] Grace period expiration alerts

### Notifications
- [ ] Email when grace period granted
- [ ] Email 7 days before grace period expires
- [ ] Email 3 days before grace period expires
- [ ] Email when grace period expires
- [ ] Email when subscription suspended

---

## Key Differences from Previous Model

| Feature | Previous Model | New Model |
|---------|---------------|-----------|
| Trial Period | 14-30 days automatic | **NONE** - No automatic trial |
| Grace Period | Not defined | **Admin-controlled** on case-by-case basis |
| Starting Price | USD $9.99 | **USD $9.99** |
| Currency | USD | **US Dollar (USD)** |
| Base Storage | 10 GB | **2 GB** |
| Storage Add-ons | Yes | **Yes** (same tiers) |
| Payment Required | After trial | **From day 1** |
| Special Cases | Trial extension | **Grace period grant** |

---

## Summary

**Final Pricing Model**:
- ✅ **Base Price**: $9.99/month (USD)
- ✅ **No Trial**: Churches must subscribe immediately
- ✅ **Grace Periods**: Admin-controlled for special cases
- ✅ **Storage**: 2 GB base + paid add-ons
- ✅ **Features**: All included (unlimited members, users, etc.)
- ✅ **Currency**: US Dollar (USD)

**Grace Period Philosophy**:
- Not automatic - granted by admin only
- Used for partnerships, hardships, promotions
- Fully tracked and monitored
- Can be extended or revoked at any time

**Next Steps**:
1. Update database schema to remove trial fields
2. Add grace period fields and logic
3. Build admin grace period management UI
4. Remove trial-related code from frontend
5. Update pricing display to $9.99 USD

---

**Document Status**: Finalized and Ready for Implementation
**Date**: 2025-12-30
**Replaces**: All previous pricing documents
