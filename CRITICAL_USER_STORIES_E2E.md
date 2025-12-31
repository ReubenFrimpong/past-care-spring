# Critical User Stories - E2E Test Coverage

This document defines the critical user journeys for PastCare Spring and maps them to E2E tests. These represent the **must-work** paths that users follow daily.

---

## 🎯 Critical User Stories Overview

| Story ID | User Story | Priority | Test Count |
|----------|------------|----------|------------|
| US-001 | Church Registration & Onboarding | P0 | 6 tests |
| US-002 | Member Registration & Profile Management | P0 | 8 tests |
| US-003 | Sunday Service Attendance Flow | P0 | 6 tests |
| US-004 | Donation Collection & Receipt | P0 | 7 tests |
| US-005 | Event Registration & Check-in | P1 | 6 tests |
| US-006 | Pastoral Care Need Management | P1 | 5 tests |
| US-007 | Fellowship Group Management | P1 | 4 tests |
| US-008 | Subscription Upgrade & Management | P0 | 5 tests |
| **TOTAL** | **8 Critical Journeys** | | **47 tests** |

---

## 📖 Detailed User Stories

### US-001: Church Registration & Onboarding (P0)
**As a** new church administrator
**I want to** register my church and set up my account
**So that** I can start managing my church operations

**Critical Path:**
1. ✅ Visit landing page
2. ✅ Click "Get Started" button
3. ✅ Fill church registration form (name, email, phone, address)
4. ✅ Fill admin user form (name, email, password)
5. ✅ Submit registration
6. ✅ **Choose subscription plan (FREE, BASIC, STANDARD, or PREMIUM)**
7. ✅ **If paid plan: Complete Paystack payment**
8. ✅ **If FREE plan: Skip payment and activate**
9. ✅ Receive email verification (optional based on config)
10. ✅ Login with credentials
11. ✅ See onboarding wizard with welcome steps
12. ✅ Complete basic setup (add first member, create first event, etc.)
13. ✅ Arrive at dashboard with subscription badge visible

**E2E Tests:**
- `test('US-001.1: Complete church registration with FREE plan')`
- `test('US-001.2: Complete church registration with BASIC plan and payment')`
- `test('US-001.3: Reject duplicate church email')`
- `test('US-001.4: Show validation errors for invalid data')`
- `test('US-001.5: Cannot access dashboard without choosing subscription')`
- `test('US-001.6: Onboarding wizard guides new admin through setup')`

---

### US-002: Member Registration & Profile Management (P0)
**As a** church admin
**I want to** register new members and manage their profiles
**So that** I can maintain accurate member records

**Critical Path:**
1. ✅ Navigate to Members page
2. ✅ Click "Add Member" button
3. ✅ Choose "Quick Add" for new visitor
4. ✅ Enter basic info (name, phone, email)
5. ✅ Save member
6. ✅ See member in list
7. ✅ Click member to view profile
8. ✅ Edit profile to add more details
9. ✅ See profile completeness increase
10. ✅ Upload profile photo

**E2E Tests:**
- `test('US-002.1: Quick add member with minimal data')`
- `test('US-002.2: Create member with full profile data')`
- `test('US-002.3: Profile completeness updates when fields added')`
- `test('US-002.4: Search and find member by name')`
- `test('US-002.5: Filter members by status and tags')`
- `test('US-002.6: Update member profile successfully')`
- `test('US-002.7: Upload and display profile photo')`
- `test('US-002.8: Delete member with confirmation')`

---

### US-003: Sunday Service Attendance Flow (P0)
**As a** pastor or admin
**I want to** mark attendance for Sunday service
**So that** I can track member participation

**Critical Path:**
1. ✅ Navigate to Attendance page
2. ✅ Click "New Session" button
3. ✅ Create attendance session (name, date, service type)
4. ✅ Generate QR code for check-in
5. ✅ Display QR code on screen
6. ✅ Members scan QR code (or manual check-in)
7. ✅ See attendance count update in real-time
8. ✅ View session summary (total present, percentage)
9. ✅ Export attendance report to Excel

**E2E Tests:**
- `test('US-003.1: Create new attendance session')`
- `test('US-003.2: Generate QR code for session')`
- `test('US-003.3: Manual check-in marks member present')`
- `test('US-003.4: Bulk check-in multiple members')`
- `test('US-003.5: View session summary with statistics')`
- `test('US-003.6: Export attendance to Excel')`

---

### US-004: Donation Collection & Receipt (P0)
**As a** treasurer
**I want to** record donations and issue receipts
**So that** we can track giving and donors have records

**Critical Path:**
1. ✅ Navigate to Giving page
2. ✅ Click "Record Donation" button
3. ✅ Select donor (member or anonymous)
4. ✅ Enter amount and donation type (tithe, offering, pledge)
5. ✅ Select payment method (cash, mobile money, online)
6. ✅ Save donation
7. ✅ Auto-generate receipt with reference number
8. ✅ Send receipt via email/SMS
9. ✅ View donation in giving history
10. ✅ See giving dashboard update

**E2E Tests:**
- `test('US-004.1: Record cash donation with receipt')`
- `test('US-004.2: Record online donation via Paystack')`
- `test('US-004.3: Record anonymous donation')`
- `test('US-004.4: Generate and display donation receipt')`
- `test('US-004.5: View donor giving history')`
- `test('US-004.6: Giving dashboard shows updated totals')`
- `test('US-004.7: Export giving report for date range')`

---

### US-005: Event Registration & Check-in (P1)
**As a** member
**I want to** register for church events and check in
**So that** I can participate in activities

**Critical Path:**
1. ✅ Browse upcoming events
2. ✅ Click on event to see details
3. ✅ Click "Register" button
4. ✅ Fill registration form (dietary requirements, etc.)
5. ✅ Submit registration
6. ✅ Receive confirmation email
7. ✅ On event day, check in at venue
8. ✅ Organizer marks attendance
9. ✅ Receive event reminder 24 hours before

**E2E Tests:**
- `test('US-005.1: Create new event with details')`
- `test('US-005.2: Member registers for event')`
- `test('US-005.3: Registration respects capacity limits')`
- `test('US-005.4: Organizer marks attendee present')`
- `test('US-005.5: Cancel registration successfully')`
- `test('US-005.6: View event attendee list')`

---

### US-006: Pastoral Care Need Management (P1)
**As a** pastor
**I want to** track and manage pastoral care needs
**So that** no member is overlooked

**Critical Path:**
1. ✅ Member submits care need (illness, bereavement, counseling)
2. ✅ Pastor sees need on dashboard
3. ✅ Pastor assigns need to self or another pastor
4. ✅ Pastor schedules visit
5. ✅ After visit, record notes (private/confidential)
6. ✅ Update care need status (Open → In Progress → Resolved)
7. ✅ See care need history for member
8. ✅ Generate pastoral care report

**E2E Tests:**
- `test('US-006.1: Member submits care need')`
- `test('US-006.2: Pastor assigns care need to another pastor')`
- `test('US-006.3: Record pastoral visit with confidential notes')`
- `test('US-006.4: Update care need status transitions')`
- `test('US-006.5: View member care history')`

---

### US-007: Fellowship Group Management (P1)
**As a** fellowship leader
**I want to** manage my fellowship group
**So that** members stay connected

**Critical Path:**
1. ✅ Create new fellowship group
2. ✅ Add members to fellowship
3. ✅ Set meeting schedule (weekly, location)
4. ✅ Track fellowship attendance
5. ✅ View fellowship health metrics (growth, retention)
6. ✅ Send announcements to fellowship members

**E2E Tests:**
- `test('US-007.1: Create fellowship group with details')`
- `test('US-007.2: Add multiple members to fellowship')`
- `test('US-007.3: Track fellowship meeting attendance')`
- `test('US-007.4: View fellowship health dashboard')`

---

### US-008: Subscription Payment Flow (P0)
**As a** church admin
**I want to** subscribe to a paid plan
**So that** I can access premium features

**Critical Path:**
1. ✅ View pricing page with plan comparison
2. ✅ Click "Subscribe" on Basic plan
3. ✅ Review plan features and pricing
4. ✅ Click "Proceed to Payment"
5. ✅ Redirected to Paystack payment page
6. ✅ Enter card details and pay
7. ✅ Redirected back with success message
8. ✅ Subscription activated immediately
9. ✅ Dashboard shows current plan badge
10. ✅ Premium features now accessible

**E2E Tests:**
- `test('US-008.1: View all subscription plans')`
- `test('US-008.2: Initialize subscription payment')`
- `test('US-008.3: Verify payment and activate subscription')`
- `test('US-008.4: Upgrade from Basic to Standard plan')`
- `test('US-008.5: Cancel subscription successfully')`

---

## 🎯 Test Implementation Strategy

### Phase 1: Infrastructure (30 min)
1. Create E2E directory structure
2. Set up Page Object Model base classes
3. Configure fixtures and helpers

### Phase 2: Critical Path Tests (2-3 hours)
1. US-001: Church Registration (5 tests)
2. US-002: Member Management (8 tests)
3. US-003: Attendance (6 tests)
4. US-004: Giving (7 tests)
5. US-008: Billing (5 tests)

### Phase 3: Important Path Tests (1-2 hours)
1. US-005: Events (6 tests)
2. US-006: Pastoral Care (5 tests)
3. US-007: Fellowship (4 tests)

---

## 📊 Coverage Goals

| Priority | Stories | Tests | Target |
|----------|---------|-------|--------|
| P0 (Must Have) | 5 | 30 | Week 1 |
| P1 (Important) | 3 | 16 | Week 2 |
| **Total** | **8** | **46** | **2 weeks** |

---

## 🚀 Running Critical Path Tests

Once implemented:

```bash
# Run all critical path tests
npx playwright test --grep "@critical"

# Run specific user story tests
npx playwright test --grep "US-001"
npx playwright test --grep "US-002"

# Run P0 tests only
npx playwright test --grep "@P0"
```

---

## ✅ Success Criteria

Each critical path test must verify:
1. ✅ User can complete the journey end-to-end
2. ✅ UI elements render correctly at each step
3. ✅ Validation errors display properly
4. ✅ Success messages appear after actions
5. ✅ Data persists correctly (check database via API)
6. ✅ Role-based access control works
7. ✅ Multi-tenancy isolation is maintained

---

**Next Step:** Implement these 46 critical path E2E tests using Playwright.
