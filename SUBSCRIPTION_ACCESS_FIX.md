# Subscription Access Control Fix

**Date**: December 30, 2025
**Issue**: Multiple problems with subscription access control and user experience

## Problems Identified

### 1. SUPERADMIN Users Blocked by Subscription Filter
**Issue**: SUPERADMIN users don't have a `churchId`, causing the SubscriptionFilter to block their access to platform routes.

**Impact**: Platform admin pages (/platform-admin, /pricing-management, etc.) were inaccessible even for SUPERADMIN users.

### 2. No Dedicated Inactive Subscription Page
**Issue**: Users without active subscriptions see the full app interface including sidenav, but get errors when trying to access features.

**Expected**: Dedicated landing page showing subscription status and "Pay Now" button with NO sidenav or other navigation.

### 3. Route Guards Not Refreshing After Payment
**Issue**: After successful payment, the subscription guard still redirects users even though subscription is active.

**Cause**: Subscription status cached in guard, not refreshing after payment completion.

### 4. Inactive Users See Full UI
**Issue**: Users with inactive subscriptions can see sidenav and attempt to navigate, creating poor UX.

**Expected**: Hide sidenav and all navigation until subscription is active.

## Solutions Implemented

### Part 1: Fix SUPERADMIN Access ✅

**File**: [SubscriptionFilter.java](src/main/java/com/reuben/pastcare_spring/security/SubscriptionFilter.java)

**Changes** (Lines 82-86, 134-145):

```java
// Check if user is SUPERADMIN (they don't have church ID and bypass subscription checks)
if (isSuperAdmin(authentication)) {
    filterChain.doFilter(request, response);
    return;
}

// New method added:
private boolean isSuperAdmin(Authentication authentication) {
    Object principal = authentication.getPrincipal();
    if (principal instanceof UserPrincipal) {
        UserPrincipal userPrincipal = (UserPrincipal) principal;
        return userPrincipal.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_SUPERADMIN"));
    }
    return false;
}
```

**Result**: SUPERADMIN users now bypass subscription checks and can access all platform routes.

### Part 2: Create Dedicated Inactive Subscription Page

**New Component**: `inactive-subscription-page.component.ts`

**Features**:
- Shows current subscription status
- Displays subscription expiry/suspension reason
- Large "Renew Subscription" or "Choose Plan" button
- NO sidenav or other navigation
- Clean, focused UI for payment flow
- Auto-redirects to dashboard after successful activation

**Route**: `/subscription/inactive`

**Access**: Only accessible to authenticated users with inactive subscriptions

### Part 3: Update Routing Logic

**Changes Required**:

1. **Auth Guard Enhancement**: After login, check subscription status
   - Active subscription → redirect to `/dashboard`
   - Inactive subscription → redirect to `/subscription/inactive`

2. **Subscription Guard Update**: Clear cache after payment
   - Add method to refresh subscription status
   - Call after payment verification completes

3. **New Route**:
```typescript
{
  path: 'subscription/inactive',
  component: InactiveSubscriptionPage,
  canActivate: [authGuard] // Only authenticated users
}
```

### Part 4: Hide Sidenav for Inactive Users

**Component**: `app.component.ts`

**Logic**:
```typescript
async ngOnInit() {
  this.subscriptionStatus$ = this.authService.isAuthenticated$.pipe(
    switchMap(isAuth => {
      if (!isAuth) return of(null);
      return this.http.get<SubscriptionStatus>('/api/billing/status');
    })
  );
}

// In template
<app-sidenav *ngIf="(subscriptionStatus$ | async)?.isActive">
```

**Result**: Sidenav only shows when user has active subscription.

### Part 5: Fix Route Guard Caching

**File**: `subscription.guard.ts`

**Problem**: HTTP GET caches response, doesn't refresh after payment.

**Solution**: Add cache-busting parameter or use BehaviorSubject to track status changes.

```typescript
export const subscriptionGuard: CanActivateFn = (route, state) => {
  const http = inject(HttpClient);
  const router = inject(Router);

  // Add timestamp to prevent caching
  const timestamp = new Date().getTime();

  return http.get<SubscriptionStatusResponse>(
    `${environment.apiUrl}/api/billing/status?t=${timestamp}`
  ).pipe(
    map((status) => {
      if (status.isActive || status.isInGracePeriod || status.hasPromotionalCredits) {
        return true;
      }

      // Redirect to inactive subscription page
      router.navigate(['/subscription/inactive'], {
        queryParams: { returnUrl: state.url }
      });
      return false;
    }),
    catchError((error) => {
      console.error('Error checking subscription status:', error);
      if (error.status === 401 || error.status === 403) {
        return of(true);
      }
      return of(true);
    })
  );
};
```

## Complete User Flow

### Flow 1: New User Registration

```
1. User registers → Lands on /dashboard
2. No active subscription → SubscriptionGuard redirects to /subscription/inactive
3. User sees "Get Started" page with plan options
4. User clicks "Choose Plan" → Redirected to /subscription/select
5. User selects plan → Redirected to Paystack
6. Payment succeeds → Webhook activates subscription
7. User returns to /payment/verify → Shows success
8. After 2 seconds → Redirects to /dashboard
9. SubscriptionGuard checks status → Allows access (subscription now active)
10. User sees full dashboard with sidenav
```

### Flow 2: Subscription Expired

```
1. User tries to access /dashboard
2. SubscriptionGuard checks status → Inactive
3. Redirected to /subscription/inactive
4. User sees "Your subscription has expired" message
5. "Renew Now" button displayed
6. User clicks → Redirected to /subscription/select
7. (Payment flow same as above)
```

### Flow 3: SUPERADMIN Access

```
1. SUPERADMIN logs in
2. SubscriptionFilter detects SUPERADMIN role
3. Bypasses all subscription checks
4. Can access /platform-admin, /pricing-management, etc.
5. No subscription status checked
```

### Flow 4: Payment Callback Session Expired

```
1. User completes payment (session expires during payment)
2. Returns to /payment/verify?reference=SUB-xxx
3. Frontend calls /api/billing/public/verify/SUB-xxx (no auth required)
4. Backend activates subscription
5. Frontend shows "Success! Please log in to continue"
6. User logs in
7. Redirected to /dashboard
8. SubscriptionGuard checks status → Active
9. Access granted
```

## Frontend Components to Create

### 1. InactiveSubscriptionPage Component

**Location**: `src/app/inactive-subscription-page/`

**Files**:
- `inactive-subscription-page.ts`
- `inactive-subscription-page.html`
- `inactive-subscription-page.css`

**Template Structure**:
```html
<div class="inactive-container">
  <div class="status-card">
    <h1>Subscription Required</h1>

    <div *ngIf="subscriptionStatus">
      <p class="status-message">{{ getStatusMessage() }}</p>

      <div class="plan-info">
        <span>Current Plan:</span>
        <strong>{{ subscriptionStatus.planDisplayName }}</strong>
      </div>

      <div *ngIf="subscriptionStatus.currentPeriodEnd">
        <span>Expired On:</span>
        <strong>{{ subscriptionStatus.currentPeriodEnd | date }}</strong>
      </div>
    </div>

    <button class="renew-button" (click)="goToSubscriptionSelect()">
      {{ getButtonText() }}
    </button>

    <a routerLink="/help" class="help-link">Need Help?</a>
  </div>
</div>
```

**Component Logic**:
```typescript
export class InactiveSubscriptionPage implements OnInit {
  subscriptionStatus: SubscriptionStatus | null = null;

  async ngOnInit() {
    this.subscriptionStatus = await this.billingService.getSubscriptionStatus();

    // If subscription becomes active, redirect to dashboard
    if (this.subscriptionStatus.isActive) {
      this.router.navigate(['/dashboard']);
    }
  }

  getStatusMessage(): string {
    if (this.subscriptionStatus?.isPastDue) {
      return 'Your payment is overdue. Please renew to continue accessing your account.';
    }
    if (this.subscriptionStatus?.isSuspended) {
      return 'Your subscription has been suspended. Please contact support or renew.';
    }
    if (this.subscriptionStatus?.isCanceled) {
      return 'Your subscription has been canceled. Renew to restore access.';
    }
    return 'You need an active subscription to access this application.';
  }

  getButtonText(): string {
    return this.subscriptionStatus?.planName === 'NO_PLAN'
      ? 'Choose a Plan'
      : 'Renew Subscription';
  }

  goToSubscriptionSelect() {
    this.router.navigate(['/subscription/select']);
  }
}
```

### 2. Update AuthGuard to Check Subscription

**File**: `auth.guard.ts`

**Add After Authentication Check**:
```typescript
export const authGuard: CanActivateFn = async (route, state) => {
  const router = inject(Router);
  const http = inject(HttpClient);
  const token = localStorage.getItem('authToken');

  if (!token) {
    router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
    return false;
  }

  // Check token expiry...

  // NEW: Check subscription status for non-exempted routes
  const exemptedRoutes = [
    '/subscription/inactive',
    '/subscription/select',
    '/payment/setup',
    '/payment/verify',
    '/billing',
    '/help',
    '/settings',
    '/logout'
  ];

  const isExempted = exemptedRoutes.some(r => state.url.startsWith(r));

  if (!isExempted) {
    try {
      const status = await firstValueFrom(
        http.get<SubscriptionStatus>(`${environment.apiUrl}/api/billing/status`)
      );

      if (!status.isActive && !status.isInGracePeriod && !status.hasPromotionalCredits) {
        router.navigate(['/subscription/inactive'], {
          queryParams: { returnUrl: state.url }
        });
        return false;
      }
    } catch (error) {
      // Allow access if subscription check fails
      console.warn('Could not verify subscription:', error);
    }
  }

  return true;
};
```

### 3. Update App Component for Sidenav Logic

**File**: `app.component.ts`

```typescript
export class AppComponent implements OnInit {
  showSidenav$ = new BehaviorSubject<boolean>(false);

  async ngOnInit() {
    this.authService.isAuthenticated$.subscribe(async (isAuth) => {
      if (!isAuth) {
        this.showSidenav$.next(false);
        return;
      }

      // Check subscription status
      try {
        const status = await firstValueFrom(
          this.http.get<SubscriptionStatus>(`${environment.apiUrl}/api/billing/status`)
        );

        // Only show sidenav if active subscription OR user is on exempted routes
        const currentRoute = this.router.url;
        const exemptedRoutes = ['/subscription/', '/payment/', '/billing', '/help'];
        const isExempted = exemptedRoutes.some(r => currentRoute.includes(r));

        this.showSidenav$.next(status.isActive || status.isInGracePeriod || isExempted);
      } catch {
        this.showSidenav$.next(false);
      }
    });
  }
}
```

**Template**:
```html
<app-sidenav *ngIf="showSidenav$ | async"></app-sidenav>
<router-outlet></router-outlet>
```

## Testing Checklist

### SUPERADMIN Access

- [ ] SUPERADMIN can log in successfully
- [ ] Can access /platform-admin without subscription check
- [ ] Can access /pricing-management
- [ ] Can access /security-monitoring
- [ ] No 402 Payment Required errors
- [ ] All platform admin features work

### Inactive Subscription Flow

- [ ] User with no subscription lands on /subscription/inactive after login
- [ ] Sidenav is hidden on inactive page
- [ ] "Choose Plan" button visible and functional
- [ ] Clicking button navigates to /subscription/select
- [ ] After payment, user can access dashboard
- [ ] Sidenav appears after successful payment

### Active Subscription Flow

- [ ] User with active subscription accesses /dashboard directly
- [ ] Sidenav is visible
- [ ] All routes accessible
- [ ] No unexpected redirects

### Payment Completion Flow

- [ ] Payment succeeds → Webhook activates subscription
- [ ] Public verification endpoint activates if webhook fails
- [ ] After activation, guard allows access
- [ ] No caching issues prevent access
- [ ] Session expiry handled gracefully

### Route Guard Behavior

- [ ] subscriptionGuard redirects inactive users to /subscription/inactive
- [ ] Cache-busting prevents stale status checks
- [ ] After payment, subsequent checks see active status
- [ ] Grace period users can access features
- [ ] Promotional credit users can access features

## Files Modified

1. **[SubscriptionFilter.java](src/main/java/com/reuben/pastcare_spring/security/SubscriptionFilter.java)** - Added SUPERADMIN bypass
2. Frontend components to be created:
   - `inactive-subscription-page.ts`
   - Updates to `auth.guard.ts`
   - Updates to `subscription.guard.ts`
   - Updates to `app.component.ts`

## Next Steps

1. Create InactiveSubscriptionPage component ✅ (This document)
2. Update auth guard with subscription check
3. Update subscription guard with cache-busting
4. Update app component to control sidenav visibility
5. Add new route for `/subscription/inactive`
6. Test all flows end-to-end
7. Deploy and monitor

---

**Status**: Backend fixes complete, frontend implementation pending

**Priority**: High - Affects user experience and SUPERADMIN functionality
