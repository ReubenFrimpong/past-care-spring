# RBAC Backend - Complete Implementation Guide

**Date**: 2025-12-29
**Goal**: Protect all 37 remaining controllers with `@RequirePermission` annotations
**Approach**: Systematic batch implementation with testing

---

## Implementation Strategy

### Batch 1: Financial Controllers (HIGH PRIORITY) - 3 controllers
- ✅ CampaignController (already done)
- PledgeController
- RecurringDonationController

### Batch 2: Communication Controllers (HIGH PRIORITY) - 4 controllers
- SmsController
- SmsTemplateController
- CommunicationLogController
- ChurchSmsCreditController

### Batch 3: Pastoral Care Controllers (HIGH PRIORITY) - 6 controllers
- CareNeedController
- VisitController
- PrayerRequestController
- CounselingSessionController
- CrisisController
- ConfidentialNoteController

### Batch 4: Member Management Controllers (MEDIUM PRIORITY) - 3 controllers
- HouseholdController
- FellowshipController
- SavedSearchController

### Batch 5: Events & Attendance Controllers (MEDIUM PRIORITY) - 6 controllers
- EventController
- EventRegistrationController
- RecurringSessionController
- AttendanceController
- AttendanceExportController
- CheckInController

### Batch 6: Reports & Analytics Controllers (MEDIUM PRIORITY) - 4 controllers
- ReportController
- AnalyticsController
- DashboardController
- ReminderController

### Batch 7: Member Features Controllers (LOW PRIORITY) - 4 controllers
- LifecycleEventController
- MemberSkillController
- SkillController
- MinistryController

### Batch 8: Admin Controllers (LOW PRIORITY) - 3 controllers
- UsersController
- LocationController
- PortalUserController

### Excluded Controllers (No RBAC Needed)
- AuthController (public endpoints)
- PaystackWebhookController (API key auth)
- SmsWebhookController (provider auth)

---

## Permission Mapping Reference

### Financial Permissions
```java
// View financial data
Permission.DONATION_VIEW_ALL      // View all donations
Permission.CAMPAIGN_VIEW          // View campaigns
Permission.PLEDGE_VIEW_ALL        // View all pledges
Permission.PLEDGE_VIEW_OWN        // View own pledges (members)

// Manage financial data
Permission.DONATION_CREATE
Permission.DONATION_EDIT
Permission.DONATION_DELETE
Permission.DONATION_EXPORT
Permission.CAMPAIGN_MANAGE        // Create, edit, delete campaigns
Permission.PLEDGE_MANAGE          // Create, edit, delete pledges
Permission.RECEIPT_ISSUE          // Issue donation receipts
```

### Communication Permissions
```java
Permission.SMS_SEND               // Send SMS to members
Permission.SMS_SEND_FELLOWSHIP    // Send SMS to own fellowship only
Permission.EMAIL_SEND             // Send emails
Permission.BULK_MESSAGE_SEND      // Send bulk messages
```

### Pastoral Care Permissions
```java
Permission.CARE_NEED_VIEW_ALL
Permission.CARE_NEED_VIEW_ASSIGNED
Permission.CARE_NEED_CREATE
Permission.CARE_NEED_EDIT
Permission.CARE_NEED_ASSIGN

Permission.VISIT_VIEW_ALL
Permission.VISIT_CREATE
Permission.VISIT_EDIT

Permission.PRAYER_REQUEST_VIEW_ALL
Permission.PRAYER_REQUEST_CREATE
Permission.PRAYER_REQUEST_EDIT
```

### Member Management Permissions
```java
Permission.MEMBER_VIEW_ALL
Permission.MEMBER_CREATE
Permission.MEMBER_EDIT_ALL
Permission.MEMBER_DELETE
Permission.MEMBER_EXPORT
Permission.MEMBER_IMPORT

Permission.HOUSEHOLD_VIEW
Permission.HOUSEHOLD_CREATE
Permission.HOUSEHOLD_EDIT
Permission.HOUSEHOLD_DELETE

Permission.FELLOWSHIP_VIEW_ALL
Permission.FELLOWSHIP_CREATE
Permission.FELLOWSHIP_EDIT_ALL
Permission.FELLOWSHIP_DELETE
Permission.FELLOWSHIP_MANAGE_MEMBERS
```

### Event Permissions
```java
Permission.EVENT_VIEW_ALL
Permission.EVENT_VIEW_PUBLIC
Permission.EVENT_CREATE
Permission.EVENT_EDIT_ALL
Permission.EVENT_EDIT_OWN
Permission.EVENT_DELETE
Permission.EVENT_REGISTER
Permission.EVENT_MANAGE_REGISTRATIONS
```

### Attendance Permissions
```java
Permission.ATTENDANCE_VIEW_ALL
Permission.ATTENDANCE_VIEW_FELLOWSHIP
Permission.ATTENDANCE_RECORD
Permission.ATTENDANCE_EDIT
```

### Report Permissions
```java
Permission.REPORT_MEMBER
Permission.REPORT_FINANCIAL
Permission.REPORT_ATTENDANCE
Permission.REPORT_ANALYTICS
Permission.REPORT_EXPORT
```

### Admin Permissions
```java
Permission.USER_VIEW
Permission.USER_CREATE
Permission.USER_EDIT
Permission.USER_DELETE
Permission.USER_MANAGE_ROLES

Permission.CHURCH_SETTINGS_VIEW
Permission.CHURCH_SETTINGS_EDIT

Permission.SUBSCRIPTION_VIEW
Permission.SUBSCRIPTION_MANAGE
```

---

## Standard Implementation Pattern

### Step 1: Add Imports

```java
package com.reuben.pastcare_spring.controllers;

import com.reuben.pastcare_spring.annotations.RequirePermission;  // ADD THIS
import com.reuben.pastcare_spring.enums.Permission;                // ADD THIS
// ... other imports
```

### Step 2: Replace Annotations

**Before**:
```java
@GetMapping
@PreAuthorize("isAuthenticated()")
public ResponseEntity<List<SomeResponse>> getAll() { }
```

**After**:
```java
@GetMapping
@RequirePermission(Permission.SOME_VIEW_PERMISSION)
public ResponseEntity<List<SomeResponse>> getAll() { }
```

### Step 3: Permission Selection Guidelines

**GET Endpoints** (Read Operations):
- Use `*_VIEW_ALL` permission for viewing all records
- Use `*_VIEW_OWN` permission if users should only see their own data
- Use `*_VIEW_FELLOWSHIP` for fellowship-scoped data

**POST Endpoints** (Create Operations):
- Use `*_CREATE` permission for creating new records
- Use `*_MANAGE` if there's no separate CREATE permission

**PUT Endpoints** (Update Operations):
- Use `*_EDIT` or `*_EDIT_ALL` permission
- Use `*_MANAGE` if there's no separate EDIT permission

**DELETE Endpoints** (Delete Operations):
- Use `*_DELETE` permission
- Use `*_MANAGE` if there's no separate DELETE permission

**Special Endpoints** (Actions):
- `/export` → Use `REPORT_EXPORT` or `*_EXPORT`
- `/stats` → Use view permission (e.g., `*_VIEW_ALL`)
- `/search` → Use view permission
- Status changes (activate, deactivate, pause, resume) → Use manage permission

---

## Implementation Progress Tracker

### ✅ Batch 1: Financial (1/3 complete)
- [x] CampaignController - 18 endpoints protected
- [ ] PledgeController - 13 endpoints
- [ ] RecurringDonationController - 9 endpoints

### ⏳ Batch 2: Communication (0/4 complete)
- [ ] SmsController
- [ ] SmsTemplateController
- [ ] CommunicationLogController
- [ ] ChurchSmsCreditController

### ⏳ Batch 3: Pastoral Care (0/6 complete)
- [ ] CareNeedController
- [ ] VisitController
- [ ] PrayerRequestController
- [ ] CounselingSessionController
- [ ] CrisisController
- [ ] ConfidentialNoteController

### ⏳ Batch 4: Member Management (0/3 complete)
- [ ] HouseholdController
- [ ] FellowshipController
- [ ] SavedSearchController

### ⏳ Batch 5: Events & Attendance (0/6 complete)
- [ ] EventController
- [ ] EventRegistrationController
- [ ] RecurringSessionController
- [ ] AttendanceController
- [ ] AttendanceExportController
- [ ] CheckInController

### ⏳ Batch 6: Reports & Analytics (0/4 complete)
- [ ] ReportController
- [ ] AnalyticsController
- [ ] DashboardController
- [ ] ReminderController

### ⏳ Batch 7: Member Features (0/4 complete)
- [ ] LifecycleEventController
- [ ] MemberSkillController
- [ ] SkillController
- [ ] MinistryController

### ⏳ Batch 8: Admin (0/3 complete)
- [ ] UsersController
- [ ] LocationController
- [ ] PortalUserController

**Total Progress**: 1/37 controllers (3%)

---

## Testing Checklist (After Each Batch)

### 1. Compilation Test
```bash
./mvnw clean compile
# Should compile without errors
```

### 2. Role-Based Access Test

**ADMIN Role** (should have full access):
```bash
# Set ADMIN token
export TOKEN="<admin_jwt_token>"

# Test each endpoint
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/<endpoint>
# Expected: 200 OK with data
```

**TREASURER Role** (financial only):
```bash
# Set TREASURER token
export TOKEN="<treasurer_jwt_token>"

# Test financial endpoint
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/campaigns
# Expected: 200 OK

# Test non-financial endpoint (should be denied)
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/care-needs
# Expected: 403 Forbidden
```

**PASTOR Role** (pastoral care, communication):
```bash
# Set PASTOR token
export TOKEN="<pastor_jwt_token>"

# Test pastoral care endpoint
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/care-needs
# Expected: 200 OK

# Test financial endpoint (should be denied)
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/donations/1
# Expected: 403 Forbidden (view only, no edit)
```

**MEMBER Role** (minimal access):
```bash
# Set MEMBER token
export TOKEN="<member_jwt_token>"

# Test restricted endpoint
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/campaigns
# Expected: 403 Forbidden (members can't view campaigns)

# Test own data endpoint (if implemented)
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/members/me
# Expected: 200 OK (view own profile)
```

### 3. Permission Denial Logging Test

After implementing audit logging (Phase 2), verify:
```sql
-- Check security_audit_logs for permission denials
SELECT * FROM security_audit_logs
WHERE event_type = 'PERMISSION_DENIED'
ORDER BY timestamp DESC
LIMIT 10;
```

---

## Next Steps After Backend Complete

### Phase 2: Audit Logging (3-4 days)

**Goal**: Log all permission denials to `security_audit_logs` table

**Implementation**:
1. Enhance `PermissionCheckAspect.java`
2. Call `SecurityMonitoringService.logPermissionDenial()` when access denied
3. Add event type: `PERMISSION_DENIED`
4. Store: userId, churchId, role, requiredPermissions, endpoint, timestamp

**File to Modify**:
```java
// src/main/java/com/reuben/pastcare_spring/aspects/PermissionCheckAspect.java

@Before("@annotation(requirePermission)")
public void checkPermission(JoinPoint joinPoint, RequirePermission requirePermission) {
    // ... existing permission check logic ...

    if (!hasAccess) {
        // ADD THIS: Log to database
        securityMonitoringService.logPermissionDenial(
            userId,
            churchId,
            role.name(),
            Arrays.toString(requiredPermissions),
            joinPoint.getSignature().toShortString()
        );

        throw new InsufficientPermissionException(...);
    }
}
```

### Phase 3: Frontend Permission Integration (1-2 weeks)

See [RBAC_FRONTEND_IMPLEMENTATION.md](RBAC_FRONTEND_IMPLEMENTATION.md) for complete frontend plan.

---

**Document Status**: Implementation Guide
**Last Updated**: 2025-12-29
**Progress**: 1/37 controllers (3%)
**Next Batch**: Pledge & RecurringDonation controllers
