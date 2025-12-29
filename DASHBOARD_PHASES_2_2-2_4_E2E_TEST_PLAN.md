# Dashboard Phases 2.2-2.4 E2E Test Plan

**Date:** 2025-12-28
**Status:** ⚠️ **TESTS CREATED - PENDING EXECUTION**

---

## Overview

Comprehensive E2E test suite created for Dashboard Phases 2.2, 2.3, and 2.4 covering:
- Role-Based Templates (Phase 2.2)
- Goal Tracking (Phase 2.3)
- Advanced Analytics/Insights (Phase 2.4)
- Dashboard Customization (Drag & Drop)
- Integration scenarios

**Test File:** `/home/reuben/Documents/workspace/past-care-spring-frontend/e2e/dashboard-phases-2-2-2-3-2-4.spec.ts`

**Total Test Cases:** 27

---

## Test Coverage

### Phase 2.2: Role-Based Templates (7 tests)

#### ✅ Test 1: Display Template Gallery Button
- **Action:** User enters edit mode on dashboard
- **Expected:** "Browse Templates" button appears

#### ✅ Test 2: Open Template Gallery Dialog
- **Action:** Click "Browse Templates" in edit mode
- **Expected:** Template gallery dialog opens with title

#### ✅ Test 3: Display 5 Default Templates
- **Action:** Open template gallery
- **Expected:** Shows all 5 templates (Admin, Pastor, Treasurer, Fellowship Leader, Member)

#### ✅ Test 4: Show Role Badges
- **Action:** Open template gallery
- **Expected:** Each template displays correct role badge

#### ✅ Test 5: Apply Template
- **Action:** Select and apply a template, confirm dialog
- **Expected:** Template applied, dashboard reloads with new layout

#### ✅ Test 6: Filter Templates by Role (Admin Only)
- **Action:** Select role from filter dropdown
- **Expected:** Only templates for selected role shown

#### ✅ Test 7: Close Template Gallery
- **Action:** Click close button on gallery
- **Expected:** Dialog closes

---

### Phase 2.3: Goal Tracking (8 tests)

#### ✅ Test 8: Navigate to Goals Page
- **Action:** Click "Goals" in navigation
- **Expected:** Redirects to /goals page

#### ✅ Test 9: Empty State Display
- **Action:** Visit goals page with no goals
- **Expected:** Shows "No goals found" message

#### ✅ Test 10: Create Attendance Goal
- **Action:** Fill goal form (title, type=ATTENDANCE, target=100, dates)
- **Expected:** Goal created and appears in list

#### ✅ Test 11: Display Goal Progress Bar
- **Action:** Create a goal
- **Expected:** Goal card shows progress bar and percentage

#### ✅ Test 12: Recalculate Goal Progress
- **Action:** Click "Recalculate" button on goal
- **Expected:** Progress updates based on latest data

#### ✅ Test 13: Edit a Goal
- **Action:** Edit existing goal, change title and target
- **Expected:** Goal updated with new values

#### ✅ Test 14: Delete a Goal
- **Action:** Click delete, confirm dialog
- **Expected:** Goal removed from list

#### ✅ Test 15: Goals Widget on Dashboard
- **Action:** Create goal, navigate to dashboard
- **Expected:** "Active Goals" widget shows the goal

---

### Phase 2.4: Advanced Analytics (8 tests)

#### ✅ Test 16: Navigate to Insights Page
- **Action:** Click "Insights" in navigation
- **Expected:** Redirects to /insights page

#### ✅ Test 17: Empty State Display
- **Action:** Visit insights page with no insights
- **Expected:** Shows empty state or "Generate Insights" button

#### ✅ Test 18: Generate Insights
- **Action:** Click "Generate Insights" button
- **Expected:** Insights generated (may be empty if no data)

#### ✅ Test 19: Display Insight Cards with Severity Badges
- **Action:** Generate insights
- **Expected:** Insight cards show severity and category badges

#### ✅ Test 20: Dismiss an Insight
- **Action:** Click "Dismiss" on an insight
- **Expected:** Insight removed from active list

#### ✅ Test 21: Filter Insights by Category
- **Action:** Select category from filter (e.g., ATTENDANCE)
- **Expected:** Only insights from that category shown

#### ✅ Test 22: Insights Widget on Dashboard
- **Action:** Generate insights, navigate to dashboard
- **Expected:** "Insights" widget displays recent insights

#### ✅ Test 23: Navigate to Insight Action
- **Action:** Click "Take Action" on actionable insight
- **Expected:** Navigates to related page or opens dialog

---

### Dashboard Customization (3 tests)

#### ✅ Test 24: Enable Drag and Drop in Edit Mode
- **Action:** Enter edit mode
- **Expected:** Widgets have cdkDrag attribute enabled

#### ✅ Test 25: Disable Drag in View Mode
- **Action:** View dashboard in normal mode
- **Expected:** cdkDragDisabled is true on widgets

#### ✅ Test 26: Save Layout After Drag and Drop
- **Action:** Drag widgets, click "Save Layout"
- **Expected:** Layout saved, exits edit mode

---

### Integration Test (1 test)

#### ✅ Test 27: Templates + Goals + Insights Integration
- **Action:**
  1. Apply Admin template
  2. Create an attendance goal
  3. Generate insights
  4. View dashboard
- **Expected:** All features work together, dashboard shows 4 widgets

---

## Prerequisites for Running Tests

### 1. Backend Running
```bash
cd /home/reuben/Documents/workspace/pastcare-spring
./mvnw spring-boot:run
```

**Verify:** `curl http://localhost:8080/actuator/health`

### 2. Frontend Running
```bash
cd /home/reuben/Documents/workspace/past-care-spring-frontend
npm start
```

**Verify:** `curl http://localhost:4200`

### 3. Database Templates Populated
```sql
-- Verify 5 templates exist
SELECT COUNT(*) FROM dashboard_templates;
-- Expected: 5
```

### 4. Run Backend Compile
```bash
./mvnw compile
# Ensure all recent fixes are compiled
```

---

## How to Run Tests

### Run All Tests
```bash
cd /home/reuben/Documents/workspace/past-care-spring-frontend
npx playwright test e2e/dashboard-phases-2-2-2-3-2-4.spec.ts
```

### Run Specific Phase
```bash
# Phase 2.2 only
npx playwright test e2e/dashboard-phases-2-2-2-3-2-4.spec.ts -g "Phase 2.2"

# Phase 2.3 only
npx playwright test e2e/dashboard-phases-2-2-2-3-2-4.spec.ts -g "Phase 2.3"

# Phase 2.4 only
npx playwright test e2e/dashboard-phases-2-2-2-3-2-4.spec.ts -g "Phase 2.4"
```

### Run with UI Mode (Debugging)
```bash
npx playwright test e2e/dashboard-phases-2-2-2-3-2-4.spec.ts --ui
```

### Run in Headed Mode
```bash
npx playwright test e2e/dashboard-phases-2-2-2-3-2-4.spec.ts --headed
```

---

## Critical Testing Rules Followed

### ✅ 1. Unique User Per Test
Every test creates its own unique user with timestamp + random string:
```typescript
const uniqueUser = {
  name: `Test Admin ${timestamp}`,
  email: `test-admin-${timestamp}-${random}@example.com`,
  password: 'TestPassword123!',
  churchName: `Test Church ${timestamp}`
};
```

**Why:** Prevents test interference, ensures multi-tenant isolation

### ✅ 2. Test Independence
- Each test is fully self-contained
- No shared state between tests
- Tests can run in any order

### ✅ 3. Proper Assertions
- Uses `expect()` from Playwright
- Waits for elements to be visible
- Checks actual user-facing behavior

### ✅ 4. Realistic User Workflows
- Follows actual user steps (register → login → navigate → action)
- Tests end-to-end flows, not just API calls
- Verifies UI feedback and state changes

---

## Known Issues & Fixes Applied

### Issue 1: Template Gallery Empty
**Problem:** `dashboard_templates` table was empty
**Fix:** Manually inserted 5 templates with proper timestamps
**Verification:** `SELECT COUNT(*) FROM dashboard_templates;` returns 5

### Issue 2: Widgets Not Draggable
**Problem:** Missing `cdkDrag` directive on widget cards
**Fix:** Added `cdkDrag [cdkDragDisabled]="!editMode()"` to 4 widgets
**Verification:** Test 24 checks for cdkDrag attribute

### Issue 3: Type Mismatch (LocalDateTime vs Instant)
**Problem:** Repository methods expected Instant but received LocalDateTime
**Fix:** Updated repository signatures and added conversion in services
**Verification:** Backend compiles without errors

### Issue 4: Null church_id in Insights
**Problem:** Church relationship not set when creating insights
**Fix:** Added `insight.setChurch(church)` in createInsight helper
**Verification:** Test 18 generates insights without DB errors

---

## Test Execution Checklist

Before marking features as complete, verify:

- [ ] Backend is running without errors
- [ ] Frontend is serving on port 4200
- [ ] Database has 5 templates
- [ ] All 27 tests pass
- [ ] No console errors during test execution
- [ ] Manual smoke test of key features

---

## Expected Test Results

### Success Criteria
```
27 passed (27)
```

### Acceptable Results
- All Phase 2.2 tests (7) pass
- All Phase 2.3 tests (8) pass
- All Phase 2.4 tests (8) pass
- Drag & drop tests (3) pass
- Integration test (1) passes

### Failure Investigation
If tests fail:
1. Check browser console for JS errors
2. Verify backend logs for API errors
3. Confirm database has test data
4. Run individual test with `--debug` flag
5. Use Playwright trace: `--trace on`

---

## Test Maintenance

### When to Update Tests
1. UI changes (new selectors, button text, etc.)
2. API endpoint changes
3. New features added
4. Bug fixes that affect user flows

### Updating Selectors
- Prefer semantic selectors: `button:has-text("Create Goal")`
- Avoid brittle selectors: `.btn-123`
- Use data-testid when needed

---

## Performance Expectations

### Test Suite Timing
- **Total runtime:** ~6-8 minutes (27 tests × ~15-20s each)
- **Per phase:**
  - Phase 2.2: ~2 minutes (7 tests)
  - Phase 2.3: ~2.5 minutes (8 tests)
  - Phase 2.4: ~2.5 minutes (8 tests)
  - Other: ~1 minute (4 tests)

### Optimization Tips
- Run in parallel: `--workers=4`
- Skip slow tests during development: `test.skip()`
- Use `test.only()` for focused testing

---

## Conclusion

**Status:** E2E tests are ready but require both backend and frontend to be running for execution.

**Next Steps:**
1. Ensure backend is running (`./mvnw spring-boot:run`)
2. Ensure frontend is running (`npm start`)
3. Run test suite: `npx playwright test e2e/dashboard-phases-2-2-2-3-2-4.spec.ts`
4. Address any failures
5. Update PLAN.md with test results

**Once all tests pass, Dashboard Phases 2.2-2.4 can be marked as complete and production-ready.**
