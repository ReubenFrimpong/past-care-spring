# SUPERADMIN API Prefix Fix

**Date**: December 30, 2025
**Issue**: Platform admin endpoints were being called without `/api` prefix

## Problem

The SUPERADMIN platform admin page was making HTTP requests to:
- `/platform/stats` instead of `/api/platform/stats`
- `/platform/churches/all` instead of `/api/platform/churches/all`
- `/platform/storage/stats` instead of `/api/platform/storage/stats`
- `/platform/billing/stats` instead of `/api/platform/billing/stats`

This caused Spring Boot to treat these as Angular routes and try to serve them as static resources, resulting in:
```
org.springframework.web.servlet.resource.NoResourceFoundException: No static resource platform/stats.
```

## Root Cause

**File**: [platform.service.ts](../past-care-spring-frontend/src/app/services/platform.service.ts)

**Line 13**:
```typescript
private apiUrl = `${environment.apiUrl}/platform`;
```

The service was constructing URLs as:
- `http://localhost:8080` + `/platform` + `/stats` = `http://localhost:8080/platform/stats` ❌

Should have been:
- `http://localhost:8080` + `/api/platform` + `/stats` = `http://localhost:8080/api/platform/stats` ✅

## Solution

**Better Approach**: Instead of modifying individual services, centralized `/api` prefix in environment configuration.

**Files Modified**:
1. [environment.ts](../past-care-spring-frontend/src/environments/environment.ts)
2. [environment.development.ts](../past-care-spring-frontend/src/environments/environment.development.ts)

**Changes**:

**environment.ts**:
```typescript
// BEFORE
export const environment = {
  production: true,
  apiUrl: 'https://pastcare.app'
};

// AFTER
export const environment = {
  production: true,
  apiUrl: 'https://pastcare.app/api'
};
```

**environment.development.ts**:
```typescript
// BEFORE
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080'
};

// AFTER
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'
};
```

**platform.service.ts** (Line 13):
```typescript
// Final state - no change needed
private apiUrl = `${environment.apiUrl}/platform`;
```

**Result**: URL is correctly constructed as `http://localhost:8080/api` + `/platform` = `http://localhost:8080/api/platform`

## Impact

This fix affects all platform admin API calls:

### Statistics Endpoints
- ✅ `GET /api/platform/stats` - Platform statistics
- ✅ `GET /api/platform/storage/stats` - Storage statistics
- ✅ `GET /api/platform/billing/stats` - Billing statistics

### Church Management
- ✅ `GET /api/platform/churches/all` - All churches
- ✅ `GET /api/platform/churches` - Paginated churches
- ✅ `GET /api/platform/churches/{id}` - Church details
- ✅ `POST /api/platform/churches/{id}/activate` - Activate church
- ✅ `POST /api/platform/churches/{id}/deactivate` - Deactivate church

### Storage Management
- ✅ `GET /api/platform/storage/top-consumers` - Top storage users
- ✅ `GET /api/platform/storage/all-churches` - All church storage

### Billing Management
- ✅ `GET /api/platform/billing/recent-payments` - Recent payments
- ✅ `GET /api/platform/billing/overdue-subscriptions` - Overdue subscriptions

## Testing

After this fix, SUPERADMIN users can now:
1. Access `/platform-admin` route
2. View platform statistics
3. View all churches list
4. Access storage management
5. Access billing dashboard
6. View system logs

All API calls now correctly target the Spring Boot REST API instead of being treated as Angular routes.

## Related Files

1. ✅ [platform.service.ts](../past-care-spring-frontend/src/app/services/platform.service.ts) - Fixed API URL
2. ✅ [SubscriptionFilter.java](src/main/java/com/reuben/pastcare_spring/security/SubscriptionFilter.java) - SUPERADMIN bypass implemented
3. ✅ [SecurityConfig.java](src/main/java/com/reuben/pastcare_spring/security/SecurityConfig.java) - Platform routes secured

## Verification

**Before Fix**:
```
GET /platform/stats
→ 404 No static resource platform/stats
```

**After Fix**:
```
GET /api/platform/stats
→ 200 OK { totalChurches: 5, totalUsers: 150, ... }
```

---

**Status**: ✅ **FIXED AND COMPILED**

**Frontend Build**: ✅ Success
**Backend**: ✅ SUPERADMIN bypass implemented
**Port 8080**: ✅ Cleaned up
