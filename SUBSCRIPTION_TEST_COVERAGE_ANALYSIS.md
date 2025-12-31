# Subscription Test Coverage Analysis

**Date:** 2025-12-31
**Requested By:** User
**Status:** ⚠️ PARTIAL COVERAGE - GAPS IDENTIFIED

## User's Three Critical Scenarios

### Scenario 1: Paid Subscriber Access ✅ COVERED
**Requirement:** "A paid subscriber sees what he must see"

**Current Test Coverage:**

#### Backend Tests (SubscriptionFilterTest.java)
- ✅ `shouldAllowAccessWithActiveSubscription()` - Lines 160-166
  - Verifies active subscription users can access `/api/dashboard/stats`
  - Confirms filterChain.doFilter() is called
  - No 402 Payment Required error

- ✅ `shouldAllowAccessWithPromotionalCredits()` - Lines 194-208
  - Verifies users with free months can access dashboard
  - Works even with SUSPENDED status if promotional credits exist

#### E2E Tests (subscription-access.spec.ts)
- ✅ `should allow dashboard access with active subscription` - Lines 20-32
  - Tests full login → dashboard flow
  - Verifies dashboard UI is visible
  - Confirms NO subscription warning banner shown

- ✅ `should allow billing access regardless of subscription status` - Lines 78-93
  - Ensures billing page always accessible

#### API Tests (subscription-access.spec.ts)
- ✅ `should allow billing endpoints with any subscription status` - Lines 264-284
  - Confirms `/api/billing/plans` returns 200 OK for active users

**✅ VERDICT: FULLY COVERED**

---

### Scenario 2: Due Subscriber Sees Only Renew Component ⚠️ PARTIALLY COVERED
**Requirement:** "A due subscriber sees only the renew component and nothing else"

**Current Test Coverage:**

#### Backend Tests
- ✅ `shouldBlockAccessWithSuspendedSubscription()` - Lines 211-234
  - Blocks access to `/api/dashboard/stats`
  - Returns 402 Payment Required
  - Error message: "Your subscription has been suspended"

- ✅ `shouldBlockAccessWithCanceledSubscription()` - Lines 237-258
  - Blocks canceled subscriptions
  - Returns appropriate error

- ✅ `shouldBlockAllProtectedEndpointsWithInactiveSubscription()` - Lines 325-358
  - Tests ALL protected endpoints are blocked
  - Covers: dashboard, members, attendance, events, donations, fellowships, users, reports

#### E2E Tests
- ⚠️ `should redirect to billing with suspended subscription` - Lines 34-45
  - Redirects to `/billing?subscription_required`
  - Sees billing page heading
  - **GAP:** Doesn't verify ONLY billing content shown

- ⚠️ `should redirect to billing with canceled subscription` - Lines 47-56
  - Redirects to billing with CANCELED status
  - **GAP:** Doesn't verify all other UI is hidden

- ⚠️ `should block API calls with expired subscription` - Lines 95-123
  - Listens for 402 errors on API calls
  - Redirects to billing
  - **GAP:** Doesn't verify UI restrictions

#### Frontend Guard Tests
- ⚠️ **MISSING:** No test verifying subscription guard blocks route navigation
- ⚠️ **MISSING:** No test verifying sidenav/navigation is restricted
- ⚠️ **MISSING:** No test verifying only billing/renew components are shown

**⚠️ VERDICT: PARTIALLY COVERED - UI RESTRICTION TESTS MISSING**

**What's Tested:**
- ✅ Backend blocks API calls (402 error)
- ✅ Frontend redirects to billing page
- ✅ Billing page is accessible

**What's NOT Tested:**
- ❌ Sidenav is hidden or shows only "Billing" link
- ❌ User cannot manually navigate to `/dashboard`, `/members`, etc.
- ❌ Only "Renew Subscription" button/component is visible
- ❌ All other features/menus are disabled/hidden
- ❌ User sees clear "suspended" status message

---

### Scenario 3: Data Deletion After 30 Days ❌ NOT IMPLEMENTED
**Requirement:** "A due subscriber who doesn't renew after 30 days gets their data completely removed from the system and the portal admin has the option to extend the duration of the removal of the data"

**Current Implementation:**

#### What Exists:
1. **Grace Period System** (ChurchSubscription.java:151-154)
   ```java
   private Integer gracePeriodDays = 0; // Default: no automatic grace period
   ```
   - Default: 0 days (no automatic grace)
   - SUPERADMIN can grant grace period via API

2. **Suspension Job** (ScheduledTasks.java:119-129)
   ```java
   @Scheduled(cron = "0 0 3 * * *", zone = "UTC")
   public void suspendPastDueSubscriptions()
   ```
   - Runs daily at 3:00 AM
   - Calls `billingService.suspendPastDueSubscriptions()`
   - Suspends subscriptions past grace period

3. **Weekly Cleanup Stub** (ScheduledTasks.java:69-85)
   ```java
   @Scheduled(cron = "0 0 2 * * SUN", zone = "UTC")
   public void weeklyCleanup() {
       // TODO: Future cleanup tasks
       // - Remove old soft-deleted records (older than 90 days)
   }
   ```
   - Placeholder only - no actual deletion logic

#### What's MISSING:

1. ❌ **30-Day Deletion Timer**
   - No mechanism to track "days since suspension"
   - No field like `suspendedAt` or `dataRetentionEndDate`

2. ❌ **Automated Data Deletion Service**
   - No scheduled job to delete church data after 30 days
   - No `deleteChurchData()` method
   - No cascade deletion of related entities

3. ❌ **SUPERADMIN Extension Interface**
   - No API endpoint to extend retention period
   - No UI for SUPERADMIN to manage retention
   - No `dataRetentionExtensionDays` field

4. ❌ **Data Retention Warning System**
   - No notification to church before deletion (e.g., 7 days warning)
   - No "days remaining" indicator in UI
   - No email alerts about pending deletion

5. ❌ **Soft Delete vs Hard Delete**
   - No `deleted_at` timestamp
   - No soft delete mechanism
   - No way to restore data within window

6. ❌ **Data Export Before Deletion**
   - No option to export church data before deletion
   - No automatic backup/archive

**❌ VERDICT: NOT IMPLEMENTED - CRITICAL FEATURE MISSING**

---

## Summary Table

| Scenario | Backend API | Frontend UI | E2E Tests | Status |
|----------|-------------|-------------|-----------|---------|
| **1. Paid subscriber access** | ✅ Full | ✅ Full | ✅ Full | ✅ COMPLETE |
| **2. Due subscriber sees only renew** | ✅ Full | ⚠️ Partial | ⚠️ Partial | ⚠️ NEEDS UI TESTS |
| **3. Data deletion after 30 days** | ❌ None | ❌ None | ❌ None | ❌ NOT IMPLEMENTED |

---

## Detailed Gap Analysis

### Gap 1: UI Restriction Tests (Scenario 2)

**Missing Test Cases:**

1. **Frontend Route Guard Test**
   ```typescript
   test('should block manual navigation to protected routes with suspended subscription', async ({ page }) => {
     // Login with suspended account
     // Try to navigate to /dashboard, /members, /events
     // Verify redirected to /billing each time
   });
   ```

2. **Sidenav Restriction Test**
   ```typescript
   test('should hide all sidenav items except billing for suspended subscription', async ({ page }) => {
     // Login with suspended account
     // Check sidenav only shows "Billing" link
     // Verify Members, Events, etc. are hidden
   });
   ```

3. **Only Renew Component Visible Test**
   ```typescript
   test('should show only subscription renewal component for suspended users', async ({ page }) => {
     // Login with suspended account
     // On billing page
     // Verify only "Renew Subscription" button visible
     // Verify no access to plan change, view history, etc.
   });
   ```

4. **Subscription Status Banner Test**
   ```typescript
   test('should show prominent SUSPENDED banner with renew CTA', async ({ page }) => {
     // Login with suspended account
     // Verify red/orange banner displayed
     // Contains: "Your subscription is suspended"
     // Contains: "Renew Now" button
   });
   ```

### Gap 2: Data Deletion Implementation (Scenario 3)

**Required Implementation Steps:**

#### Step 1: Database Schema Updates
```sql
-- Add retention tracking fields to church_subscriptions table
ALTER TABLE church_subscriptions ADD COLUMN suspended_at TIMESTAMP;
ALTER TABLE church_subscriptions ADD COLUMN data_retention_end_date DATE;
ALTER TABLE church_subscriptions ADD COLUMN retention_extension_days INT DEFAULT 0;
ALTER TABLE church_subscriptions ADD COLUMN deletion_warning_sent_at TIMESTAMP;
```

#### Step 2: ChurchSubscription Model Updates
```java
@Column(name = "suspended_at")
private LocalDateTime suspendedAt;

@Column(name = "data_retention_end_date")
private LocalDate dataRetentionEndDate;

@Column(name = "retention_extension_days")
@Builder.Default
private Integer retentionExtensionDays = 0;

@Column(name = "deletion_warning_sent_at")
private LocalDateTime deletionWarningSentAt;

/**
 * Calculate data deletion date: suspension date + 30 days + extension days
 */
public LocalDate getDataDeletionDate() {
    if (suspendedAt == null) return null;
    int totalRetentionDays = 30 + (retentionExtensionDays != null ? retentionExtensionDays : 0);
    return suspendedAt.toLocalDate().plusDays(totalRetentionDays);
}

/**
 * Check if church data is eligible for deletion
 */
public boolean isEligibleForDeletion() {
    if (!isSuspended()) return false;
    LocalDate deletionDate = getDataDeletionDate();
    return deletionDate != null && LocalDate.now().isAfter(deletionDate);
}

/**
 * Get days until data deletion
 */
public long getDaysUntilDeletion() {
    LocalDate deletionDate = getDataDeletionDate();
    if (deletionDate == null) return -1;
    return ChronoUnit.DAYS.between(LocalDate.now(), deletionDate);
}
```

#### Step 3: BillingService Data Deletion Methods
```java
/**
 * Mark subscription as suspended and set deletion timer
 */
public void suspendSubscription(Long churchId) {
    ChurchSubscription subscription = getChurchSubscription(churchId);
    subscription.setStatus("SUSPENDED");
    subscription.setSuspendedAt(LocalDateTime.now());
    subscription.setDataRetentionEndDate(LocalDate.now().plusDays(30));
    subscriptionRepository.save(subscription);

    log.warn("Subscription suspended for church {}. Data will be deleted after {}",
        churchId, subscription.getDataRetentionEndDate());
}

/**
 * Extend data retention period (SUPERADMIN only)
 */
@RequirePermission(Permission.PLATFORM_ACCESS)
public void extendDataRetention(Long churchId, int additionalDays, String reason) {
    ChurchSubscription subscription = getChurchSubscription(churchId);
    subscription.setRetentionExtensionDays(
        subscription.getRetentionExtensionDays() + additionalDays
    );
    subscription.setDataRetentionEndDate(
        subscription.getDataRetentionEndDate().plusDays(additionalDays)
    );
    subscriptionRepository.save(subscription);

    log.info("SUPERADMIN extended data retention for church {} by {} days. Reason: {}",
        churchId, additionalDays, reason);
}

/**
 * Delete all church data permanently
 */
@Transactional
public void deleteChurchData(Long churchId) {
    // Verify eligibility
    ChurchSubscription subscription = getChurchSubscription(churchId);
    if (!subscription.isEligibleForDeletion()) {
        throw new IllegalStateException("Church not eligible for deletion");
    }

    // Delete in order (foreign key dependencies)
    attendanceRepository.deleteAllByChurchId(churchId);
    eventRepository.deleteAllByChurchId(churchId);
    donationRepository.deleteAllByChurchId(churchId);
    pledgeRepository.deleteAllByChurchId(churchId);
    memberRepository.deleteAllByChurchId(churchId);
    fellowshipRepository.deleteAllByChurchId(churchId);
    userRepository.deleteAllByChurchId(churchId);
    subscriptionRepository.deleteByChurchId(churchId);
    churchRepository.deleteById(churchId);

    log.warn("PERMANENT DATA DELETION: All data for church {} has been removed", churchId);
}
```

#### Step 4: Scheduled Deletion Job
```java
/**
 * Process church data deletions daily at 4:00 AM
 * Deletes churches that have been suspended for 30+ days
 */
@Scheduled(cron = "0 0 4 * * *", zone = "UTC")
public void processChurchDataDeletions() {
    log.info("Starting church data deletion job...");

    try {
        List<ChurchSubscription> eligibleForDeletion =
            subscriptionRepository.findEligibleForDeletion(LocalDate.now());

        for (ChurchSubscription subscription : eligibleForDeletion) {
            try {
                // Send final warning if not sent
                if (subscription.getDeletionWarningSentAt() == null) {
                    sendDeletionWarningEmail(subscription.getChurchId());
                    subscription.setDeletionWarningSentAt(LocalDateTime.now());
                    subscriptionRepository.save(subscription);

                    log.info("Deletion warning sent to church {}. Will delete in 7 days.",
                        subscription.getChurchId());
                    continue;
                }

                // Check if 7 days have passed since warning
                if (subscription.getDeletionWarningSentAt()
                    .isBefore(LocalDateTime.now().minusDays(7))) {

                    billingService.deleteChurchData(subscription.getChurchId());
                    log.warn("Church {} data DELETED after 30+ days suspension",
                        subscription.getChurchId());
                }

            } catch (Exception e) {
                log.error("Error deleting church {}: {}",
                    subscription.getChurchId(), e.getMessage(), e);
            }
        }

        log.info("Church data deletion job completed. Deleted {} churches",
            eligibleForDeletion.size());

    } catch (Exception e) {
        log.error("Error in deletion job: {}", e.getMessage(), e);
    }
}
```

#### Step 5: SUPERADMIN API Endpoints
```java
@RestController
@RequestMapping("/api/platform/data-retention")
@RequirePermission(Permission.PLATFORM_ACCESS)
public class DataRetentionController {

    @GetMapping("/pending-deletion")
    public ResponseEntity<List<PendingDeletionResponse>> getPendingDeletions() {
        // Return list of churches with deletion date < 7 days
    }

    @PostMapping("/{churchId}/extend")
    public ResponseEntity<?> extendRetention(
        @PathVariable Long churchId,
        @RequestBody ExtendRetentionRequest request
    ) {
        billingService.extendDataRetention(
            churchId,
            request.getAdditionalDays(),
            request.getReason()
        );
        return ResponseEntity.ok(Map.of("message", "Retention extended"));
    }

    @PostMapping("/{churchId}/cancel-deletion")
    public ResponseEntity<?> cancelDeletion(@PathVariable Long churchId) {
        // Reset deletion timer, set retention to 90 days
    }

    @DeleteMapping("/{churchId}/force-delete")
    public ResponseEntity<?> forceDelete(@PathVariable Long churchId) {
        // Immediate deletion (emergency use only)
    }
}
```

#### Step 6: Frontend UI Components

**Suspended User View:**
```typescript
// subscription-inactive-page.component.ts
daysUntilDeletion: number;

ngOnInit() {
  this.loadSubscriptionStatus();
}

loadSubscriptionStatus() {
  this.billingService.getSubscriptionStatus().subscribe(status => {
    this.daysUntilDeletion = status.daysUntilDeletion;
    this.showDeletionWarning = this.daysUntilDeletion <= 7;
  });
}
```

**HTML Template:**
```html
<!-- Deletion Warning Banner (7 days or less) -->
<div *ngIf="daysUntilDeletion <= 7" class="deletion-warning">
  <i class="pi pi-exclamation-triangle"></i>
  <div>
    <h3>URGENT: Data Deletion in {{ daysUntilDeletion }} Days</h3>
    <p>All your church data will be permanently deleted if you don't renew.</p>
    <p><strong>This cannot be undone.</strong></p>
  </div>
  <button (click)="renewNow()">Renew Now to Save Data</button>
</div>

<!-- Regular Suspended Status -->
<div class="suspended-status">
  <h2>Subscription Suspended</h2>
  <p>Renew within {{ daysUntilDeletion }} days or data will be deleted.</p>
  <button (click)="renewSubscription()">Renew Subscription</button>
</div>
```

**SUPERADMIN Platform View:**
```html
<!-- platform-admin-page/data-retention-tab -->
<table>
  <tr *ngFor="let church of pendingDeletions">
    <td>{{ church.name }}</td>
    <td>{{ church.suspendedAt }}</td>
    <td>{{ church.daysUntilDeletion }} days</td>
    <td>
      <button (click)="extendRetention(church.id)">
        Extend 30 Days
      </button>
      <button (click)="cancelDeletion(church.id)">
        Cancel Deletion
      </button>
    </td>
  </tr>
</table>
```

#### Step 7: Test Coverage

**Backend Tests:**
```java
@Test
void shouldMarkSuspendedAtWhenSuspending() {
    // When suspending subscription
    // Then suspendedAt should be set
    // And dataRetentionEndDate should be now + 30 days
}

@Test
void shouldIdentifyEligibleForDeletion() {
    // Given subscription suspended 31 days ago
    // When checking isEligibleForDeletion()
    // Then should return true
}

@Test
void shouldExtendRetentionPeriod() {
    // Given subscription with 5 days until deletion
    // When SUPERADMIN extends by 30 days
    // Then deletion date should be pushed to 35 days
}

@Test
void shouldDeleteAllChurchData() {
    // Given church eligible for deletion
    // When deleteChurchData() called
    // Then all related data should be deleted
    // And church should be deleted
}
```

**E2E Tests:**
```typescript
test('should show deletion countdown for suspended users', async ({ page }) => {
  // Login with suspended account (20 days ago)
  // Verify sees "10 days until deletion" message
});

test('should prevent deletion if user renews within 30 days', async ({ page }) => {
  // Login with suspended account
  // Complete payment
  // Verify deletion timer is cleared
});

test('SUPERADMIN should be able to extend retention period', async ({ page }) => {
  // Login as SUPERADMIN
  // Navigate to data retention dashboard
  // Find church with 5 days remaining
  // Click "Extend 30 Days"
  // Verify updated to 35 days
});
```

---

## Recommendations

### Immediate Actions (Critical)

1. **Implement Scenario 3 (Data Deletion)**
   - Priority: HIGH
   - Complexity: HIGH
   - Time Estimate: 3-5 days
   - Impact: Legal/GDPR compliance, data hygiene

2. **Add UI Restriction Tests (Scenario 2)**
   - Priority: MEDIUM
   - Complexity: LOW
   - Time Estimate: 4-6 hours
   - Impact: User experience validation

### Implementation Order

**Phase 1: Data Deletion Foundation (Day 1-2)**
- [ ] Add database fields (suspended_at, data_retention_end_date, etc.)
- [ ] Update ChurchSubscription model with retention logic
- [ ] Create migration script

**Phase 2: Deletion Service (Day 2-3)**
- [ ] Implement deleteChurchData() method
- [ ] Implement extendDataRetention() method
- [ ] Add scheduled deletion job
- [ ] Add email warning system

**Phase 3: SUPERADMIN Interface (Day 3-4)**
- [ ] Create DataRetentionController
- [ ] Build pending deletions API
- [ ] Create platform admin UI tab
- [ ] Add extend/cancel deletion actions

**Phase 4: User-Facing UI (Day 4-5)**
- [ ] Add deletion countdown to inactive page
- [ ] Create urgent warning banner (7 days)
- [ ] Update billing page with retention info
- [ ] Add "days until deletion" indicator

**Phase 5: Testing (Day 5)**
- [ ] Backend unit tests
- [ ] Integration tests
- [ ] E2E tests for deletion flow
- [ ] SUPERADMIN E2E tests

### Legal/Compliance Considerations

1. **GDPR Right to Deletion**
   - 30-day retention aligns with "right to be forgotten"
   - Ensure complete data removal (no backups retained)

2. **Data Export Before Deletion**
   - Offer automatic export/backup before deletion
   - Send download link 7 days before deletion

3. **Notification Requirements**
   - Email at suspension
   - Email at 23 days (7 days warning)
   - Email at 29 days (final 24-hour warning)

4. **Audit Trail**
   - Log all deletions with timestamp and reason
   - Track who extended retention periods
   - Maintain deletion history for compliance

---

## Conclusion

### Current Status
- ✅ **Scenario 1:** Fully covered and tested
- ⚠️ **Scenario 2:** Partially covered - missing UI restriction tests
- ❌ **Scenario 3:** Not implemented - critical gap

### Next Steps
1. Prioritize Scenario 3 implementation (data deletion)
2. Add missing UI tests for Scenario 2
3. Document retention policy for users
4. Review GDPR compliance

### Risk Assessment
- **HIGH RISK:** No automated data deletion after suspension
- **MEDIUM RISK:** Incomplete UI restriction validation
- **LOW RISK:** Paid subscriber access (fully tested)
