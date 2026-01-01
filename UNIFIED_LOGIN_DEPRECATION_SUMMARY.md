# Unified Login System - Deprecated Code Summary

## Overview

The unified login system has been implemented, allowing both church administrators and members to login through a single `/login` page. This document tracks deprecated code that has been updated or retained for backward compatibility.

---

## Deprecated Routes

### Frontend Route: `/portal/login`

**Status:** DEPRECATED (Kept for backward compatibility)

**Location:** `past-care-spring-frontend/src/app/app.routes.ts:260-266`

**Reason:** The `/login` page now handles both admin and member authentication automatically. Members no longer need to remember a separate portal login URL.

**Action Taken:**
- Route kept in place to avoid breaking old bookmarks/links
- Component updated to automatically redirect to `/login`
- Marked with deprecation comment

**Removal Timeline:** Can be safely removed after 3-6 months when users have transitioned to the unified login.

---

## Updated Components

### 1. PortalLoginComponent

**File:** `past-care-spring-frontend/src/app/components/portal-login/portal-login.component.ts`

**Changes:**
- Constructor now immediately redirects to `/login` with return URL preserved
- Added deprecation comment explaining the change
- Component kept functional for backward compatibility

**Code:**
```typescript
constructor(...) {
  // DEPRECATED: This component is deprecated. Redirect to unified login.
  // The unified login at /login now handles both admin and member authentication.
  this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/portal/home';

  // Redirect to the unified login page
  this.router.navigate(['/login'], {
    queryParams: { returnUrl: this.returnUrl }
  });

  // ... rest of initialization
}
```

### 2. PortalRegistrationComponent

**File:** `past-care-spring-frontend/src/app/components/portal-registration/portal-registration.component.ts:117-120`

**Changes:**
- `goToLogin()` method updated to redirect to `/login` instead of `/portal/login`
- Added comment explaining the redirect to unified login

**Before:**
```typescript
goToLogin() {
  this.router.navigate(['/portal/login']);
}
```

**After:**
```typescript
goToLogin() {
  // Redirect to unified login page (handles both admin and member login)
  this.router.navigate(['/login']);
}
```

### 3. PortalAuthGuard

**File:** `past-care-spring-frontend/src/app/guards/portal-auth.guard.ts:28-40`

**Changes:**
- Redirect destination changed from `/portal/login` to `/login`
- Preserves return URL for redirect after authentication

**Before:**
```typescript
// Redirect to portal login
router.navigate(['/portal/login'], {
  queryParams: { returnUrl: state.url }
});
```

**After:**
```typescript
// Redirect to unified login (handles both admin and member login)
router.navigate(['/login'], {
  queryParams: { returnUrl: state.url }
});
```

---

## Files NOT Changed (Intentionally)

### 1. Auth Interceptor

**File:** `past-care-spring-frontend/src/app/interceptors/auth.interceptor.ts:28`

**Reason:** The check `currentUrl.includes('/portal/login')` is still valid because the route exists (it just redirects). This check determines if the user is on a public page to avoid redirect loops.

**Status:** No change needed

---

## Backend Changes

### New Endpoint: `/api/auth/unified-login`

**File:** `src/main/java/com/reuben/pastcare_spring/controllers/AuthController.java:86-157`

**Purpose:** Unified authentication endpoint that automatically detects user type (admin/staff vs portal user).

**Logic:**
1. Check if email exists in `users` table
   - If yes → perform admin login (cookie-based auth)
   - Returns `userType: "ADMIN"` and `redirectTo: "/dashboard"` or `"/platform-admin"`
2. If not in users table, check `portal_users` table
   - If found → perform portal user login (token-based auth)
   - Returns `userType: "PORTAL_USER"` and `redirectTo: "/portal/home"`
3. If not found in either → throw "Invalid email or password" error

**Status:** New feature, not deprecated

### Repository Enhancement

**File:** `src/main/java/com/reuben/pastcare_spring/repositories/PortalUserRepository.java:17-21`

**Changes:** Added `findByEmail(String email)` method to search portal users across all churches.

**Status:** New feature, not deprecated

---

## Frontend Service Changes

### AuthService - New Method

**File:** `past-care-spring-frontend/src/app/services/auth-service.ts:30-62`

**New Method:** `unifiedAuthenticate()`

**Purpose:** Calls the `/api/auth/unified-login` endpoint and handles authentication for both user types:
- Admin users: stores user info in localStorage (tokens in HttpOnly cookies)
- Portal users: stores token and user info in localStorage

**Status:** New feature, not deprecated

### LoginPage Component

**File:** `past-care-spring-frontend/src/app/login-page/login-page.ts:52-103`

**Changes:**
- `authenticate()` method now calls `authService.unifiedAuthenticate()`
- Routes users based on `response.redirectTo` or `response.userType`

**Status:** Updated to use new unified authentication

### LoginPage Template

**File:** `past-care-spring-frontend/src/app/login-page/login-page.html:11-17`

**Changes:**
- Added UX hint informing users that both admins and members can login on the same page

**Content:**
```html
<!-- UX hint for members -->
<div class="mt-4 p-3 bg-indigo-50 border border-indigo-200 rounded-lg">
    <p class="text-sm text-indigo-800">
        <i class="pi pi-info-circle mr-1"></i>
        <strong>Church admins and members</strong> can both login here using their email and password
    </p>
</div>
```

**Status:** New UX improvement

---

## Migration Path for Existing Users

### For Members with Bookmarked `/portal/login`:
1. User navigates to `/portal/login`
2. `PortalLoginComponent` constructor immediately redirects to `/login`
3. User enters credentials
4. System detects they are a portal user
5. User is logged in and redirected to `/portal/home`

### For Admins:
1. No change - they already use `/login`
2. System continues to detect them as admin users
3. They are redirected to `/dashboard` or `/platform-admin`

---

## Testing Checklist

- [x] Backend compiles successfully
- [x] Frontend builds successfully
- [x] Unified login endpoint tested (returns correct error for non-existent users)
- [x] `/portal/login` route still accessible (redirects to `/login`)
- [x] UX hint displays on login page
- [ ] E2E test: Admin login via unified endpoint
- [ ] E2E test: Member login via unified endpoint
- [ ] E2E test: Old `/portal/login` URL redirects properly
- [ ] E2E test: Portal auth guard redirects to `/login` when unauthenticated

---

## Future Cleanup Tasks

After 3-6 months of the unified login being in production:

1. **Remove deprecated route:**
   - Delete `/portal/login` route from `app.routes.ts`

2. **Remove deprecated component:**
   - Delete `portal-login.component.ts`
   - Delete `portal-login.component.html`
   - Delete `portal-login.component.css`
   - Remove import from `app.routes.ts`

3. **Update documentation:**
   - Remove all references to `/portal/login` from user guides
   - Update E2E test documentation

4. **Update E2E tests:**
   - Remove tests that specifically test `/portal/login` component
   - Keep tests that verify backward compatibility redirect

---

## Benefits of Unified Login

1. **Single Entry Point:** Both admins and members use `/login`
2. **Automatic Detection:** System determines user type automatically
3. **Better UX:** Members don't need to remember a special URL
4. **Backward Compatible:** Old `/portal/login` links still work
5. **Correct Routing:** Each user type is automatically redirected to their appropriate dashboard
6. **Consistent Experience:** Same login page design for all users

---

## Questions & Answers

**Q: Why not delete the old `/portal/login` route immediately?**

A: Backward compatibility. Users may have bookmarked the old URL or have it saved in password managers. The redirect ensures a smooth transition.

**Q: Can the old endpoint `/api/portal/login` be removed?**

A: Not yet. The unified login uses it internally when authenticating portal users. It can be refactored later to use a shared authentication service.

**Q: Will the old login method still work?**

A: Yes, both `/api/auth/login` (admin) and `/api/portal/login` (member) still work. The unified endpoint is an additional option, not a replacement.

**Q: What happens if a member tries to login as an admin or vice versa?**

A: The system checks the email against both `users` and `portal_users` tables. It will authenticate them as the correct user type automatically.

---

## Document Version

- **Created:** 2026-01-01
- **Last Updated:** 2026-01-01
- **Version:** 1.0
- **Author:** Claude Code Assistant
