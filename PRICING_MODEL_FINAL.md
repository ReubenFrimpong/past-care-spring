# PastCare SaaS - Final Pricing Model (No Free Trial)

**Date**: 2025-12-28
**Status**: FINAL
**Policy**: NO FREE TRIAL - Payment required upfront

---

## Final Pricing Structure

```
┌──────────────────────────────────────────────────────────────┐
│              PASTCARE SAAS - FINAL PRICING                   │
├──────────────────────────────────────────────────────────────┤
│ Monthly Plan: USD 9.99/month                                │
│ Includes: 2 GB storage (sufficient for most churches)        │
│ Payment: REQUIRED UPFRONT - No free trial                   │
│                                                              │
│ Yearly Plan: USD 99/year (save $20 = 2 months free)         │
│ Payment: REQUIRED UPFRONT - No free trial                   │
│                                                              │
│ Storage Add-ons (charged monthly):                           │
│   • +3 GB:  $1.50/month (5 GB total  = $11.49/mo)           │
│   • +8 GB:  $3.00/month (10 GB total = $12.99/mo)           │
│   • +18 GB: $6.00/month (20 GB total = $15.99/mo)           │
│   • +48 GB: $12.00/month (50 GB total = $21.99/mo)          │
│                                                              │
│ Features: All features included (unlimited)                  │
│ - Unlimited members, fellowships, events                     │
│ - Unlimited users                                            │
│ - Full feature access                                        │
│ - Email & priority support                                   │
│                                                              │
│ Trial: NONE - Payment required on signup                    │
│ Custom Incentives: Admin can grant discounts/free months    │
└──────────────────────────────────────────────────────────────┘
```

---

## No Free Trial Policy

### Why No Free Trial?

1. **Quality Over Quantity**
   - Attracts serious customers who are committed
   - Reduces fake signups and tire-kickers
   - Lower support burden from trial users who never convert

2. **Better Economics**
   - No infrastructure costs for non-paying users
   - Immediate revenue from every signup
   - Better cash flow for business sustainability

3. **Reduced Fraud**
   - Prevents repeated trial abuse
   - Requires payment method verification
   - Reduces spam and fake accounts

4. **Industry Precedent**
   - Many successful SaaS products have no trial (e.g., Netflix requires payment)
   - Churches are serious organizations willing to pay for quality tools
   - $9.99/month is affordable - low barrier to entry

### Payment Flow

```
User Signup
    ↓
Enter Church Details
    ↓
Choose Plan (Monthly/Yearly)
    ↓
PAYMENT REQUIRED ←── No trial option
    ↓
Payment Successful
    ↓
Account Activated Immediately
    ↓
Full Access to All Features
```

---

## Custom Incentive System (Admin-Controlled)

While there's **NO standard free trial**, admins can grant custom incentives to specific churches:

### Types of Incentives Available

#### 1. Free Months
```sql
incentive_type = 'FREE_MONTHS'
value_amount = 1-12  -- Number of free months
```

**Use Cases**:
- Partner churches: 1-3 free months
- Referral rewards: 1 free month per referral
- Non-profit organizations: Custom duration
- Early adopters: 2-3 free months
- Conference attendees: 1 free month

**How it Works**:
- Admin grants X free months to church
- Church pays $0 for first X months
- After free period, normal billing starts
- No credit card required during free period

#### 2. Percentage Discount
```sql
incentive_type = 'DISCOUNT'
value_amount = 10-50  -- Percentage off
discount_expires_at = '2025-12-31'  -- Optional expiry
```

**Use Cases**:
- Long-term partners: 20% off permanently
- Annual subscribers: 10% off first year
- Bulk church networks: 15% off
- Special promotions: 25% off for 6 months
- Loyalty rewards: 10% off after 1 year

**How it Works**:
- Admin grants X% discount to church
- Discount applies to all invoices
- Can be permanent or time-limited
- Discount shown on checkout and invoices

#### 3. One-Time Credit
```sql
incentive_type = 'CREDIT'
value_amount = 9.99-99.00  -- Dollar amount
```

**Use Cases**:
- Apology for downtime: $10 credit
- Referral bonus: $9.99 credit
- Promotional gift: $20 credit
- Contest winners: $50 credit

**How it Works**:
- Admin grants $X credit to church account
- Credit applied to next invoice(s)
- Automatically deducted from charges
- Shows as "Account Credit" on billing page

#### 4. Storage Upgrade
```sql
incentive_type = 'STORAGE_UPGRADE'
value_amount = 3-48  -- Additional GB
```

**Use Cases**:
- Media-heavy churches: Free storage upgrade
- Special events: Temporary storage boost
- Loyalty reward: Permanent storage increase

**How it Works**:
- Admin grants additional storage at no cost
- Church gets upgraded tier without extra charge
- Can be permanent or time-limited

---

## Database Schema (Updated)

```sql
-- Church subscriptions
CREATE TABLE church_subscription (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    church_id BIGINT NOT NULL UNIQUE,
    plan_id BIGINT NOT NULL,

    -- Status
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',  -- CHANGED: No TRIAL status
    -- ACTIVE, PAST_DUE, CANCELLED, SUSPENDED, GRACE_PERIOD

    -- Billing
    billing_cycle VARCHAR(20) DEFAULT 'MONTHLY',  -- MONTHLY, YEARLY
    current_period_start DATE NOT NULL,
    current_period_end DATE NOT NULL,
    next_billing_date DATE,

    -- NO TRIAL FIELDS - REMOVED
    -- trial_start, trial_end, is_trial removed

    -- Storage
    storage_limit_gb INT NOT NULL DEFAULT 2,
    current_storage_gb DECIMAL(10, 2) DEFAULT 0.00,

    -- Pricing
    base_price_usd DECIMAL(10, 2) NOT NULL DEFAULT 9.99,
    storage_addon_price_usd DECIMAL(10, 2) DEFAULT 0.00,

    -- Payment
    last_payment_date DATE,
    last_payment_amount DECIMAL(10, 2),
    payment_method_id VARCHAR(255),  -- Paystack customer ID

    -- Account Credit (for incentives)
    account_credit_usd DECIMAL(10, 2) DEFAULT 0.00,

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
    INDEX idx_subscription_billing_date (next_billing_date)
);

-- Active incentives
CREATE TABLE church_incentive (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    church_subscription_id BIGINT NOT NULL,

    -- Incentive details
    incentive_type VARCHAR(30) NOT NULL,
    -- FREE_MONTHS, DISCOUNT, CREDIT, STORAGE_UPGRADE

    value_amount DECIMAL(10, 2) NOT NULL,
    -- FREE_MONTHS: number of months
    -- DISCOUNT: percentage (10.00 = 10%)
    -- CREDIT: dollar amount
    -- STORAGE_UPGRADE: GB amount

    -- Tracking
    incentive_code VARCHAR(50),      -- e.g., "PARTNER2025", "REF_CHURCH_123"
    reason VARCHAR(200),             -- "Partner church referral"

    -- Granted by
    granted_by_user_id BIGINT,       -- SUPERADMIN who granted it
    granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Expiration (null = permanent)
    starts_at DATE,                  -- When incentive becomes active
    expires_at DATE,                 -- When it ends (null = never)

    -- Status
    is_active BOOLEAN DEFAULT TRUE,
    redeemed_value DECIMAL(10, 2) DEFAULT 0.00,  -- How much has been used

    notes TEXT,

    CONSTRAINT fk_incentive_subscription
        FOREIGN KEY (church_subscription_id)
        REFERENCES church_subscription(id) ON DELETE CASCADE,
    CONSTRAINT fk_incentive_granted_by
        FOREIGN KEY (granted_by_user_id)
        REFERENCES user(id) ON DELETE SET NULL,

    INDEX idx_incentive_church (church_subscription_id),
    INDEX idx_incentive_code (incentive_code),
    INDEX idx_incentive_active (is_active, expires_at)
);

-- Subscription plan
CREATE TABLE subscription_plan (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL DEFAULT 'STANDARD',
    display_name VARCHAR(100) NOT NULL DEFAULT 'PastCare Standard',

    -- Pricing
    base_price_monthly_usd DECIMAL(10, 2) NOT NULL DEFAULT 9.99,
    base_price_yearly_usd DECIMAL(10, 2) NOT NULL DEFAULT 99.00,
    base_storage_gb INT NOT NULL DEFAULT 2,

    -- Storage pricing tiers (JSON)
    storage_tiers JSON,
    -- [{"gb": 3, "price": 1.50}, {"gb": 8, "price": 3.00}, ...]

    currency VARCHAR(3) DEFAULT 'USD',
    is_active BOOLEAN DEFAULT TRUE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

---

## Signup & Payment Flow

### New Church Registration

```
Step 1: Church Information
━━━━━━━━━━━━━━━━━━━━━━━━━━
- Church name
- Email address
- Phone number
- Location
- Admin details

Step 2: Choose Plan
━━━━━━━━━━━━━━━━━━━━━━━━━━
○ Monthly: $9.99/month
● Yearly: $99/year (Save $20!)

Step 3: Payment Required ⚠️
━━━━━━━━━━━━━━━━━━━━━━━━━━
No free trial available.
Payment is required to activate your account.

[Enter Payment Details]
- Card number
- Expiry date
- CVV
- Billing address

[Pay $9.99] or [Pay $99.00]

Step 4: Account Activated ✓
━━━━━━━━━━━━━━━━━━━━━━━━━━
Your account is now active!
Access all features immediately.
```

### With Admin-Granted Incentive

```
Step 1-2: Same as above

Step 3: Incentive Code (Optional)
━━━━━━━━━━━━━━━━━━━━━━━━━━
Have an incentive code?
[Enter Code: PARTNER2025]
[Apply Code]

✓ Code Applied!
- 3 free months granted
- No payment required now
- Billing starts: March 28, 2026

[Activate Account] (no payment)

OR

- 20% discount applied
- Pay only $7.99/month
[Pay $7.99]
```

---

## Admin Incentive Management

### Admin Interface (Superadmin Only)

```typescript
// Grant incentive to church
interface GrantIncentiveRequest {
  churchId: number;
  type: 'FREE_MONTHS' | 'DISCOUNT' | 'CREDIT' | 'STORAGE_UPGRADE';
  value: number;
  code?: string;       // Optional tracking code
  reason: string;      // Why granted
  startsAt?: Date;     // When it starts (default: now)
  expiresAt?: Date;    // When it ends (null = permanent)
}

// Example: Grant 3 free months
{
  churchId: 123,
  type: 'FREE_MONTHS',
  value: 3,
  code: 'PARTNER2025',
  reason: 'Partner church referral program',
  startsAt: '2025-12-28',
  expiresAt: null  // Permanent (until 3 months consumed)
}

// Example: Grant 20% discount for 1 year
{
  churchId: 456,
  type: 'DISCOUNT',
  value: 20,
  code: 'EARLY_ADOPTER',
  reason: 'Early adopter discount',
  startsAt: '2025-12-28',
  expiresAt: '2026-12-28'
}
```

### Incentive Application Logic

```java
@Service
public class IncentiveService {

    public BigDecimal calculateFinalPrice(ChurchSubscription subscription) {
        BigDecimal basePrice = subscription.getBasePriceUsd();
        BigDecimal storageAddon = subscription.getStorageAddonPriceUsd();
        BigDecimal total = basePrice.add(storageAddon);

        // Apply active discounts
        List<ChurchIncentive> discounts = incentiveRepository
            .findActiveDiscounts(subscription.getId());

        for (ChurchIncentive incentive : discounts) {
            if (incentive.getIncentiveType() == IncentiveType.DISCOUNT) {
                BigDecimal discountPercent = incentive.getValueAmount();
                BigDecimal discountAmount = total
                    .multiply(discountPercent)
                    .divide(BigDecimal.valueOf(100));
                total = total.subtract(discountAmount);
            }
        }

        // Apply account credit
        BigDecimal credit = subscription.getAccountCreditUsd();
        if (credit.compareTo(BigDecimal.ZERO) > 0) {
            if (credit.compareTo(total) >= 0) {
                // Credit covers full amount
                subscription.setAccountCreditUsd(credit.subtract(total));
                return BigDecimal.ZERO;
            } else {
                // Partial credit
                total = total.subtract(credit);
                subscription.setAccountCreditUsd(BigDecimal.ZERO);
            }
        }

        // Check for FREE_MONTHS incentive
        List<ChurchIncentive> freeMonths = incentiveRepository
            .findActiveFreeMonths(subscription.getId());

        if (!freeMonths.isEmpty()) {
            // Has unused free months - charge $0
            ChurchIncentive freeMonth = freeMonths.get(0);
            freeMonth.setRedeemedValue(
                freeMonth.getRedeemedValue().add(BigDecimal.ONE)
            );

            if (freeMonth.getRedeemedValue().compareTo(
                freeMonth.getValueAmount()) >= 0) {
                // All free months consumed
                freeMonth.setIsActive(false);
            }
            incentiveRepository.save(freeMonth);
            return BigDecimal.ZERO;
        }

        return total;
    }
}
```

---

## Billing Examples

### Example 1: Normal Church (No Incentives)
```
Subscription: Monthly $9.99
Storage: 1.5 GB (within 2 GB limit)
Incentives: None
Account Credit: $0

Invoice:
- Base plan:        $9.99
- Storage add-on:   $0.00
- Subtotal:         $9.99
- Discount:         $0.00
- Credit:           $0.00
━━━━━━━━━━━━━━━━━━━━━━━━
TOTAL DUE:          $9.99
```

### Example 2: Partner Church (3 Free Months)
```
Subscription: Monthly $9.99
Incentive: 3 free months (PARTNER2025)
Month: 1 of 3

Invoice:
- Base plan:        $9.99
- Free month (1/3): -$9.99
━━━━━━━━━━━━━━━━━━━━━━━━
TOTAL DUE:          $0.00

Next billing: March 28, 2026
```

### Example 3: Discounted Church (20% Off)
```
Subscription: Monthly $9.99
Storage: 8 GB (using +8 GB tier)
Incentive: 20% discount (permanent)

Invoice:
- Base plan:        $9.99
- Storage add-on:   $3.00
- Subtotal:         $12.99
- Discount (20%):   -$2.60
━━━━━━━━━━━━━━━━━━━━━━━━
TOTAL DUE:          $10.39
```

### Example 4: Church with Account Credit
```
Subscription: Monthly $9.99
Account Credit: $25.00 (from referral bonus)

Invoice:
- Base plan:        $9.99
- Account credit:   -$9.99
━━━━━━━━━━━━━━━━━━━━━━━━
TOTAL DUE:          $0.00

Remaining credit: $15.01
```

---

## Revenue Projections (Without Free Trial)

### Scenario: 100 Churches, First Month

**With Free Trial Model** (for comparison):
- 200 signups
- 100 convert after trial (50% conversion)
- Infrastructure cost for 200 churches: $100
- Revenue: $999 (100 paying)
- Profit: $899

**No Free Trial Model** (proposed):
- 100 signups (lower, but all paying)
- 100 active from day 1 (100% conversion)
- Infrastructure cost: $50 (only 100 churches)
- Revenue: $999 (100 paying)
- Profit: $949 (+$50 more profit!)

**Benefits**:
- Same revenue with half the signups
- Lower infrastructure costs
- No wasted support on trial users
- Better cash flow (immediate payment)
- Higher quality customers

---

## Summary

### Final Pricing Policy

✅ **NO FREE TRIAL** - Payment required upfront
✅ **$9.99/month** or **$99/year**
✅ **2 GB base storage** (sufficient for 90% of churches)
✅ **Storage add-ons** available ($1.50-$12.00/month)
✅ **Admin-controlled incentives** for special cases
✅ **Immediate activation** upon payment

### Incentive Options for Admins

1. **Free Months** - Grant 1-12 free months to partner churches
2. **Percentage Discounts** - 10-50% off, permanent or time-limited
3. **Account Credits** - One-time dollar credits for promotions
4. **Storage Upgrades** - Free storage boosts for special cases

### Next Steps

1. ✅ Database schema designed (no trial fields)
2. ⏳ Create subscription management service
3. ⏳ Integrate Paystack payment gateway
4. ⏳ Build incentive management UI (superadmin)
5. ⏳ Create public pricing page (no trial mentioned)
6. ⏳ Build invoice generation system

---

**Document Status**: FINAL
**Policy**: NO FREE TRIAL
**Created**: 2025-12-28
**Approved**: Pending user confirmation
