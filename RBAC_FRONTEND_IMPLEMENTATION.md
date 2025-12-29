# RBAC Frontend Implementation - Complete Guide

**Date**: 2025-12-29
**Goal**: Implement permission-based UI visibility across all Angular components
**Estimated Effort**: 1-2 weeks

---

## Overview

Extend RBAC to the frontend by:
1. Creating permission directive to show/hide UI elements
2. Enhancing AuthService with permission checking
3. Adding route guards for permission-based routing
4. Updating all components to use permission checks

---

## Architecture

### Current State
- JWT contains: `userId`, `churchId`, `role`
- Frontend stores user info in AuthService
- No permission-based UI hiding currently

### Target State
- AuthService exposes `hasPermission()`, `hasAnyPermission()`, `hasAllPermissions()`
- `*hasPermission` directive shows/hides elements based on permissions
- Route guards prevent unauthorized navigation
- All action buttons (Create, Edit, Delete, Export) conditionally shown

---

## Implementation Plan

### Phase 1: Core Infrastructure (2-3 days)

#### 1.1 Create Permission Enum (TypeScript)

**File**: `src/app/enums/permission.enum.ts`

```typescript
/**
 * Permission enum matching backend Permission.java
 * IMPORTANT: Keep in sync with backend permissions
 */
export enum Permission {
  // ========== MEMBER PERMISSIONS ==========
  MEMBER_VIEW_ALL = 'MEMBER_VIEW_ALL',
  MEMBER_VIEW_OWN = 'MEMBER_VIEW_OWN',
  MEMBER_VIEW_FELLOWSHIP = 'MEMBER_VIEW_FELLOWSHIP',
  MEMBER_CREATE = 'MEMBER_CREATE',
  MEMBER_EDIT_ALL = 'MEMBER_EDIT_ALL',
  MEMBER_EDIT_OWN = 'MEMBER_EDIT_OWN',
  MEMBER_EDIT_PASTORAL = 'MEMBER_EDIT_PASTORAL',
  MEMBER_DELETE = 'MEMBER_DELETE',
  MEMBER_EXPORT = 'MEMBER_EXPORT',
  MEMBER_IMPORT = 'MEMBER_IMPORT',

  // ========== HOUSEHOLD PERMISSIONS ==========
  HOUSEHOLD_VIEW = 'HOUSEHOLD_VIEW',
  HOUSEHOLD_CREATE = 'HOUSEHOLD_CREATE',
  HOUSEHOLD_EDIT = 'HOUSEHOLD_EDIT',
  HOUSEHOLD_DELETE = 'HOUSEHOLD_DELETE',

  // ========== FELLOWSHIP PERMISSIONS ==========
  FELLOWSHIP_VIEW_ALL = 'FELLOWSHIP_VIEW_ALL',
  FELLOWSHIP_VIEW_OWN = 'FELLOWSHIP_VIEW_OWN',
  FELLOWSHIP_CREATE = 'FELLOWSHIP_CREATE',
  FELLOWSHIP_EDIT_ALL = 'FELLOWSHIP_EDIT_ALL',
  FELLOWSHIP_EDIT_OWN = 'FELLOWSHIP_EDIT_OWN',
  FELLOWSHIP_DELETE = 'FELLOWSHIP_DELETE',
  FELLOWSHIP_MANAGE_MEMBERS = 'FELLOWSHIP_MANAGE_MEMBERS',

  // ========== FINANCIAL PERMISSIONS ==========
  DONATION_VIEW_ALL = 'DONATION_VIEW_ALL',
  DONATION_VIEW_OWN = 'DONATION_VIEW_OWN',
  DONATION_CREATE = 'DONATION_CREATE',
  DONATION_EDIT = 'DONATION_EDIT',
  DONATION_DELETE = 'DONATION_DELETE',
  DONATION_EXPORT = 'DONATION_EXPORT',

  CAMPAIGN_VIEW = 'CAMPAIGN_VIEW',
  CAMPAIGN_MANAGE = 'CAMPAIGN_MANAGE',

  PLEDGE_VIEW_ALL = 'PLEDGE_VIEW_ALL',
  PLEDGE_VIEW_OWN = 'PLEDGE_VIEW_OWN',
  PLEDGE_MANAGE = 'PLEDGE_MANAGE',

  RECEIPT_ISSUE = 'RECEIPT_ISSUE',

  // ========== EVENT PERMISSIONS ==========
  EVENT_VIEW_ALL = 'EVENT_VIEW_ALL',
  EVENT_VIEW_PUBLIC = 'EVENT_VIEW_PUBLIC',
  EVENT_CREATE = 'EVENT_CREATE',
  EVENT_EDIT_ALL = 'EVENT_EDIT_ALL',
  EVENT_EDIT_OWN = 'EVENT_EDIT_OWN',
  EVENT_DELETE = 'EVENT_DELETE',
  EVENT_REGISTER = 'EVENT_REGISTER',
  EVENT_MANAGE_REGISTRATIONS = 'EVENT_MANAGE_REGISTRATIONS',

  // ========== ATTENDANCE PERMISSIONS ==========
  ATTENDANCE_VIEW_ALL = 'ATTENDANCE_VIEW_ALL',
  ATTENDANCE_VIEW_FELLOWSHIP = 'ATTENDANCE_VIEW_FELLOWSHIP',
  ATTENDANCE_RECORD = 'ATTENDANCE_RECORD',
  ATTENDANCE_EDIT = 'ATTENDANCE_EDIT',

  // ========== PASTORAL CARE PERMISSIONS ==========
  CARE_NEED_VIEW_ALL = 'CARE_NEED_VIEW_ALL',
  CARE_NEED_VIEW_ASSIGNED = 'CARE_NEED_VIEW_ASSIGNED',
  CARE_NEED_CREATE = 'CARE_NEED_CREATE',
  CARE_NEED_EDIT = 'CARE_NEED_EDIT',
  CARE_NEED_ASSIGN = 'CARE_NEED_ASSIGN',

  VISIT_VIEW_ALL = 'VISIT_VIEW_ALL',
  VISIT_CREATE = 'VISIT_CREATE',
  VISIT_EDIT = 'VISIT_EDIT',

  PRAYER_REQUEST_VIEW_ALL = 'PRAYER_REQUEST_VIEW_ALL',
  PRAYER_REQUEST_CREATE = 'PRAYER_REQUEST_CREATE',
  PRAYER_REQUEST_EDIT = 'PRAYER_REQUEST_EDIT',

  // ========== COMMUNICATION PERMISSIONS ==========
  SMS_SEND = 'SMS_SEND',
  SMS_SEND_FELLOWSHIP = 'SMS_SEND_FELLOWSHIP',
  EMAIL_SEND = 'EMAIL_SEND',
  BULK_MESSAGE_SEND = 'BULK_MESSAGE_SEND',

  // ========== REPORT PERMISSIONS ==========
  REPORT_MEMBER = 'REPORT_MEMBER',
  REPORT_FINANCIAL = 'REPORT_FINANCIAL',
  REPORT_ATTENDANCE = 'REPORT_ATTENDANCE',
  REPORT_ANALYTICS = 'REPORT_ANALYTICS',
  REPORT_EXPORT = 'REPORT_EXPORT',

  // ========== ADMIN PERMISSIONS ==========
  USER_VIEW = 'USER_VIEW',
  USER_CREATE = 'USER_CREATE',
  USER_EDIT = 'USER_EDIT',
  USER_DELETE = 'USER_DELETE',
  USER_MANAGE_ROLES = 'USER_MANAGE_ROLES',

  CHURCH_SETTINGS_VIEW = 'CHURCH_SETTINGS_VIEW',
  CHURCH_SETTINGS_EDIT = 'CHURCH_SETTINGS_EDIT',

  SUBSCRIPTION_VIEW = 'SUBSCRIPTION_VIEW',
  SUBSCRIPTION_MANAGE = 'SUBSCRIPTION_MANAGE',

  // ========== PLATFORM PERMISSIONS ==========
  PLATFORM_ACCESS = 'PLATFORM_ACCESS',
  ALL_CHURCHES_VIEW = 'ALL_CHURCHES_VIEW',
  ALL_CHURCHES_MANAGE = 'ALL_CHURCHES_MANAGE',
  BILLING_MANAGE = 'BILLING_MANAGE',
  SYSTEM_CONFIG = 'SYSTEM_CONFIG'
}
```

#### 1.2 Create Role-Permission Mapping

**File**: `src/app/constants/role-permissions.ts`

```typescript
import { Permission } from '../enums/permission.enum';

/**
 * Role-Permission mapping matching backend Role.java
 * IMPORTANT: Keep in sync with backend roles
 */
export const ROLE_PERMISSIONS: Record<string, Permission[]> = {
  SUPERADMIN: [
    // SUPERADMIN has ALL permissions
    ...Object.values(Permission)
  ],

  ADMIN: [
    // Member permissions
    Permission.MEMBER_VIEW_ALL,
    Permission.MEMBER_CREATE,
    Permission.MEMBER_EDIT_ALL,
    Permission.MEMBER_DELETE,
    Permission.MEMBER_EXPORT,
    Permission.MEMBER_IMPORT,

    // Household permissions
    Permission.HOUSEHOLD_VIEW,
    Permission.HOUSEHOLD_CREATE,
    Permission.HOUSEHOLD_EDIT,
    Permission.HOUSEHOLD_DELETE,

    // Fellowship permissions
    Permission.FELLOWSHIP_VIEW_ALL,
    Permission.FELLOWSHIP_CREATE,
    Permission.FELLOWSHIP_EDIT_ALL,
    Permission.FELLOWSHIP_DELETE,
    Permission.FELLOWSHIP_MANAGE_MEMBERS,

    // Financial permissions (view only)
    Permission.DONATION_VIEW_ALL,
    Permission.CAMPAIGN_VIEW,
    Permission.PLEDGE_VIEW_ALL,

    // Event permissions
    Permission.EVENT_VIEW_ALL,
    Permission.EVENT_CREATE,
    Permission.EVENT_EDIT_ALL,
    Permission.EVENT_DELETE,
    Permission.EVENT_MANAGE_REGISTRATIONS,

    // Attendance permissions
    Permission.ATTENDANCE_VIEW_ALL,
    Permission.ATTENDANCE_RECORD,
    Permission.ATTENDANCE_EDIT,

    // Pastoral care permissions
    Permission.CARE_NEED_VIEW_ALL,
    Permission.CARE_NEED_CREATE,
    Permission.CARE_NEED_EDIT,
    Permission.CARE_NEED_ASSIGN,
    Permission.VISIT_VIEW_ALL,
    Permission.VISIT_CREATE,
    Permission.VISIT_EDIT,
    Permission.PRAYER_REQUEST_VIEW_ALL,
    Permission.PRAYER_REQUEST_CREATE,
    Permission.PRAYER_REQUEST_EDIT,

    // Communication permissions
    Permission.SMS_SEND,
    Permission.EMAIL_SEND,
    Permission.BULK_MESSAGE_SEND,

    // Report permissions
    Permission.REPORT_MEMBER,
    Permission.REPORT_FINANCIAL,
    Permission.REPORT_ATTENDANCE,
    Permission.REPORT_ANALYTICS,
    Permission.REPORT_EXPORT,

    // Admin permissions
    Permission.USER_VIEW,
    Permission.USER_CREATE,
    Permission.USER_EDIT,
    Permission.USER_DELETE,
    Permission.USER_MANAGE_ROLES,
    Permission.CHURCH_SETTINGS_VIEW,
    Permission.CHURCH_SETTINGS_EDIT,
    Permission.SUBSCRIPTION_VIEW,
    Permission.SUBSCRIPTION_MANAGE
  ],

  PASTOR: [
    // Member permissions
    Permission.MEMBER_VIEW_ALL,
    Permission.MEMBER_EDIT_PASTORAL,

    // Household permissions
    Permission.HOUSEHOLD_VIEW,

    // Fellowship permissions
    Permission.FELLOWSHIP_VIEW_ALL,

    // Event permissions
    Permission.EVENT_VIEW_ALL,
    Permission.EVENT_CREATE,
    Permission.EVENT_EDIT_OWN,
    Permission.EVENT_REGISTER,

    // Attendance permissions
    Permission.ATTENDANCE_VIEW_ALL,
    Permission.ATTENDANCE_RECORD,

    // Pastoral care permissions
    Permission.CARE_NEED_VIEW_ALL,
    Permission.CARE_NEED_CREATE,
    Permission.CARE_NEED_EDIT,
    Permission.CARE_NEED_ASSIGN,
    Permission.VISIT_VIEW_ALL,
    Permission.VISIT_CREATE,
    Permission.VISIT_EDIT,
    Permission.PRAYER_REQUEST_VIEW_ALL,
    Permission.PRAYER_REQUEST_CREATE,
    Permission.PRAYER_REQUEST_EDIT,

    // Communication permissions
    Permission.SMS_SEND,
    Permission.EMAIL_SEND,

    // Report permissions
    Permission.REPORT_MEMBER,
    Permission.REPORT_ATTENDANCE,
    Permission.REPORT_ANALYTICS
  ],

  TREASURER: [
    // Member permissions (view only for donor management)
    Permission.MEMBER_VIEW_ALL,

    // Financial permissions
    Permission.DONATION_VIEW_ALL,
    Permission.DONATION_CREATE,
    Permission.DONATION_EDIT,
    Permission.DONATION_DELETE,
    Permission.DONATION_EXPORT,
    Permission.CAMPAIGN_VIEW,
    Permission.CAMPAIGN_MANAGE,
    Permission.PLEDGE_VIEW_ALL,
    Permission.PLEDGE_MANAGE,
    Permission.RECEIPT_ISSUE,

    // Report permissions
    Permission.REPORT_FINANCIAL,
    Permission.REPORT_EXPORT
  ],

  FELLOWSHIP_LEADER: [
    // Member permissions (fellowship-scoped)
    Permission.MEMBER_VIEW_FELLOWSHIP,

    // Fellowship permissions (own fellowship only)
    Permission.FELLOWSHIP_VIEW_OWN,
    Permission.FELLOWSHIP_EDIT_OWN,

    // Event permissions
    Permission.EVENT_VIEW_ALL,
    Permission.EVENT_CREATE,
    Permission.EVENT_EDIT_OWN,

    // Attendance permissions (fellowship-scoped)
    Permission.ATTENDANCE_VIEW_FELLOWSHIP,
    Permission.ATTENDANCE_RECORD,

    // Communication permissions (fellowship-scoped)
    Permission.SMS_SEND_FELLOWSHIP,
    Permission.EMAIL_SEND
  ],

  MEMBER_MANAGER: [
    // Member permissions
    Permission.MEMBER_VIEW_ALL,
    Permission.MEMBER_CREATE,
    Permission.MEMBER_EDIT_ALL,
    Permission.MEMBER_DELETE,
    Permission.MEMBER_EXPORT,
    Permission.MEMBER_IMPORT,

    // Household permissions
    Permission.HOUSEHOLD_VIEW,
    Permission.HOUSEHOLD_CREATE,
    Permission.HOUSEHOLD_EDIT,
    Permission.HOUSEHOLD_DELETE,

    // Report permissions
    Permission.REPORT_MEMBER,
    Permission.REPORT_ANALYTICS,
    Permission.REPORT_EXPORT
  ],

  MEMBER: [
    // Member permissions (own profile only)
    Permission.MEMBER_VIEW_OWN,
    Permission.MEMBER_EDIT_OWN,

    // Fellowship permissions (view own membership)
    Permission.FELLOWSHIP_VIEW_OWN,

    // Financial permissions (own giving history)
    Permission.DONATION_VIEW_OWN,
    Permission.PLEDGE_VIEW_OWN,

    // Event permissions
    Permission.EVENT_VIEW_PUBLIC,
    Permission.EVENT_REGISTER,

    // Pastoral care permissions
    Permission.PRAYER_REQUEST_CREATE
  ]
};
```

#### 1.3 Enhance AuthService

**File**: `src/app/services/auth.service.ts`

```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { Permission } from '../enums/permission.enum';
import { ROLE_PERMISSIONS } from '../constants/role-permissions';

export interface User {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  role: string;
  churchId: number;
  churchName: string;
  permissions?: Permission[];  // Add this
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private currentUserSubject: BehaviorSubject<User | null>;
  public currentUser: Observable<User | null>;

  constructor(private http: HttpClient) {
    const user = this.getUserFromStorage();
    this.currentUserSubject = new BehaviorSubject<User | null>(user);
    this.currentUser = this.currentUserSubject.asObservable();

    // Load permissions for current user
    if (user) {
      this.loadUserPermissions(user);
    }
  }

  /**
   * Load permissions for user based on their role
   */
  private loadUserPermissions(user: User): void {
    const permissions = ROLE_PERMISSIONS[user.role] || [];
    user.permissions = permissions;
    this.currentUserSubject.next(user);
    this.saveUserToStorage(user);
  }

  /**
   * Get current user value
   */
  public get currentUserValue(): User | null {
    return this.currentUserSubject.value;
  }

  /**
   * Check if user has a specific permission
   */
  public hasPermission(permission: Permission): boolean {
    const user = this.currentUserValue;
    if (!user) return false;

    // SUPERADMIN has all permissions
    if (user.role === 'SUPERADMIN') return true;

    // Check if user has the permission
    return user.permissions?.includes(permission) || false;
  }

  /**
   * Check if user has ANY of the specified permissions (OR logic)
   */
  public hasAnyPermission(permissions: Permission[]): boolean {
    if (!permissions || permissions.length === 0) return false;
    return permissions.some(permission => this.hasPermission(permission));
  }

  /**
   * Check if user has ALL of the specified permissions (AND logic)
   */
  public hasAllPermissions(permissions: Permission[]): boolean {
    if (!permissions || permissions.length === 0) return false;
    return permissions.every(permission => this.hasPermission(permission));
  }

  /**
   * Check if user is SUPERADMIN
   */
  public isSuperAdmin(): boolean {
    return this.currentUserValue?.role === 'SUPERADMIN';
  }

  /**
   * Check if user is ADMIN
   */
  public isAdmin(): boolean {
    return this.currentUserValue?.role === 'ADMIN';
  }

  /**
   * Check if user is authenticated
   */
  public isAuthenticated(): boolean {
    return this.currentUserValue !== null;
  }

  /**
   * Login user
   */
  login(email: string, password: string): Observable<any> {
    return this.http.post<any>('/api/auth/login', { email, password })
      .pipe(tap(response => {
        if (response.user && response.token) {
          // Store token
          localStorage.setItem('token', response.token);

          // Load permissions
          this.loadUserPermissions(response.user);
        }
      }));
  }

  /**
   * Logout user
   */
  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    this.currentUserSubject.next(null);
  }

  // ... existing methods (saveUserToStorage, getUserFromStorage, etc.)
}
```

#### 1.4 Create Permission Directive

**File**: `src/app/directives/has-permission.directive.ts`

```typescript
import {
  Directive,
  Input,
  TemplateRef,
  ViewContainerRef,
  OnInit,
  OnDestroy
} from '@angular/core';
import { Subject, takeUntil } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { Permission } from '../enums/permission.enum';

/**
 * Structural directive to show/hide elements based on user permissions
 *
 * Usage:
 * <button *hasPermission="'MEMBER_CREATE'">Create Member</button>
 * <button *hasPermission="['MEMBER_EDIT_ALL', 'MEMBER_DELETE']">Edit/Delete</button>
 * <button *hasPermission="['MEMBER_VIEW_ALL', 'MEMBER_EDIT_ALL']; operation: 'AND'">View & Edit</button>
 */
@Directive({
  selector: '[hasPermission]',
  standalone: true
})
export class HasPermissionDirective implements OnInit, OnDestroy {
  private permissions: Permission[] = [];
  private operation: 'OR' | 'AND' = 'OR';
  private destroy$ = new Subject<void>();
  private hasView = false;

  constructor(
    private templateRef: TemplateRef<any>,
    private viewContainer: ViewContainerRef,
    private authService: AuthService
  ) {}

  @Input() set hasPermission(val: Permission | Permission[]) {
    this.permissions = Array.isArray(val) ? val : [val];
    this.updateView();
  }

  @Input() set hasPermissionOperation(val: 'OR' | 'AND') {
    this.operation = val;
    this.updateView();
  }

  ngOnInit(): void {
    // Subscribe to user changes
    this.authService.currentUser
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => this.updateView());
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private updateView(): void {
    const hasAccess = this.checkPermissions();

    if (hasAccess && !this.hasView) {
      this.viewContainer.createEmbeddedView(this.templateRef);
      this.hasView = true;
    } else if (!hasAccess && this.hasView) {
      this.viewContainer.clear();
      this.hasView = false;
    }
  }

  private checkPermissions(): boolean {
    if (!this.permissions || this.permissions.length === 0) {
      return true;  // No permissions required, show element
    }

    if (this.operation === 'AND') {
      return this.authService.hasAllPermissions(this.permissions);
    } else {
      return this.authService.hasAnyPermission(this.permissions);
    }
  }
}
```

#### 1.5 Create Permission Guard

**File**: `src/app/guards/permission.guard.ts`

```typescript
import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { Permission } from '../enums/permission.enum';

/**
 * Route guard to check if user has required permissions
 *
 * Usage in routing:
 * {
 *   path: 'members/create',
 *   component: CreateMemberComponent,
 *   canActivate: [AuthGuard, PermissionGuard],
 *   data: {
 *     permissions: [Permission.MEMBER_CREATE]
 *   }
 * }
 */
@Injectable({ providedIn: 'root' })
export class PermissionGuard implements CanActivate {
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(route: ActivatedRouteSnapshot): boolean {
    const requiredPermissions = route.data['permissions'] as Permission[];

    if (!requiredPermissions || requiredPermissions.length === 0) {
      return true;  // No permissions required
    }

    const operation = route.data['permissionOperation'] || 'OR';

    const hasAccess = operation === 'AND'
      ? this.authService.hasAllPermissions(requiredPermissions)
      : this.authService.hasAnyPermission(requiredPermissions);

    if (!hasAccess) {
      console.warn('Permission denied. Required:', requiredPermissions);
      this.router.navigate(['/unauthorized']);
      return false;
    }

    return true;
  }
}
```

---

### Phase 2: Component Updates (1 week)

#### 2.1 Members Page Example

**Before** (no permission checks):
```html
<!-- members-page.component.html -->
<div class="page-header">
  <h2>Members</h2>
  <button pButton label="Create Member" (click)="createMember()"></button>
  <button pButton label="Export" (click)="exportMembers()"></button>
</div>

<p-table [value]="members">
  <ng-template pTemplate="body" let-member>
    <tr>
      <td>{{member.name}}</td>
      <td>{{member.email}}</td>
      <td>
        <button pButton icon="pi pi-pencil" (click)="editMember(member)"></button>
        <button pButton icon="pi pi-trash" (click)="deleteMember(member)"></button>
      </td>
    </tr>
  </ng-template>
</p-table>
```

**After** (with permission checks):
```html
<!-- members-page.component.html -->
<div class="page-header">
  <h2>Members</h2>

  <!-- Only show Create button if user has MEMBER_CREATE permission -->
  <button
    *hasPermission="Permission.MEMBER_CREATE"
    pButton
    label="Create Member"
    (click)="createMember()">
  </button>

  <!-- Only show Export button if user has MEMBER_EXPORT permission -->
  <button
    *hasPermission="Permission.MEMBER_EXPORT"
    pButton
    label="Export"
    (click)="exportMembers()">
  </button>
</div>

<p-table [value]="members">
  <ng-template pTemplate="body" let-member>
    <tr>
      <td>{{member.name}}</td>
      <td>{{member.email}}</td>
      <td>
        <!-- Only show Edit button if user has MEMBER_EDIT_ALL permission -->
        <button
          *hasPermission="Permission.MEMBER_EDIT_ALL"
          pButton
          icon="pi pi-pencil"
          (click)="editMember(member)">
        </button>

        <!-- Only show Delete button if user has MEMBER_DELETE permission -->
        <button
          *hasPermission="Permission.MEMBER_DELETE"
          pButton
          icon="pi pi-trash"
          (click)="deleteMember(member)">
        </button>
      </td>
    </tr>
  </ng-template>
</p-table>
```

**Component TypeScript** (add Permission enum):
```typescript
import { Component } from '@angular/core';
import { Permission } from '../enums/permission.enum';

@Component({
  selector: 'app-members-page',
  templateUrl: './members-page.component.html'
})
export class MembersPageComponent {
  // Expose Permission enum to template
  Permission = Permission;

  members: Member[] = [];

  // ... rest of component
}
```

#### 2.2 Route Protection Example

**Before** (no route protection):
```typescript
// app-routing.module.ts
const routes: Routes = [
  {
    path: 'members',
    component: MembersPageComponent,
    canActivate: [AuthGuard]  // Only checks if authenticated
  },
  {
    path: 'members/create',
    component: CreateMemberComponent,
    canActivate: [AuthGuard]
  }
];
```

**After** (with permission guard):
```typescript
// app-routing.module.ts
import { PermissionGuard } from './guards/permission.guard';
import { Permission } from './enums/permission.enum';

const routes: Routes = [
  {
    path: 'members',
    component: MembersPageComponent,
    canActivate: [AuthGuard, PermissionGuard],
    data: {
      permissions: [Permission.MEMBER_VIEW_ALL]
    }
  },
  {
    path: 'members/create',
    component: CreateMemberComponent,
    canActivate: [AuthGuard, PermissionGuard],
    data: {
      permissions: [Permission.MEMBER_CREATE]
    }
  },
  {
    path: 'donations',
    component: DonationsPageComponent,
    canActivate: [AuthGuard, PermissionGuard],
    data: {
      permissions: [Permission.DONATION_VIEW_ALL, Permission.DONATION_VIEW_OWN],
      permissionOperation: 'OR'  // User needs either permission
    }
  }
];
```

#### 2.3 Conditional Menu Items

**Side Navigation** (hide menu items based on permissions):

```html
<!-- side-nav.component.html -->
<nav class="side-nav">
  <!-- Dashboard - always visible -->
  <a routerLink="/dashboard" routerLinkActive="active">
    <i class="pi pi-home"></i> Dashboard
  </a>

  <!-- Members - only if can view members -->
  <a
    *hasPermission="Permission.MEMBER_VIEW_ALL"
    routerLink="/members"
    routerLinkActive="active">
    <i class="pi pi-users"></i> Members
  </a>

  <!-- Donations - only if can view donations -->
  <a
    *hasPermission="[Permission.DONATION_VIEW_ALL, Permission.DONATION_VIEW_OWN]"
    routerLink="/donations"
    routerLinkActive="active">
    <i class="pi pi-dollar"></i> Donations
  </a>

  <!-- Campaigns - only if can view campaigns -->
  <a
    *hasPermission="Permission.CAMPAIGN_VIEW"
    routerLink="/campaigns"
    routerLinkActive="active">
    <i class="pi pi-megaphone"></i> Campaigns
  </a>

  <!-- SMS - only if can send SMS -->
  <a
    *hasPermission="[Permission.SMS_SEND, Permission.SMS_SEND_FELLOWSHIP]"
    routerLink="/sms"
    routerLinkActive="active">
    <i class="pi pi-comment"></i> SMS
  </a>

  <!-- Care Needs - only if can view care needs -->
  <a
    *hasPermission="[Permission.CARE_NEED_VIEW_ALL, Permission.CARE_NEED_VIEW_ASSIGNED]"
    routerLink="/care-needs"
    routerLinkActive="active">
    <i class="pi pi-heart"></i> Care Needs
  </a>

  <!-- Reports - only if can view reports -->
  <a
    *hasPermission="[Permission.REPORT_MEMBER, Permission.REPORT_FINANCIAL, Permission.REPORT_ATTENDANCE]"
    routerLink="/reports"
    routerLinkActive="active">
    <i class="pi pi-chart-bar"></i> Reports
  </a>

  <!-- Settings - only if ADMIN -->
  <a
    *hasPermission="Permission.CHURCH_SETTINGS_VIEW"
    routerLink="/settings"
    routerLinkActive="active">
    <i class="pi pi-cog"></i> Settings
  </a>

  <!-- Users - only if can manage users -->
  <a
    *hasPermission="Permission.USER_VIEW"
    routerLink="/users"
    routerLinkActive="active">
    <i class="pi pi-user-edit"></i> Users
  </a>
</nav>
```

---

### Phase 3: Complete Component Updates (3-5 days)

#### Components to Update (All Pages)

1. **Members Page** - ✅ Example above
2. **Donations Page**
3. **Campaigns Page**
4. **Pledges Page**
5. **Fellowships Page**
6. **Households Page**
7. **Events Page**
8. **Attendance Page**
9. **Care Needs Page**
10. **Visits Page**
11. **Prayer Requests Page**
12. **SMS Page**
13. **Reports Page**
14. **Settings Page**
15. **Users Page**

#### Standard Pattern for All Components

**Template Changes**:
1. Add `Permission = Permission` to component class
2. Wrap "Create" button with `*hasPermission="Permission.*_CREATE"`
3. Wrap "Export" button with `*hasPermission="Permission.*_EXPORT"`
4. Wrap "Edit" button with `*hasPermission="Permission.*_EDIT*"`
5. Wrap "Delete" button with `*hasPermission="Permission.*_DELETE"`

**TypeScript Changes**:
```typescript
export class SomePageComponent {
  // Expose Permission enum to template
  Permission = Permission;

  // ... rest of component
}
```

---

## Testing Checklist

### 1. Unit Tests

```typescript
// has-permission.directive.spec.ts
describe('HasPermissionDirective', () => {
  it('should show element if user has required permission', () => {
    // Mock user with MEMBER_CREATE permission
    const user = { role: 'ADMIN', permissions: [Permission.MEMBER_CREATE] };
    authService.currentUserSubject.next(user);

    // Element should be visible
    expect(fixture.nativeElement.querySelector('button')).toBeTruthy();
  });

  it('should hide element if user lacks permission', () => {
    // Mock user without MEMBER_CREATE permission
    const user = { role: 'MEMBER', permissions: [Permission.MEMBER_VIEW_OWN] };
    authService.currentUserSubject.next(user);

    // Element should be hidden
    expect(fixture.nativeElement.querySelector('button')).toBeFalsy();
  });
});
```

### 2. E2E Tests (Playwright)

```typescript
// members-page.spec.ts
test('ADMIN can see all action buttons', async ({ page }) => {
  // Login as ADMIN
  await page.goto('/login');
  await page.fill('[name="email"]', 'admin@church.org');
  await page.fill('[name="password"]', 'password');
  await page.click('button[type="submit"]');

  // Navigate to members page
  await page.goto('/members');

  // Verify all buttons are visible
  await expect(page.locator('text=Create Member')).toBeVisible();
  await expect(page.locator('text=Export')).toBeVisible();
  await expect(page.locator('button[icon="pi-pencil"]').first()).toBeVisible();
  await expect(page.locator('button[icon="pi-trash"]').first()).toBeVisible();
});

test('TREASURER cannot see member management buttons', async ({ page }) => {
  // Login as TREASURER
  await page.goto('/login');
  await page.fill('[name="email"]', 'treasurer@church.org');
  await page.fill('[name="password"]', 'password');
  await page.click('button[type="submit"]');

  // Navigate to members page (should redirect or show limited view)
  await page.goto('/members');

  // Verify action buttons are hidden
  await expect(page.locator('text=Create Member')).not.toBeVisible();
  await expect(page.locator('text=Export')).not.toBeVisible();
  await expect(page.locator('button[icon="pi-pencil"]')).not.toBeVisible();
  await expect(page.locator('button[icon="pi-trash"]')).not.toBeVisible();

  // But can navigate to financial pages
  await page.goto('/donations');
  await expect(page.locator('text=Create Donation')).toBeVisible();
});
```

### 3. Manual Testing Checklist

**Test with Each Role**:

✅ **ADMIN**:
- Can see all menu items
- Can see all action buttons (Create, Edit, Delete, Export)
- Can navigate to all pages
- Can perform all actions

✅ **TREASURER**:
- Can only see financial menu items (Donations, Campaigns, Pledges)
- Can only see financial action buttons
- Cannot access member management pages
- Cannot see pastoral care or communication menu items

✅ **PASTOR**:
- Can see member, pastoral care, communication menu items
- Can create/edit care needs, visits, prayer requests
- Can send SMS/emails
- Cannot see financial action buttons (view only)
- Cannot delete members

✅ **FELLOWSHIP_LEADER**:
- Can only see own fellowship
- Can only send SMS to own fellowship members
- Cannot see all members list
- Limited menu items

✅ **MEMBER**:
- Can only see public pages
- Can view own profile
- Can view own giving history
- Cannot see admin menu items
- No action buttons visible

---

## Implementation Checklist

### Phase 1: Infrastructure (2-3 days)
- [ ] Create `permission.enum.ts` with all 79 permissions
- [ ] Create `role-permissions.ts` with role mappings
- [ ] Enhance `AuthService` with permission methods
- [ ] Create `HasPermissionDirective`
- [ ] Create `PermissionGuard`
- [ ] Add unit tests for directive and guard

### Phase 2: Route Protection (1 day)
- [ ] Add PermissionGuard to all routes in routing modules
- [ ] Define required permissions in route data
- [ ] Create `/unauthorized` page
- [ ] Test navigation with different roles

### Phase 3: Component Updates (3-5 days)
- [ ] Update all 15 main page components
- [ ] Add Permission enum to each component
- [ ] Wrap action buttons with `*hasPermission`
- [ ] Update side navigation with permission checks
- [ ] Update top navigation/toolbar with permission checks

### Phase 4: Testing (2-3 days)
- [ ] Write unit tests for permission directive
- [ ] Write unit tests for permission guard
- [ ] Write E2E tests for each role
- [ ] Manual testing with all 7 roles
- [ ] Fix any bugs found during testing

**Total Effort**: 8-12 days (1.5-2.5 weeks)

---

## Maintenance Notes

### Keeping Frontend in Sync with Backend

**IMPORTANT**: When adding new permissions to backend `Permission.java`, you must:
1. Add the same permission to `permission.enum.ts`
2. Update `role-permissions.ts` with role mappings
3. Update affected components to use the new permission

**Recommended**: Create a script to generate TypeScript enums from Java enums:
```bash
# scripts/sync-permissions.sh
java -jar permission-sync-tool.jar \
  --input src/main/java/com/reuben/pastcare_spring/enums/Permission.java \
  --output ../past-care-spring-frontend/src/app/enums/permission.enum.ts
```

---

**Document Status**: Implementation Guide
**Last Updated**: 2025-12-29
**Effort Estimate**: 1.5-2.5 weeks
**Dependencies**: Backend RBAC must be 100% complete first
