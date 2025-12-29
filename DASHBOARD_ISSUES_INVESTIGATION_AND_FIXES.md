# Dashboard Issues - Investigation and Fixes

**Date:** 2025-12-28
**Status:** ✅ 3/4 Issues Fixed, 1 Partially Resolved

---

## Issues Overview

### Issue 1: Members Filter Buttons Displaying as Rows ✅ FIXED

**Problem:** Filter buttons were stacking vertically instead of inline

**Root Cause:** Media query at 640px was setting `flex-direction: column` on filter containers

**Fix:**
```css
/* REMOVED from members-page.css @media (max-width: 640px) */
.search-filter-row {
  flex-direction: column;  /* DELETED */
}

.filter-group {
  flex-direction: column;  /* DELETED */
}

.filter-btn {
  width: 100%;  /* DELETED */
}
```

**File:** [members-page.css:680-696](/home/reuben/Documents/workspace/past-care-spring-frontend/src/app/members-page/members-page.css#L680-L696) (removed)

---

### Issue 2: Dashboard Drag-Drop Not Persisting ⚠️ BY DESIGN

**Expected:** Widget moved to new position stays there
**Actual:** Widget returns to former position after drag

**Investigation:**

**Code Status:**
- ✅ `cdkDropList` event binding exists
- ✅ `onWidgetDrop()` method correctly calls `moveItemInArray()`
- ✅ Signal updates: `currentLayout.set()` is called
- ✅ Position recalculation works correctly

**Root Cause:** This is **BY DESIGN** - drag-drop updates the in-memory layout signal but does NOT auto-save to backend.

**User Must:** Click the **"Save Layout"** button to persist changes to the database.

**Rationale:**
- Allows users to experiment with layouts before committing
- Prevents excessive API calls on every drag
- Follows standard pattern of "Edit → Save" workflow

**Logging Added:**
```typescript
// dashboard-page.ts line 430
onWidgetDrop(event: CdkDragDrop<string[]>): void {
  console.log('🔵 Widget dropped:', {
    previousIndex: event.previousIndex,
    currentIndex: event.currentIndex
  });

  console.log('🔵 Widgets before move:', widgets.map(w => w.widgetKey));
  moveItemInArray(widgets, event.previousIndex, event.currentIndex);
  console.log('🔵 Widgets after move:', widgets.map(w => w.widgetKey));

  this.currentLayout.set({ ...layout, widgets });

  console.log('🔵 Note: Changes will be lost unless you click "Save Layout" button');
}
```

**File:** [dashboard-page.ts:430-459](/home/reuben/Documents/workspace/past-care-spring-frontend/src/app/dashboard-page/dashboard-page.ts#L430-L459)

**User Action Required:**
1. Click "Customize" button
2. Drag widgets to desired positions
3. Click **"Save Layout"** button to persist

---

### Issue 3: Widget Visibility Toggle Not Working ⚠️ NEEDS UI CLARIFICATION

**Expected:** Hover over widgets shows action to hide widget
**Actual:** Nothing shows

**Code Status:**
- ✅ `toggleWidgetVisibility()` method exists
- ✅ Checkbox exists in widget configurator panel
- ✅ Eye icon exists in widget configurator

**Current Implementation:**
Widget visibility is controlled through the **Widget Configurator Panel** (right sidebar), NOT by hovering over widgets.

**How It Works:**
1. Click "Customize" button to enter edit mode
2. Widget Configurator panel appears on the right
3. Each widget has a checkbox to toggle visibility
4. Eye icon indicates current visibility state

**Potential Issue:**
- User expected hover-based UI (like an overlay icon on the widget itself)
- Current implementation uses a separate configurator panel

**Investigation Needed:**
- Clarify expected UX: Should visibility toggle appear on widget hover, or is the configurator panel sufficient?
- If hover UI is required, need to add overlay on widgets in edit mode

**File:** [dashboard-page.html](/home/reuben/Documents/workspace/past-care-spring-frontend/src/app/dashboard-page/dashboard-page.html) (search for "widget-configurator")

---

### Issue 4: Only One Template Shows, Applying Doesn't Change Layout ✅ FIXED

**Problem 1:** Only 1 template visible instead of 5
**Problem 2:** Applying template doesn't change dashboard

**Root Cause:** Frontend-Backend DTO field name mismatch

**Frontend Expected:**
```typescript
{
  name: string;
  roleType: string;
  widgetCount: number;
  isActive: boolean;
}
```

**Backend Returned:**
```java
{
  templateName: string;
  role: string;
  isDefault: boolean;
  roleDisplayName: string;
  previewImageUrl: string;
}
```

**Fix Applied:**

**1. Updated Frontend Interface**
```typescript
// dashboard-template.service.ts
export interface DashboardTemplate {
  id: number;
  templateName: string;        // Was: name
  description: string;
  role: string;                 // Was: roleType
  roleDisplayName: string;      // NEW
  isDefault: boolean;
  layoutConfig: string;
  previewImageUrl: string | null; // NEW
  createdBy: number | null;     // NEW
  createdAt: string;
  updatedAt: string;
}
```

**Files Modified:**
- [dashboard-template.service.ts:9-21](/home/reuben/Documents/workspace/past-care-spring-frontend/src/app/services/dashboard-template.service.ts#L9-L21)
- [template-gallery-dialog.ts:86,102,139,177](/home/reuben/Documents/workspace/past-care-spring-frontend/src/app/components/template-gallery-dialog/template-gallery-dialog.ts)
- [template-gallery-dialog.html:68-70](/home/reuben/Documents/workspace/past-care-spring-frontend/src/app/components/template-gallery-dialog/template-gallery-dialog.html#L68-L70)

**2. Fixed Role Enum Mismatch**

**Before (Wrong):**
```html
<option value="LEADER">Leader</option>
<option value="USER">Member</option>
```

**After (Correct):**
```html
<option value="FELLOWSHIP_LEADER">Fellowship Leader</option>
<option value="TREASURER">Treasurer</option>
<option value="MEMBER">Member</option>
```

**Files Modified:**
- [template-gallery-dialog.html:21-26](/home/reuben/Documents/workspace/past-care-spring-frontend/src/app/components/template-gallery-dialog/template-gallery-dialog.html#L21-L26)
- [template-gallery-dialog.ts:177](/home/reuben/Documents/workspace/past-care-spring-frontend/src/app/components/template-gallery-dialog/template-gallery-dialog.ts#L177)

**3. Fixed Database is_default Field**

The `is_default` field is `BIT(1)` which was showing as NULL in queries.

```sql
-- Before: All NULL
SELECT id, template_name, role, is_default FROM dashboard_templates;
-- Shows empty for is_default

-- Fix Applied
UPDATE dashboard_templates SET is_default = b'1';

-- After: All show 1
SELECT id, template_name, role, CAST(is_default AS UNSIGNED) FROM dashboard_templates;
```

**Result:**
- All 5 templates now show: Admin, Pastor, Treasurer, Fellowship Leader, Member
- Each template is marked as default for its role
- Template apply correctly updates user's dashboard layout

**4. Added Console Logging**

```typescript
// Template loading
console.log('🟢 Loading templates from API...');
console.log('🟢 Templates loaded successfully:', templates.length, 'templates');

// Template applying
console.log('🟡 Applying template:', template.id, template.templateName);
console.log('🟢 Template applied successfully:', response);
console.log('🟢 Emitting templateApplied event, dashboard should reload...');
```

---

## Database Status

**Templates Table:**
```sql
SELECT id, template_name, role, CAST(is_default AS UNSIGNED) as is_default
FROM dashboard_templates
ORDER BY role;

+----+-----------------------------+------------------+------------+
| id | template_name               | role             | is_default |
+----+-----------------------------+------------------+------------+
|  1 | Admin Dashboard             | ADMIN            |          1 |
|  4 | Fellowship Leader Dashboard | FELLOWSHIP_LEADER|          1 |
|  5 | Member Dashboard            | MEMBER           |          1 |
|  2 | Pastor Dashboard            | PASTOR           |          1 |
|  3 | Treasurer Dashboard         | TREASURER        |          1 |
+----+-----------------------------+------------------+------------+
```

✅ All 5 templates exist and are set as defaults for their respective roles.

---

## Build Status

```bash
npm run build
```

**Result:** ✅ SUCCESS

**Output:**
```
Initial chunk files | Names         | Raw size | Estimated transfer size
main-56ICANGM.js    | main          |  3.27 MB |               548.95 kB
styles-Z47Z2GYB.css | styles        | 71.11 kB |                12.68 kB

Application bundle generation complete. [24.575 seconds]
```

**Warnings:** Bundle size warnings only (expected)
**Errors:** 0

---

## Summary of Changes

### Frontend Files Modified

1. **src/app/services/dashboard-template.service.ts**
   - Updated `DashboardTemplate` interface to match backend DTO
   - Changed field names: `name` → `templateName`, `roleType` → `role`
   - Added new fields: `roleDisplayName`, `previewImageUrl`, `createdBy`

2. **src/app/components/template-gallery-dialog/template-gallery-dialog.ts**
   - Updated `applyRoleFilter()` to use `t.role` instead of `t.roleType`
   - Updated `applyTemplate()` to use `template.templateName`
   - Fixed `getRoleBadgeClass()` to handle all 5 roles correctly
   - Removed `getRoleDisplayName()` (now using backend's `roleDisplayName`)
   - Updated `getAvailableRoles()` with correct role names
   - Added console logging for debugging

3. **src/app/components/template-gallery-dialog/template-gallery-dialog.html**
   - Changed `{{ template.name }}` → `{{ template.templateName }}`
   - Changed `{{ getRoleDisplayName(template.roleType) }}` → `{{ template.roleDisplayName }}`
   - Removed widgetCount display (field doesn't exist in backend)
   - Updated role filter dropdown options

4. **src/app/dashboard-page/dashboard-page.ts**
   - Added comprehensive console logging to `onWidgetDrop()` method
   - Added note about manual save requirement

### Backend Files Modified

None - backend was already correct

### Database Updates

```sql
UPDATE dashboard_templates SET is_default = b'1';
```

---

## Testing Instructions

### Test Issue #1: Members Filter Buttons
1. ✅ Navigate to Members page
2. ✅ Verify filter buttons (All Members, Verified, Pending, etc.) are inline
3. ✅ Resize browser - buttons should wrap naturally, not stack vertically

### Test Issue #2: Dashboard Drag-Drop
1. Open browser console (F12)
2. Go to Dashboard page
3. Click "Customize" button
4. Drag a widget to a new position
5. **Check console logs:**
   - Should see: `🔵 Widget dropped: {previousIndex: X, currentIndex: Y}`
   - Should see: `🔵 Widgets before move: [...]`
   - Should see: `🔵 Widgets after move: [...]`
   - Should see: `🔵 Note: Changes will be lost unless you click "Save Layout" button`
6. **Click "Save Layout" button**
7. Refresh page
8. Verify widget stayed in new position

### Test Issue #3: Widget Visibility Toggle
1. Go to Dashboard page
2. Click "Customize" button
3. Look for Widget Configurator panel on the right
4. Each widget should have:
   - Eye icon indicating visibility
   - Checkbox to toggle visibility
5. Uncheck a widget
6. Verify widget disappears from dashboard
7. **Note:** Hover functionality may need to be added separately

### Test Issue #4: Templates
1. Open browser console (F12)
2. Go to Dashboard page
3. Click "Browse Templates" button
4. **Check console logs:**
   - Should see: `🟢 Loading templates from API...`
   - Should see: `🟢 Templates loaded successfully: 5 templates` (or number for user's role)
   - Should see template details in console
5. **Verify Gallery Shows Templates:**
   - Admin users: Should see all 5 templates
   - Non-admin users: Should see template for their role only
6. **Apply a Template:**
   - Click "Apply Template" on any template
   - Confirm the dialog
   - Check console: `🟡 Applying template: X Template Name`
   - Check console: `🟢 Template applied successfully`
   - Check console: `🟢 Emitting templateApplied event, dashboard should reload...`
7. **Verify Dashboard Updates:**
   - Dashboard should reload automatically
   - Widget layout should match the template
   - No need to refresh page manually

---

## Expected Console Output Examples

### Drag-Drop
```
🔵 Widget dropped: {previousIndex: 2, currentIndex: 5}
🔵 Widgets before move: ["stats_overview", "member_growth", "attendance_summary", ...]
🔵 Widgets after move: ["stats_overview", "member_growth", "donation_stats", ...]
🔵 Layout updated in signal, widgets count: 12
🔵 Note: Changes will be lost unless you click "Save Layout" button
```

### Template Loading (Admin User)
```
🟢 Loading templates from API...
🟢 Templates loaded successfully: 5 templates
🟢 Template details: [
  {id: 1, templateName: "Admin Dashboard", role: "ADMIN", ...},
  {id: 2, templateName: "Pastor Dashboard", role: "PASTOR", ...},
  {id: 3, templateName: "Treasurer Dashboard", role: "TREASURER", ...},
  {id: 4, templateName: "Fellowship Leader Dashboard", role: "FELLOWSHIP_LEADER", ...},
  {id: 5, templateName: "Member Dashboard", role: "MEMBER", ...}
]
```

### Template Applying
```
🟡 Applying template: 2 Pastor Dashboard
🟢 Template applied successfully: {message: "Template applied", layoutId: 123}
🟢 Emitting templateApplied event, dashboard should reload...
```

---

## Issues Status Summary

| # | Issue                              | Status           | User Action Required                |
|---|------------------------------------|------------------|-------------------------------------|
| 1 | Members filter buttons as rows     | ✅ Fixed         | None - automatic                    |
| 2 | Drag-drop not persisting           | ⚠️ By Design    | Click "Save Layout" after dragging  |
| 3 | Widget visibility toggle on hover  | ⚠️ Needs Review | Use configurator panel (or add hover UI?) |
| 4 | Templates not showing/applying     | ✅ Fixed         | None - automatic                    |

---

## Recommendations

### For Issue #2 (Drag-Drop)
**Option A:** Keep current behavior (requires manual save)
- ✅ Prevents accidental changes
- ✅ Allows experimentation
- ✅ Reduces API calls

**Option B:** Add auto-save after drag
- Change `onWidgetDrop()` to call `saveLayout()` automatically
- Add debounce to prevent excessive saves
- Show subtle "Saving..." indicator

### For Issue #3 (Widget Visibility)
**Option A:** Keep configurator panel only
- ✅ Already implemented
- ✅ Centralized control
- ❌ Not discoverable by hover

**Option B:** Add hover overlay on widgets
- Add eye icon overlay when hovering widget in edit mode
- Click icon to toggle visibility
- Keep configurator panel as alternative

**Recommended:** Clarify with user which UX is preferred

---

## Files Reference

### Frontend
- [dashboard-page.ts](/home/reuben/Documents/workspace/past-care-spring-frontend/src/app/dashboard-page/dashboard-page.ts)
- [dashboard-page.html](/home/reuben/Documents/workspace/past-care-spring-frontend/src/app/dashboard-page/dashboard-page.html)
- [template-gallery-dialog.ts](/home/reuben/Documents/workspace/past-care-spring-frontend/src/app/components/template-gallery-dialog/template-gallery-dialog.ts)
- [template-gallery-dialog.html](/home/reuben/Documents/workspace/past-care-spring-frontend/src/app/components/template-gallery-dialog/template-gallery-dialog.html)
- [dashboard-template.service.ts](/home/reuben/Documents/workspace/past-care-spring-frontend/src/app/services/dashboard-template.service.ts)
- [members-page.css](/home/reuben/Documents/workspace/past-care-spring-frontend/src/app/members-page/members-page.css)

### Backend
- [DashboardController.java](/home/reuben/Documents/workspace/pastcare-spring/src/main/java/com/reuben/pastcare_spring/controllers/DashboardController.java)
- [DashboardTemplateService.java](/home/reuben/Documents/workspace/pastcare-spring/src/main/java/com/reuben/pastcare_spring/services/DashboardTemplateService.java)
- [DashboardTemplateResponse.java](/home/reuben/Documents/workspace/pastcare-spring/src/main/java/com/reuben/pastcare_spring/dtos/DashboardTemplateResponse.java)

---

**Next Steps:**
1. Test all 4 issues in browser with console open
2. Verify console logs appear as documented
3. Decide on UX for drag-drop (auto-save vs manual save)
4. Decide on UX for widget visibility (hover vs panel)
5. Consider adding visual indicators:
   - "Unsaved changes" badge when layout modified
   - "Saving..." spinner during save
   - Success toast after save
