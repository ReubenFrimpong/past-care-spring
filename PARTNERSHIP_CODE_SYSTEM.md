# Partnership Code System - Implementation Summary

**Created**: 2025-12-30
**Purpose**: Allow churches to apply partnership codes to receive grace periods instead of auto-granting on registration

---

## Overview

The partnership code system provides a flexible way to grant grace periods to churches. Instead of automatically giving all churches a trial period, they must apply a valid partnership code.

This gives you control over:
- Who gets grace periods
- How long the grace period lasts
- When codes expire
- How many times a code can be used

---

## Components Created

### 1. Database Schema

**File**: `src/main/resources/db/migration/V68__create_partnership_codes_table.sql`

```sql
CREATE TABLE partnership_codes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL,
    grace_period_days INT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    expires_at DATETIME NULL,
    max_uses INT NULL,
    current_uses INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);
```

**Default Codes Created**:
- `PARTNER2025` - 30 days, expires Dec 31 2025
- `TRIAL14` - 14 days, no expiration, unlimited uses
- `LAUNCH2025` - 60 days, expires June 30 2025, max 100 uses

### 2. Model

**File**: `src/main/java/com/reuben/pastcare_spring/models/PartnershipCode.java`

Key features:
- `isValid()` method checks if code can be used
- `incrementUsage()` tracks how many times code is used
- Auto-timestamps on create/update

### 3. Repository

**File**: `src/main/java/com/reuben/pastcare_spring/repositories/PartnershipCodeRepository.java`

Methods:
- `findByCodeIgnoreCase(String code)` - Case-insensitive lookup
- `existsByCodeIgnoreCase(String code)` - Check existence

### 4. Service

**File**: `src/main/java/com/reuben/pastcare_spring/services/PartnershipCodeService.java`

**Methods**:
```java
// Apply code to grant grace period
ChurchSubscription applyPartnershipCode(Long churchId, String code)

// Validate code without applying
PartnershipCode validateCode(String code)

// Check if church has active grace period
boolean hasActiveGracePeriod(Long churchId)
```

**Validation Rules**:
- Code must exist
- Code must be active
- Code must not be expired
- Code must not have reached max uses
- Church must not already have paid subscription

### 5. Controller

**File**: `src/main/java/com/reuben/pastcare_spring/controllers/PartnershipCodeController.java`

**Endpoints**:

#### Apply Partnership Code
```http
POST /api/partnership-codes/apply
Authorization: Bearer <token>
Content-Type: application/json

{
  "code": "PARTNER2025"
}

Response (Success):
{
  "message": "Partnership code applied successfully",
  "gracePeriodEnd": "2025-01-29",
  "status": "ACTIVE"
}

Response (Error):
{
  "error": "Invalid partnership code"
}
```

#### Validate Code
```http
GET /api/partnership-codes/validate?code=PARTNER2025

Response:
{
  "valid": true,
  "description": "General partnership code for 2025",
  "gracePeriodDays": 30
}
```

#### Check Grace Period Status
```http
GET /api/partnership-codes/grace-period/status
Authorization: Bearer <token>

Response:
{
  "hasActiveGracePeriod": true
}
```

---

## How It Works

### Registration Flow

1. **Church Registers** → Creates church + admin user
2. **Redirected to Subscription Page** → Must choose plan OR apply partnership code
3. **User Enters Partnership Code** → `POST /api/partnership-codes/apply`
4. **System Validates Code** → Checks if valid, not expired, not maxed out
5. **Grace Period Granted** → Subscription status set to ACTIVE with end date
6. **User Can Access Dashboard** → `subscriptionGuard` sees ACTIVE status

### Grace Period vs Paid Subscription

**Grace Period**:
- `status = "ACTIVE"`
- `paystackSubscriptionCode = null`
- `currentPeriodEnd` = grace period end date
- No payment required

**Paid Subscription**:
- `status = "ACTIVE"`
- `paystackSubscriptionCode` = "SUB_xxxxx"
- `currentPeriodEnd` = next billing date
- Recurring payments

---

## Frontend Integration

### Update Payment/Subscription Page

**File**: `/home/reuben/Documents/workspace/past-care-spring-frontend/src/app/payment-setup-page/payment-setup-page.html`

Add partnership code section:

```html
<!-- Partnership Code Section -->
<div class="partnership-code-section">
  <h3>Have a Partnership Code?</h3>
  <p>Enter your code to get free access for a limited time</p>

  <div class="code-input-group">
    <input
      type="text"
      placeholder="Enter code (e.g., PARTNER2025)"
      [(ngModel)]="partnershipCode"
      data-testid="partnership-code-input"
    />
    <button
      (click)="applyPartnershipCode()"
      [disabled]="!partnershipCode || isApplying"
      data-testid="apply-code-button"
    >
      {{ isApplying ? 'Applying...' : 'Apply Code' }}
    </button>
  </div>

  @if (codeError) {
    <div class="error-message" data-testid="code-error">
      {{ codeError }}
    </div>
  }

  @if (codeSuccess) {
    <div class="success-message" data-testid="code-success">
      {{ codeSuccess }}
      <a routerLink="/dashboard">Go to Dashboard →</a>
    </div>
  }
</div>

<div class="divider">OR</div>

<!-- Existing payment plans section -->
```

### Update TypeScript

**File**: `/home/reuben/Documents/workspace/past-care-spring-frontend/src/app/payment-setup-page/payment-setup-page.ts`

```typescript
partnershipCode: string = '';
isApplying: boolean = false;
codeError: string = '';
codeSuccess: string = '';

async applyPartnershipCode() {
  this.isApplying = true;
  this.codeError = '';
  this.codeSuccess = '';

  try {
    const response = await this.http.post('/api/partnership-codes/apply', {
      code: this.partnershipCode
    }).toPromise();

    this.codeSuccess = `Code applied! You have ${response.gracePeriodDays} days of free access.`;

    // Redirect to dashboard after 2 seconds
    setTimeout(() => {
      this.router.navigate(['/dashboard']);
    }, 2000);
  } catch (error: any) {
    this.codeError = error.error?.error || 'Invalid code';
  } finally {
    this.isApplying = false;
  }
}
```

---

## Testing

### Manual Testing

1. **Start Backend**:
   ```bash
   ./mvnw spring-boot:run -Dmaven.test.skip=true
   ```

2. **Register a New Church**:
   - Go to http://localhost:4200/register
   - Fill in church and admin details
   - Submit → Redirected to `/subscription/select`

3. **Apply Partnership Code**:
   - Enter code: `TRIAL14`
   - Click "Apply Code"
   - Should see success message
   - Should be able to access dashboard

4. **Verify in Database**:
   ```sql
   SELECT * FROM church_subscriptions WHERE church_id = <your_church_id>;
   -- Should show status = 'ACTIVE' and current_period_end set

   SELECT * FROM partnership_codes WHERE code = 'TRIAL14';
   -- Should show current_uses incremented
   ```

### cURL Testing

```bash
# Register church first, then login to get token
TOKEN="your_jwt_token"

# Apply partnership code
curl -X POST http://localhost:8080/api/partnership-codes/apply \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"code": "TRIAL14"}'

# Validate code (no auth required)
curl http://localhost:8080/api/partnership-codes/validate?code=TRIAL14

# Check grace period status
curl http://localhost:8080/api/partnership-codes/grace-period/status \
  -H "Authorization: Bearer $TOKEN"
```

---

## Managing Partnership Codes

### Create New Codes (SQL)

```sql
INSERT INTO partnership_codes
(code, description, grace_period_days, is_active, expires_at, max_uses, current_uses, created_at, updated_at)
VALUES
('EVENT2025', 'Conference attendees', 45, TRUE, '2025-12-31 23:59:59', 500, 0, NOW(), NOW());
```

### Deactivate Code

```sql
UPDATE partnership_codes SET is_active = FALSE WHERE code = 'OLDCODE';
```

### Check Usage

```sql
SELECT code, description, current_uses, max_uses,
       CASE
         WHEN max_uses IS NULL THEN 'Unlimited'
         ELSE CONCAT(current_uses, '/', max_uses)
       END as usage
FROM partnership_codes
WHERE is_active = TRUE
ORDER BY current_uses DESC;
```

---

## E2E Test Updates

### Update Test Fixtures

**File**: `e2e/fixtures/test-data.ts`

```typescript
/**
 * Get default partnership code for testing
 */
static getTestPartnershipCode(): string {
  return 'TRIAL14'; // Unlimited uses, no expiration
}
```

### Update Registration Tests

**File**: `e2e/tests/critical-path-01-church-registration.spec.ts`

```typescript
test('US-001.1: Complete church registration and apply partnership code', async ({ page }) => {
  // Given: Test data
  const churchData = TestData.generateChurch();
  const adminData = TestData.generateAdmin();

  // When: User registers
  await registrationPage.registerChurch(churchData, adminData);

  // Then: Redirected to subscription selection
  await expect(page).toHaveURL(/.*subscription\/select/);

  // When: User applies partnership code
  await page.fill('[data-testid="partnership-code-input"]', 'TRIAL14');
  await page.click('[data-testid="apply-code-button"]');

  // Then: Success message appears
  await expect(page.locator('[data-testid="code-success"]')).toBeVisible();

  // And: Can access dashboard
  await page.goto('/dashboard');
  await expect(page).toHaveURL(/.*dashboard/);
});
```

---

## Benefits

1. **Control**: You decide who gets grace periods
2. **Flexibility**: Different codes for different partners/events
3. **Tracking**: See how many churches use each code
4. **Expiration**: Codes can have time limits
5. **Limits**: Codes can have usage limits
6. **Marketing**: Use codes for promotions and campaigns

---

## Next Steps

1. ✅ Backend partnership code system created
2. 📋 Add frontend UI for partnership code input
3. 📋 Update E2E tests to use partnership codes
4. 📋 Add admin panel to manage codes
5. 📋 Add grace period banner to dashboard
6. 📋 Add notifications before grace period expires

---

## Files Modified/Created

### Created:
- `src/main/java/com/reuben/pastcare_spring/models/PartnershipCode.java`
- `src/main/java/com/reuben/pastcare_spring/repositories/PartnershipCodeRepository.java`
- `src/main/java/com/reuben/pastcare_spring/services/PartnershipCodeService.java`
- `src/main/java/com/reuben/pastcare_spring/controllers/PartnershipCodeController.java`
- `src/main/resources/db/migration/V68__create_partnership_codes_table.sql`
- `src/main/resources/db/migration/V69__add_partnership_code_to_subscriptions.sql`

### To Modify:
- Frontend payment setup page (add partnership code UI)
- E2E tests (use partnership codes instead of FREE plan)
- Dashboard (add grace period banner)

---

**Status**: Backend Complete ✅ | Frontend Pending 📋 | Tests Pending 📋
