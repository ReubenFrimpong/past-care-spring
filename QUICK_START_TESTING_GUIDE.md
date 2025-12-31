# Quick Start Guide: Continue Testing Implementation

## Step 1: Verify Current Tests Work (2 minutes)

```bash
cd /home/reuben/Documents/workspace/pastcare-spring

# Rebuild project to fix IDE errors
mvn clean compile test-compile

# Run a single test to verify setup
mvn test -Dtest=MemberCrudIntegrationTest

# Expected output: All 20 tests should pass ✅
```

## Step 2: Review What's Been Created (5 minutes)

**Read these files in order**:
1. `TESTING_IMPLEMENTATION_COMPLETE.md` - Overview of what's done
2. `TEST_SUITE_IMPLEMENTATION_SUMMARY.md` - Detailed guide for remaining work
3. `src/test/java/com/reuben/pastcare_spring/integration/members/MemberCrudIntegrationTest.java` - Example of excellent test structure

## Step 3: Create Your Next Test File (30 minutes)

**Let's implement Fellowship Tests together**:

```bash
# Create the file
touch src/test/java/com/reuben/pastcare_spring/integration/fellowship/FellowshipIntegrationTest.java
```

**Copy this template** (based on GivingIntegrationTestTemplate):

```java
package com.reuben.pastcare_spring.integration.fellowship;

import com.reuben.pastcare_spring.integration.BaseIntegrationTest;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@Tag("integration")
@Tag("module:fellowship")
@DisplayName("Fellowship Integration Tests")
@Transactional
class FellowshipIntegrationTest extends BaseIntegrationTest {

    private Long churchId;
    private String adminToken;

    @BeforeEach
    void setUp() {
        churchId = createTestChurch();
        adminToken = getAdminToken(churchId);
    }

    @Nested
    @DisplayName("Fellowship CRUD Tests")
    class CrudTests {

        @Test
        @DisplayName("Should create fellowship")
        void shouldCreateFellowship() {
            // Given: Fellowship request
            Long leaderId = createTestMember();
            String payload = """
                {
                    "name": "Youth Fellowship",
                    "type": "YOUTH",
                    "leaderId": %d,
                    "maxCapacity": 50
                }
                """.formatted(leaderId);

            // When: Create fellowship
            given()
                .spec(authenticatedSpec(adminToken))
                .body(payload)
            .when()
                .post("/api/fellowships")
            .then()
                .statusCode(201)
                .body("name", equalTo("Youth Fellowship"))
                .body("type", equalTo("YOUTH"))
                .body("leaderId", equalTo(leaderId.intValue()));
        }

        @Test
        @DisplayName("Should get fellowship by ID")
        void shouldGetFellowshipById() {
            // Given: Existing fellowship
            Long fellowshipId = createTestFellowship();

            // When: Get by ID
            given()
                .spec(authenticatedSpec(adminToken))
            .when()
                .get("/api/fellowships/" + fellowshipId)
            .then()
                .statusCode(200)
                .body("id", equalTo(fellowshipId.intValue()));
        }

        // TODO: Add update test
        // TODO: Add delete test
        // TODO: Add list test
    }

    @Nested
    @DisplayName("Join Request Tests")
    class JoinRequestTests {

        @Test
        @DisplayName("Should submit join request")
        void shouldSubmitJoinRequest() {
            // Given: Fellowship and member
            Long fellowshipId = createTestFellowship();
            Long memberId = createTestMember();

            String payload = """
                {
                    "fellowshipId": %d,
                    "memberId": %d,
                    "message": "I want to join"
                }
                """.formatted(fellowshipId, memberId);

            // When: Submit request
            given()
                .spec(authenticatedSpec(adminToken))
                .body(payload)
            .when()
                .post("/api/fellowships/join-requests")
            .then()
                .statusCode(201)
                .body("status", equalTo("PENDING"));
        }

        // TODO: Add approve request test
        // TODO: Add reject request test
    }

    @Nested
    @DisplayName("Multi-Tenancy Tests")
    class MultiTenancyTests {

        @Test
        @DisplayName("Should isolate fellowships by church")
        void shouldIsolateFellowshipsByChurch() {
            // Given: Two churches
            Long church1 = createTestChurch("Church 1");
            Long church2 = createTestChurch("Church 2");

            // TODO: Create fellowships in both churches
            // TODO: Verify isolation
        }
    }

    // Helper method
    private Long createTestFellowship() {
        Long leaderId = createTestMember();
        String payload = """
            {
                "name": "Test Fellowship",
                "type": "GENERAL",
                "leaderId": %d
            }
            """.formatted(leaderId);

        return Long.valueOf(given().spec(authenticatedSpec(adminToken))
            .body(payload)
            .post("/api/fellowships")
            .jsonPath()
            .getInt("id"));
    }

    private Long createTestMember() {
        String payload = """
            {
                "firstName": "Test",
                "lastName": "Member",
                "sex": "male",
                "phoneNumber": "+23324400%05d",
                "maritalStatus": "single"
            }
            """.formatted((int)(Math.random() * 100000));

        return Long.valueOf(given().spec(authenticatedSpec(adminToken))
            .body(payload)
            .post("/api/members")
            .jsonPath()
            .getInt("id"));
    }
}
```

## Step 4: Run Your New Test

```bash
# Run the fellowship test
mvn test -Dtest=FellowshipIntegrationTest

# If tests pass, continue adding more tests!
```

## Step 5: Complete the Fellowship Tests (3-4 hours)

Add these remaining tests to `FellowshipIntegrationTest.java`:

**CRUD Tests (2 more)**:
- `shouldUpdateFellowship()`
- `shouldDeleteFellowship()`

**Join Request Tests (2 more)**:
- `shouldApprovejoinRequest()`
- `shouldRejectJoinRequest()`

**Member Management Tests (3)**:
- `shouldAddMemberToFellowship()`
- `shouldRemoveMemberFromFellowship()`
- `shouldListFellowshipMembers()`

**Analytics Tests (3)**:
- `shouldGetFellowshipHealthMetrics()`
- `shouldGetGrowthRate()`
- `shouldGetRetentionRate()`

**Multiplication Tests (2)**:
- `shouldRecordFellowshipMultiplication()`
- `shouldLinkParentChildFellowships()`

**Permission Tests (3)**:
- `adminShouldCreateFellowship()`
- `fellowshipLeaderShouldManageOwnFellowship()`
- `memberShouldNotCreateFellowship()`

**Total: 22 tests for Fellowship module ✅**

## Step 6: Repeat for Remaining Modules

Follow the same pattern for:
1. ✅ Fellowship (22 tests) - Do first
2. Pastoral Care (33 tests) - Split into 4 files
3. Events (29 tests)
4. Communications (15 tests)
5. Billing (19 tests)

**Reference**: Use `GivingIntegrationTestTemplate.java` as your guide!

## Step 7: Start E2E Tests

Once API tests are complete:

```bash
cd past-care-spring-frontend

# Install Playwright
npm install -D @playwright/test
npx playwright install

# Create test structure
mkdir -p e2e/{pages,tests,fixtures}

# Create first test
touch e2e/tests/01-auth.spec.ts
```

**First E2E Test Example**:

```typescript
// e2e/tests/01-auth.spec.ts
import { test, expect } from '@playwright/test';

test.describe('Authentication', () => {
  test('should login with valid credentials', async ({ page }) => {
    await page.goto('http://localhost:4200/login');

    await page.fill('[data-testid="email-input"]', 'admin@test.com');
    await page.fill('[data-testid="password-input"]', 'Password@123');
    await page.click('[data-testid="login-btn"]');

    // Should redirect to dashboard
    await expect(page).toHaveURL(/.*dashboard/);
    await expect(page.locator('h1')).toContainText('Dashboard');
  });

  test('should show error for invalid credentials', async ({ page }) => {
    await page.goto('http://localhost:4200/login');

    await page.fill('[data-testid="email-input"]', 'wrong@test.com');
    await page.fill('[data-testid="password-input"]', 'wrong');
    await page.click('[data-testid="login-btn"]');

    // Should show error message
    await expect(page.locator('[data-testid="error-message"]'))
      .toBeVisible();
    await expect(page.locator('[data-testid="error-message"]'))
      .toContainText('Invalid credentials');
  });
});
```

## Useful Commands

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=FellowshipIntegrationTest

# Run tests by tag
mvn test -Dgroups="module:fellowship"

# Run with coverage report
mvn clean test jacoco:report
# View: target/site/jacoco/index.html

# Run E2E tests
cd past-care-spring-frontend
npx playwright test

# Run E2E with UI (debugging)
npx playwright test --ui

# Run specific E2E test
npx playwright test e2e/tests/01-auth.spec.ts
```

## Test Writing Checklist

For every module test file, ensure you have:
- [ ] `@Nested` class for CRUD operations
- [ ] `@Nested` class for Multi-Tenancy tests
- [ ] `@Nested` class for Permission tests
- [ ] Helper methods at bottom of file
- [ ] All tests use `authenticatedSpec(token)`
- [ ] All tests verify multi-tenancy with `assertBelongsToChurch()`
- [ ] Descriptive `@DisplayName` annotations
- [ ] Given/When/Then comments in tests

## Getting Help

**Check these resources**:
1. `MemberCrudIntegrationTest.java` - Best example of test structure
2. `GivingIntegrationTestTemplate.java` - Comprehensive template
3. `TEST_SUITE_IMPLEMENTATION_SUMMARY.md` - Detailed requirements
4. `BaseIntegrationTest.java` - All available helper methods

**Common Issues**:
- **Import errors**: Run `mvn clean compile test-compile`
- **Test fails**: Check test data setup in `@BeforeEach`
- **401 Unauthorized**: Verify token generation in test
- **404 Not Found**: Check endpoint URL matches controller

## Progress Tracking

**Current Status**:
- ✅ Phase 1: Infrastructure (100%)
- ✅ Phase 2: Core Modules - 60 tests (100%)
- ⏳ Phase 3-5: Remaining APIs - 144 tests (0%)
- ⏳ Phase 6-8: E2E Tests - 200+ tests (0%)

**Next Milestone**: Complete Fellowship tests (22 tests)
**After That**: Pastoral Care tests (33 tests)

---

**Ready to code? Start with Fellowship tests! 🚀**

Follow Step 3 above to create your first test file, then run it with `mvn test -Dtest=FellowshipIntegrationTest`.

Good luck! The patterns are all established - just follow the examples and you'll have comprehensive test coverage in no time.
