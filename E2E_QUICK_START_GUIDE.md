# E2E Critical Path Tests - Quick Start Guide

## 🚀 Get Started in 3 Steps

### Step 1: Install Dependencies
```bash
cd past-care-spring-frontend
npm install
npx playwright install --with-deps
```

### Step 2: Start the Application
```bash
# Terminal 1 - Backend
cd /home/reuben/Documents/workspace/pastcare-spring
./mvnw spring-boot:run

# Terminal 2 - Frontend
cd past-care-spring-frontend
npm run dev
```

### Step 3: Run the Tests
```bash
# Terminal 3 - Tests
cd past-care-spring-frontend
npx playwright test --grep "@critical"
```

---

## 📋 Quick Command Reference

### Run All Critical Path Tests
```bash
npx playwright test --grep "@critical"
```

### Run by Priority
```bash
# Critical (P0) tests only
npx playwright test --grep "@P0"

# Important (P1) tests only
npx playwright test --grep "@P1"
```

### Run Specific User Story
```bash
npx playwright test --grep "US-001"  # Church Registration (6 tests)
npx playwright test --grep "US-002"  # Member Management (8 tests)
npx playwright test --grep "US-003"  # Attendance (6 tests)
npx playwright test --grep "US-004"  # Donations (7 tests)
npx playwright test --grep "US-005"  # Events (6 tests)
npx playwright test --grep "US-006"  # Pastoral Care (5 tests)
npx playwright test --grep "US-007"  # Fellowship (4 tests)
npx playwright test --grep "US-008"  # Billing (4 tests)
```

### Run Single Test File
```bash
npx playwright test e2e/tests/critical-path-01-church-registration.spec.ts
npx playwright test e2e/tests/critical-path-02-member-management.spec.ts
npx playwright test e2e/tests/critical-path-03-attendance-flow.spec.ts
npx playwright test e2e/tests/critical-path-04-donation-receipts.spec.ts
npx playwright test e2e/tests/critical-path-05-event-registration.spec.ts
npx playwright test e2e/tests/critical-path-06-pastoral-care.spec.ts
npx playwright test e2e/tests/critical-path-07-fellowship-groups.spec.ts
npx playwright test e2e/tests/critical-path-08-billing-subscriptions.spec.ts
```

### Interactive Modes
```bash
# UI Mode (interactive test runner)
npx playwright test --grep "@critical" --ui

# Headed Mode (see browser)
npx playwright test --grep "@critical" --headed

# Debug Mode (step through tests)
npx playwright test --grep "@critical" --debug
```

### Generate Report
```bash
# Run tests and generate HTML report
npx playwright test --grep "@critical" --reporter=html

# View report
npx playwright show-report
```

---

## 📊 Test Files Overview

| File | Tests | User Story | Priority |
|------|-------|------------|----------|
| `critical-path-01-church-registration.spec.ts` | 6 | Church Registration & Onboarding | P0 |
| `critical-path-02-member-management.spec.ts` | 8 | Member Registration & Profile | P0 |
| `critical-path-03-attendance-flow.spec.ts` | 6 | Sunday Service Attendance | P0 |
| `critical-path-04-donation-receipts.spec.ts` | 7 | Donation Collection & Receipt | P0 |
| `critical-path-05-event-registration.spec.ts` | 6 | Event Registration & Check-in | P1 |
| `critical-path-06-pastoral-care.spec.ts` | 5 | Pastoral Care Management | P1 |
| `critical-path-07-fellowship-groups.spec.ts` | 4 | Fellowship Group Management | P1 |
| `critical-path-08-billing-subscriptions.spec.ts` | 4 | Billing/Subscription Management | P0 |
| **TOTAL** | **46** | **8 User Stories** | **P0 + P1** |

---

## 🎯 What Each Test Suite Covers

### US-001: Church Registration (6 tests)
- ✅ Complete registration with FREE plan
- ✅ Complete registration with BASIC plan and payment
- ✅ Reject duplicate church email
- ✅ Show validation errors
- ✅ Block dashboard without subscription
- ✅ Onboarding wizard guidance

### US-002: Member Management (8 tests)
- ✅ Quick add member with minimal data
- ✅ Create member with full profile
- ✅ Profile completeness updates
- ✅ Search and find members
- ✅ Filter by status and tags
- ✅ Update member profile
- ✅ Validation errors next to fields
- ✅ Duplicate phone number rejection

### US-003: Attendance Flow (6 tests)
- ✅ Create attendance session
- ✅ Generate QR code
- ✅ Manual check-in
- ✅ Bulk check-in
- ✅ View session summary
- ✅ Export to Excel

### US-004: Donation & Receipts (7 tests)
- ✅ Record cash donation with receipt
- ✅ Record online donation via Paystack
- ✅ Record anonymous donation
- ✅ Generate and display receipt
- ✅ View donor giving history
- ✅ Giving dashboard updates
- ✅ Export giving report

### US-005: Event Registration (6 tests)
- ✅ Create event with details
- ✅ Member registers for event
- ✅ Registration respects capacity
- ✅ Mark attendee present
- ✅ Cancel registration
- ✅ View attendee list

### US-006: Pastoral Care (5 tests)
- ✅ Member submits care need
- ✅ Pastor assigns to another pastor
- ✅ Record visit with confidential notes
- ✅ Update status transitions
- ✅ View member care history

### US-007: Fellowship Groups (4 tests)
- ✅ Create fellowship group
- ✅ Add multiple members
- ✅ Track meeting attendance
- ✅ View health dashboard

### US-008: Billing/Subscriptions (4 tests)
- ✅ View all subscription plans
- ✅ Initialize subscription payment
- ✅ Verify payment and activate
- ✅ Upgrade from Basic to Standard

---

## 🔧 Troubleshooting

### Tests Failing?

**1. Check Backend is Running**
```bash
curl http://localhost:8080/api/health
```

**2. Check Frontend is Running**
```bash
curl http://localhost:4200
```

**3. Clear Test Data**
```bash
# If tests fail due to duplicate data, restart backend to clear H2 database
# Or use PostgreSQL and clear test data manually
```

**4. Update Playwright**
```bash
npx playwright install --with-deps
```

**5. Run Single Test for Debugging**
```bash
npx playwright test --grep "US-001.1" --debug
```

---

## 📝 Test Data

All test data is automatically generated with timestamps to ensure uniqueness:

```typescript
// Example generated data
Church: "Test Church 1735513200000"
Email: "church1735513200000@test.pastcare.com"
Phone: "+233244513200"
Member: "John1735513200000 Doe1735513200000"
```

Tests are **idempotent** - you can run them multiple times without conflicts.

---

## 🎉 Success Indicators

When tests pass, you'll see:
```
✓ US-001.1: Complete church registration with FREE plan (5.2s)
✓ US-001.2: Complete church registration with BASIC plan and payment (3.8s)
...
✓ US-008.4: Upgrade from Basic to Standard plan (4.1s)

46 passed (2.3m)
```

---

## 📚 More Information

- **Full Documentation:** `E2E_CRITICAL_PATH_COMPLETE_IMPLEMENTATION.md`
- **User Stories:** `CRITICAL_USER_STORIES_E2E.md`
- **Test Data:** `e2e/fixtures/test-data.ts`
- **Page Objects:** `e2e/pages/`

---

**Ready to run?** Execute: `npx playwright test --grep "@critical"`
