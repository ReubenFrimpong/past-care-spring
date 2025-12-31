# Next Steps - Action Plan for E2E Testing

**Current Status**: Infrastructure ready, tests failing due to business model mismatch
**Goal**: Get all 46 E2E tests passing

---

## Priority 1: Fix Test Business Model Mismatch (CRITICAL)

### Issue
Tests expect a FREE subscription plan, but the business model has:
- Single plan: "PastCare Standard"
- Grace period instead of FREE tier
- Multiple billing periods (Monthly, 3M, 6M, Yearly)

### Action Items

#### 1.1 Update Test Expectations
**File**: `past-care-spring-frontend/e2e/tests/critical-path-01-church-registration.spec.ts`

**Current Code** (Lines 27-43):
```typescript
test('US-001.1: Complete church registration with FREE plan', async ({ page }) => {
  // When: User selects FREE plan
  await subscriptionPage.activateFreePlan();

  // Then: Dashboard is accessible
  await expect(page).toHaveURL(/.*dashboard/);
});
```

**Change To**:
```typescript
test('US-001.1: Complete church registration and start grace period', async ({ page }) => {
  // When: User completes registration
  // Registration should automatically grant grace period

  // Then: Can access dashboard during grace period
  await page.goto('/dashboard');
  await expect(page).toHaveURL(/.*dashboard/);

  // And: Grace period banner is visible
  await expect(page.locator('[data-testid="grace-period-banner"]')).toBeVisible();
});
```

#### 1.2 Update Subscription Page Object
**File**: `past-care-spring-frontend/e2e/pages/billing/subscription-selection.page.ts`

**Remove/Update**:
- Line 23-26: `selectPlan('FREE')` method
- Line 31-34: `clickSubscribeButton('FREE')` method
- Line 62-67: `activateFreePlan()` method

**Add**:
```typescript
/**
 * Skip subscription setup (uses grace period)
 */
async skipToGracePeriod() {
  // Navigate directly to dashboard
  await this.page.goto('/dashboard');
  await this.waitForLoad();
}

/**
 * Select billing period and subscribe
 */
async subscribeToPlan(period: 'monthly' | '3months' | '6months' | 'yearly') {
  const periodSelector = `[data-testid="period-${period}"]`;
  await this.click(periodSelector);
  await this.click('[data-testid="proceed-payment-button"]');
}
```

#### 1.3 Add Grace Period Data to Test Fixtures
**File**: `past-care-spring-frontend/e2e/fixtures/test-data.ts`

**Add**:
```typescript
/**
 * Calculate grace period end date (e.g., 14 days from now)
 */
static getGracePeriodEndDate(): string {
  const endDate = new Date();
  endDate.setDate(endDate.getDate() + 14);
  return endDate.toISOString();
}
```

---

## Priority 2: Fix Login Flow for Tests

### Issue
After login, users redirect to `/subscription/select` instead of `/dashboard` because they have no active subscription.

### Solution Options

#### Option A: Auto-Activate Grace Period (Recommended)
Modify backend to automatically grant grace period on registration.

**Backend File**: `src/main/java/com/reuben/pastcare_spring/services/AuthService.java`

**Add to registration logic**:
```java
// After creating church subscription
subscription.setStatus(SubscriptionStatus.ACTIVE);
subscription.setGracePeriodEnd(LocalDateTime.now().plusDays(14));
subscription.setCurrentPeriodStart(LocalDateTime.now());
subscription.setCurrentPeriodEnd(LocalDateTime.now().plusDays(14));
subscriptionRepository.save(subscription);
```

#### Option B: Update Test Setup
Add subscription activation to test beforeEach hooks.

**All Test Files** (US-002 through US-008):

**Current beforeEach**:
```typescript
test.beforeEach(async ({ page }) => {
  await loginPage.login(adminEmail, adminPassword);
  await loginPage.verifyLoginSuccess();
});
```

**Change To**:
```typescript
test.beforeEach(async ({ page }) => {
  // Register and login includes grace period
  const churchData = TestData.generateChurch();
  const adminData = TestData.generateAdmin();

  await registrationPage.registerChurch(churchData, adminData);
  // Registration now grants grace period automatically

  await loginPage.login(adminData.email, adminData.password);
  await expect(page).toHaveURL(/.*dashboard/);
});
```

---

## Priority 3: Add Missing data-testid Attributes

### Systematic Approach

Run tests one user story at a time, add attributes where failures occur:

```bash
# Run US-002 tests only
npx playwright test e2e/tests/critical-path-02-member-management.spec.ts --project=chromium --headed

# Watch for errors like:
# "Timeout waiting for [data-testid="xyz"]"

# Add the missing attribute to the corresponding page
# Repeat until US-002 passes

# Move to US-003, US-004, etc.
```

### Pages Needing Attributes

#### Attendance Page (`/attendance`)
**File**: `/home/reuben/Documents/workspace/past-care-spring-frontend/src/app/attendance-page/attendance-page.html`

**Add**:
```html
<!-- Session creation -->
<button data-testid="create-session-button">Create Session</button>
<input data-testid="session-name-input" />
<input data-testid="session-date-input" />
<button data-testid="save-session-button">Save</button>

<!-- QR Code -->
<img data-testid="qr-code-image" />
<button data-testid="download-qr-button">Download QR</button>

<!-- Check-in -->
<input data-testid="member-search-input" />
<button data-testid="check-in-button">Check In</button>
<div data-testid="present-count">0</div>

<!-- Export -->
<button data-testid="export-attendance-button">Export</button>
```

#### Events Page (`/events`)
**File**: Find event pages and add similar attributes

#### Pastoral Care Page (`/pastoral-care`)
**File**: Find pastoral care pages and add attributes

#### Fellowship Page (`/fellowships`)
**File**: Find fellowship pages and add attributes

---

## Priority 4: Update All Test Files

### Tests Needing Updates

1. **critical-path-01-church-registration.spec.ts** (6 tests)
   - Remove FREE plan expectations
   - Update to grace period flow
   - Test grace period banner visibility

2. **critical-path-02-member-management.spec.ts** (8 tests)
   - Update beforeEach to ensure dashboard access
   - Keep test logic same, just fix setup

3. **critical-path-03-attendance-flow.spec.ts** (6 tests)
   - Update beforeEach
   - Add missing data-testid to attendance page
   - Run and fix

4. **critical-path-04-donation-receipts.spec.ts** (7 tests)
   - Update beforeEach
   - Add data-testid to donations page (partially done)
   - Complete remaining attributes

5. **critical-path-05-event-registration.spec.ts** (6 tests)
   - Update beforeEach
   - Add data-testid to events pages
   - Run and fix

6. **critical-path-06-pastoral-care.spec.ts** (5 tests)
   - Update beforeEach
   - Add data-testid to pastoral care pages
   - Run and fix

7. **critical-path-07-fellowship-groups.spec.ts** (4 tests)
   - Update beforeEach
   - Add data-testid to fellowship pages
   - Run and fix

8. **critical-path-08-billing-subscriptions.spec.ts** (4 tests)
   - Update to match actual billing page
   - Test subscription upgrades, not FREE tier
   - Add data-testid to billing page

---

## Execution Plan

### Day 1: Fix Core Issues (4-6 hours)
1. ✅ Update backend to grant grace period on registration (1 hour)
2. ✅ Update US-001 tests to remove FREE plan expectations (2 hours)
3. ✅ Update subscription page object (1 hour)
4. ✅ Test US-001 until all 6 tests pass (1-2 hours)

### Day 2: Fix Login Flow & US-002 (4-5 hours)
1. ✅ Verify grace period grants dashboard access (1 hour)
2. ✅ Update all test beforeEach hooks (1 hour)
3. ✅ Add data-testid to members page (remaining attributes) (1 hour)
4. ✅ Run US-002 tests until all pass (2 hours)

### Day 3: US-003 Attendance (3-4 hours)
1. ✅ Add data-testid to attendance page (2 hours)
2. ✅ Run US-003 tests until all pass (1-2 hours)

### Day 4: US-004 Donations (3-4 hours)
1. ✅ Complete data-testid on donations page (1-2 hours)
2. ✅ Run US-004 tests until all pass (2 hours)

### Day 5: US-005, US-006, US-007, US-008 (6-8 hours)
1. ✅ Add data-testid to events, pastoral care, fellowship, billing pages (4-5 hours)
2. ✅ Run remaining tests until all pass (2-3 hours)

### Total Time: 20-27 hours

---

## Quick Command Reference

### Run Specific Test File
```bash
cd past-care-spring-frontend
npx playwright test e2e/tests/critical-path-01-church-registration.spec.ts --project=chromium
```

### Run Single Test
```bash
npx playwright test e2e/tests/critical-path-01-church-registration.spec.ts:27 --project=chromium
```

### Run with UI (Interactive)
```bash
npx playwright test --ui
```

### Run with Headed Browser (See What's Happening)
```bash
npx playwright test e2e/tests/critical-path-01-church-registration.spec.ts --headed --project=chromium
```

### Generate HTML Report
```bash
npx playwright test --reporter=html
npx playwright show-report
```

### Debug Mode
```bash
npx playwright test --debug
```

---

## Testing Workflow

1. **Start Backend**:
   ```bash
   cd /home/reuben/Documents/workspace/pastcare-spring
   ./mvnw spring-boot:run -Dmaven.test.skip=true
   ```

2. **Start Frontend** (separate terminal):
   ```bash
   cd /home/reuben/Documents/workspace/past-care-spring-frontend
   npm start
   ```

3. **Run Tests** (separate terminal):
   ```bash
   cd /home/reuben/Documents/workspace/pastcare-spring/past-care-spring-frontend
   npx playwright test e2e/tests/critical-path-01-church-registration.spec.ts --headed
   ```

4. **Watch for Errors**:
   - "Timeout waiting for [data-testid='...']" → Add the attribute
   - "Expected URL /dashboard, got /subscription" → Fix navigation
   - "Element not found" → Check selector or page structure

5. **Fix and Repeat**:
   - Add missing attributes
   - Update test expectations
   - Re-run until green

---

## Success Metrics

- **Phase 1 Complete**: All US-001 tests passing (6/46)
- **Phase 2 Complete**: US-001 + US-002 passing (14/46)
- **Phase 3 Complete**: US-001 through US-003 passing (20/46)
- **Phase 4 Complete**: US-001 through US-004 passing (27/46)
- **Final Goal**: All 46 tests passing (46/46) ✅

---

## Current Status Files

- **Test Report**: `E2E_TEST_EXECUTION_REPORT.md` (comprehensive analysis)
- **Test Files**: `past-care-spring-frontend/e2e/tests/critical-path-*.spec.ts`
- **Page Objects**: `past-care-spring-frontend/e2e/pages/`
- **Test Data**: `past-care-spring-frontend/e2e/fixtures/test-data.ts`

---

## Notes

- Backend integration tests have compilation errors but don't affect E2E tests
- All infrastructure is set up and working
- Main work is aligning tests with business model + adding data-testid attributes
- Tests are well-written, just need configuration updates
- Estimated 20-27 hours to full green suite

**Good luck! The foundation is solid, now it's just iteration and refinement.** 🚀
