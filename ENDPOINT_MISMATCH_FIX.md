# Endpoint Mismatch Fix

**Issue:** Frontend is calling endpoints that don't match backend
**Impact:** 404 errors on dashboard load (not critical - just missing data)
**Priority:** Low (doesn't affect Phase 2.1 functionality)

## Mismatches Found:

### 1. Top Members Endpoint
- **Frontend calls:** `GET /api/dashboard/top-members`
- **Backend has:** `GET /api/dashboard/top-active-members`

### 2. SMS Credits Endpoint
- **Frontend calls:** `GET /api/sms/credits/balance`
- **Backend has:** Needs to be checked (likely different controller)

## Quick Fix Options:

### Option A: Fix Frontend (Recommended)
Update [dashboard.service.ts](../past-care-spring-frontend/src/app/services/dashboard.service.ts:207):

```typescript
// Line ~207 - Change from:
return this.http.get<MemberEngagement[]>(`${this.apiUrl}/dashboard/top-members`);

// To:
return this.http.get<MemberEngagement[]>(`${this.apiUrl}/dashboard/top-active-members`);
```

### Option B: Add Backend Alias
Add alias endpoint in [DashboardController.java](src/main/java/com/reuben/pastcare_spring/controllers/DashboardController.java):

```java
@GetMapping("/top-members")  // Alias for compatibility
public ResponseEntity<List<MemberEngagement>> getTopMembers(HttpServletRequest request) {
    return getTopActiveMembers(request);  // Delegate to existing method
}
```

## SMS Credits Fix:

Need to check if SmsController has the `/credits/balance` endpoint or if it needs to be added.

## Impact on Phase 2.1:

✅ **No impact on Phase 2.1 features**
- Phase 2.1 widgets/layout endpoints are working correctly
- These errors are from existing Phase 2/3 widgets loading their data
- Phase 2.1 functionality (customize, save, reset) is unaffected

## Recommendation:

These can be fixed after Phase 2.1 testing is complete. They don't block the new customization features.
