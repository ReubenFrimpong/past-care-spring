# E2E Test Structure Overview

## 📂 Directory Structure

```
past-care-spring-frontend/e2e/
├── fixtures/
│   └── test-data.ts                    # Test data generators
│
├── pages/                              # Page Object Model
│   ├── base.page.ts                    # Base page with common methods
│   │
│   ├── auth/
│   │   ├── login.page.ts              # Authentication
│   │   └── registration.page.ts       # Church registration
│   │
│   ├── billing/
│   │   └── subscription-selection.page.ts  # Subscription management
│   │
│   ├── events/
│   │   └── event-form.page.ts         # Event creation & registration
│   │
│   ├── fellowship/
│   │   └── fellowship-form.page.ts    # Fellowship group management
│   │
│   ├── giving/
│   │   └── donation-form.page.ts      # Donation & receipts
│   │
│   ├── members/
│   │   └── member-form.page.ts        # Member management
│   │
│   └── pastoral-care/
│       └── care-need-form.page.ts     # Pastoral care needs
│
└── tests/                              # Test specifications
    ├── critical-path-01-church-registration.spec.ts    # 6 tests
    ├── critical-path-02-member-management.spec.ts      # 8 tests
    ├── critical-path-03-attendance-flow.spec.ts        # 6 tests
    ├── critical-path-04-donation-receipts.spec.ts      # 7 tests
    ├── critical-path-05-event-registration.spec.ts     # 6 tests
    ├── critical-path-06-pastoral-care.spec.ts          # 5 tests
    ├── critical-path-07-fellowship-groups.spec.ts      # 4 tests
    └── critical-path-08-billing-subscriptions.spec.ts  # 4 tests

Total: 9 Page Objects + 8 Test Files = 46 Tests
```

---

## 🎯 Test Coverage Matrix

| Module | Page Object | Test File | Tests | Priority |
|--------|-------------|-----------|-------|----------|
| **Auth** | registration.page.ts<br>login.page.ts | critical-path-01-church-registration.spec.ts | 6 | P0 |
| **Members** | member-form.page.ts | critical-path-02-member-management.spec.ts | 8 | P0 |
| **Attendance** | *(uses existing pages)* | critical-path-03-attendance-flow.spec.ts | 6 | P0 |
| **Giving** | donation-form.page.ts | critical-path-04-donation-receipts.spec.ts | 7 | P0 |
| **Events** | event-form.page.ts | critical-path-05-event-registration.spec.ts | 6 | P1 |
| **Pastoral Care** | care-need-form.page.ts | critical-path-06-pastoral-care.spec.ts | 5 | P1 |
| **Fellowship** | fellowship-form.page.ts | critical-path-07-fellowship-groups.spec.ts | 4 | P1 |
| **Billing** | subscription-selection.page.ts | critical-path-08-billing-subscriptions.spec.ts | 4 | P0 |

---

## 🔄 Test Data Flow

```
TestData Generators → Page Objects → Test Specs
        ↓                   ↓              ↓
  Unique data         Reusable        User Stories
  (timestamp)         methods         (Given-When-Then)
```

### Test Data Generators (test-data.ts)
```typescript
generateChurch()          → Church registration data
generateAdmin()           → Admin user credentials
generateMember()          → Member profile data
generatePastor()          → Pastor user data
generateAttendanceSession() → Attendance session data
generateDonation()        → Donation transaction data
generateEvent()           → Event details
generateCareNeed()        → Pastoral care need data
generateFellowship()      → Fellowship group data
```

### Page Objects (Reusable Methods)
```typescript
navigate()               → Go to page
fill{Module}Form()       → Fill form fields
save()                   → Submit form
view{Entity}Details()    → Navigate to details
getFieldError()          → Get validation error
```

### Test Specs (User Journeys)
```typescript
test('US-XXX.Y: Description', async ({ page }) => {
  // Given: Setup test data and preconditions
  const data = TestData.generate{Entity}();

  // When: Perform the action being tested
  await page{Object}.{action}(data);

  // Then: Verify the expected outcome
  await expect(page.locator('...')).toBeVisible();
});
```

---

## 📊 Test Coverage by Priority

### P0 Tests (Critical - Must Work) - 27 tests
```
US-001: Church Registration          6 tests  ✅
US-002: Member Management             8 tests  ✅
US-003: Attendance Flow               6 tests  ✅
US-004: Donation & Receipts           7 tests  ✅
US-008: Billing/Subscriptions         4 tests  ✅
                                     ─────────
                                     27 tests
```

### P1 Tests (Important - Should Work) - 19 tests
```
US-005: Event Registration            6 tests  ✅
US-006: Pastoral Care                 5 tests  ✅
US-007: Fellowship Groups             4 tests  ✅
                                     ─────────
                                     19 tests
```

### Total: 46 tests (100% coverage of critical paths)

---

## 🎨 Test Patterns

### 1. Page Object Pattern
```typescript
// page object: donation-form.page.ts
export class DonationFormPage extends BasePage {
  async recordDonation(donationData: any) {
    await this.navigateToGiving();
    await this.clickRecordDonation();
    await this.fillDonationForm(donationData);
    await this.save();
  }
}

// test spec: critical-path-04-donation-receipts.spec.ts
test('US-004.1: Record cash donation', async ({ page }) => {
  const donationData = TestData.generateDonation();
  await donationFormPage.recordDonation(donationData);
  await expect(page.locator('.alert-success')).toBeVisible();
});
```

### 2. Given-When-Then Pattern
```typescript
test('US-002.1: Quick add member', async ({ page }) => {
  // Given: Admin wants to add a new visitor
  const memberData = TestData.generateMember();

  // When: Admin fills minimal fields and saves
  await memberFormPage.createMember(memberData);

  // Then: Member is created successfully
  await expect(page.locator('.alert-success')).toBeVisible();
  await expect(page).toHaveURL(/.*members\/\d+/);
});
```

### 3. Idempotent Data Pattern
```typescript
// Always generates unique data
const ts = Date.now();
return {
  name: `Test Church ${ts}`,
  email: `church${ts}@test.pastcare.com`,
  phoneNumber: `+233244${String(ts).slice(-6)}`
};
```

### 4. Validation Error Pattern
```typescript
// Error must appear NEXT TO affected field
await expect(page.locator('[data-testid="firstName-error"]'))
  .toBeVisible();
await expect(page.locator('[data-testid="phoneNumber-error"]'))
  .toContainText('already exists');
```

---

## 🔍 Test Selectors

All UI elements use `data-testid` attributes for reliable selection:

### Form Fields
```html
<input data-testid="firstName-input" />
<input data-testid="phoneNumber-input" />
<select data-testid="donationType-select" />
<textarea data-testid="description-input" />
```

### Buttons
```html
<button data-testid="save-button" />
<button data-testid="submit-button" />
<button data-testid="record-donation-button" />
<button data-testid="proceed-to-payment-button" />
```

### Validation Errors
```html
<span data-testid="firstName-error">Required</span>
<span data-testid="phoneNumber-error">Already exists</span>
```

### Display Elements
```html
<div data-testid="subscription-badge">FREE</div>
<div data-testid="receipt-reference">RCP-12345</div>
<div data-testid="total-giving">GH₵1,250.00</div>
```

---

## 📈 Test Execution Flow

```
1. beforeAll
   ↓
   Set up test accounts (admin, pastor, etc.)

2. beforeEach (runs before EACH test)
   ↓
   Initialize page objects
   ↓
   Login as appropriate user

3. test() - Individual test
   ↓
   Given: Setup test data
   ↓
   When: Perform actions
   ↓
   Then: Verify outcomes

4. afterEach (optional)
   ↓
   Cleanup test data

5. afterAll (optional)
   ↓
   Teardown test accounts
```

---

## 🚀 Quick Commands

```bash
# Install dependencies
npm install
npx playwright install --with-deps

# Run all critical path tests
npx playwright test --grep "@critical"

# Run by priority
npx playwright test --grep "@P0"    # Critical
npx playwright test --grep "@P1"    # Important

# Run specific user story
npx playwright test --grep "US-004"  # Donations

# Interactive modes
npx playwright test --grep "@critical" --ui       # UI mode
npx playwright test --grep "@critical" --headed   # See browser
npx playwright test --grep "@critical" --debug    # Step through

# Generate report
npx playwright test --grep "@critical" --reporter=html
npx playwright show-report
```

---

## ✅ Success Criteria

Each test verifies:
- ✅ User can complete the journey end-to-end
- ✅ UI elements render correctly at each step
- ✅ Validation errors display next to fields
- ✅ Success messages appear after actions
- ✅ Data persists correctly
- ✅ Role-based access control works
- ✅ Multi-tenancy isolation maintained

---

## 📚 Documentation

- **Implementation Guide:** `E2E_CRITICAL_PATH_COMPLETE_IMPLEMENTATION.md`
- **Quick Start:** `E2E_QUICK_START_GUIDE.md`
- **Test Structure:** `E2E_TEST_STRUCTURE.md` (this file)
- **User Stories:** `CRITICAL_USER_STORIES_E2E.md`

---

**Total Test Coverage:** 46 critical path tests across 8 user stories
**Status:** ✅ Complete and ready to run
