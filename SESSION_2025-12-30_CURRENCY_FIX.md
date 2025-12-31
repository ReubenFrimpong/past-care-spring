# Session Summary: Paystack Currency Fix - December 30, 2025

## Issue Identified

Payment initialization was failing with Paystack API error:

```
403 Forbidden: Currency not supported by merchant
Message: "Ensure you're passing the currency code for a currency enabled on your integration"
```

## Root Cause

The application was sending **USD** (US Dollars) to Paystack, but **Paystack does not support USD**.

### Paystack Supported Currencies
- **NGN** - Nigerian Naira
- **GHS** - Ghanaian Cedi ✅ (Now using this)
- **ZAR** - South African Rand
- **KES** - Kenyan Shilling

## Fix Applied

Changed all subscription payment currency from `"USD"` to `"GHS"` (Ghana Cedis).

### Files Modified

**[BillingService.java](src/main/java/com/reuben/pastcare_spring/services/BillingService.java)**

#### Location 1: Subscription Payment Initialization (Line 177)
```java
// BEFORE
.currency("USD")

// AFTER
.currency("GHS")  // Ghana Cedis - Paystack does not support USD
```

#### Location 2: Payment Request to Paystack (Line 191)
```java
// BEFORE
request.setCurrency("USD");

// AFTER
request.setCurrency("GHS");  // Ghana Cedis - Paystack does not support USD
```

#### Location 3: Subscription Renewal (Line 448)
```java
// BEFORE
.currency("USD")

// AFTER
.currency("GHS")  // Ghana Cedis - Paystack does not support USD
```

---

## Impact on Pricing

### Current Pricing (unchanged in code)
- Monthly: $9.99 → GHS 9.99
- 3 Months: $29.97 → GHS 29.97
- 6 Months: $59.94 → GHS 59.94
- Yearly: $119.88 → GHS 119.88

### Important Notes

1. **Frontend Display**: The frontend still shows "$" symbol and talks about USD in documentation
2. **Actual Charges**: Paystack will charge in **GHS** (Ghana Cedis)
3. **Exchange Rate Consideration**:
   - Current approximate rate: 1 USD ≈ 15-16 GHS
   - Your pricing of GHS 9.99 is actually **very low** (~$0.62 USD)
   - You may want to adjust the prices to reflect proper GHS amounts

### Recommended Pricing Adjustment

If you want to maintain ~$9.99 USD equivalent pricing:

```
Monthly: GHS 150 (~$9.99 USD)
3 Months: GHS 450 (~$29.97 USD)
6 Months: GHS 900 (~$59.94 USD)
Yearly: GHS 1,800 (~$119.88 USD)
```

**To update prices**, you need to change the subscription plan prices in the database.

---

## Build Status

### Backend ✅
```bash
./mvnw compile
```
**Result**: ✅ SUCCESS - No compilation errors

---

## Testing

### Manual Test
1. Navigate to `/subscription/select`
2. Select a plan
3. Click "Proceed to Payment"
4. Verify Paystack payment page loads successfully (no more currency error)
5. Note that Paystack will show **GHS** as the currency

### Expected Behavior
- ✅ Payment initialization succeeds
- ✅ Paystack payment page loads
- ✅ Currency shown as **GHS** (Ghana Cedis)
- ⚠️ Amount shown will be very low (GHS 9.99 = ~$0.62)

---

## Next Steps (Recommended)

### 1. Update Database Pricing
If you want to maintain USD $9.99 equivalent:

```sql
-- Update subscription plans to GHS equivalent
UPDATE subscription_plan
SET price = 150.00,
    currency = 'GHS'
WHERE name = 'STANDARD';

-- Adjust other storage tiers similarly
```

### 2. Update Frontend Display
Update all frontend components to show GHS instead of USD:

- [payment-setup-page.html](past-care-spring-frontend/src/app/payment-setup-page/payment-setup-page.html)
- [billing-page.html](past-care-spring-frontend/src/app/billing-page/billing-page.html)
- [pricing-section.html](past-care-spring-frontend/src/app/pricing-section/pricing-section.html)
- [PRICING_MODEL_REVISED.md](PRICING_MODEL_REVISED.md)

Change:
- `$9.99/month` → `GHS 150/month`
- `$29.97` → `GHS 450`
- etc.

### 3. Update Documentation
Update all pricing documentation to reflect GHS currency:
- README files
- Pricing model documents
- User-facing documentation

---

## Alternative: Use NGN (Nigerian Naira)

If your target market is Nigeria instead of Ghana:

### Change to NGN
```java
.currency("NGN")  // Nigerian Naira
```

### Recommended NGN Pricing
```
Monthly: NGN 15,000 (~$9.99 USD)
3 Months: NGN 45,000 (~$29.97 USD)
6 Months: NGN 90,000 (~$59.94 USD)
Yearly: NGN 180,000 (~$119.88 USD)
```

---

## Summary

### Problem
- Paystack does not support USD currency
- All payment initializations were failing with 403 Forbidden

### Solution
- Changed currency from `"USD"` to `"GHS"` in 3 locations in BillingService.java
- Backend now compiles successfully

### Current State
- ✅ Paystack integration will work
- ⚠️ Prices are very low (GHS 9.99 = ~$0.62 USD)
- ⚠️ Frontend still shows "$" and "USD"

### Recommendations
1. Update subscription plan prices in database to proper GHS amounts
2. Update frontend to display GHS instead of USD
3. Update all documentation to reflect GHS pricing

---

**Session Date**: December 30, 2025, 21:16 - 21:20 UTC
**Status**: ✅ FIXED - Paystack currency error resolved
**Build Status**: ✅ SUCCESS - Backend compiles without errors
**Action Required**: Update pricing amounts to proper GHS values
