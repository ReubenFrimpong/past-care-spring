# Testing Quick Reference Guide

A comprehensive guide for running all tests in the PastCare Spring application. All tests are **idempotent** and can be run multiple times without any side effects.

## 🎯 Quick Start

### Run Everything (Recommended)
```bash
./run-all-tests.sh
```
Runs all 450+ tests (API + E2E) in the correct order.

### Run Only API Tests
```bash
./run-api-tests.sh
# or
mvn verify -P api-tests
```
Runs 250+ backend API integration tests.

### Run Only E2E Tests
```bash
./run-e2e-tests.sh
# or
cd past-care-spring-frontend && npx playwright test
```
Runs 200+ frontend end-to-end tests.

---

## 🧪 Backend API Testing

All API tests are idempotent using:
- ✅ H2 in-memory database (fresh for each test run)
- ✅ `@Transactional` annotation (automatic rollback after each test)
- ✅ Isolated test data (no collisions between tests)

### Maven Profiles

| Profile | Command | Description |
|---------|---------|-------------|
| `unit-tests` | `mvn test` | Unit tests only (default) |
| `api-tests` | `mvn verify -P api-tests` | API integration tests only |
| `all-tests` | `mvn verify -P all-tests` | Both unit and API tests |

### Running Specific Tests

```bash
# Run specific test class
mvn test -Dtest=MemberCrudIntegrationTest

# Run specific test method
mvn test -Dtest=MemberCrudIntegrationTest#shouldCreateMemberWithFullProfile

# Run tests by module tag
mvn test -Dgroups="integration & module:members"

# Run all integration tests
mvn test -Dgroups="integration"
```

### Test Modules

| Module | Tests | Status | Location |
|--------|-------|--------|----------|
| Authentication | 13 | ✅ Complete | `integration/auth/` |
| Members | 28 | ✅ Complete | `integration/members/` |
| Attendance | 19 | ✅ Complete | `integration/attendance/` |
| Fellowship | 22 | 🔄 In Progress | `integration/fellowship/` |
| Giving | 26 | 🔄 In Progress | `integration/giving/` |
| Pastoral Care | 33 | 🔄 In Progress | `integration/pastoral/` |
| Events | 29 | 🔄 In Progress | `integration/events/` |
| Communications | 15 | 🔄 In Progress | `integration/communications/` |
| Billing | 19 | 🔄 In Progress | `integration/billing/` |

---

## 🎭 Frontend E2E Testing

All E2E tests are idempotent using:
- ✅ Isolated test data (unique timestamps, random identifiers)
- ✅ Automatic cleanup (delete created resources after tests)
- ✅ Parallel execution (separate test contexts)

### Playwright Commands

```bash
# Run all E2E tests
npx playwright test

# Run with UI (interactive mode)
npx playwright test --ui

# Run in headed mode (see browser)
npx playwright test --headed

# Run in debug mode
npx playwright test --debug

# Run specific test file
npx playwright test e2e/tests/03-members.spec.ts

# Run tests in specific browser
npx playwright test --project=chromium
npx playwright test --project=firefox
npx playwright test --project=webkit

# View test report
npx playwright show-report
```

### E2E Test Modules

| Module | E2E Tests | Validation Tests | Total |
|--------|-----------|------------------|-------|
| Authentication & Users | 30 | 10 | 40 |
| Billing | 25 | 0 | 25 |
| Members | 30 | 12 | 42 |
| Attendance | 20 | 8 | 28 |
| Fellowship | 18 | 8 | 26 |
| Giving | 22 | 10 | 32 |
| Pastoral Care | 25 | 13 | 38 |
| Events | 20 | 10 | 30 |
| Communications | 10 | 6 | 16 |
| Cross-Module | 10 | 0 | 10 |
| **TOTAL** | **210** | **77** | **287** |

---

## 📊 Test Coverage Summary

### Backend API Tests
- **Total**: 250+ tests
- **Completed**: 60 tests (24%)
- **Pattern**: REST Assured + H2 + Spring Boot Test
- **Idempotency**: Transaction rollback

### Frontend E2E Tests
- **Total**: 287 tests (210 E2E + 77 validation)
- **Completed**: 0 tests (0%)
- **Pattern**: Playwright + Page Object Model
- **Idempotency**: Isolated test data + cleanup

### Total Test Suite
- **Grand Total**: 537 tests
- **Current Coverage**: 60 tests (11%)
- **Target**: 100% coverage (537 tests)

---

## 🔍 Debugging Failed Tests

### Backend API Tests

1. **View detailed test output:**
   ```bash
   mvn verify -P api-tests -X
   ```

2. **Check test reports:**
   - Surefire (unit tests): `target/surefire-reports/`
   - Failsafe (integration tests): `target/failsafe-reports/`

3. **Run single test with debug:**
   ```bash
   mvn test -Dtest=MemberCrudIntegrationTest -Dmaven.surefire.debug
   # Connect debugger on port 5005
   ```

### Frontend E2E Tests

1. **Run with UI mode (best for debugging):**
   ```bash
   npx playwright test --ui
   ```

2. **Run with headed browser:**
   ```bash
   npx playwright test --headed
   ```

3. **Run in debug mode (step through):**
   ```bash
   npx playwright test --debug
   ```

4. **View screenshots/videos of failures:**
   - Location: `past-care-spring-frontend/test-results/`
   - Videos: Only saved on failure
   - Screenshots: Only saved on failure

5. **View detailed HTML report:**
   ```bash
   npx playwright show-report
   ```

---

## ⚙️ CI/CD Integration

### GitHub Actions Example

```yaml
name: Comprehensive Test Suite

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  api-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Setup JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
      - name: Run API Tests
        run: mvn verify -P api-tests

  e2e-tests:
    runs-on: ubuntu-latest
    needs: api-tests
    steps:
      - uses: actions/checkout@v3
      - name: Setup Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '18'
      - name: Install dependencies
        run: cd past-care-spring-frontend && npm install
      - name: Install Playwright
        run: cd past-care-spring-frontend && npx playwright install --with-deps
      - name: Run E2E Tests
        run: cd past-care-spring-frontend && npx playwright test
      - name: Upload test report
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: playwright-report
          path: past-care-spring-frontend/playwright-report/
```

---

## 🚀 Performance Benchmarks

### Expected Test Execution Times

| Test Type | Tests | Expected Duration |
|-----------|-------|-------------------|
| Backend Unit Tests | TBD | < 2 minutes |
| Backend API Integration | 250+ | < 10 minutes |
| Frontend E2E Tests | 287 | < 20 minutes |
| **Total (All Tests)** | **537+** | **< 32 minutes** |

### Optimization Tips

**Backend:**
- Use Maven parallel execution: `mvn -T 4 verify -P api-tests`
- H2 in-memory DB is already optimized
- Tests run in parallel at class level

**Frontend:**
- Playwright runs 6 workers by default
- Adjust workers: `npx playwright test --workers=8`
- On CI, use 1 worker for stability

---

## 📝 Test Naming Conventions

### Backend (JUnit 5)

```java
@Test
@DisplayName("Should create member with full profile and calculate profile completeness")
void shouldCreateMemberWithFullProfile() {
    // Given: Full member profile
    // When: Create member
    // Then: Verify member created with all fields
}
```

### Frontend (Playwright)

```typescript
test('Member Form - Empty first name → error NEXT TO first name field', async ({ page }) => {
  // Arrange
  await page.goto('/members/new');

  // Act
  await page.click('[data-testid="save-button"]');

  // Assert
  await expect(page.locator('[data-testid="firstName-error"]'))
    .toHaveText('First name is required');
});
```

---

## 🛡️ Test Idempotency Guarantees

### Backend API Tests
✅ **H2 in-memory database** - Fresh database for each test run
✅ **@Transactional** - Automatic rollback after each test method
✅ **Unique test data** - Random phone numbers, emails, names
✅ **No shared state** - Each test class gets fresh context

### Frontend E2E Tests
✅ **Isolated test data** - Timestamp-based unique identifiers
✅ **Cleanup hooks** - `test.afterEach()` deletes created resources
✅ **Parallel workers** - Separate browser contexts
✅ **No cross-test dependencies** - Tests can run in any order

---

## 📚 Additional Resources

- [Backend README](README.md) - Full backend documentation
- [Frontend README](past-care-spring-frontend/README.md) - Full frontend documentation
- [Test Suite Implementation Summary](TEST_SUITE_IMPLEMENTATION_SUMMARY.md) - Detailed test strategy
- [Deployment Plan](DEPLOYMENT_PLAN.md) - Production deployment guide

---

## 🆘 Troubleshooting

### "Tests are failing after running multiple times"
This should NOT happen (tests are idempotent). If it does:
1. Check for hardcoded test data (should use random/dynamic data)
2. Verify @Transactional is present on backend tests
3. Verify E2E cleanup hooks are running
4. Report as a bug

### "H2 database errors"
```bash
# Clean and rebuild
mvn clean
mvn verify -P api-tests
```

### "Playwright browser not installed"
```bash
cd past-care-spring-frontend
npx playwright install --with-deps
```

### "Port 4200 already in use"
```bash
# Kill process using port 4200
lsof -ti:4200 | xargs kill -9
```

---

**Last Updated:** 2025-12-29
**Test Suite Version:** 1.0
**Status:** Phase 1-2 Complete (60/537 tests)
