# Partnership Code E2E Test Integration - Complete

**Date**: 2025-12-30
**Status**: ✅ Complete

---

## Summary

Successfully updated all E2E tests to use the new partnership code system instead of the deprecated FREE plan approach. This ensures tests match the actual user flow where churches must apply a partnership code to receive a grace period instead of getting automatic free access.

---

## Changes Made

### 1. Test Data Fixtures Updated

**File**: `e2e/fixtures/test-data.ts`

Added helper method to get the standard test partnership code:

```typescript
/**
 * Get default partnership code for testing
 * This code should exist in the database with unlimited uses
 */
static getTestPartnershipCode(): string {
  return 'TRIAL14'; // 14-day trial, unlimited uses, no expiration
}
```

**Why TRIAL14?**: This code was created in the database migration with:
- 14 days grace period
- Unlimited uses (`max_uses = NULL`)
- No expiration date (`expires_at = NULL`)
- Always active (`is_active = TRUE`)

Perfect for automated testing as it won't run out or expire.

---

### 2. Subscription Page Object Enhanced

**File**: `e2e/pages/billing/subscription-selection.page.ts`

Added three new methods:

```typescript
/**
 * Apply partnership code to get grace period
 */
async applyPartnershipCode(code: string) {
  await this.fillField('[data-testid="partnership-code-input"]', code);
  await this.click('[data-testid="apply-code-button"]');
  await this.waitForSelector('[data-testid="code-success"]', { timeout: 10000 });
}

/**
 * Verify partnership code was applied successfully
 */
async verifyPartnershipCodeSuccess() {
  const successMessage = await this.getText('[data-testid="code-success"]');
  return successMessage.toLowerCase().includes('code applied');
}

/**
 * Verify partnership code error message
 */
async verifyPartnershipCodeError(expectedError: string) {
  const errorMessage = await this.getText('[data-testid="code-error"]');
  return errorMessage.toLowerCase().includes(expectedError.toLowerCase());
}
```

**Deprecated Method**: `activateFreePlan()` is now marked as `@deprecated` but kept for backward compatibility until all references are removed.

---

### 3. Base Page Object Enhanced

**File**: `e2e/pages/base.page.ts`

Added two improvements:

1. **fillField() alias**: Added for consistency with page object naming
2. **waitForSelector() options**: Added support for timeout and state options

```typescript
async fillField(selector: string, value: string) {
  await this.fillInput(selector, value);
}

async waitForSelector(selector: string, options?: {
  timeout?: number;
  state?: 'attached' | 'detached' | 'visible' | 'hidden'
}) {
  await this.page.waitForSelector(selector, { state: 'visible', ...options });
}
```

---

### 4. Registration Tests Updated

**File**: `e2e/tests/critical-path-01-church-registration.spec.ts`

#### US-001.1: Complete church registration (UPDATED)

**Before**: Used `activateFreePlan()` and expected FREE badge
**After**: Uses `applyPartnershipCode()` and expects ACTIVE badge

```typescript
test('US-001.1: Complete church registration with partnership code', async ({ page }) => {
  const partnershipCode = TestData.getTestPartnershipCode();

  // Register church
  await registrationPage.navigate();
  await registrationPage.fillChurchInfo(churchData);
  await registrationPage.fillAdminInfo(adminData);
  await registrationPage.submit();

  // Apply partnership code
  await subscriptionPage.applyPartnershipCode(partnershipCode);

  // Verify success and redirect to dashboard
  await expect(await subscriptionPage.verifyPartnershipCodeSuccess()).toBe(true);
  await page.waitForURL(/.*dashboard/, { timeout: 5000 });

  // Verify ACTIVE subscription
  const badge = await page.locator('[data-testid="subscription-badge"]');
  await expect(badge).toContainText(/ACTIVE|Active/);
});
```

#### US-001.6: Onboarding wizard (UPDATED)

Updated to use partnership code instead of FREE plan activation:

```typescript
// Before:
await subscriptionPage.activateFreePlan();

// After:
await subscriptionPage.applyPartnershipCode(partnershipCode);
await page.waitForURL(/.*dashboard/, { timeout: 5000 });
```

---

### 5. Billing Tests Updated

**File**: `e2e/tests/critical-path-08-billing-subscriptions.spec.ts`

#### US-008.1: View subscription plans (UPDATED)

**Before**: Checked for FREE plan display and FREE plan button
**After**: Checks for partnership code section and only paid plans (BASIC, STANDARD, PREMIUM)

```typescript
test('US-008.1: View all subscription plans and partnership code option', async ({ page }) => {
  await subscriptionPage.navigate();

  // Verify partnership code section is visible
  await expect(page.locator('[data-testid="partnership-code-section"]')).toBeVisible();
  await expect(page.locator('[data-testid="partnership-code-input"]')).toBeVisible();
  await expect(page.locator('[data-testid="apply-code-button"]')).toBeVisible();

  // Verify only paid plans are shown
  await expect(page.locator('[data-testid="plan-basic"]')).toBeVisible();
  await expect(page.locator('[data-testid="plan-standard"]')).toBeVisible();
  await expect(page.locator('[data-testid="plan-premium"]')).toBeVisible();

  // No FREE plan checks
});
```

---

## Tests Requiring No Changes

The following tests did NOT need updates because they only deal with paid subscriptions:

- **US-001.2**: Complete registration with BASIC plan and payment
- **US-001.3**: Reject duplicate church email
- **US-001.4**: Show validation errors
- **US-001.5**: Cannot access dashboard without subscription
- **US-008.2**: Initialize subscription payment
- **US-008.3**: Verify payment and activate subscription
- **US-008.4**: Upgrade from BASIC to STANDARD plan

These tests already use proper paid subscription flows, so the partnership code change doesn't affect them.

---

## Expected Test Results

### Tests That Should Pass

After these updates, the following tests should pass (assuming backend and frontend are running):

1. ✅ **US-001.1**: Registration with partnership code
2. ✅ **US-001.6**: Onboarding wizard with grace period
3. ✅ **US-008.1**: View plans and partnership code section

### Tests That May Need Data-TestId Attributes

Tests US-001.2 through US-001.5 and US-008.2 through US-008.4 may still fail if:
- Payment setup page is missing `data-testid` attributes
- Dashboard is missing subscription badge `data-testid`
- Billing page is missing various `data-testid` attributes
- Onboarding wizard components are missing `data-testid` attributes

---

## Next Steps

1. ✅ Partnership code backend - COMPLETE
2. ✅ Partnership code frontend UI - COMPLETE
3. ✅ E2E test updates - COMPLETE
4. 📋 **Add remaining data-testid attributes** - IN PROGRESS
   - Payment setup page (if any missing)
   - Dashboard (subscription badge, onboarding wizard)
   - Billing page (plan details, upgrade buttons, history)
   - Other feature pages (attendance, events, pastoral care, fellowship)
5. 📋 **Run full E2E test suite** - PENDING
6. 📋 **Fix any failing tests** - PENDING

---

## Database Prerequisite

**IMPORTANT**: Before running these E2E tests, ensure the database has the partnership codes:

```sql
-- Verify TRIAL14 code exists
SELECT * FROM partnership_codes WHERE code = 'TRIAL14';

-- If missing, run this migration:
-- src/main/resources/db/migration/V68__create_partnership_codes_table.sql
```

The migration should have created:
- `PARTNER2025` - 30 days, expires Dec 31 2025
- `TRIAL14` - 14 days, unlimited uses, no expiration ← Used by E2E tests
- `LAUNCH2025` - 60 days, max 100 uses, expires June 30 2025

---

## Running the Tests

```bash
# Run all registration tests
npm test -- critical-path-01-church-registration.spec.ts

# Run specific test
npm test -- critical-path-01-church-registration.spec.ts -g "US-001.1"

# Run billing tests
npm test -- critical-path-08-billing-subscriptions.spec.ts

# Run all critical path tests
npm test -- critical-path-*.spec.ts
```

---

## Files Modified

### Created:
- ✅ `PARTNERSHIP_CODE_SYSTEM.md` - Backend documentation
- ✅ `PARTNERSHIP_CODE_E2E_INTEGRATION.md` - This document

### Modified:
- ✅ `e2e/fixtures/test-data.ts` - Added getTestPartnershipCode()
- ✅ `e2e/pages/base.page.ts` - Added fillField() and enhanced waitForSelector()
- ✅ `e2e/pages/billing/subscription-selection.page.ts` - Added partnership code methods
- ✅ `e2e/tests/critical-path-01-church-registration.spec.ts` - Updated US-001.1 and US-001.6
- ✅ `e2e/tests/critical-path-08-billing-subscriptions.spec.ts` - Updated US-008.1

---

## Summary

The partnership code system is now fully integrated into both the application and E2E test suite. Churches can no longer get free access automatically - they must:

1. Register their church
2. Enter a valid partnership code (like `TRIAL14`)
3. Receive a grace period (e.g., 14 days)
4. Access the dashboard with ACTIVE subscription status

This gives you full control over who gets trial access through partnership codes while maintaining a clean, testable codebase.
