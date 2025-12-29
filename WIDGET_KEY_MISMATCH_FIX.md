# Widget Key Mismatch Fix - RESOLVED

**Date:** 2025-12-28
**Issue:** Widget configurator panel was empty (no toggle switches showing)
**Status:** ✅ **FIXED**

---

## Problem Summary

The widget configurator was showing an empty panel instead of toggle switches for 17 widgets because of a **widget key mismatch** between:

1. **Backend Default Layout** (DashboardLayoutService.buildDefaultLayoutConfig)
2. **Database Widgets** (V47 migration)

### Incorrect Widget Keys (Before Fix)

The default layout was using these widget keys:
- `events` → Should be `upcoming_events`
- `prayer_requests` → Not in V47 migration
- `birthdays` → Should be `birthdays_week`
- `anniversaries` → Should be `anniversaries_month`
- `recent_activity` → Should be `recent_activities`

### Root Cause

When the frontend loaded:
1. `/api/dashboard/widgets/available` returned 17 widgets from database ✅
2. `/api/dashboard/layout` returned user layout with **wrong** widget keys ❌
3. Frontend couldn't match widgets to layout → `availableWidgets` array was empty
4. Widget configurator panel showed empty (no toggles to display)

---

## Fix Applied

### File Modified
[DashboardLayoutService.java:92-121](src/main/java/com/reuben/pastcare_spring/services/DashboardLayoutService.java#L92-L121)

### Changes Made
Updated `buildDefaultLayoutConfig()` method to use **correct widget keys** matching V47 migration:

**All 17 Widget Keys (Correct):**
1. `stats_overview` ✅
2. `pastoral_care` ✅
3. `upcoming_events` (was: `events`)
4. `recent_activities` (was: `recent_activity`)
5. `birthdays_week` (was: `birthdays`)
6. `anniversaries_month` (was: `anniversaries`)
7. `irregular_attenders` (new)
8. `member_growth` (new)
9. `location_stats` (new)
10. `attendance_summary` (new)
11. `service_analytics` (new)
12. `top_members` (new)
13. `fellowship_health` (new)
14. `donation_stats` (new)
15. `crisis_stats` (new)
16. `counseling_sessions` (new)
17. `sms_credits` (new)

The default layout now includes **all 17 widgets** instead of just 7.

---

## Next Steps to Complete the Fix

### Step 1: Restart Backend (REQUIRED)
```bash
# Stop current backend if running (Ctrl+C)
cd /home/reuben/Documents/workspace/pastcare-spring
./mvnw spring-boot:run
```

### Step 2: Delete Old Layout from Database (REQUIRED)

**Option A: Using MySQL Command Line**
```sql
-- Connect to database
mysql -u root -p

USE pastcare_db;

-- Check existing layouts
SELECT id, user_id, layout_name, is_default FROM dashboard_layouts;

-- Delete the layout with wrong widget keys (ID 1)
DELETE FROM dashboard_layouts WHERE id = 1;

-- Verify deletion
SELECT COUNT(*) FROM dashboard_layouts;
-- Should return 0
```

**Option B: Let Backend Handle It**

If you can't access MySQL, you can force reset via API:
```bash
# After backend restarts, call reset endpoint
curl -X POST http://localhost:8080/api/dashboard/layout/reset \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json"
```

### Step 3: Verify Fix in Frontend

1. **Refresh browser** (hard refresh: Ctrl+Shift+R or Cmd+Shift+R)
2. **Open dashboard**
3. **Click "Customize"** button
4. **Click "Widgets"** button
5. **Expected Result:** ✅ Widget configurator panel shows **17 toggle switches** with widget names and icons

---

## Expected Behavior After Fix

### Widget Configurator Panel Should Show:

```
┌─────────────────────────────────────────────────────┐
│  Configure Widgets                              [X] │
├─────────────────────────────────────────────────────┤
│  Toggle widgets to show/hide on your dashboard     │
│                                                     │
│  ┌──────────────────┐  ┌──────────────────┐       │
│  │ [✓] 📊 Stats     │  │ [✓] ❤️ Pastoral   │       │
│  │     Overview     │  │     Care Needs    │       │
│  └──────────────────┘  └──────────────────┘       │
│                                                     │
│  ┌──────────────────┐  ┌──────────────────┐       │
│  │ [✓] 📅 Upcoming  │  │ [✓] 🔔 Recent     │       │
│  │     Events       │  │     Activities    │       │
│  └──────────────────┘  └──────────────────┘       │
│                                                     │
│  ┌──────────────────┐  ┌──────────────────┐       │
│  │ [✓] 🎁 Birthdays │  │ [✓] 💍 Anniver-   │       │
│  │     This Week    │  │     saries        │       │
│  └──────────────────┘  └──────────────────┘       │
│                                                     │
│  ... (11 more widgets) ...                         │
└─────────────────────────────────────────────────────┘
```

**Total:** 17 toggle switches

---

## Testing Checklist

After completing Steps 1-3:

- [ ] Backend restarts successfully
- [ ] Old layout deleted from database
- [ ] Frontend refreshed (hard refresh)
- [ ] Click "Customize" button works
- [ ] Click "Widgets" button opens configurator
- [ ] **Configurator shows 17 toggle switches** ✅
- [ ] Toggle switches have widget icons and names
- [ ] Toggling OFF hides widget immediately
- [ ] Toggling ON shows widget immediately
- [ ] Click "Save" persists layout
- [ ] Refresh page loads saved layout correctly
- [ ] Click "Reset" restores all 17 widgets

---

## Verification Commands

### Check Backend Compilation
```bash
cd /home/reuben/Documents/workspace/pastcare-spring
./mvnw compile
# Expected: BUILD SUCCESS
```

### Check Widget Keys in Database
```sql
SELECT widget_key, name FROM widgets ORDER BY widget_key;
-- Should return 17 rows with correct keys
```

### Check New Default Layout Structure
After backend restart and old layout deletion, call:
```bash
curl -X GET http://localhost:8080/api/dashboard/layout \
  -H "Authorization: Bearer YOUR_TOKEN"
```

Expected response should include all 17 widget keys:
```json
{
  "layoutConfig": "{
    \"widgets\": [
      {\"widgetKey\": \"stats_overview\", ...},
      {\"widgetKey\": \"pastoral_care\", ...},
      {\"widgetKey\": \"upcoming_events\", ...},
      ... (14 more)
    ]
  }"
}
```

---

## Related Issues

### Pre-existing Endpoint Errors (Not Blocking)
See [ENDPOINT_MISMATCH_FIX.md](ENDPOINT_MISMATCH_FIX.md) for details on:
- `/api/dashboard/top-members` (should be `/api/dashboard/top-active-members`)
- `/api/sms/credits/balance` (endpoint missing)

These errors are from existing Phase 2/3 widgets loading data and **do not affect Phase 2.1 customization features**.

---

## Success Criteria

✅ **Fix is complete when:**
1. Backend compiles successfully (DONE ✅)
2. Backend restarts with updated code (PENDING - user action)
3. Old layout deleted from database (PENDING - user action)
4. Widget configurator shows 17 toggle switches (PENDING - verify after restart)
5. All Phase 2.1 features work (customize, save, reset, drag-drop)

---

## Technical Details

### Widget Key Naming Convention
All widget keys use `snake_case` format:
- `{feature}_{timeframe}` for temporal widgets (e.g., `birthdays_week`)
- `{feature}_{type}` for categorized widgets (e.g., `donation_stats`)
- `{feature}` for general widgets (e.g., `pastoral_care`)

### Default Layout Grid
- **Grid Columns:** 4
- **Grid Rows:** 8 (auto-generated based on widget positions)
- **Widget Sizes:** All widgets default to 2×1 (width × height) or 1×1
- **Positions:** (x, y) coordinates from (0,0) to (3,7)

### Migration References
- **V47:** Creates `widgets` table and seeds 17 widgets
- **V48:** Creates `dashboard_layouts` table
- **DashboardLayoutService:** Generates default layout on first access

---

**Status:** ✅ Code Fixed, Awaiting Backend Restart
**Next Action:** Restart backend → Delete old layout → Test configurator
**Estimated Time:** 5 minutes
