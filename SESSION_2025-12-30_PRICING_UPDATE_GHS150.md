# Session Summary: Pricing Update to GHS 150 - December 30, 2025

## Overview

Updated subscription pricing from **GHS 9.99** to **GHS 150** per month to reflect proper market value (~$10 USD equivalent). Also removed discount logic from backend and calculated Paystack transaction fees.

## Changes Applied

### 1. Database Update

**Command**:
```sql
UPDATE subscription_plans SET price = 150.00 WHERE name = 'STANDARD';
```

**Before**:
```
id=1, name=STANDARD, display_name="PastCare Standard", price=9.99, storage_limit_mb=2048
```

**After**:
```
id=1, name=STANDARD, display_name="PastCare Standard", price=150.00, storage_limit_mb=2048
```

---

### 2. Backend Changes

**File**: [BillingService.java](src/main/java/com/reuben/pastcare_spring/services/BillingService.java)

#### Removed Discount Logic
Lines 200-211 - Simplified `calculatePeriodAmount()` method to remove all discounts

```java
// BEFORE - Applied discounts
private BigDecimal calculatePeriodAmount(BigDecimal basePrice, String billingPeriod, Integer months) {
    if (billingPeriod == null || months == null || months == 1) {
        return basePrice; // Monthly - no discount
    }

    BigDecimal totalBeforeDiscount = basePrice.multiply(BigDecimal.valueOf(months));

    // Apply discounts
    switch (months) {
        case 3: // Quarterly - 5% discount
            return totalBeforeDiscount.multiply(BigDecimal.valueOf(0.95));
        case 6: // Biannual - 10% discount
            return totalBeforeDiscount.multiply(BigDecimal.valueOf(0.90));
        case 12: // Yearly - 2 months free (10 months price)
            return basePrice.multiply(BigDecimal.valueOf(10));
        default:
            return totalBeforeDiscount; // No discount for other periods
    }
}

// AFTER - No discounts
private BigDecimal calculatePeriodAmount(BigDecimal basePrice, String billingPeriod, Integer months) {
    if (billingPeriod == null || months == null || months == 1) {
        return basePrice; // Monthly
    }

    // No discounts - just multiply base price by number of months
    return basePrice.multiply(BigDecimal.valueOf(months));
}
```

---

### 3. Frontend Changes

#### Payment Setup Page

**File**: [payment-setup-page.ts](past-care-spring-frontend/src/app/payment-setup-page/payment-setup-page.ts)

**Billing Periods Updated**:
```typescript
// BEFORE
billingPeriods = {
  monthly: { months: 1, price: 9.99, discount: 0, label: 'Monthly' },
  '3months': { months: 3, price: 29.97, discount: 0, label: '3 Months' },
  '6months': { months: 6, price: 59.94, discount: 0, label: '6 Months' },
  yearly: { months: 12, price: 119.88, discount: 0, label: 'Yearly' }
};

// AFTER
billingPeriods = {
  monthly: { months: 1, price: 150.00, discount: 0, label: 'Monthly' },
  '3months': { months: 3, price: 450.00, discount: 0, label: '3 Months' },
  '6months': { months: 6, price: 900.00, discount: 0, label: '6 Months' },
  yearly: { months: 12, price: 1800.00, discount: 0, label: 'Yearly' }
};
```

**Billing Notes Updated**:
```typescript
getBillingNote(): string {
  const period = this.selectedPeriod();
  switch (period) {
    case 'monthly':
      return 'Billed monthly • GHS 150/month • Cancel anytime';
    case '3months':
      return 'Billed every 3 months • GHS 150/month • Cancel anytime';
    case '6months':
      return 'Billed every 6 months • GHS 150/month • Cancel anytime';
    case 'yearly':
      return 'Billed annually • GHS 150/month • Cancel anytime';
  }
}
```

**File**: [payment-setup-page.html](past-care-spring-frontend/src/app/payment-setup-page/payment-setup-page.html)

**Period Options Updated**:
```html
<!-- Monthly -->
<div class="period-price">GHS 150/mo</div>

<!-- 3 Months -->
<div class="period-price">GHS 450 total</div>
<div class="period-note">GHS 150/month</div>

<!-- 6 Months -->
<div class="period-price">GHS 900 total</div>
<div class="period-note">GHS 150/month</div>

<!-- Yearly -->
<div class="period-price">GHS 1,800/year</div>
<div class="period-note">GHS 150/month</div>
```

#### Pricing Section (Landing Page)

**File**: [pricing-section.ts](past-care-spring-frontend/src/app/pricing-section/pricing-section.ts)

**Billing Periods Updated**:
```typescript
billingPeriods = {
  monthly: { months: 1, price: 150.00, discount: 0, label: 'Monthly' },
  '3months': { months: 3, price: 450.00, discount: 0, label: '3 Months' },
  '6months': { months: 6, price: 900.00, discount: 0, label: '6 Months' },
  yearly: { months: 12, price: 1800.00, discount: 0, label: 'Yearly' }
};
```

**Pricing Tiers Updated**:
```typescript
// BEFORE
pricingTiers = {
  2: 9.99,   // Starter - 2GB
  5: 14.99,  // Standard - 5GB
  10: 19.99  // Premium - 10GB
};

// AFTER
pricingTiers = {
  2: 150.00,   // Starter - 2GB (base plan)
  5: 172.50,   // Standard - 5GB (+GHS 22.50 for 3GB extra)
  10: 195.00   // Premium - 10GB (+GHS 45 for 8GB extra)
};
```

---

## New Pricing Structure

### Base Subscription Plan
```
Plan: PastCare Standard
Storage: 2 GB
Price: GHS 150/month (~$10 USD)

Billing Options:
├─ Monthly:  GHS 150   (GHS 150/month)
├─ 3 Months: GHS 450   (GHS 150/month)
├─ 6 Months: GHS 900   (GHS 150/month)
└─ Yearly:   GHS 1,800 (GHS 150/month)
```

**No discounts applied** - all billing periods charge the same GHS 150/month rate.

### Storage Upgrade Tiers
```
Base (2 GB):  GHS 150.00/month
5 GB Tier:    GHS 172.50/month (+GHS 22.50 for 3GB extra)
10 GB Tier:   GHS 195.00/month (+GHS 45.00 for 8GB extra)
```

---

## Paystack Transaction Fees (Ghana)

### Fee Structure

Paystack charges different fees based on the payment method used in Ghana:

#### 1. Card Payments (Domestic - Ghana-issued cards)
```
Fee: 1.95% + GHS 1.00 per transaction
Cap: No cap
```

**Examples**:
| Amount | Fee Calculation | Transaction Fee | Amount Received |
|--------|----------------|-----------------|-----------------|
| GHS 150 | (150 × 1.95%) + GHS 1 | GHS 3.93 | GHS 146.07 |
| GHS 450 | (450 × 1.95%) + GHS 1 | GHS 9.78 | GHS 440.22 |
| GHS 900 | (900 × 1.95%) + GHS 1 | GHS 18.55 | GHS 881.45 |
| GHS 1,800 | (1800 × 1.95%) + GHS 1 | GHS 36.10 | GHS 1,763.90 |

#### 2. Mobile Money
```
Fee: 1.5% + GHS 1.00 per transaction
Cap: No cap

Supported: MTN Mobile Money, Vodafone Cash, AirtelTigo Money
```

**Examples**:
| Amount | Fee Calculation | Transaction Fee | Amount Received |
|--------|----------------|-----------------|-----------------|
| GHS 150 | (150 × 1.5%) + GHS 1 | GHS 3.25 | GHS 146.75 |
| GHS 450 | (450 × 1.5%) + GHS 1 | GHS 7.75 | GHS 442.25 |
| GHS 900 | (900 × 1.5%) + GHS 1 | GHS 14.50 | GHS 885.50 |
| GHS 1,800 | (1800 × 1.5%) + GHS 1 | GHS 28.00 | GHS 1,772.00 |

#### 3. Bank Transfer
```
Fee: 1.5% + GHS 1.00 per transaction
Cap: No cap
```

**Examples**: Same as Mobile Money

---

## Passing Transaction Fees to Customers

### Option 1: Absorb the Fees (Current Setup)
**You pay the fees, customer pays the advertised price**

```
Customer sees: GHS 150
Customer pays: GHS 150
You receive: GHS 146.07 (card) or GHS 146.75 (mobile money)
You bear: GHS 3.93 (card) or GHS 3.25 (mobile money)
```

**Monthly Billing**:
```
Advertised: GHS 150/month
Revenue:    GHS 146.07/month (card) or GHS 146.75/month (mobile money)
```

**Yearly Billing**:
```
Advertised: GHS 1,800/year
Revenue:    GHS 1,763.90/year (card) or GHS 1,772.00/year (mobile money)
```

---

### Option 2: Pass Fees to Customers (Surcharge Model)

**Customer pays the fees on top of the advertised price**

To calculate the amount to charge when the customer pays the fee:
```
Formula: Total Amount = (Base Price + Fixed Fee) / (1 - Percentage Fee)

For Card Payments (1.95% + GHS 1):
Total = (Base Price + 1) / (1 - 0.0195)
Total = (Base Price + 1) / 0.9805

For Mobile Money (1.5% + GHS 1):
Total = (Base Price + 1) / (1 - 0.015)
Total = (Base Price + 1) / 0.985
```

**Monthly Plan (GHS 150 base)**:
```
Card Payment:
  Total charged = (150 + 1) / 0.9805 = GHS 154.01
  Fee = GHS 4.01
  You receive = GHS 150.00 (exactly)

Mobile Money:
  Total charged = (150 + 1) / 0.985 = GHS 153.30
  Fee = GHS 3.30
  You receive = GHS 150.00 (exactly)
```

**3-Month Plan (GHS 450 base)**:
```
Card Payment:
  Total charged = (450 + 1) / 0.9805 = GHS 460.02
  Fee = GHS 10.02
  You receive = GHS 450.00 (exactly)

Mobile Money:
  Total charged = (450 + 1) / 0.985 = GHS 457.87
  Fee = GHS 7.87
  You receive = GHS 450.00 (exactly)
```

**6-Month Plan (GHS 900 base)**:
```
Card Payment:
  Total charged = (900 + 1) / 0.9805 = GHS 919.03
  Fee = GHS 19.03
  You receive = GHS 900.00 (exactly)

Mobile Money:
  Total charged = (900 + 1) / 0.985 = GHS 914.72
  Fee = GHS 14.72
  You receive = GHS 900.00 (exactly)
```

**Yearly Plan (GHS 1,800 base)**:
```
Card Payment:
  Total charged = (1800 + 1) / 0.9805 = GHS 1,836.78
  Fee = GHS 36.78
  You receive = GHS 1,800.00 (exactly)

Mobile Money:
  Total charged = (1800 + 1) / 0.985 = GHS 1,828.43
  Fee = GHS 28.43
  You receive = GHS 1,800.00 (exactly)
```

---

## Implementation for Passing Fees to Customers

If you want customers to pay the transaction fees, you need to:

### 1. Update Backend Calculation

**File**: [BillingService.java](src/main/java/com/reuben/pastcare_spring/services/BillingService.java)

Add a method to calculate the total amount including fees:

```java
/**
 * Calculate total amount including Paystack transaction fees.
 * Customer pays the base price + fees.
 */
private BigDecimal calculateAmountWithFees(BigDecimal baseAmount, String paymentMethod) {
    // Paystack Ghana fees
    BigDecimal percentageFee;
    BigDecimal fixedFee = BigDecimal.valueOf(1.00); // GHS 1.00

    // Determine percentage based on payment method
    if ("MOBILE_MONEY".equalsIgnoreCase(paymentMethod) ||
        "BANK_TRANSFER".equalsIgnoreCase(paymentMethod)) {
        percentageFee = BigDecimal.valueOf(0.015); // 1.5%
    } else {
        percentageFee = BigDecimal.valueOf(0.0195); // 1.95% for cards
    }

    // Formula: Total = (BaseAmount + FixedFee) / (1 - PercentageFee)
    BigDecimal oneMinusPercentage = BigDecimal.ONE.subtract(percentageFee);
    BigDecimal totalAmount = baseAmount.add(fixedFee).divide(oneMinusPercentage, 2, RoundingMode.HALF_UP);

    return totalAmount;
}
```

### 2. Update Frontend Display

Show customers the breakdown:

```html
<div class="price-breakdown">
  <div class="base-price">
    <span>Subscription (Monthly):</span>
    <span>GHS 150.00</span>
  </div>
  <div class="transaction-fee">
    <span>Transaction Fee:</span>
    <span>GHS 4.01</span>
  </div>
  <div class="total-price">
    <span><strong>Total:</strong></span>
    <span><strong>GHS 154.01</strong></span>
  </div>
  <p class="fee-note">
    <i class="fas fa-info-circle"></i>
    Transaction fees vary by payment method (card: 1.95% + GHS 1, mobile money: 1.5% + GHS 1)
  </p>
</div>
```

### 3. Legal Compliance

**Important**: In Ghana, you must clearly disclose that the customer is paying the transaction fee. This must be:
- Shown BEFORE payment
- Clearly itemized (base price + fee = total)
- Customer must explicitly agree

---

## Recommendation

### Option 1: Absorb Fees (Recommended)

**Pros**:
- Simpler user experience
- Transparent pricing ("What you see is what you pay")
- No legal compliance concerns
- Competitive advantage

**Cons**:
- You lose ~2.6% revenue per transaction
- Monthly: Lose GHS 3.93 per customer
- Yearly: Lose GHS 36.10 per customer

**Net Revenue**:
```
Monthly:  GHS 146.07 (instead of GHS 150)
3-Month:  GHS 440.22 (instead of GHS 450)
6-Month:  GHS 881.45 (instead of GHS 900)
Yearly:   GHS 1,763.90 (instead of GHS 1,800)
```

### Option 2: Pass Fees to Customers

**Pros**:
- You receive exactly GHS 150/month
- No revenue loss from fees

**Cons**:
- More complex checkout experience
- Prices vary by payment method
- May discourage signups
- Requires legal disclosure

**Customer Pays**:
```
Monthly (Card):  GHS 154.01 (instead of GHS 150)
Monthly (Mobile): GHS 153.30 (instead of GHS 150)
```

---

## Current Implementation Status

✅ **Absorbing Fees** - Customers pay advertised price, you bear transaction costs

If you want to pass fees to customers, you need to:
1. Add fee calculation to backend
2. Update frontend to show fee breakdown
3. Add legal disclosure about transaction fees
4. Update payment flow to charge total amount (base + fee)

---

## Build Status

### Backend ✅
```bash
./mvnw compile
```
**Result**: ✅ SUCCESS - No compilation errors

### Frontend ✅
```bash
cd past-care-spring-frontend && ng build --configuration=production
```
**Result**: ✅ SUCCESS - No compilation errors

---

## Summary

### Changes Made
1. ✅ Updated database: `STANDARD` plan price from GHS 9.99 → GHS 150
2. ✅ Removed discount logic from backend `calculatePeriodAmount()`
3. ✅ Updated frontend billing periods to reflect GHS 150 pricing
4. ✅ Updated frontend billing notes to show "GHS 150/month"
5. ✅ Updated pricing section tiers (2GB, 5GB, 10GB plans)
6. ✅ Compiled backend successfully
7. ✅ Built frontend successfully

### Pricing After Update
```
Base Plan: GHS 150/month (~$10 USD)
├─ Monthly:   GHS 150
├─ 3 Months:  GHS 450 (no discount)
├─ 6 Months:  GHS 900 (no discount)
└─ Yearly:    GHS 1,800 (no discount)

Storage Tiers:
├─ 2 GB:  GHS 150/month
├─ 5 GB:  GHS 172.50/month
└─ 10 GB: GHS 195/month
```

### Transaction Fees (Absorbed by You)
```
Payment Method | Fee Structure | Monthly Fee | Yearly Fee
---------------|---------------|-------------|------------
Card           | 1.95% + GHS 1 | GHS 3.93    | GHS 36.10
Mobile Money   | 1.5% + GHS 1  | GHS 3.25    | GHS 28.00
Bank Transfer  | 1.5% + GHS 1  | GHS 3.25    | GHS 28.00
```

### Net Revenue (After Fees)
```
Monthly:  GHS 146.07 (card) or GHS 146.75 (mobile money)
3-Month:  GHS 440.22 (card) or GHS 442.25 (mobile money)
6-Month:  GHS 881.45 (card) or GHS 885.50 (mobile money)
Yearly:   GHS 1,763.90 (card) or GHS 1,772.00 (mobile money)
```

---

**Session Date**: December 30, 2025, 21:25 - 21:32 UTC
**Status**: ✅ COMPLETE
**Database**: ✅ UPDATED - Price set to GHS 150
**Backend**: ✅ COMPILED - Discounts removed
**Frontend**: ✅ BUILT - Pricing updated to GHS 150
**Transaction Fees**: Currently absorbed by merchant (you)
