# RBAC Implementation Status

**Date:** 2025-12-29
**Overall Status:** ⏳ PARTIALLY COMPLETE (30%)
**Backend Infrastructure:** ✅ COMPLETE
**Endpoint Protection:** ⏳ IN PROGRESS (10%)
**Frontend Integration:** ❌ NOT STARTED

---

## Executive Summary

The Role-Based Access Control (RBAC) system has been **partially implemented** with a solid foundation in place:

- ✅ **Infrastructure Complete** - Permission system (79 permissions), Role system (8 roles), and enforcement aspect fully implemented
- ⏳ **Endpoint Protection In Progress** - Only 4 out of 41 controllers (10%) have `@RequirePermission` annotations
- ❌ **Frontend Not Started** - No Angular directives for permission-based UI hiding
- ❌ **Advanced Features Missing** - No resource-level authorization, no role management UI, no audit logging

**Critical Gap**: Most API endpoints are only protected by `@PreAuthorize("isAuthenticated()")`, meaning **any authenticated user can access any endpoint** regardless of their role. This is a **security vulnerability**.

---

## ✅ What's Implemented (Infrastructure - 100%)

### 1. Permission Enum (79 Permissions)

**File**: [Permission.java](src/main/java/com/reuben/pastcare_spring/enums/Permission.java)

**Categories** (10 total):
- **MEMBER** - 10 permissions (VIEW_ALL, VIEW_OWN, CREATE, EDIT_ALL, etc.)
- **HOUSEHOLD** - 4 permissions (VIEW, CREATE, EDIT, DELETE)
- **FELLOWSHIP** - 6 permissions (VIEW_ALL, VIEW_OWN, EDIT_ALL, EDIT_OWN, etc.)
- **FINANCIAL** - 12 permissions (DONATION_*, CAMPAIGN_*, PLEDGE_*, RECEIPT_ISSUE)
- **EVENT** - 8 permissions (VIEW_ALL, CREATE, EDIT_ALL, REGISTER, etc.)
- **ATTENDANCE** - 4 permissions (VIEW_ALL, VIEW_FELLOWSHIP, RECORD, EDIT)
- **PASTORAL_CARE** - 9 permissions (CARE_NEED_*, VISIT_*, PRAYER_REQUEST_*)
- **COMMUNICATION** - 4 permissions (SMS_SEND, EMAIL_SEND, BULK_MESSAGE_SEND, SMS_SEND_FELLOWSHIP)
- **REPORT** - 5 permissions (MEMBER, FINANCIAL, ATTENDANCE, ANALYTICS, EXPORT)
- **ADMIN** - 12 permissions (USER_*, CHURCH_SETTINGS_*, SUBSCRIPTION_*)
- **PLATFORM** - 5 permissions (PLATFORM_ACCESS, ALL_CHURCHES_*, BILLING_MANAGE, SYSTEM_CONFIG)

**Features**:
- `getDisplayName()` - Human-readable permission name
- `getCategory()` - Groups permissions by functional area
- `isViewPermission()` - Checks if permission is read-only
- `isManagementPermission()` - Checks if permission allows create/edit/delete

---

### 2. Role Enum (8 Roles with Permission Mappings)

**File**: [Role.java](src/main/java/com/reuben/pastcare_spring/enums/Role.java)

**Roles Defined**:

| Role | Permissions | Level | Description |
|------|------------|-------|-------------|
| **SUPERADMIN** | ALL | Platform | Platform admin - manages all churches and billing |
| **ADMIN** | 46 | Church | Church admin - full access to church operations |
| **PASTOR** | 23 | Church | Pastoral care, member oversight, communication |
| **TREASURER** | 11 | Church | Financial operations only (donations, campaigns, pledges) |
| **MEMBER_MANAGER** | 13 | Church | Member data management (CRUD, import/export) |
| **FELLOWSHIP_LEADER** | 9 | Fellowship | Manages own fellowship (members, attendance, messaging) |
| **MEMBER** | 7 | Individual | Personal access only (own profile, giving history, events) |
| **FELLOWSHIP_HEAD** | 9 | Fellowship | ⚠️ DEPRECATED (use FELLOWSHIP_LEADER) |

**Features**:
- `hasPermission(Permission p)` - Check if role has specific permission (SUPERADMIN always returns true)
- `hasAnyPermission(Permission... ps)` - OR logic for multiple permissions
- `hasAllPermissions(Permission... ps)` - AND logic for multiple permissions
- `isPlatformRole()`, `isChurchAdmin()`, `isDepartmentalRole()`, `isFellowshipRole()` - Role categorization helpers

---

### 3. @RequirePermission Annotation

**File**: [RequirePermission.java](src/main/java/com/reuben/pastcare_spring/annotations/RequirePermission.java)

**Features**:
- Can be applied to methods or classes (`@Target({ElementType.METHOD, ElementType.TYPE})`)
- Supports multiple permissions with AND/OR logic
- Optional custom error message
- SUPERADMIN automatically bypasses all checks

**Usage Examples**:

```java
// Single permission
@PostMapping("/members")
@RequirePermission(Permission.MEMBER_CREATE)
public ResponseEntity<MemberResponse> createMember(@RequestBody MemberRequest request) { ... }

// Multiple permissions with OR logic (default)
@DeleteMapping("/members/{id}")
@RequirePermission({Permission.MEMBER_DELETE, Permission.PLATFORM_ACCESS})
public ResponseEntity<Void> deleteMember(@PathVariable Long id) { ... }

// Multiple permissions with AND logic
@PostMapping("/donations/export")
@RequirePermission(value = {Permission.DONATION_VIEW_ALL, Permission.REPORT_EXPORT}, operation = LogicalOperation.AND)
public ResponseEntity<byte[]> exportDonations() { ... }

// Custom error message
@PutMapping("/church/settings")
@RequirePermission(value = Permission.CHURCH_SETTINGS_EDIT, message = "Only church administrators can modify settings")
public ResponseEntity<ChurchResponse> updateSettings(@RequestBody ChurchRequest request) { ... }
```

---

### 4. Permission Check Aspect (AOP)

**File**: [PermissionCheckAspect.java](src/main/java/com/reuben/pastcare_spring/aspects/PermissionCheckAspect.java)

**How It Works**:
1. Intercepts all methods annotated with `@RequirePermission`
2. Extracts current user's role from `TenantContext.getCurrentUserRole()`
3. Checks if SUPERADMIN (bypasses all checks)
4. Evaluates required permissions using AND/OR logic
5. Throws `InsufficientPermissionException` if access denied
6. Logs all permission checks (DEBUG level) and denials (WARN level)

**Security Features**:
- ✅ SUPERADMIN bypass for platform administration
- ✅ Role validation (ensures role exists in enum)
- ✅ Detailed logging of denials (userId, churchId, role, method, required permissions)
- ❌ **Missing**: Audit logging to `security_audit_logs` table (not implemented)

---

## ⏳ What's Partially Implemented (Endpoint Protection - 10%)

### Controllers with @RequirePermission (4/41 = 10%)

1. ✅ **[SecurityMonitoringController.java](src/main/java/com/reuben/pastcare_spring/controllers/SecurityMonitoringController.java)**
   - All endpoints require `PLATFORM_ACCESS` (SUPERADMIN only)
   - Endpoints: `/api/security/stats`, `/api/security/violations/*`

2. ✅ **[StorageUsageController.java](src/main/java/com/reuben/pastcare_spring/controllers/StorageUsageController.java)**
   - GET endpoints: `SUBSCRIPTION_VIEW` or `CHURCH_SETTINGS_VIEW`
   - POST /calculate: `SUBSCRIPTION_MANAGE`

3. ✅ **[DonationController.java](src/main/java/com/reuben/pastcare_spring/controllers/DonationController.java)**
   - View: `DONATION_VIEW_ALL` or `DONATION_VIEW_OWN`
   - Create/Edit/Delete: `DONATION_CREATE`, `DONATION_EDIT`, `DONATION_DELETE`
   - Export: `DONATION_EXPORT`

4. ✅ **[MembersController.java](src/main/java/com/reuben/pastcare_spring/controllers/MembersController.java)**
   - View: `MEMBER_VIEW_ALL`
   - Create: `MEMBER_CREATE`
   - Edit: `MEMBER_EDIT_ALL`
   - Delete: `MEMBER_DELETE`
   - Export: `MEMBER_EXPORT`

**Total Protected Endpoints**: ~29 endpoints across 4 controllers

---

### Controllers WITHOUT @RequirePermission (37/41 = 90%) ❌

These controllers currently use only `@PreAuthorize("isAuthenticated()")`, meaning **any authenticated user can access them**:

#### High Priority (Financial - TREASURER only)
- ❌ **CampaignController** - Campaign management (CAMPAIGN_MANAGE)
- ❌ **PledgeController** - Pledge management (PLEDGE_MANAGE)
- ❌ **RecurringDonationController** - Recurring donation setup (DONATION_EDIT)

#### High Priority (Member Management - ADMIN/MEMBER_MANAGER)
- ❌ **HouseholdController** - Household CRUD (HOUSEHOLD_*)
- ❌ **FellowshipController** - Fellowship CRUD (FELLOWSHIP_*)
- ❌ **SavedSearchController** - Saved searches (MEMBER_VIEW_ALL)

#### High Priority (Communication - ADMIN/PASTOR)
- ❌ **SmsController** - SMS sending (SMS_SEND)
- ❌ **SmsTemplateController** - SMS template management (SMS_SEND)
- ❌ **CommunicationLogController** - View communication logs (SMS_SEND)

#### High Priority (Pastoral Care - ADMIN/PASTOR)
- ❌ **CareNeedController** - Care need management (CARE_NEED_*)
- ❌ **VisitController** - Visit tracking (VISIT_*)
- ❌ **PrayerRequestController** - Prayer request management (PRAYER_REQUEST_*)
- ❌ **CounselingSessionController** - Counseling session tracking (CARE_NEED_*)
- ❌ **CrisisController** - Crisis management (CARE_NEED_*)

#### Medium Priority (Events)
- ❌ **EventController** - Event CRUD (EVENT_*)
- ❌ **EventRegistrationController** - Event registration (EVENT_REGISTER)
- ❌ **RecurringSessionController** - Recurring event sessions (EVENT_EDIT_ALL)

#### Medium Priority (Attendance)
- ❌ **AttendanceController** - Attendance tracking (ATTENDANCE_*)
- ❌ **AttendanceExportController** - Attendance export (ATTENDANCE_VIEW_ALL, REPORT_EXPORT)
- ❌ **CheckInController** - Member check-in (ATTENDANCE_RECORD)

#### Medium Priority (Reports & Analytics)
- ❌ **ReportController** - Report generation (REPORT_*)
- ❌ **AnalyticsController** - Analytics queries (REPORT_ANALYTICS)
- ❌ **DashboardController** - Dashboard stats (various view permissions)

#### Medium Priority (Member Features)
- ❌ **LifecycleEventController** - Lifecycle events (MEMBER_EDIT_ALL)
- ❌ **MemberSkillController** - Member skills (MEMBER_EDIT_ALL)
- ❌ **SkillController** - Skill definitions (MEMBER_EDIT_ALL)
- ❌ **ConfidentialNoteController** - Confidential notes (CARE_NEED_VIEW_ALL)

#### Low Priority (Church Settings - ADMIN only)
- ❌ **UsersController** - User management (USER_*)
- ❌ **LocationController** - Location management (CHURCH_SETTINGS_EDIT)
- ❌ **MinistryController** - Ministry management (FELLOWSHIP_EDIT_ALL)

#### Low Priority (System/Integration)
- ❌ **ReminderController** - Reminder management (CARE_NEED_ASSIGN)
- ❌ **ChurchSmsCreditController** - SMS credits (SUBSCRIPTION_VIEW)
- ❌ **PortalUserController** - Portal user management (USER_MANAGE_ROLES)

#### Excluded (No Authorization Needed)
- ✅ **AuthController** - Login/register (public)
- ✅ **PaystackWebhookController** - Webhook callbacks (API key authentication)
- ✅ **SmsWebhookController** - SMS webhook callbacks (provider authentication)

---

## ❌ What's NOT Implemented

### 1. Resource-Level Authorization (Fellowship Scope)

**Problem**: Fellowship leaders can't be restricted to managing only their specific fellowship(s).

**Current State**:
- `FELLOWSHIP_LEADER` role has permissions like `MEMBER_VIEW_FELLOWSHIP` and `FELLOWSHIP_EDIT_OWN`
- But there's no enforcement at the service layer to filter data by fellowship scope
- User model doesn't store fellowship assignment

**What's Needed**:
1. Add fellowship scope to User model (e.g., `assignedFellowships: Set<Fellowship>`)
2. Service methods filter data by user's assigned fellowships
3. Controller/aspect validates resource ownership before allowing edit

**Example Implementation**:
```java
@Service
public class FellowshipService {
    public List<Member> getAccessibleMembers(User user) {
        if (user.hasPermission(Permission.MEMBER_VIEW_ALL)) {
            return memberRepository.findByChurch(user.getChurch());
        } else if (user.hasPermission(Permission.MEMBER_VIEW_FELLOWSHIP)) {
            // Filter by user's assigned fellowships
            return memberRepository.findByFellowshipsIn(user.getAssignedFellowships());
        } else {
            return Collections.emptyList();
        }
    }
}
```

---

### 2. Audit Logging for Permission Denials

**Problem**: Permission denials are logged to application logs but not stored in `security_audit_logs` table.

**Current State**:
- `PermissionCheckAspect` logs warnings when permission is denied
- `InsufficientPermissionException` is thrown but not captured for audit
- No persistent record of authorization failures

**What's Needed**:
1. Enhance `PermissionCheckAspect` to log to `security_audit_logs` table
2. Add event type: `PERMISSION_DENIED`
3. Store userId, churchId, role, requiredPermissions, endpoint, timestamp

**Example Implementation**:
```java
@Aspect
@Component
public class PermissionCheckAspect {
    @Autowired
    private SecurityMonitoringService securityMonitoringService;

    @Before("@annotation(requirePermission)")
    public void checkPermission(JoinPoint joinPoint, RequirePermission requirePermission) {
        // ... existing permission check logic ...

        if (!hasAccess) {
            // Log to security_audit_logs table
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
}
```

---

### 3. Database Schema for Dynamic Roles (Optional)

**Current State**: Roles are hardcoded in `Role` enum - no custom roles possible

**What's Needed** (if dynamic roles are required):
- `role` table - Store custom roles
- `permission` table - Store permissions
- `role_permission` table - Many-to-many mapping
- `user_role` table - Users can have multiple roles with scope (e.g., FELLOWSHIP_LEADER for Fellowship #5)

**Migration Path**:
```sql
CREATE TABLE role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    display_name VARCHAR(100) NOT NULL,
    description TEXT,
    level VARCHAR(20) NOT NULL,  -- PLATFORM, CHURCH, FELLOWSHIP, CUSTOM
    is_system BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    scope_type VARCHAR(20),  -- CHURCH, FELLOWSHIP, GLOBAL
    scope_id BIGINT,  -- fellowship_id if scope_type = FELLOWSHIP
    granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE
);
```

**Decision**: Not needed immediately - enum-based roles are sufficient for MVP

---

### 4. Frontend Permission Integration

**Current State**: No Angular directives or guards for permission-based UI

**What's Needed**:

#### A. Permission Directive
```typescript
@Directive({ selector: '[hasPermission]' })
export class HasPermissionDirective implements OnInit {
  @Input() hasPermission: Permission[];

  constructor(
    private templateRef: TemplateRef<any>,
    private viewContainer: ViewContainerRef,
    private authService: AuthService
  ) {}

  ngOnInit() {
    const hasAccess = this.hasPermission.some(p => this.authService.hasPermission(p));
    if (hasAccess) {
      this.viewContainer.createEmbeddedView(this.templateRef);
    } else {
      this.viewContainer.clear();
    }
  }
}

// Usage:
<button *hasPermission="['MEMBER_DELETE']" (click)="deleteMember()">Delete</button>
```

#### B. AuthService Enhancement
```typescript
export class AuthService {
  private currentUser: User;

  hasPermission(permission: Permission): boolean {
    if (!this.currentUser) return false;
    if (this.currentUser.role === 'SUPERADMIN') return true;
    return this.currentUser.permissions.includes(permission);
  }

  hasAnyPermission(permissions: Permission[]): boolean {
    return permissions.some(p => this.hasPermission(p));
  }

  hasAllPermissions(permissions: Permission[]): boolean {
    return permissions.every(p => this.hasPermission(p));
  }
}
```

#### C. Route Guards
```typescript
@Injectable({ providedIn: 'root' })
export class PermissionGuard implements CanActivate {
  constructor(private authService: AuthService, private router: Router) {}

  canActivate(route: ActivatedRouteSnapshot): boolean {
    const requiredPermissions = route.data['permissions'] as Permission[];
    if (this.authService.hasAnyPermission(requiredPermissions)) {
      return true;
    }
    this.router.navigate(['/unauthorized']);
    return false;
  }
}

// Usage in routing module:
{
  path: 'members/create',
  component: CreateMemberComponent,
  canActivate: [PermissionGuard],
  data: { permissions: ['MEMBER_CREATE'] }
}
```

---

### 5. Role Management UI

**Current State**: No UI for assigning roles to users

**What's Needed**:

#### Church Admin Features:
- View all users in church
- Assign/remove roles from users
- View role permissions
- Cannot create custom roles (uses predefined enum roles)

#### SUPERADMIN Features (Future):
- View all roles across all churches
- Create custom roles
- Assign permissions to custom roles
- View role usage statistics

**Component Structure**:
```
UsersPage
├── UserList (table with role badges)
├── EditUserDialog
│   ├── UserInfo (name, email, status)
│   └── RoleAssignment
│       ├── RoleSelector (dropdown or chips)
│       └── ScopeSelector (if FELLOWSHIP_LEADER, select fellowships)
└── RolePermissionsDialog (read-only permission matrix)
```

---

## 📊 Implementation Statistics

### Code Metrics
- **Permission Definitions**: 79 permissions across 10 categories
- **Role Definitions**: 8 roles (7 active + 1 deprecated)
- **Protected Controllers**: 4 out of 41 (10%)
- **Protected Endpoints**: ~29 endpoints
- **Unprotected Controllers**: 37 out of 41 (90%)
- **Files Created**: 3 (Permission.java, RequirePermission.java, PermissionCheckAspect.java)
- **Files Modified**: 2 (Role.java enhanced, InsufficientPermissionException.java created)

### Completion Breakdown
| Component | Status | Completion |
|-----------|--------|------------|
| Permission System | ✅ Complete | 100% |
| Role System | ✅ Complete | 100% |
| Enforcement Aspect | ✅ Complete | 100% |
| Endpoint Protection | ⏳ In Progress | 10% |
| Resource-Level Auth | ❌ Not Started | 0% |
| Audit Logging | ❌ Not Started | 0% |
| Frontend Integration | ❌ Not Started | 0% |
| Role Management UI | ❌ Not Started | 0% |
| **Overall** | **⏳ Partial** | **30%** |

---

## 🎯 Recommended Implementation Plan

### Phase 1: Complete Endpoint Protection (1-2 weeks) 🔴 HIGH PRIORITY

**Goal**: Add `@RequirePermission` to all 37 remaining controllers

**Approach**: 2-3 controllers per day for 10-15 working days

**Priority Order**:
1. **Day 1-2: Financial Controllers** (TREASURER-only access)
   - CampaignController
   - PledgeController
   - RecurringDonationController

2. **Day 3-4: Member Management** (ADMIN/MEMBER_MANAGER)
   - HouseholdController
   - FellowshipController
   - SavedSearchController

3. **Day 5-6: Communication** (ADMIN/PASTOR)
   - SmsController
   - SmsTemplateController
   - CommunicationLogController

4. **Day 7-8: Pastoral Care** (ADMIN/PASTOR)
   - CareNeedController
   - VisitController
   - PrayerRequestController
   - CounselingSessionController
   - CrisisController

5. **Day 9-10: Events & Attendance** (ADMIN/PASTOR)
   - EventController
   - EventRegistrationController
   - AttendanceController
   - CheckInController

6. **Day 11-12: Reports & Analytics** (Various)
   - ReportController
   - AnalyticsController
   - DashboardController
   - AttendanceExportController

7. **Day 13-14: Remaining Controllers**
   - LifecycleEventController
   - MemberSkillController
   - SkillController
   - ConfidentialNoteController
   - UsersController
   - LocationController
   - MinistryController
   - ReminderController
   - ChurchSmsCreditController
   - PortalUserController

**Testing**: After each controller is protected, test with different roles:
- ADMIN: Should have full access
- TREASURER: Should only access financial endpoints
- PASTOR: Should access pastoral care, member view, communication
- FELLOWSHIP_LEADER: Should have limited access (fellowship-scoped)
- MEMBER: Should only access own data

---

### Phase 2: Audit Logging (3-4 days) 🔴 HIGH PRIORITY

**Goal**: Log all permission denials to `security_audit_logs` table

**Implementation**:
1. Enhance `PermissionCheckAspect` to call `SecurityMonitoringService.logPermissionDenial()`
2. Add new event type: `PERMISSION_DENIED` to security_audit_logs
3. Store: userId, churchId, role, requiredPermissions, endpoint, HTTP method, IP address
4. Add alert for excessive denials (e.g., >10 denials in 1 hour)

**Testing**:
- Attempt unauthorized access with TREASURER role to member endpoints
- Verify denial logged to database
- Check alert threshold triggers email notification

---

### Phase 3: Resource-Level Authorization (1 week) 🟡 MEDIUM PRIORITY

**Goal**: Implement fellowship scope for FELLOWSHIP_LEADER role

**Implementation**:
1. Add `assignedFellowships` to User model (ManyToMany relationship)
2. Update FellowshipService to filter members by user's assigned fellowships
3. Update MemberService to filter members by assigned fellowships
4. Add scope validation in controllers (e.g., can only edit members in assigned fellowships)

**Testing**:
- Assign FELLOWSHIP_LEADER role to user with Fellowship #5
- Verify user can view/edit members in Fellowship #5
- Verify user cannot view/edit members in Fellowship #3 (not assigned)

---

### Phase 4: Frontend Integration (1 week) 🟡 MEDIUM PRIORITY

**Goal**: Hide UI elements based on user permissions

**Implementation**:
1. Create `HasPermissionDirective` in Angular
2. Enhance `AuthService` with permission check methods
3. Add permission checks to all action buttons (Delete, Edit, Create)
4. Implement route guards for permission-based routing

**Example**: Members page
- Hide "Delete" button if user lacks `MEMBER_DELETE`
- Hide "Export" button if user lacks `MEMBER_EXPORT`
- Hide "Create Member" button if user lacks `MEMBER_CREATE`

---

### Phase 5: Role Management UI (1-2 weeks) 🟢 NICE TO HAVE

**Goal**: Allow church admins to assign roles to users

**Implementation**:
1. Create UsersPage component with user list
2. Add EditUserDialog with role assignment
3. Add role badges to user table
4. Implement role assignment API endpoint (if not exists)

**Features**:
- View all users with current roles
- Assign/remove roles from users
- View role permissions (read-only matrix)
- For FELLOWSHIP_LEADER role, select which fellowships to assign

---

## 🧪 Testing Checklist

### Per-Role Testing

For each role, test access to all endpoint categories:

**ADMIN** (should have full access to church data):
- ✅ Member CRUD
- ✅ Fellowship CRUD
- ✅ Financial view (donations, campaigns, pledges)
- ✅ Event CRUD
- ✅ Attendance tracking
- ✅ Pastoral care (care needs, visits, prayers)
- ✅ Communication (SMS, email)
- ✅ Reports
- ✅ User management
- ✅ Church settings
- ❌ Platform admin (SUPERADMIN only)

**TREASURER** (financial only):
- ❌ Member CRUD (view only)
- ❌ Fellowship CRUD
- ✅ Financial CRUD (donations, campaigns, pledges, receipts)
- ❌ Event CRUD
- ❌ Attendance tracking
- ❌ Pastoral care
- ❌ Communication
- ✅ Financial reports
- ❌ User management

**PASTOR** (pastoral care + communication):
- ✅ Member view (edit pastoral data only)
- ✅ Fellowship view
- ❌ Financial view only
- ✅ Event create (own events only)
- ✅ Attendance view/record
- ✅ Pastoral care (care needs, visits, prayers)
- ✅ Communication (SMS, email)
- ✅ Member/attendance reports
- ❌ User management

**FELLOWSHIP_LEADER** (fellowship-scoped):
- ✅ Member view (fellowship members only)
- ✅ Fellowship view/edit (own fellowship only)
- ❌ Financial
- ✅ Event create
- ✅ Attendance record (fellowship only)
- ❌ Pastoral care (view assigned only)
- ✅ SMS send (fellowship members only)
- ❌ Reports

**MEMBER** (personal access only):
- ✅ Own profile view/edit
- ✅ Own fellowship view
- ✅ Own giving history
- ✅ Public event view/register
- ❌ Attendance tracking
- ✅ Prayer request create
- ❌ Communication
- ❌ Reports

---

## 📝 Documentation

### Files to Reference

**Backend**:
- [Permission.java](src/main/java/com/reuben/pastcare_spring/enums/Permission.java) - All 79 permissions defined
- [Role.java](src/main/java/com/reuben/pastcare_spring/enums/Role.java) - 8 roles with permission mappings
- [RequirePermission.java](src/main/java/com/reuben/pastcare_spring/annotations/RequirePermission.java) - Annotation definition
- [PermissionCheckAspect.java](src/main/java/com/reuben/pastcare_spring/aspects/PermissionCheckAspect.java) - AOP enforcement
- [InsufficientPermissionException.java](src/main/java/com/reuben/pastcare_spring/exceptions/InsufficientPermissionException.java) - Exception handling

**Architecture**:
- [ARCHITECTURE_CRITICAL_ISSUES.md](ARCHITECTURE_CRITICAL_ISSUES.md) - Issue #3 section (lines 832-1363)

---

## 🚨 Security Warnings

**CRITICAL SECURITY GAP**: 90% of API endpoints (37/41 controllers) are **not protected by RBAC**. Any authenticated user can:
- ❌ Manage campaigns and pledges (should be TREASURER only)
- ❌ Send SMS to all members (should be ADMIN/PASTOR only)
- ❌ Edit care needs and visits (should be ADMIN/PASTOR only)
- ❌ Export reports (should require REPORT_EXPORT permission)
- ❌ Manage church settings (should be ADMIN only)

**Recommended Immediate Action**:
1. **Week 1**: Protect financial controllers (Campaign, Pledge, RecurringDonation) - TREASURER-only
2. **Week 1**: Protect communication controllers (Sms, SmsTemplate, CommunicationLog) - ADMIN/PASTOR-only
3. **Week 2**: Protect remaining 32 controllers

**Risk Level**: 🔴 **HIGH** - Current RBAC implementation provides minimal security benefit until endpoint protection is complete.

---

**Document Status**: Complete
**Last Updated**: 2025-12-29
**Next Review**: After Phase 1 completion (all controllers protected)
