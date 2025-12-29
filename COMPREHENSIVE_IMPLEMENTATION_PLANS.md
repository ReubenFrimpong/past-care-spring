# Comprehensive Implementation Plans

**Date**: 2025-12-29
**Status**: Planning Document
**Purpose**: Implementation roadmap for high-priority features

---

## Table of Contents

1. [RBAC Endpoint Protection (In Progress)](#1-rbac-endpoint-protection)
2. [User Management Module](#2-user-management-module)
3. [Pricing on Landing Page](#3-pricing-on-landing-page)
4. [Platform Admin Dashboard](#4-platform-admin-dashboard)

---

## 1. RBAC Endpoint Protection

### Status: ⏳ IN PROGRESS (Started 2025-12-29)

### Goal
Add `@RequirePermission` annotations to all 37 unprotected controllers to enforce role-based access control.

### Progress: 5/41 controllers (12%)
- ✅ SecurityMonitoringController (already done)
- ✅ StorageUsageController (already done)
- ✅ DonationController (already done)
- ✅ MembersController (already done)
- ✅ CampaignController (completed today)
- ⏳ PledgeController (in progress)
- ⏳ RecurringDonationController (in progress)

### Implementation Plan - Remaining 34 Controllers

Due to the large scope, I'll provide a **detailed script** that can be used to systematically protect all controllers. See [RBAC_ENDPOINT_PROTECTION_SCRIPT.md](RBAC_ENDPOINT_PROTECTION_SCRIPT.md) for the complete automation script.

**Recommended Approach**: Manual review + semi-automated application
- Use the script as a reference
- Apply permissions controller-by-controller
- Test each controller after protection
- Target: 2-3 controllers per day = 10-15 working days

### Permission Mapping Guide

| Controller | View Permission | Create/Manage Permission | Delete Permission |
|------------|----------------|-------------------------|-------------------|
| **Financial** ||||
| CampaignController | CAMPAIGN_VIEW | CAMPAIGN_MANAGE | CAMPAIGN_MANAGE |
| PledgeController | PLEDGE_VIEW_ALL / PLEDGE_VIEW_OWN | PLEDGE_MANAGE | PLEDGE_MANAGE |
| RecurringDonationController | DONATION_VIEW_ALL | DONATION_EDIT | DONATION_DELETE |
| **Member Management** ||||
| HouseholdController | HOUSEHOLD_VIEW | HOUSEHOLD_CREATE, HOUSEHOLD_EDIT | HOUSEHOLD_DELETE |
| FellowshipController | FELLOWSHIP_VIEW_ALL | FELLOWSHIP_CREATE, FELLOWSHIP_EDIT_ALL | FELLOWSHIP_DELETE |
| SavedSearchController | MEMBER_VIEW_ALL | MEMBER_VIEW_ALL | MEMBER_VIEW_ALL |
| **Communication** ||||
| SmsController | SMS_SEND | SMS_SEND | N/A |
| SmsTemplateController | SMS_SEND | SMS_SEND | SMS_SEND |
| CommunicationLogController | SMS_SEND | N/A | N/A |
| ChurchSmsCreditController | SUBSCRIPTION_VIEW | SUBSCRIPTION_MANAGE | N/A |
| **Pastoral Care** ||||
| CareNeedController | CARE_NEED_VIEW_ALL | CARE_NEED_CREATE, CARE_NEED_EDIT | CARE_NEED_EDIT |
| VisitController | VISIT_VIEW_ALL | VISIT_CREATE, VISIT_EDIT | VISIT_EDIT |
| PrayerRequestController | PRAYER_REQUEST_VIEW_ALL | PRAYER_REQUEST_CREATE, PRAYER_REQUEST_EDIT | PRAYER_REQUEST_EDIT |
| CounselingSessionController | CARE_NEED_VIEW_ALL | CARE_NEED_CREATE, CARE_NEED_EDIT | CARE_NEED_EDIT |
| CrisisController | CARE_NEED_VIEW_ALL | CARE_NEED_CREATE, CARE_NEED_EDIT | CARE_NEED_EDIT |
| ConfidentialNoteController | CARE_NEED_VIEW_ALL | CARE_NEED_CREATE, CARE_NEED_EDIT | CARE_NEED_EDIT |
| **Events** ||||
| EventController | EVENT_VIEW_ALL | EVENT_CREATE, EVENT_EDIT_ALL | EVENT_DELETE |
| EventRegistrationController | EVENT_REGISTER | EVENT_REGISTER | EVENT_MANAGE_REGISTRATIONS |
| RecurringSessionController | EVENT_VIEW_ALL | EVENT_EDIT_ALL | EVENT_EDIT_ALL |
| **Attendance** ||||
| AttendanceController | ATTENDANCE_VIEW_ALL | ATTENDANCE_RECORD, ATTENDANCE_EDIT | ATTENDANCE_EDIT |
| AttendanceExportController | ATTENDANCE_VIEW_ALL, REPORT_EXPORT | N/A | N/A |
| CheckInController | ATTENDANCE_RECORD | ATTENDANCE_RECORD | N/A |
| **Reports & Analytics** ||||
| ReportController | REPORT_* (based on type) | REPORT_* | REPORT_* |
| AnalyticsController | REPORT_ANALYTICS | N/A | N/A |
| DashboardController | Various (mixed) | N/A | N/A |
| **Admin** ||||
| UsersController | USER_VIEW | USER_CREATE, USER_EDIT, USER_MANAGE_ROLES | USER_DELETE |
| LocationController | CHURCH_SETTINGS_VIEW | CHURCH_SETTINGS_EDIT | CHURCH_SETTINGS_EDIT |
| MinistryController | FELLOWSHIP_VIEW_ALL | FELLOWSHIP_CREATE, FELLOWSHIP_EDIT_ALL | FELLOWSHIP_DELETE |
| PortalUserController | USER_VIEW | USER_CREATE, USER_EDIT | USER_DELETE |
| **Member Features** ||||
| LifecycleEventController | MEMBER_VIEW_ALL | MEMBER_EDIT_ALL | MEMBER_EDIT_ALL |
| MemberSkillController | MEMBER_VIEW_ALL | MEMBER_EDIT_ALL | MEMBER_EDIT_ALL |
| SkillController | MEMBER_VIEW_ALL | MEMBER_EDIT_ALL | MEMBER_EDIT_ALL |
| **Other** ||||
| ReminderController | CARE_NEED_VIEW_ALL | CARE_NEED_ASSIGN | CARE_NEED_EDIT |

### Testing Checklist (Per Controller)

After protecting each controller:

1. **ADMIN Role** - Should have full access
   ```bash
   # Test with ADMIN JWT token
   curl -H "Authorization: Bearer $ADMIN_TOKEN" http://localhost:8080/api/[endpoint]
   # Expected: 200 OK
   ```

2. **TREASURER Role** - Should only access financial endpoints
   ```bash
   # Test financial endpoint
   curl -H "Authorization: Bearer $TREASURER_TOKEN" http://localhost:8080/api/campaigns
   # Expected: 200 OK (if financial) or 403 Forbidden (if not)
   ```

3. **PASTOR Role** - Should access pastoral care, communication, member view
   ```bash
   # Test pastoral care endpoint
   curl -H "Authorization: Bearer $PASTOR_TOKEN" http://localhost:8080/api/care-needs
   # Expected: 200 OK
   ```

4. **MEMBER Role** - Should have very limited access
   ```bash
   # Test restricted endpoint
   curl -H "Authorization: Bearer $MEMBER_TOKEN" http://localhost:8080/api/campaigns
   # Expected: 403 Forbidden
   ```

---

## 2. User Management Module

### Status: ❌ NOT STARTED

### Overview

A comprehensive user management module for church administrators to manage users, assign roles, and control access within their church.

### Business Requirements

**Primary Goals**:
1. Allow church admins to invite and manage users
2. Enable role assignment with proper permissions
3. Support user activation/deactivation
4. Track user activity and last login
5. Handle multi-role users (future enhancement)

**Target Users**:
- Church Administrators (ADMIN role)
- Platform Administrators (SUPERADMIN role)

### Database Schema

#### Current State
The `user` table already exists with basic fields:
- id, email, password, firstName, lastName
- role (enum: single role per user)
- church (ManyToOne)
- isActive, createdAt, updatedAt

#### Required Enhancements

**Option 1: Keep Enum-Based Roles (Recommended for MVP)**
- No database changes needed
- Use existing `Role` enum
- Simpler implementation, faster to market

**Option 2: Dynamic Roles (Future Enhancement)**

```sql
-- User roles (many-to-many for multiple roles per user)
CREATE TABLE user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_name VARCHAR(50) NOT NULL,  -- References Role enum

    -- Scope for fellowship leaders
    scope_type VARCHAR(20),  -- CHURCH, FELLOWSHIP, GLOBAL
    scope_id BIGINT,  -- fellowship_id if scope_type = FELLOWSHIP

    -- Metadata
    granted_by_id BIGINT,
    granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,  -- Optional: temporary roles

    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id)
        REFERENCES user(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_role_granted_by FOREIGN KEY (granted_by_id)
        REFERENCES user(id) ON DELETE SET NULL,

    INDEX idx_user_role_user (user_id),
    INDEX idx_user_role_scope (scope_type, scope_id),
    INDEX idx_user_role_expires (expires_at)
);

-- User activity tracking
CREATE TABLE user_activity_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    activity_type VARCHAR(50) NOT NULL,  -- LOGIN, LOGOUT, PASSWORD_CHANGE, ROLE_CHANGE
    ip_address VARCHAR(45),
    user_agent TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_activity_user FOREIGN KEY (user_id)
        REFERENCES user(id) ON DELETE CASCADE,

    INDEX idx_activity_user_timestamp (user_id, timestamp),
    INDEX idx_activity_type (activity_type)
);

-- User invitations (email-based user creation)
CREATE TABLE user_invitation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    church_id BIGINT NOT NULL,
    invited_role VARCHAR(50) NOT NULL,

    invitation_token VARCHAR(255) NOT NULL UNIQUE,
    invited_by_id BIGINT NOT NULL,
    invited_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,

    accepted_at TIMESTAMP,
    accepted_user_id BIGINT,

    status VARCHAR(20) DEFAULT 'PENDING',  -- PENDING, ACCEPTED, EXPIRED, CANCELLED

    CONSTRAINT fk_invitation_church FOREIGN KEY (church_id)
        REFERENCES church(id) ON DELETE CASCADE,
    CONSTRAINT fk_invitation_invited_by FOREIGN KEY (invited_by_id)
        REFERENCES user(id) ON DELETE CASCADE,
    CONSTRAINT fk_invitation_accepted_user FOREIGN KEY (accepted_user_id)
        REFERENCES user(id) ON DELETE SET NULL,

    INDEX idx_invitation_token (invitation_token),
    INDEX idx_invitation_email (email),
    INDEX idx_invitation_status (status)
);
```

### Backend Implementation

#### Phase 1: Core User Management (Week 1-2)

**Files to Create**:

1. **DTOs** (`src/main/java/com/reuben/pastcare_spring/dtos/`)
   - `UserInvitationRequest.java`
   - `UserInvitationResponse.java`
   - `UserUpdateRequest.java`
   - `UserDetailResponse.java`
   - `UserActivityResponse.java`

2. **Services** (`src/main/java/com/reuben/pastcare_spring/services/`)
   - `UserManagementService.java` - Core user CRUD
   - `UserInvitationService.java` - Handle invitations
   - `UserActivityService.java` - Track user activity

3. **Controllers** (`src/main/java/com/reuben/pastcare_spring/controllers/`)
   - Enhance `UsersController.java` with:
     - `GET /api/users` - List all users in church (paginated)
     - `GET /api/users/{id}` - Get user details
     - `POST /api/users/invite` - Invite new user
     - `PUT /api/users/{id}` - Update user
     - `POST /api/users/{id}/activate` - Activate user
     - `POST /api/users/{id}/deactivate` - Deactivate user
     - `PUT /api/users/{id}/role` - Change user role
     - `GET /api/users/{id}/activity` - Get user activity log
     - `DELETE /api/users/{id}` - Delete user

**Permission Requirements**:
```java
@RestController
@RequestMapping("/api/users")
public class UsersController {

    @GetMapping
    @RequirePermission(Permission.USER_VIEW)
    public ResponseEntity<Page<UserDetailResponse>> getAllUsers(Pageable pageable) { }

    @GetMapping("/{id}")
    @RequirePermission(Permission.USER_VIEW)
    public ResponseEntity<UserDetailResponse> getUserById(@PathVariable Long id) { }

    @PostMapping("/invite")
    @RequirePermission(Permission.USER_CREATE)
    public ResponseEntity<UserInvitationResponse> inviteUser(@RequestBody UserInvitationRequest request) { }

    @PutMapping("/{id}")
    @RequirePermission(Permission.USER_EDIT)
    public ResponseEntity<UserDetailResponse> updateUser(@PathVariable Long id, @RequestBody UserUpdateRequest request) { }

    @PostMapping("/{id}/activate")
    @RequirePermission(Permission.USER_EDIT)
    public ResponseEntity<Void> activateUser(@PathVariable Long id) { }

    @PostMapping("/{id}/deactivate")
    @RequirePermission(Permission.USER_EDIT)
    public ResponseEntity<Void> deactivateUser(@PathVariable Long id) { }

    @PutMapping("/{id}/role")
    @RequirePermission(Permission.USER_MANAGE_ROLES)
    public ResponseEntity<UserDetailResponse> changeUserRole(@PathVariable Long id, @RequestParam String role) { }

    @DeleteMapping("/{id}")
    @RequirePermission(Permission.USER_DELETE)
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) { }
}
```

#### Phase 2: User Invitations (Week 2-3)

**Invitation Flow**:
1. Admin clicks "Invite User" in UI
2. Enters email, firstName, lastName, role
3. Backend generates unique invitation token
4. Email sent with invitation link: `https://app.pastcare.com/accept-invitation?token=XYZ123`
5. User clicks link, sees pre-filled form (email, name from invitation)
6. User sets password, submits
7. Account created, invitation marked as accepted
8. User redirected to login

**Email Template**:
```html
Subject: You've been invited to join [Church Name] on PastCare

Hi [User Name],

[Inviter Name] has invited you to join [Church Name] on PastCare as a [Role].

Click the link below to accept your invitation and set up your account:
[Accept Invitation Button - expires in 7 days]

If you didn't expect this invitation, you can safely ignore this email.

Thanks,
The PastCare Team
```

**Security Considerations**:
- Invitation tokens expire after 7 days
- Tokens are single-use (marked as accepted or expired)
- Email must match invitation email exactly
- Cannot invite existing users (check by email)
- Rate limiting: 10 invitations per hour per church

#### Phase 3: Activity Logging (Week 3)

**Events to Log**:
- User login (successful and failed attempts)
- User logout
- Password change/reset
- Role change
- Account activation/deactivation
- Permission denied events (from RBAC)

**Implementation**:
```java
@Component
public class UserActivityLogger {

    @EventListener
    public void onUserLogin(UserLoginEvent event) {
        userActivityService.logActivity(
            event.getUserId(),
            ActivityType.LOGIN,
            event.getIpAddress(),
            event.getUserAgent()
        );
    }

    @EventListener
    public void onRoleChange(RoleChangeEvent event) {
        userActivityService.logActivity(
            event.getUserId(),
            ActivityType.ROLE_CHANGE,
            Map.of(
                "oldRole", event.getOldRole(),
                "newRole", event.getNewRole(),
                "changedBy", event.getChangedBy()
            )
        );
    }
}
```

### Frontend Implementation

#### Phase 1: Users List Page (Week 1)

**Component**: `UsersPage` (`src/app/users-page/`)

**Features**:
- Table showing all users in church
- Columns: Name, Email, Role, Status (Active/Inactive), Last Login, Actions
- Search by name/email
- Filter by role, status
- Pagination
- Sort by name, email, last login

**UI Layout**:
```
┌────────────────────────────────────────────────────────────┐
│ Users                                              [+ Invite User] │
├────────────────────────────────────────────────────────────┤
│ [Search...]  [Role: All ▼]  [Status: All ▼]  [Export]     │
├────────────────────────────────────────────────────────────┤
│ Name          Email           Role        Status  Actions  │
├────────────────────────────────────────────────────────────┤
│ John Doe      john@church.org  ADMIN      Active  [⋮]      │
│ Jane Smith    jane@church.org  PASTOR     Active  [⋮]      │
│ Bob Johnson   bob@church.org   TREASURER  Inactive [⋮]     │
│ ...                                                         │
└────────────────────────────────────────────────────────────┘
```

**Actions Menu** (⋮):
- View Details
- Edit User
- Change Role
- Activate/Deactivate
- View Activity Log
- Delete User (confirmation required)

#### Phase 2: Invite User Dialog (Week 1)

**Component**: `InviteUserDialog`

**Form Fields**:
- Email (required, validated)
- First Name (required)
- Last Name (required)
- Role (dropdown: ADMIN, PASTOR, TREASURER, MEMBER_MANAGER, FELLOWSHIP_LEADER, MEMBER)
- Fellowship (shown only if role = FELLOWSHIP_LEADER)
- Send Invitation Email (checkbox, default: checked)

**Validation**:
- Email must be valid format
- Email must not already exist in church
- All fields required except fellowship (conditional)

#### Phase 3: Edit User Dialog (Week 2)

**Component**: `EditUserDialog`

**Form Fields**:
- First Name
- Last Name
- Email (readonly - cannot change email)
- Status (Active/Inactive toggle)
- Role (dropdown with current role selected)
- Last Login (readonly, display only)

#### Phase 4: User Activity Log Dialog (Week 3)

**Component**: `UserActivityDialog`

**Display**:
- Timeline view of user activities
- Filters: Activity Type, Date Range
- Each entry shows: Timestamp, Activity Type, IP Address, Details

**Activity Types Displayed**:
- LOGIN - "Logged in from [IP]"
- LOGOUT - "Logged out"
- PASSWORD_CHANGE - "Changed password"
- ROLE_CHANGE - "Role changed from [OLD] to [NEW] by [ADMIN NAME]"
- PERMISSION_DENIED - "Access denied to [ENDPOINT]"

### API Endpoints Summary

| Method | Endpoint | Permission | Description |
|--------|----------|-----------|-------------|
| GET | /api/users | USER_VIEW | List all users (paginated) |
| GET | /api/users/{id} | USER_VIEW | Get user details |
| GET | /api/users/search | USER_VIEW | Search users by name/email |
| POST | /api/users/invite | USER_CREATE | Invite new user |
| PUT | /api/users/{id} | USER_EDIT | Update user info |
| POST | /api/users/{id}/activate | USER_EDIT | Activate user |
| POST | /api/users/{id}/deactivate | USER_EDIT | Deactivate user |
| PUT | /api/users/{id}/role | USER_MANAGE_ROLES | Change user role |
| DELETE | /api/users/{id} | USER_DELETE | Delete user |
| GET | /api/users/{id}/activity | USER_VIEW | Get user activity log |
| POST | /api/users/invitations/accept | (public) | Accept invitation & create account |
| GET | /api/users/invitations/{token} | (public) | Get invitation details |

### Testing Plan

#### Unit Tests
- UserManagementService - CRUD operations
- UserInvitationService - Invitation creation, expiration, acceptance
- UserActivityService - Activity logging

#### Integration Tests
- Invite user → Send email → Accept invitation → Create account
- Change user role → Log activity → Verify role change
- Deactivate user → Verify cannot login
- Delete user → Verify cascade deletion

#### E2E Tests (Playwright)
- Navigate to Users page
- Click "Invite User"
- Fill form, submit
- Verify user appears in list
- Click actions menu → Change Role
- Verify role updated in UI
- View activity log
- Verify activity shown

### Effort Estimate

**Backend**: 2-3 weeks
- Phase 1 (Core CRUD): 1 week
- Phase 2 (Invitations): 1 week
- Phase 3 (Activity Logging): 3-5 days

**Frontend**: 2-3 weeks
- Phase 1 (Users List): 1 week
- Phase 2 (Invite/Edit Dialogs): 1 week
- Phase 3 (Activity Log): 3-5 days

**Total**: 4-6 weeks (1-1.5 months)

---

## 3. Pricing on Landing Page

### Status: ❌ NOT STARTED

### Overview

Add a comprehensive pricing section to the PastCare landing page to showcase subscription tiers and drive conversions.

### Business Goals

1. **Transparency**: Clear pricing reduces friction in decision-making
2. **Conversion**: Pricing page drives sign-ups for FREE and BASIC tiers
3. **Upsell**: Encourage upgrades to PRO and ENTERPRISE tiers
4. **Trust**: Professional pricing builds credibility

### Pricing Tiers (from ARCHITECTURE_CRITICAL_ISSUES.md)

Based on the billing module design:

| Tier | Price | Members | Fellowships | SMS/month | Storage | Features |
|------|-------|---------|-------------|-----------|---------|----------|
| **FREE** | GHS 0 | 50 | 3 | 0 | 500MB | Basic member management, limited attendance |
| **BASIC** | GHS 99/mo | 200 | 10 | 100 | 5GB | + Online giving (3% fee), basic reports, email support |
| **PRO** | GHS 499/mo | 1,000 | Unlimited | 500 | 50GB | + SMS communications, advanced analytics, custom fields, priority support |
| **ENTERPRISE** | Custom | Unlimited | Unlimited | Unlimited | Unlimited | + API access, custom integrations, dedicated account manager, SLA |

**Annual Discount**: 2 months free (16% off) on yearly plans

### Design Concept

#### Section Layout

**Hero Section** (existing - no changes)

**Pricing Section** (new):
```
┌────────────────────────────────────────────────────────────────┐
│               Choose the Perfect Plan for Your Church           │
│          Start free, upgrade anytime. No credit card required.  │
│                                                                  │
│ ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────┐ │
│ │    FREE     │  │    BASIC    │  │     PRO     │  │ ENTERPRISE│
│ │  GHS 0/mo   │  │  GHS 99/mo  │  │ GHS 499/mo  │  │  Custom  │
│ ├─────────────┤  ├─────────────┤  ├─────────────┤  ├─────────┤ │
│ │ ✓ 50 members│  │ ✓ 200 members│  │ ✓ 1,000 mbrs│  │✓Unlimited│
│ │ ✓ 3 fellows │  │ ✓ 10 fellows │  │ ✓ Unlimited │  │✓Unlimited│
│ │ ✓ Basic mgmt│  │ ✓ Online give│  │ ✓ SMS comms │  │✓ API     │
│ │ ✓ Attendance│  │ ✓ Reports    │  │ ✓ Analytics │  │✓ SLA     │
│ │ ✓ Events    │  │ ✓ Email supp │  │ ✓ Priority  │  │✓ Manager │
│ │             │  │ ✓ 100 SMS/mo │  │ ✓ 500 SMS/mo│  │✓ Custom  │
│ │ [Get Started]  │ [Start Free Trial]│ [Start Free Trial]│ [Contact Sales]│
│ └─────────────┘  └─────────────┘  └─────────────┘  └─────────┘ │
└────────────────────────────────────────────────────────────────┘
```

#### Design Specifications

**Color Scheme**:
- FREE: Light gray border, white background
- BASIC: Blue border (primary color), white background
- PRO: Gradient border (primary + accent), highlighted (Most Popular badge)
- ENTERPRISE: Dark border, subtle gradient background

**Typography**:
- Tier Name: 24px, bold, uppercase
- Price: 36px, bold
- `/mo`: 16px, regular, gray
- Features: 16px, regular, line-height: 1.8

**Spacing**:
- Cards: 24px gap between cards
- Card padding: 32px
- Feature list: 16px vertical spacing
- Button margin-top: 32px

**Components**:
```typescript
<div class="pricing-section">
  <h2>Choose the Perfect Plan for Your Church</h2>
  <p class="subtitle">Start free, upgrade anytime. No credit card required.</p>

  <div class="billing-toggle">
    <span [class.active]="billingCycle === 'monthly'">Monthly</span>
    <label class="switch">
      <input type="checkbox" [(ngModel)]="isYearly" (change)="toggleBilling()">
      <span class="slider"></span>
    </label>
    <span [class.active]="billingCycle === 'yearly'">
      Yearly <span class="badge">Save 16%</span>
    </span>
  </div>

  <div class="pricing-cards">
    <pricing-card
      *ngFor="let tier of pricingTiers"
      [tier]="tier"
      [billingCycle]="billingCycle"
      (ctaClick)="onCTAClick(tier)">
    </pricing-card>
  </div>

  <div class="faq-section">
    <h3>Frequently Asked Questions</h3>
    <faq-accordion [items]="faqItems"></faq-accordion>
  </div>

  <div class="comparison-table">
    <h3>Compare All Features</h3>
    <feature-comparison-table [tiers]="pricingTiers"></feature-comparison-table>
  </div>
</div>
```

#### Pricing Card Component

```typescript
@Component({
  selector: 'pricing-card',
  template: `
    <div class="card" [class.popular]="tier.isPopular">
      <div class="badge" *ngIf="tier.isPopular">Most Popular</div>
      <h3 class="tier-name">{{ tier.name }}</h3>
      <div class="price">
        <span class="currency">GHS</span>
        <span class="amount">{{ getPrice() }}</span>
        <span class="period">/{{ billingCycle === 'yearly' ? 'year' : 'month' }}</span>
      </div>
      <div class="savings" *ngIf="billingCycle === 'yearly' && tier.price > 0">
        Save GHS {{ getSavings() }}
      </div>
      <ul class="features">
        <li *ngFor="let feature of tier.features">
          <i class="check-icon">✓</i> {{ feature }}
        </li>
      </ul>
      <button class="cta-button" (click)="ctaClick.emit()">
        {{ tier.ctaText }}
      </button>
      <p class="trial-text" *ngIf="tier.hasTrialDays">
        {{ tier.trialDays }}-day free trial, no credit card required
      </p>
    </div>
  `
})
export class PricingCardComponent {
  @Input() tier: PricingTier;
  @Input() billingCycle: 'monthly' | 'yearly' = 'monthly';
  @Output() ctaClick = new EventEmitter<void>();

  getPrice(): number {
    if (this.billingCycle === 'yearly') {
      return this.tier.yearlyPrice || (this.tier.monthlyPrice * 10); // 2 months free
    }
    return this.tier.monthlyPrice;
  }

  getSavings(): number {
    return this.tier.monthlyPrice * 12 - (this.tier.yearlyPrice || 0);
  }
}
```

#### Data Model

```typescript
interface PricingTier {
  id: string;
  name: string;
  monthlyPrice: number;
  yearlyPrice?: number;
  isPopular?: boolean;
  trialDays?: number;
  hasTrialDays: boolean;
  ctaText: string;
  features: string[];
  limits: {
    members: number | string;  // number or "Unlimited"
    fellowships: number | string;
    smsPerMonth: number | string;
    storage: string;  // "500MB", "5GB", etc.
    users: number | string;
  };
}

const PRICING_TIERS: PricingTier[] = [
  {
    id: 'free',
    name: 'FREE',
    monthlyPrice: 0,
    yearlyPrice: 0,
    isPopular: false,
    hasTrialDays: false,
    ctaText: 'Get Started',
    features: [
      'Up to 50 members',
      '3 fellowships',
      'Basic member management',
      'Attendance tracking',
      'Event management',
      '500MB storage',
      '2 admin users'
    ],
    limits: {
      members: 50,
      fellowships: 3,
      smsPerMonth: 0,
      storage: '500MB',
      users: 2
    }
  },
  {
    id: 'basic',
    name: 'BASIC',
    monthlyPrice: 99,
    yearlyPrice: 990,  // 10 months price (2 months free)
    isPopular: false,
    trialDays: 14,
    hasTrialDays: true,
    ctaText: 'Start Free Trial',
    features: [
      'Up to 200 members',
      '10 fellowships',
      'Online giving (3% + GHS 0.50 fee)',
      'Basic reports',
      '100 SMS per month',
      '5GB storage',
      '5 admin users',
      'Email support'
    ],
    limits: {
      members: 200,
      fellowships: 10,
      smsPerMonth: 100,
      storage: '5GB',
      users: 5
    }
  },
  {
    id: 'pro',
    name: 'PRO',
    monthlyPrice: 499,
    yearlyPrice: 4990,  // 10 months price
    isPopular: true,
    trialDays: 14,
    hasTrialDays: true,
    ctaText: 'Start Free Trial',
    features: [
      'Up to 1,000 members',
      'Unlimited fellowships',
      'SMS communications (500/month)',
      'Advanced analytics',
      'Custom fields',
      'Bulk operations',
      '50GB storage',
      '20 admin users',
      'Priority support'
    ],
    limits: {
      members: 1000,
      fellowships: 'Unlimited',
      smsPerMonth: 500,
      storage: '50GB',
      users: 20
    }
  },
  {
    id: 'enterprise',
    name: 'ENTERPRISE',
    monthlyPrice: 0,  // Custom pricing
    isPopular: false,
    hasTrialDays: true,
    trialDays: 30,
    ctaText: 'Contact Sales',
    features: [
      'Unlimited members',
      'Unlimited fellowships',
      'Unlimited SMS',
      'API access',
      'Custom integrations',
      'Dedicated account manager',
      'SLA guarantee',
      'Unlimited storage',
      'Unlimited users',
      'Custom training'
    ],
    limits: {
      members: 'Unlimited',
      fellowships: 'Unlimited',
      smsPerMonth: 'Unlimited',
      storage: 'Unlimited',
      users: 'Unlimited'
    }
  }
];
```

### FAQ Section

**Common Questions**:

1. **Can I change plans later?**
   - Yes, you can upgrade or downgrade at any time. Changes take effect at the start of the next billing cycle.

2. **What happens if I exceed my limits?**
   - Members: You'll be notified when you reach 80% capacity and prompted to upgrade.
   - SMS: Once you hit your monthly limit, you can purchase additional SMS credits or upgrade.
   - Storage: You'll receive warnings at 80%, 90%, and 100% capacity.

3. **Is there a free trial?**
   - Yes! BASIC and PRO tiers include a 14-day free trial. ENTERPRISE includes 30 days. No credit card required.

4. **What payment methods do you accept?**
   - Mobile Money (MTN, Vodafone Cash), Bank Transfer, Credit/Debit Cards (via Paystack).

5. **Can I get a refund?**
   - Yes, we offer a 30-day money-back guarantee for all paid plans.

6. **What's included in "Online Giving"?**
   - Integrated donation forms for your church website/member portal, recurring donations, campaign tracking, and donor receipts.

7. **How does SMS pricing work?**
   - Each plan includes a monthly SMS allowance. Additional credits can be purchased at GHS 0.10 per SMS.

8. **What kind of support do you offer?**
   - FREE: Self-service help center
   - BASIC: Email support (48h response)
   - PRO: Priority email + phone support (24h response)
   - ENTERPRISE: Dedicated account manager + 24/7 support

### Feature Comparison Table

| Feature | FREE | BASIC | PRO | ENTERPRISE |
|---------|------|-------|-----|------------|
| **Members** | 50 | 200 | 1,000 | Unlimited |
| **Fellowships** | 3 | 10 | Unlimited | Unlimited |
| **Attendance Tracking** | ✓ | ✓ | ✓ | ✓ |
| **Event Management** | ✓ | ✓ | ✓ | ✓ |
| **Online Giving** | ✗ | ✓ (3% fee) | ✓ (3% fee) | ✓ (custom) |
| **SMS Communications** | ✗ | 100/mo | 500/mo | Unlimited |
| **Reports** | Basic | Standard | Advanced | Custom |
| **Analytics** | ✗ | ✗ | ✓ | ✓ |
| **Custom Fields** | ✗ | ✗ | ✓ | ✓ |
| **API Access** | ✗ | ✗ | ✗ | ✓ |
| **Storage** | 500MB | 5GB | 50GB | Unlimited |
| **Users** | 2 | 5 | 20 | Unlimited |
| **Support** | Self-service | Email | Priority | Dedicated |

### Implementation Plan

#### Phase 1: Design & Data (Week 1)

**Tasks**:
1. Create `pricing-tiers.ts` with tier definitions
2. Design pricing card component (Figma/mockup)
3. Get approval on pricing, features, and copy

**Deliverables**:
- Pricing data model
- Component design mockups
- Copywriting for tier descriptions

#### Phase 2: Frontend Development (Week 2)

**Tasks**:
1. Create `PricingCardComponent`
2. Create `FeatureComparisonTableComponent`
3. Create `FaqAccordionComponent`
4. Add pricing section to landing page
5. Implement billing toggle (monthly/yearly)
6. Style components (responsive design)

**Files to Create**:
- `src/app/landing/pricing-card/pricing-card.component.ts`
- `src/app/landing/pricing-card/pricing-card.component.html`
- `src/app/landing/pricing-card/pricing-card.component.scss`
- `src/app/landing/feature-comparison-table/feature-comparison-table.component.ts`
- `src/app/landing/faq-accordion/faq-accordion.component.ts`

#### Phase 3: CTA Integration (Week 2-3)

**CTA Actions**:
- FREE → Redirect to `/register`
- BASIC/PRO → Redirect to `/register?plan=basic` (with 14-day trial)
- ENTERPRISE → Open contact form or redirect to `/contact-sales`

**Registration Flow Enhancement**:
1. Detect `?plan=basic` or `?plan=pro` in URL
2. Pre-select plan in registration form
3. Show trial countdown: "14 days free, then GHS 99/month"
4. No credit card required during trial
5. Send reminder email 3 days before trial ends

#### Phase 4: Testing & Optimization (Week 3)

**A/B Testing Ideas**:
- Test different pricing copy
- Test annual discount messaging ("Save 16%" vs "2 Months Free")
- Test CTA button text ("Start Free Trial" vs "Try 14 Days Free")

**Analytics Tracking**:
- Track pricing section views (scroll depth)
- Track CTA clicks per tier
- Track conversion rate per tier
- Track billing toggle usage (monthly vs yearly)

### Effort Estimate

**Frontend Only**: 2-3 weeks
- Design & Data: 3-5 days
- Component Development: 5-7 days
- CTA Integration: 2-3 days
- Testing & Polish: 2-3 days

**Note**: Backend billing system (payment processing, subscriptions, quotas) is a separate 10-week project (see Issue #1 in ARCHITECTURE_CRITICAL_ISSUES.md). This pricing page can be deployed **before** the billing backend is ready.

---

## 4. Platform Admin Dashboard

### Status: ❌ NOT STARTED

### Overview

A comprehensive dashboard for SUPERADMIN users to manage all churches, monitor platform health, handle billing, and view analytics across the entire platform.

### Business Requirements

**Primary Goals**:
1. Monitor platform-wide KPIs (total churches, active users, revenue)
2. Manage all churches (view, edit, suspend, delete)
3. Handle billing and subscriptions
4. View security violations and system health
5. Manage platform settings and configuration

**Target Users**:
- SUPERADMIN role only

### Architecture

#### Access Control

**CRITICAL**: Platform admin dashboard must be **completely isolated** from church-level dashboards.

**URL Structure**:
- Church Dashboard: `/dashboard` (existing)
- Platform Admin Dashboard: `/platform-admin` (new)

**Route Guard**:
```typescript
@Injectable({ providedIn: 'root' })
export class PlatformAdminGuard implements CanActivate {
  constructor(private authService: AuthService, private router: Router) {}

  canActivate(): boolean {
    if (this.authService.isSuperAdmin()) {
      return true;
    }
    this.router.navigate(['/dashboard']);  // Redirect to church dashboard
    return false;
  }
}

// In routing module:
{
  path: 'platform-admin',
  component: PlatformAdminDashboardComponent,
  canActivate: [AuthGuard, PlatformAdminGuard],  // Double-check
  children: [
    { path: '', redirectTo: 'overview', pathMatch: 'full' },
    { path: 'overview', component: PlatformOverviewComponent },
    { path: 'churches', component: ChurchesManagementComponent },
    { path: 'churches/:id', component: ChurchDetailComponent },
    { path: 'billing', component: PlatformBillingComponent },
    { path: 'users', component: PlatformUsersComponent },
    { path: 'security', component: SecurityDashboardComponent },
    { path: 'settings', component: PlatformSettingsComponent }
  ]
}
```

### Dashboard Sections

#### 1. Overview (Landing Page)

**KPI Cards**:
```
┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│ Total Churches│  │ Active Users │  │ Monthly Revenue│  │ System Health│
│     1,247    │  │    4,893     │  │  GHS 147,300  │  │    99.8%     │
│ +12 this week│  │ +234 this mo │  │  +18% vs last │  │   All OK ✓   │
└──────────────┘  └──────────────┘  └──────────────┘  └──────────────┘
```

**Charts**:
- Church Growth (line chart - last 12 months)
- Revenue Trends (bar chart - monthly recurring revenue)
- Plan Distribution (pie chart - FREE, BASIC, PRO, ENTERPRISE)
- User Activity (heat map - logins per day/hour)

**Recent Activity**:
- New church registrations
- Plan upgrades/downgrades
- Failed payments
- Security violations

#### 2. Churches Management

**Church List**:
```
┌────────────────────────────────────────────────────────────────────┐
│ Churches                                    [+ Add Church] [Export]│
├────────────────────────────────────────────────────────────────────┤
│ [Search...]  [Plan: All ▼]  [Status: All ▼]  [Region: All ▼]      │
├────────────────────────────────────────────────────────────────────┤
│ Name              Plan    Members  Status   MRR      Actions       │
├────────────────────────────────────────────────────────────────────┤
│ First Baptist     PRO     847      Active   GHS 499  [⋮]           │
│ Grace Chapel      BASIC   156      Active   GHS 99   [⋮]           │
│ New Life Church   FREE    42       Trial    GHS 0    [⋮]           │
│ ...                                                                 │
└────────────────────────────────────────────────────────────────────┘
```

**Church Detail View**:
```
┌────────────────────────────────────────────────────────────────────┐
│ First Baptist Church                           [Edit] [Suspend]    │
├────────────────────────────────────────────────────────────────────┤
│ Overview                                                            │
│   Contact: Pastor John Doe (john@firstbaptist.org)                │
│   Phone: +233 24 123 4567                                          │
│   Location: Accra, Ghana                                           │
│   Registered: 2024-03-15                                           │
│                                                                     │
│ Subscription                                                        │
│   Plan: PRO (GHS 499/month)                                        │
│   Status: Active                                                   │
│   Next Billing: 2025-01-15                                         │
│   Payment Method: Mobile Money (MTN)                               │
│                                                                     │
│ Usage                                                               │
│   Members: 847 / 1,000 (85%)                                       │
│   Fellowships: 24 (Unlimited)                                      │
│   SMS Used: 384 / 500 this month (77%)                            │
│   Storage: 28.4 GB / 50 GB (57%)                                   │
│                                                                     │
│ Users (5)                                                           │
│   - John Doe (ADMIN)                                               │
│   - Jane Smith (PASTOR)                                            │
│   - Bob Johnson (TREASURER)                                        │
│   ...                                                               │
│                                                                     │
│ Activity                                                            │
│   Last Login: 2025-12-29 08:32 AM (John Doe)                      │
│   Last Donation: 2025-12-28 03:15 PM                               │
│   Recent Violations: 0                                             │
│                                                                     │
│ [View Detailed Logs] [Billing History] [Impersonate Admin]        │
└────────────────────────────────────────────────────────────────────┘
```

**Actions Menu** (⋮):
- View Details
- Edit Church Info
- View Billing History
- Manage Subscription (upgrade/downgrade)
- Suspend Church (disables access)
- Reactivate Church
- Impersonate Admin (login as church admin)
- Delete Church (requires confirmation + password)

**Impersonate Feature**:
- SUPERADMIN can temporarily "login as" any church admin
- Useful for debugging and support
- Banner shown: "You are viewing as [Church Name]. Exit Impersonation"
- All actions logged to audit trail

#### 3. Billing Management

**Overview**:
```
┌────────────────────────────────────────────────────────────────────┐
│ Billing Overview                                                    │
├────────────────────────────────────────────────────────────────────┤
│ Monthly Recurring Revenue (MRR)                                    │
│   Current MRR: GHS 147,300                                         │
│   vs Last Month: +18% (GHS 22,400)                                │
│   Annual Run Rate: GHS 1,767,600                                   │
│                                                                     │
│ Revenue by Plan                                                     │
│   FREE: GHS 0 (438 churches)                                       │
│   BASIC: GHS 29,700 (300 churches @ GHS 99)                       │
│   PRO: GHS 109,780 (220 churches @ GHS 499)                       │
│   ENTERPRISE: GHS 7,820 (custom pricing, 8 churches)              │
│                                                                     │
│ Payment Status                                                      │
│   Successful: 98.4% (GHS 145,000)                                  │
│   Failed: 1.6% (GHS 2,300) ⚠️                                      │
│                                                                     │
│ Upcoming Renewals (Next 7 Days)                                    │
│   124 churches, GHS 48,650 expected                                │
│                                                                     │
│ [View Failed Payments] [Download Report]                          │
└────────────────────────────────────────────────────────────────────┘
```

**Failed Payments**:
- List of churches with failed payments
- Retry count, last attempt, failure reason
- Actions: Retry Payment, Contact Church, Suspend Account

**Invoice Management**:
- Search invoices by church, date range, status
- Regenerate invoices
- Send invoice reminders
- Mark as paid (manual)

#### 4. Users Management (Platform-Wide)

**All Users Across All Churches**:
```
┌────────────────────────────────────────────────────────────────────┐
│ Platform Users                                  [Export]            │
├────────────────────────────────────────────────────────────────────┤
│ [Search...]  [Church: All ▼]  [Role: All ▼]  [Status: All ▼]      │
├────────────────────────────────────────────────────────────────────┤
│ Name         Email              Church          Role     Status    │
├────────────────────────────────────────────────────────────────────┤
│ John Doe     john@first.org    First Baptist   ADMIN    Active    │
│ Jane Smith   jane@grace.org    Grace Chapel    PASTOR   Active    │
│ ...                                                                 │
└────────────────────────────────────────────────────────────────────┘
```

**Features**:
- Search across all churches
- Filter by church, role, status
- View user activity across multiple churches (if user is in multiple churches)
- Suspend/reactivate users
- Reset passwords (send reset email)

#### 5. Security Dashboard

**See Backend Implementation** from previous session:
- Security violation statistics
- Recent violations table
- Top violating users/churches
- Security trends chart

**Additional Features**:
- Real-time alerts for critical violations
- Automated response rules:
  - Auto-suspend user after 10 violations in 24h
  - Auto-suspend church after 50 violations in 7 days
  - Email platform admin for critical violations
- Security audit log export (CSV)

#### 6. Platform Settings

**Global Configuration**:

```
┌────────────────────────────────────────────────────────────────────┐
│ Platform Settings                                                   │
├────────────────────────────────────────────────────────────────────┤
│ General                                                             │
│   Platform Name: [PastCare]                                        │
│   Support Email: [support@pastcare.com]                           │
│   Default Timezone: [Africa/Accra]                                │
│   Default Currency: [GHS]                                          │
│                                                                     │
│ Subscription Plans                                                  │
│   [Edit Plan Pricing] [Add New Plan] [View Plan History]          │
│                                                                     │
│ Email Configuration                                                 │
│   Provider: [SendGrid ▼]                                           │
│   From Address: [noreply@pastcare.com]                            │
│   [Test Email Configuration]                                       │
│                                                                     │
│ SMS Configuration                                                   │
│   Provider: [Twilio ▼]                                             │
│   Default Sender: [PastCare]                                       │
│   Bulk SMS Limit: [1000] per church per day                       │
│                                                                     │
│ Payment Gateway                                                     │
│   Provider: [Paystack ▼]                                           │
│   Public Key: [pk_live_...]                                        │
│   Secret Key: [••••••••]                                           │
│   Webhook URL: [https://api.pastcare.com/webhooks/paystack]       │
│   [Test Connection]                                                │
│                                                                     │
│ Feature Flags                                                       │
│   ☑ Enable SMS Module                                             │
│   ☑ Enable Online Giving                                          │
│   ☑ Enable Member Portal                                          │
│   ☐ Enable API Access (ENTERPRISE only)                           │
│                                                                     │
│ Security                                                            │
│   Session Timeout: [30] minutes                                    │
│   Password Policy: [Strong - min 8 chars, uppercase, number]      │
│   2FA Requirement: [Optional ▼]                                    │
│   Max Login Attempts: [5] per 15 minutes                          │
│                                                                     │
│ [Save Changes]                                                     │
└────────────────────────────────────────────────────────────────────┘
```

### Backend Implementation

#### API Endpoints (SUPERADMIN Only)

All endpoints require `@RequirePermission(Permission.PLATFORM_ACCESS)`:

```java
@RestController
@RequestMapping("/api/platform-admin")
public class PlatformAdminController {

    // Overview
    @GetMapping("/stats")
    @RequirePermission(Permission.PLATFORM_ACCESS)
    public ResponseEntity<PlatformStatsResponse> getPlatformStats() { }

    // Churches
    @GetMapping("/churches")
    @RequirePermission(Permission.ALL_CHURCHES_VIEW)
    public ResponseEntity<Page<ChurchResponse>> getAllChurches(Pageable pageable) { }

    @GetMapping("/churches/{id}")
    @RequirePermission(Permission.ALL_CHURCHES_VIEW)
    public ResponseEntity<ChurchDetailResponse> getChurchDetail(@PathVariable Long id) { }

    @PutMapping("/churches/{id}")
    @RequirePermission(Permission.ALL_CHURCHES_MANAGE)
    public ResponseEntity<ChurchResponse> updateChurch(@PathVariable Long id, @RequestBody ChurchRequest request) { }

    @PostMapping("/churches/{id}/suspend")
    @RequirePermission(Permission.ALL_CHURCHES_MANAGE)
    public ResponseEntity<Void> suspendChurch(@PathVariable Long id) { }

    @PostMapping("/churches/{id}/reactivate")
    @RequirePermission(Permission.ALL_CHURCHES_MANAGE)
    public ResponseEntity<Void> reactivateChurch(@PathVariable Long id) { }

    @DeleteMapping("/churches/{id}")
    @RequirePermission(Permission.ALL_CHURCHES_MANAGE)
    public ResponseEntity<Void> deleteChurch(@PathVariable Long id) { }

    @PostMapping("/churches/{id}/impersonate")
    @RequirePermission(Permission.ALL_CHURCHES_MANAGE)
    public ResponseEntity<ImpersonationTokenResponse> impersonateChurchAdmin(@PathVariable Long id) { }

    // Billing
    @GetMapping("/billing/overview")
    @RequirePermission(Permission.BILLING_MANAGE)
    public ResponseEntity<BillingOverviewResponse> getBillingOverview() { }

    @GetMapping("/billing/failed-payments")
    @RequirePermission(Permission.BILLING_MANAGE)
    public ResponseEntity<Page<FailedPaymentResponse>> getFailedPayments(Pageable pageable) { }

    @PostMapping("/billing/invoices/{id}/retry")
    @RequirePermission(Permission.BILLING_MANAGE)
    public ResponseEntity<Void> retryPayment(@PathVariable Long id) { }

    // Users
    @GetMapping("/users")
    @RequirePermission(Permission.PLATFORM_ACCESS)
    public ResponseEntity<Page<UserResponse>> getAllPlatformUsers(Pageable pageable) { }

    @PostMapping("/users/{id}/reset-password")
    @RequirePermission(Permission.ALL_CHURCHES_MANAGE)
    public ResponseEntity<Void> resetUserPassword(@PathVariable Long id) { }

    // Settings
    @GetMapping("/settings")
    @RequirePermission(Permission.SYSTEM_CONFIG)
    public ResponseEntity<PlatformSettingsResponse> getPlatformSettings() { }

    @PutMapping("/settings")
    @RequirePermission(Permission.SYSTEM_CONFIG)
    public ResponseEntity<PlatformSettingsResponse> updatePlatformSettings(@RequestBody PlatformSettingsRequest request) { }
}
```

#### Database Schema

**Platform Settings**:
```sql
CREATE TABLE platform_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    setting_key VARCHAR(100) NOT NULL UNIQUE,
    setting_value TEXT,
    setting_type VARCHAR(20) NOT NULL,  -- STRING, INTEGER, BOOLEAN, JSON
    description TEXT,
    is_encrypted BOOLEAN DEFAULT FALSE,
    updated_by_id BIGINT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_settings_updated_by FOREIGN KEY (updated_by_id)
        REFERENCES user(id) ON DELETE SET NULL,

    INDEX idx_settings_key (setting_key)
);

-- Examples:
-- INSERT INTO platform_settings (setting_key, setting_value, setting_type, description)
-- VALUES ('platform.name', 'PastCare', 'STRING', 'Platform display name');
-- VALUES ('platform.support_email', 'support@pastcare.com', 'STRING', 'Support contact email');
-- VALUES ('payment.paystack_public_key', 'pk_live_...', 'STRING', 'Paystack public key');
-- VALUES ('payment.paystack_secret_key', 'sk_live_...', 'STRING', 'Paystack secret key (encrypted)');
-- VALUES ('sms.daily_limit_per_church', '1000', 'INTEGER', 'Max SMS per church per day');
```

**Impersonation Log**:
```sql
CREATE TABLE impersonation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    superadmin_id BIGINT NOT NULL,
    impersonated_user_id BIGINT NOT NULL,
    church_id BIGINT NOT NULL,
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ended_at TIMESTAMP,
    duration_seconds INT,
    actions_performed INT DEFAULT 0,

    CONSTRAINT fk_impersonate_superadmin FOREIGN KEY (superadmin_id)
        REFERENCES user(id) ON DELETE CASCADE,
    CONSTRAINT fk_impersonate_user FOREIGN KEY (impersonated_user_id)
        REFERENCES user(id) ON DELETE CASCADE,
    CONSTRAINT fk_impersonate_church FOREIGN KEY (church_id)
        REFERENCES church(id) ON DELETE CASCADE,

    INDEX idx_impersonate_superadmin (superadmin_id),
    INDEX idx_impersonate_user (impersonated_user_id),
    INDEX idx_impersonate_started (started_at)
);
```

### Frontend Implementation

#### Navigation

**SUPERADMIN sees two dashboards**:
```
Main Nav (when logged in as SUPERADMIN):
  - Church Dashboard (switch to church view)
  - Platform Admin (switch to platform view)
  - Logout
```

**Side Nav (Platform Admin)**:
```
Platform Admin Dashboard
├── Overview
├── Churches
│   ├── All Churches
│   ├── Active Subscriptions
│   ├── Trial Churches
│   └── Suspended Churches
├── Billing
│   ├── Overview
│   ├── Invoices
│   ├── Failed Payments
│   └── Revenue Reports
├── Users (Platform-Wide)
├── Security
│   ├── Dashboard
│   ├── Violations
│   └── Audit Logs
├── Analytics
│   ├── Usage Metrics
│   ├── Growth Trends
│   └── Retention Reports
└── Settings
    ├── General
    ├── Subscription Plans
    ├── Payment Gateway
    ├── Email/SMS Config
    └── Feature Flags
```

### Effort Estimate

**Backend**: 4-6 weeks
- Phase 1 (API Endpoints): 2 weeks
- Phase 2 (Settings Management): 1 week
- Phase 3 (Impersonation): 1 week
- Phase 4 (Analytics): 1 week

**Frontend**: 4-6 weeks
- Phase 1 (Overview + Churches): 2 weeks
- Phase 2 (Billing + Users): 1.5 weeks
- Phase 3 (Security Dashboard): 1 week
- Phase 4 (Settings): 1 week

**Total**: 8-12 weeks (2-3 months)

**Priority**: MEDIUM (can be deferred until after User Management Module and RBAC completion)

---

## Summary & Prioritization

### Recommended Implementation Order

1. **RBAC Endpoint Protection** (1-2 weeks) - 🔴 HIGH PRIORITY - IN PROGRESS
   - Started: CampaignController completed
   - Next: PledgeController, RecurringDonationController, then communication controllers
   - Critical for security

2. **Pricing on Landing Page** (2-3 weeks) - 🟡 MEDIUM PRIORITY
   - Can be done in parallel with RBAC
   - Frontend-only, no backend dependencies
   - Drives conversions

3. **User Management Module** (4-6 weeks) - 🟡 MEDIUM PRIORITY
   - After RBAC endpoint protection is complete
   - Needed for church admin operations
   - Enables user invitations, role management

4. **Platform Admin Dashboard** (8-12 weeks) - 🟢 LOW PRIORITY
   - Can be deferred until after other priorities
   - Needed for platform management and support
   - Requires billing system to be useful

### Total Effort

- RBAC: 1-2 weeks (already started)
- Pricing: 2-3 weeks
- User Management: 4-6 weeks
- Platform Admin: 8-12 weeks

**Total**: 15-23 weeks (3.5-6 months)

**Realistic Timeline**: 4-5 months (accounting for testing, bug fixes, and iteration)

---

**Document Status**: Complete
**Created**: 2025-12-29
**Author**: Claude Sonnet 4.5
**Next Steps**: Begin protecting remaining controllers with @RequirePermission annotations
