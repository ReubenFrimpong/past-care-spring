# Dashboard Issues - Fix Plan

**Date:** 2025-12-28
**Status:** 🔍 **ANALYSIS COMPLETE - FIXES IN PROGRESS**

---

## Issues Identified

### Issue 1: Templates Not Showing in Gallery
**Symptom:** Only one template shows, or templates don't load properly
**Root Cause Analysis:**
1. Templates exist in V49 migration (5 templates defined)
2. `/api/dashboard/templates` endpoint exists but requires authentication
3. Frontend may not be properly fetching/displaying templates

**Investigation Needed:**
- Check if DashboardTemplateService is correctly injected in template-gallery-dialog
- Verify API call is being made with proper auth headers
- Check console for any errors during template loading

### Issue 2: Drag-Drop Not Rearranging Widgets
**Symptom:** Dragging widgets doesn't actually move them
**Root Cause Analysis:**
1. `cdkDrag` directives are present on widgets (added earlier)
2. `onWidgetDrop()` method exists in dashboard-page.ts
3. Method calls `moveItemInArray()` from Angular CDK
4. Layout is updated in signal: `currentLayout.set({ ...layout, widgets })`

**Potential Issue:**
- The `cdkDropListDropped` event may not be properly bound in the HTML
- The drop event handler might not be called
- Widget positions might be calculated but not visually reflected

**Investigation Needed:**
- Check if `(cdkDropListDropped)="onWidgetDrop($event)"` is bound in HTML
- Verify widgets are actually inside `cdkDropList`
- Check if CSS is preventing drag or if drag handle is needed

### Issue 3: E2E Tests Failing on Registration
**Symptom:** All 12 tests fail on register page
**Root Cause:** Test uses wrong selector for church name field

**Registration Form Structure:**
```html
<form [formGroup]="registerForm">
  <!-- Church Information -->
  <div formGroupName="church">
    <input id="churchName" formControlName="name" />  <!-- Church name -->
    <input id="churchAddress" formControlName="address" />
    <input id="churchPhone" formControlName="phoneNumber" />
    <input id="churchEmail" formControlName="email" />
    <input id="churchWebsite" formControlName="website" />
  </div>

  <!-- User Information -->
  <div formGroupName="user">
    <input id="name" formControlName="name" />  <!-- User name -->
    <input id="email" formControlName="email" />
    <input id="phoneNumber" formControlName="phoneNumber" />
    <input id="password" formControlName="password" />
  </div>
</form>
```

**Fix:** Use `#churchName` selector instead of `formControlName="churchName"`

---

## Fix Priority

1. **FIX E2E TESTS FIRST** - Update selectors to match actual form
2. **FIX DRAG-DROP** - Verify cdkDropList binding and event handling
3. **FIX TEMPLATES** - Debug template loading and display

---

## Immediate Actions

### Action 1: Fix E2E Test Registration Helper

**Current (broken):**
```typescript
await page.fill('input[formControlName="churchName"]', user.churchName);
```

**Fixed:**
```typescript
await page.fill('#churchName', user.churchName);  // Church info group
await page.fill('#name', user.name);              // User info group (not churchName!)
await page.fill('#email', user.email);
await page.fill('#password', user.password);
```

### Action 2: Verify Drag-Drop HTML Binding

**Check dashboard-page.html for:**
```html
<div cdkDropList (cdkDropListDropped)="onWidgetDrop($event)">
  <div *ngFor="let widget of getVisibleWidgets()"
       cdkDrag
       [cdkDragDisabled]="!editMode()">
    <!-- Widget content -->
  </div>
</div>
```

### Action 3: Debug Template Loading

**Add logging to template-gallery-dialog.ts:**
```typescript
ngOnInit() {
  console.log('Loading templates...');
  this.loadTemplates();
}

loadTemplates() {
  this.templateService.getTemplatesForUser().subscribe({
    next: (templates) => {
      console.log('Templates loaded:', templates.length, templates);
      this.templates.set(templates);
    },
    error: (err) => {
      console.error('Error loading templates:', err);
    }
  });
}
```

---

## Next Steps

1. Update E2E test with correct selectors
2. Run single E2E test to verify registration works
3. Check dashboard HTML for drag-drop binding
4. Add logging to template loading
5. Run full E2E suite once fixes are applied

---

**CRITICAL:** Once E2E tests pass, all three issues will be validated as fixed.
