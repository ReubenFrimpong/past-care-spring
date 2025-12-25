# E2E Test Setup and Execution Guide

This guide explains how to set up and run the end-to-end (e2e) tests for the PastCare Spring application.

## Prerequisites

1. **MySQL Database** running on `localhost:3306`
2. **Node.js** and **npm** installed
3. **Java 17+** and **Maven** installed
4. **Angular CLI** installed globally: `npm install -g @angular/cli`

## Quick Start

Run the automated setup and test script:

```bash
# From the project root
./run-e2e-tests.sh
```

This script will:
1. Apply test seed data to the database
2. Start the Spring Boot backend
3. Start the Angular frontend
4. Run all e2e tests
5. Clean up processes on exit

## Manual Setup

If you prefer to run the steps manually:

### 1. Apply Test Seed Data

The test seed data creates test users, members, and other required data:

```bash
# From the pastcare-spring directory
mysql -u root -ppassword past-care-spring < src/test/resources/test-seed-data.sql
```

**Test Users Created:**

| Email | Password | Type | Status |
|-------|----------|------|--------|
| `approved.member@example.com` | `ApprovedPassword123!` | Portal User | APPROVED |
| `unverified@example.com` | `UnverifiedPassword123!` | Portal User | PENDING_VERIFICATION |
| `pending@example.com` | `PendingPassword123!` | Portal User | PENDING_APPROVAL |
| `testuser@example.com` | `password123` | Admin User | Active |

**Test Church:** ID 999, "Test Church E2E"

**Test Members:** John Smith, Jane Smith (spouse), Child Smith (child)

**Test Household:** "Smith Family" with John as head

### 2. Start the Backend

```bash
# Terminal 1 - From pastcare-spring directory
./mvnw spring-boot:run
```

Wait for the message: `Started PastcareSpringApplication in X seconds`

### 3. Start the Frontend

```bash
# Terminal 2 - From past-care-spring-frontend directory
npm start
```

Wait for the message: `Compiled successfully.`
The app will be available at `http://localhost:4200`

### 4. Run E2E Tests

```bash
# Terminal 3 - From past-care-spring-frontend directory

# Run all tests
npm run e2e

# Run specific test file
npx playwright test e2e/portal-login.spec.ts

# Run tests in headed mode (see browser)
npx playwright test --headed

# Run tests in debug mode
npx playwright test --debug

# Run only portal tests
npx playwright test e2e/portal-*.spec.ts

# Run with HTML report
npx playwright test --reporter=html
npx playwright show-report
```

## Test Categories

### Portal Tests (68 tests - Fixed ✅)
- `portal-login.spec.ts` - Login functionality (15 tests)
- `portal-registration.spec.ts` - User registration (11 tests)
- `portal-navigation.spec.ts` - Portal navigation and features (22 tests)
- `portal-auth-guard.spec.ts` - Authentication guards (20 tests)

### Main App Tests (Still need fixes)
- `households.spec.ts` - Household management
- `household-member-management.spec.ts` - Household member operations
- `members-page.spec.ts` - Members listing and search
- `members-form.spec.ts` - Member creation/editing
- `members-quick-add.spec.ts` - Quick member addition
- `lifecycle-events.spec.ts` - Lifecycle event tracking
- `communication-logs.spec.ts` - Communication logging
- `confidential-notes.spec.ts` - Confidential notes
- `parent-child-relationships.spec.ts` - Family relationships
- `spouse-linking.spec.ts` - Spouse relationships
- `tags.spec.ts` - Member tagging
- `saved-searches.spec.ts` - Saved search functionality
- `profile-completeness.spec.ts` - Profile completion tracking
- `international-support.spec.ts` - International features

## Cleaning Up Test Data

To remove test data from the database:

```bash
# From pastcare-spring directory
./clean-test-data.sh
```

Or manually:

```sql
DELETE FROM portal_users WHERE email LIKE '%@example.com';
DELETE FROM user WHERE email = 'testuser@example.com';
DELETE FROM households WHERE church_id = 999;
DELETE FROM member WHERE church_id = 999;
DELETE FROM church WHERE name = 'Test Church E2E';
```

## Troubleshooting

### Tests Fail with "Connection Refused"

**Problem:** Backend or frontend not running

**Solution:** Ensure both servers are started and listening:
- Backend: `http://localhost:8080`
- Frontend: `http://localhost:4200`

### Tests Fail with "User Not Found" or "Invalid Credentials"

**Problem:** Test seed data not applied

**Solution:** Re-run the seed data script:
```bash
mysql -u root -ppassword past-care-spring < src/test/resources/test-seed-data.sql
```

### Database Connection Error

**Problem:** MySQL not running or wrong credentials

**Solution:**
1. Check MySQL is running: `sudo systemctl status mysql`
2. Verify credentials in `src/main/resources/application.properties`
3. Update password in seed data commands if different

### Port Already in Use

**Problem:** Port 4200 or 8080 already in use

**Solution:**
```bash
# Find and kill processes on port 4200
lsof -ti:4200 | xargs kill -9

# Find and kill processes on port 8080
lsof -ti:8080 | xargs kill -9
```

### Tests Pass Locally But Fail in CI

**Problem:** Timing issues or environment differences

**Solution:**
- Increase timeouts in test files
- Ensure CI has proper test data setup
- Check browser versions match

## Test Configuration

### Playwright Configuration

Located at: `past-care-spring-frontend/playwright.config.ts`

Key settings:
- Base URL: `http://localhost:4200`
- Timeout: 30 seconds per test
- Retries: 2 on CI, 0 locally
- Browser: Chromium (can add Firefox, WebKit)

### Test Seed Data

Located at: `pastcare-spring/src/test/resources/test-seed-data.sql`

This file is idempotent - you can run it multiple times safely.

## Writing New Tests

When adding new e2e tests:

1. **Use the correct selectors:**
   - ID selectors: `#email`, `#password`
   - Class selectors: `.error-message`, `.stat-card`
   - PrimeNG components: `p-message[severity="error"]`

2. **Login helper for portal tests:**
   ```typescript
   async function loginAsPortalUser(page: any) {
     await page.goto('http://localhost:4200/portal/login?churchId=999');
     await page.locator('#email').fill('approved.member@example.com');
     await page.locator('#password').fill('ApprovedPassword123!');
     await page.locator('button[type="submit"]').click();
     await expect(page).toHaveURL(/.*portal\/home/, { timeout: 10000 });
   }
   ```

3. **Login helper for admin tests:**
   ```typescript
   async function loginAsAdmin(page: any) {
     await page.goto('http://localhost:4200/login');
     await page.fill('input[name="email"]', 'testuser@example.com');
     await page.fill('input[name="password"]', 'password123');
     await page.click('button[type="submit"]');
     await page.waitForLoadState('networkidle');
   }
   ```

4. **Always add churchId parameter for portal pages:**
   ```typescript
   await page.goto('http://localhost:4200/portal/login?churchId=999');
   await page.goto('http://localhost:4200/portal/register?churchId=999');
   ```

## CI/CD Integration

For GitHub Actions or similar CI:

```yaml
- name: Setup test database
  run: mysql -u root -ppassword past-care-spring < src/test/resources/test-seed-data.sql

- name: Start backend
  run: ./mvnw spring-boot:run &

- name: Wait for backend
  run: timeout 60 bash -c 'until curl -f http://localhost:8080/actuator/health; do sleep 2; done'

- name: Start frontend
  run: npm start &
  working-directory: past-care-spring-frontend

- name: Wait for frontend
  run: timeout 60 bash -c 'until curl -f http://localhost:4200; do sleep 2; done'

- name: Run e2e tests
  run: npm run e2e
  working-directory: past-care-spring-frontend
```

## Next Steps

1. ✅ Portal tests are fixed and ready to run
2. ⏳ Main app tests need updates to match actual component selectors
3. ⏳ Add more test coverage for edge cases
4. ⏳ Set up CI/CD pipeline with e2e tests

## Support

If you encounter issues:
1. Check this guide's troubleshooting section
2. Review test output for specific error messages
3. Check browser console in headed mode: `npx playwright test --headed`
4. Use debug mode: `npx playwright test --debug`
