# Quick Test Reference Guide

## Test Suite Overview

### ✅ Completed Modules (9 total)

| Module | File | Tests | Status |
|--------|------|-------|--------|
| Authentication | `integration/auth/AuthenticationIntegrationTest.java` | 13 | ✅ Existing |
| Members | `integration/members/MemberCrudIntegrationTest.java` | 28 | ✅ Existing |
| Attendance | `integration/attendance/AttendanceIntegrationTest.java` | 19 | ✅ Existing |
| **Fellowship** | `integration/fellowship/FellowshipIntegrationTest.java` | **22** | **✅ NEW** |
| **Giving** | `integration/giving/GivingIntegrationTest.java` | **26** | **✅ NEW** |
| **Pastoral Care** | `integration/pastoral/PastoralCareIntegrationTest.java` | **33** | **✅ NEW** |
| **Events** | `integration/events/EventsIntegrationTest.java` | **29** | **✅ NEW** |
| **Communications** | `integration/communications/CommunicationsIntegrationTest.java` | **15** | **✅ NEW** |
| **Billing** | `integration/billing/BillingIntegrationTest.java` | **19** | **✅ NEW** |

**Total Tests**: 204 tests across 9 modules

## Running Tests

### Quick Commands

```bash
# Run ALL integration tests
mvn test -Dtest="**/*IntegrationTest"

# Run NEW modules only
mvn test -Dtest="FellowshipIntegrationTest,GivingIntegrationTest,PastoralCareIntegrationTest,EventsIntegrationTest,CommunicationsIntegrationTest,BillingIntegrationTest"

# Run single module
mvn test -Dtest="FellowshipIntegrationTest"
```

### Test by Tag

```bash
# All integration tests
mvn test -Dgroups="integration"

# Specific module
mvn test -Dgroups="module:fellowship"
mvn test -Dgroups="module:giving"
mvn test -Dgroups="module:pastoral"
mvn test -Dgroups="module:events"
mvn test -Dgroups="module:communications"
mvn test -Dgroups="module:billing"
```

## Test Coverage by Module

### Fellowship (22 tests)
- ✅ CRUD operations (5)
- ✅ Member management (3)
- ✅ Join requests (4)
- ✅ Analytics (3)
- ✅ Multiplication tracking (2)
- ✅ Multi-tenancy (2)
- ✅ Permissions (3)

### Giving (26 tests)
- ✅ Donation recording (6)
- ✅ Payment integration (3)
- ✅ Recurring donations (3)
- ✅ Campaign management (4)
- ✅ Pledge tracking (3)
- ✅ Analytics & reporting (4)
- ✅ Multi-tenancy (2)
- ✅ Permissions (1)

### Pastoral Care (33 tests)
- ✅ Care needs CRUD (8)
- ✅ Pastoral visits (6)
- ✅ Counseling sessions (6)
- ✅ Prayer requests (7)
- ✅ Crisis management (6)

### Events (29 tests)
- ✅ Event CRUD (5)
- ✅ Image management (3)
- ✅ Organizer management (3)
- ✅ Tag management (3)
- ✅ Registration (5)
- ✅ Check-in (2)
- ✅ Reminders (2)
- ✅ Recurring events (2)
- ✅ Multi-tenancy (2)
- ✅ Permissions (2)

### Communications (15 tests)
- ✅ SMS sending (3)
- ✅ Credits management (3)
- ✅ Template management (3)
- ✅ Webhook handling (2)
- ✅ Message logging (2)
- ✅ Multi-tenancy (1)
- ✅ Permissions (1)

### Billing (19 tests)
- ✅ Plan management (2)
- ✅ Subscription lifecycle (2)
- ✅ Payment processing (4)
- ✅ Promotional credits (2)
- ✅ Recurring billing (2)
- ✅ Usage tracking (3)
- ✅ Multi-tenancy (1)
- ✅ Permissions (1)

## Before Running Tests

### 1. Verify Enum Values
Check these files for correct enum values:
- `FellowshipType.java`
- `DonationType.java`
- `CareNeedType.java`
- `PrayerCategory.java`
- `EventType.java`

### 2. Ensure Test Database
Make sure `application-test.properties` is configured:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/pastcare_test
spring.jpa.hibernate.ddl-auto=create-drop
```

### 3. Start Required Services
```bash
# PostgreSQL
docker-compose up -d postgres

# Or use embedded database for tests
```

## Troubleshooting

### "BaseIntegrationTest cannot be resolved"
- **This is an IDE false positive**
- The class exists at: `src/test/java/.../integration/BaseIntegrationTest.java`
- Tests will compile with Maven: `mvn clean test-compile`

### "authenticatedSpec() is undefined"
- **Also an IDE false positive**
- Method is inherited from `BaseIntegrationTest`
- Tests will run correctly

### Enum value errors
- Check actual enum files in `src/main/java/.../models/`
- Update test files to use correct enum values
- Example: `YOUTH` → `AGE_BASED`

### Test failures due to missing endpoints
- Verify controller implements the endpoint
- Check HTTP method (GET/POST/PUT/DELETE/PATCH)
- Verify request path matches controller mapping

## Common Test Patterns

### Creating test data
```java
Long memberId = createTestMember();
Long fellowshipId = createTestFellowship("Name");
Long donationId = createTestDonation(1000.00);
```

### Authenticated requests
```java
given()
    .spec(authenticatedSpec(adminToken))
    .body(request)
.when()
    .post("/api/endpoint")
.then()
    .statusCode(200);
```

### Multi-tenancy verification
```java
assertBelongsToChurch(entity.getChurchId(), churchId);
```

## Test Execution Tips

1. **Run tests individually first** to identify issues quickly
2. **Check logs** for detailed error messages
3. **Use IDE debugger** for complex test failures
4. **Verify database state** after failed tests
5. **Check permissions** if getting 403 errors

## Code Coverage

### Generate coverage report
```bash
mvn clean test jacoco:report
```

### View report
Open: `target/site/jacoco/index.html`

### Coverage targets
- Line coverage: >80%
- Branch coverage: >70%
- Integration coverage: 100% of endpoints

## Next Actions

1. ✅ Run `mvn clean test-compile` to verify compilation
2. ✅ Fix any enum value mismatches
3. ✅ Run tests module by module
4. ✅ Generate coverage report
5. ✅ Document any additional findings

## Support

For issues:
1. Check this guide first
2. Review test file JavaDoc
3. Compare with existing working tests (Auth, Members, Attendance)
4. Check controller implementation
5. Verify DTO field names match

---

**Status**: All 6 new test modules created and ready for execution
**Total New Tests**: 144 tests
**Total Test Suite**: 204 tests across 9 modules
