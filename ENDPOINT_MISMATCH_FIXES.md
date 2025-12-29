# Endpoint Mismatch Fixes

**Date:** 2025-12-28
**Issue:** Backend 500 errors - endpoints not found

---

## Root Cause

The issue was **NOT** that the backend needed restarting. The real problem was **endpoint URL mismatches** between frontend and backend.

### Errors Observed
```
GET http://localhost:8080/api/dashboard/top-members 500 (Internal Server Error)
GET http://localhost:8080/api/sms/credits/balance 500 (Internal Server Error)
```

These appeared to be "No static resource" errors, which initially suggested missing endpoints. However, the endpoints existed - they just had different URLs.

---

## Fixes Applied

### Fix 1: Top Active Members Endpoint

**Frontend was calling:** `/api/dashboard/top-members`  
**Backend endpoint is:** `/api/dashboard/top-active-members`

**Fixed in:** `dashboard.service.ts:142`

**Before:**
```typescript
getTopActiveMembers(): Observable<MemberEngagement[]> {
  return this.http.get<MemberEngagement[]>(`${this.apiUrl}/dashboard/top-members`);
}
```

**After:**
```typescript
getTopActiveMembers(): Observable<MemberEngagement[]> {
  return this.http.get<MemberEngagement[]>(`${this.apiUrl}/dashboard/top-active-members`);
}
```

---

### Fix 2: SMS Credits Balance Endpoint

**Frontend was calling:** `/api/sms/credits/balance`  
**Backend endpoint is:** `/api/church-sms-credits/balance`

**Fixed in:** `dashboard.service.ts:184`

**Before:**
```typescript
getSmsCredits(): Observable<SmsCreditsBalance> {
  return this.http.get<SmsCreditsBalance>(`${this.apiUrl}/sms/credits/balance`);
}
```

**After:**
```typescript
getSmsCredits(): Observable<SmsCreditsBalance> {
  return this.http.get<SmsCreditsBalance>(`${this.apiUrl}/church-sms-credits/balance`);
}
```

---

## Why This Happened

These endpoints were created in **different sessions**:

1. **DashboardController** endpoints were created with specific naming conventions
2. **ChurchSmsCreditController** was created later with the `/church-sms-credits` prefix
3. Frontend service methods were created with assumed endpoint names that didn't match

The backend was working correctly - the frontend just had the wrong URLs.

---

## Build Status

✅ **Frontend:** SUCCESS (build completed with warnings only)

---

## Testing

After frontend rebuild, these endpoints should now work:
1. Top Active Members widget will load correctly
2. SMS Credits Balance widget will load correctly
3. No more 500 errors for these endpoints

---

## Files Modified

1. **dashboard.service.ts** (line 142) - Fixed top-members → top-active-members
2. **dashboard.service.ts** (line 184) - Fixed sms/credits/balance → church-sms-credits/balance

---

**Status:** ✅ Fixed - Frontend needs reload in browser to pick up new build
