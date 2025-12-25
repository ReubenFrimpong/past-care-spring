# E2E Testing Quick Reference

## Quick Commands

### Run Everything Automatically
```bash
cd /home/reuben/Documents/workspace/pastcare-spring
./run-e2e-tests.sh
```

### Run Specific Test File
```bash
./run-e2e-tests.sh e2e/portal-login.spec.ts
```

### Run Tests in Headed Mode (See Browser)
```bash
./run-e2e-tests.sh e2e/portal-login.spec.ts --headed
```

### Run Tests in Debug Mode
```bash
./run-e2e-tests.sh e2e/portal-login.spec.ts --debug
```

### Clean Test Data
```bash
./clean-test-data.sh
```

## Manual Steps

### 1. Setup Test Data Only
```bash
mysql -u root -ppassword past-care-spring < src/test/resources/test-seed-data.sql
```

### 2. Start Backend Only
```bash
cd /home/reuben/Documents/workspace/pastcare-spring
./mvnw spring-boot:run
```

### 3. Start Frontend Only
```bash
cd /home/reuben/Documents/workspace/past-care-spring-frontend
npm start
```

### 4. Run Tests Only (requires backend + frontend running)
```bash
cd /home/reuben/Documents/workspace/past-care-spring-frontend

# All portal tests
npx playwright test e2e/portal-*.spec.ts

# Specific test
npx playwright test e2e/portal-login.spec.ts

# With options
npx playwright test --headed                    # See browser
npx playwright test --debug                     # Debug mode
npx playwright test --reporter=html             # HTML report
npx playwright show-report                      # View report
```

## Test Credentials

### Portal Users
| Email | Password | Status |
|-------|----------|--------|
| `approved.member@example.com` | `ApprovedPassword123!` | ✅ Approved |
| `unverified@example.com` | `UnverifiedPassword123!` | ⏳ Pending Verification |
| `pending@example.com` | `PendingPassword123!` | ⏳ Pending Approval |

### Admin User
| Email | Password |
|-------|----------|
| `testuser@example.com` | `password123` |

## Test Status

### ✅ Fixed (68 tests)
- `portal-login.spec.ts` - 15 tests
- `portal-registration.spec.ts` - 11 tests
- `portal-navigation.spec.ts` - 22 tests
- `portal-auth-guard.spec.ts` - 20 tests

### ⏳ Needs Fixes (226 tests)
- `households.spec.ts`
- `household-member-management.spec.ts`
- `members-page.spec.ts`
- `members-form.spec.ts`
- `members-quick-add.spec.ts`
- `lifecycle-events.spec.ts`
- `communication-logs.spec.ts`
- `confidential-notes.spec.ts`
- `parent-child-relationships.spec.ts`
- `spouse-linking.spec.ts`
- `tags.spec.ts`
- `saved-searches.spec.ts`
- `profile-completeness.spec.ts`
- `international-support.spec.ts`

## Common Issues

### "Connection Refused"
```bash
# Check if services are running
curl http://localhost:8080/actuator/health
curl http://localhost:4200
```

### "User Not Found"
```bash
# Re-apply test data
mysql -u root -ppassword past-care-spring < src/test/resources/test-seed-data.sql
```

### "Port Already in Use"
```bash
# Kill processes on ports
lsof -ti:8080 | xargs kill -9  # Backend
lsof -ti:4200 | xargs kill -9  # Frontend
```

### "Database Connection Error"
```bash
# Check MySQL is running
sudo systemctl status mysql

# Start if needed
sudo systemctl start mysql
```

## Playwright Commands

```bash
# Run tests matching pattern
npx playwright test --grep "login"

# Run tests NOT matching pattern
npx playwright test --grep-invert "skip"

# Run only failed tests from last run
npx playwright test --last-failed

# Update snapshots
npx playwright test --update-snapshots

# Generate code from browser actions
npx playwright codegen http://localhost:4200/portal/login?churchId=999
```

## File Locations

```
pastcare-spring/
├── E2E_TEST_SETUP.md              # Full setup guide
├── E2E_QUICK_REFERENCE.md         # This file
├── run-e2e-tests.sh               # Automated test runner
├── clean-test-data.sh             # Clean test data
└── src/test/resources/
    └── test-seed-data.sql         # Test data SQL

past-care-spring-frontend/
├── e2e/                           # Test files
│   ├── portal-login.spec.ts
│   ├── portal-registration.spec.ts
│   ├── portal-navigation.spec.ts
│   └── portal-auth-guard.spec.ts
└── playwright.config.ts           # Playwright config
```

## Useful Playwright Selectors

```typescript
// ID selectors (preferred)
page.locator('#email')
page.locator('#password')

// Class selectors
page.locator('.error-message')
page.locator('.stat-card')

// PrimeNG components
page.locator('p-message[severity="error"]')
page.locator('p-dialog')
page.locator('p-button')

// Text content
page.locator('button:has-text("Login")')
page.locator('h2:has-text("Welcome")')

// Form controls
page.locator('input[type="email"]')
page.locator('button[type="submit"]')
```

## Tips

1. **Always include churchId=999** for portal pages
2. **Use `#password` selector** (not `input[formControlName="password"]`)
3. **Use `div.error-message`** for validation errors (not `small.p-error`)
4. **Add timeouts** for async operations: `{ timeout: 10000 }`
5. **Skip flaky tests** with `test.skip()` and add TODO comment
6. **Run in headed mode** when debugging: `--headed`
7. **Use codegen** to generate selectors: `npx playwright codegen`
