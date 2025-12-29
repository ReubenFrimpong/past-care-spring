# Template Gallery Empty Select Fix

**Date:** 2025-12-28
**Issue:** Template gallery shows empty select dropdown

---

## Two Issues Identified

### Issue 1: Backend Not Restarted ⚠️ PRIMARY ISSUE

**Problem:** The backend is returning "No static resource" errors for `/api/dashboard/templates`

**Cause:** Backend was started before DashboardTemplateService was created, so the endpoints don't exist in the running instance.

**Solution:** Restart the backend
```bash
pkill -f "spring-boot:run"
./mvnw spring-boot:run
```

**Expected Result:** After restart, `GET /api/dashboard/templates` will return the 5 pre-configured templates:
- Admin Dashboard (12 widgets)
- Pastor Dashboard (10 widgets)  
- Treasurer Dashboard (8 widgets)
- Fellowship Leader Dashboard (9 widgets)
- Member Dashboard (5 widgets)

---

### Issue 2: Signal vs ngModel Binding ✅ FIXED

**Problem:** `selectedRole` is a signal but was used with two-way binding `[(ngModel)]`

**Before (template-gallery-dialog.html:17):**
```html
<select [(ngModel)]="selectedRole" (change)="onRoleFilterChange()">
```

**After:**
```html
<select 
  [ngModel]="selectedRole()" 
  (ngModelChange)="selectedRole.set($event); onRoleFilterChange()"
>
```

**Why This Matters:**
- Angular signals require explicit `.set()` to update values
- Two-way binding `[(ngModel)]` doesn't work with signals
- Must use one-way binding `[ngModel]` + change handler `(ngModelChange)`

---

## Testing Steps

1. **Restart Backend:**
   ```bash
   ./mvnw spring-boot:run
   ```

2. **Wait for startup** (look for "Started PastcareSpringApplication")

3. **Test endpoint manually:**
   ```bash
   # Should return array of 5 templates (with auth token)
   curl http://localhost:8080/api/dashboard/templates \
     -H "Authorization: Bearer YOUR_TOKEN"
   ```

4. **Open template gallery in frontend:**
   - Click "Browse Templates" button on dashboard
   - Should show 5 template cards
   - Role filter dropdown should work (if admin)

---

## Root Cause Analysis

### Why Empty Select?

The select dropdown was empty because:

1. **Backend not responding** → templates array stays empty `[]`
2. **Signal binding issue** → select couldn't update even if data loaded

### Why Backend Not Responding?

Spring Boot loads beans (services, controllers) at startup. The new services were created AFTER startup, so:
- `DashboardTemplateService` bean doesn't exist
- Controller endpoints that depend on it fail to register
- Spring treats requests as static resource lookups

---

## Files Modified

1. **template-gallery-dialog.html** (line 17-18)
   - Fixed: Signal binding for role filter select

## Next Actions

✅ **Frontend:** Fixed - build successful  
⚠️ **Backend:** Needs restart to load new services

Once backend is restarted, the template gallery will work correctly.

---

**Status:** Frontend fix complete, backend restart required
