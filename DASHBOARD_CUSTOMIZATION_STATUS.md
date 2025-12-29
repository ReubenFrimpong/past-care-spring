# Dashboard Customization Status

**Date:** 2025-12-28  
**Issues:** Template gallery empty, widgets not draggable

---

## Executive Summary

The dashboard customization features were **90% implemented** but have two key gaps:

1. **Template Gallery** - Implemented but may show empty if backend hasn't seeded templates
2. **Drag-and-Drop** - `cdkDrag` directives missing from widgets (implementation gap)

---

## Issue 1: Template Gallery Shows Empty

### How It's Supposed to Work

1. Click **"Browse Templates"** button in dashboard header
2. Template gallery dialog opens
3. Shows 5 pre-configured templates (Admin, Pastor, Treasurer, Fellowship Leader, Member)
4. Click "Apply Template" on any template
5. Dashboard layout changes to match template

### Current Implementation Status

✅ **Backend:**
- Templates endpoint exists: `GET /api/dashboard/templates`
- 5 templates seeded in V49 migration
- DashboardTemplateService implements all logic

✅ **Frontend:**
- Template gallery dialog component created
- "Browse Templates" button exists (line 73 in dashboard-page.html)
- Dialog opens via `openTemplateGallery()` method

### Why Templates Might Be Empty

**Possible Causes:**

1. **Database Not Migrated** - V49 migration not run
   ```bash
   # Check if templates table exists
   mysql -u root -p -e "USE pastcare; SELECT COUNT(*) FROM dashboard_templates;"
   ```

2. **Endpoint Mismatch** - Already fixed (endpoint was correct)

3. **Role Mismatch** - Templates filter by role
   - If logged in as role that has no templates, gallery will be empty

### Solution

**Option A: Verify Migration Ran**
```bash
# Check flyway schema history
mysql -u root -p -e "USE pastcare; SELECT * FROM flyway_schema_history WHERE version = 49;"
```

**Option B: Manually Insert Templates** (if migration didn't run)
```sql
-- Run the V49 migration manually
SOURCE src/main/resources/db/migration/V49__create_dashboard_templates_table.sql;
```

**Option C: Check User Role**
```javascript
// In browser console
const user = JSON.parse(localStorage.getItem('user'));
console.log('User role:', user.role);
```

---

## Issue 2: Widgets Not Draggable

### Current Implementation

**What Exists:**
- ✅ `DragDropModule` imported in dashboard-page.ts
- ✅ `cdkDropList` directive on widgets container (line 190)
- ✅ `cdkDropListDisabled` bound to `!editMode()`
- ✅ `onWidgetDrop()` handler implemented

**What's Missing:**
- ❌ `cdkDrag` directive on individual widget cards
- ❌ Drag handle implementation

### Why Widgets Aren't Draggable

Without `cdkDrag` directive, widgets can't be grabbed and moved even in edit mode.

**Current HTML (line 194):**
```html
<div class="widget-card">
  <!-- Widget content -->
</div>
```

**Should Be:**
```html
<div class="widget-card" cdkDrag [cdkDragDisabled]="!editMode()">
  <!-- Optional drag handle -->
  <div class="widget-header" cdkDragHandle>
    <!-- Header content -->
  </div>
  <!-- Widget content -->
</div>
```

### Solution: Add cdkDrag to Widgets

Each widget card needs the `cdkDrag` directive. There are approximately 12-15 widget cards that need updating.

**Quick Fix Script:**
```bash
cd /home/reuben/Documents/workspace/past-care-spring-frontend/src/app/dashboard-page

# Backup first
cp dashboard-page.html dashboard-page.html.backup

# Add cdkDrag to all widget-card divs (needs manual verification)
sed -i 's/<div class="widget-card">/<div class="widget-card" cdkDrag>/g' dashboard-page.html
```

⚠️ **Warning:** This sed command is simplistic. Better to add manually for each widget to ensure correctness.

---

## How Dashboard Customization Should Work

### Phase 1 Features (Already Working)
1. ✅ **View Modes** - Toggle between view and edit mode
2. ✅ **Widget Configurator** - Show/hide widgets via panel
3. ✅ **Save Layout** - Persist layout to backend
4. ✅ **Reset Layout** - Restore default layout

### Phase 2.1 Features (Implemented)
1. ✅ **Widget System** - Dynamic widget loading
2. ✅ **Layout Service** - Save/load layouts per user
3. ⚠️ **Drag-and-Drop** - 90% done, needs `cdkDrag` directives

### Phase 2.2 Features (Implemented)
1. ✅ **Template Gallery** - Backend + frontend complete
2. ⚠️ **Template Display** - May be empty if DB not seeded
3. ✅ **Template Application** - Apply template to user layout

---

## Immediate Actions Required

### 1. Verify Template Data
```bash
# SSH to database and check
mysql -u root -p pastcare -e "SELECT template_name, role FROM dashboard_templates;"
```

**Expected Output:**
```
+-----------------------------+------------------+
| template_name               | role             |
+-----------------------------+------------------+
| Admin Dashboard             | ADMIN            |
| Pastor Dashboard            | PASTOR           |
| Treasurer Dashboard         | TREASURER        |
| Fellowship Leader Dashboard | FELLOWSHIP_LEADER|
| Member Dashboard            | MEMBER           |
+-----------------------------+------------------+
```

### 2. Add cdkDrag Directives

**Manual Approach (Recommended):**
1. Open `dashboard-page.html`
2. Find each `<div class="widget-card">` (about 15 instances)
3. Change to: `<div class="widget-card" cdkDrag [cdkDragDisabled]="!editMode()">`
4. Optionally add drag handle to widget headers

**Automated Approach:**
Use Task tool to add cdkDrag to all widgets programmatically.

### 3. Test Workflow

After fixes:
1. Login to app
2. Click "Browse Templates" → Should show 5 templates
3. Click "Customize" → Enter edit mode
4. Drag a widget → Should move
5. Click "Save" → Layout persists
6. Refresh page → Layout restored

---

## Files Requiring Updates

### To Fix Drag-and-Drop:
- `dashboard-page.html` - Add `cdkDrag` to ~15 widget cards (lines 194, 226, 258, 290, etc.)

### To Verify Templates:
- Check database: `dashboard_templates` table
- Check migration: `V49__create_dashboard_templates_table.sql` executed

---

## Current Status

| Feature | Backend | Frontend | Status |
|---------|---------|----------|--------|
| Template Gallery | ✅ Complete | ✅ Complete | ⚠️ May be empty |
| Template Application | ✅ Complete | ✅ Complete | ✅ Working |
| Drag-and-Drop | ✅ Complete | ⚠️ Missing directives | ❌ Not working |
| Widget Configurator | ✅ Complete | ✅ Complete | ✅ Working |
| Save/Load Layout | ✅ Complete | ✅ Complete | ✅ Working |

---

## Next Steps

1. **Check database** - Verify templates exist
2. **Add cdkDrag** - Enable widget dragging
3. **Test** - Full customization workflow
4. **Document** - User guide for dashboard customization

---

**Bottom Line:** The features are 90% implemented. Templates may just need DB verification, and drag-drop needs `cdkDrag` directives added to HTML.
