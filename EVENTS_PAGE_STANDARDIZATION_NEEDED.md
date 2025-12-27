# Events Page - Standardization Needed

**Date:** December 27, 2025
**Reference:** Pastoral Care Page
**Current Status:** Events page uses PrimeNG components and FormBuilder; needs standardization

---

## Required Changes to Match Pastoral Care Pattern

### 1. Component Structure

**Current (Events):**
- Uses `ReactiveFormsModule` and `FormBuilder`
- Imports 11 PrimeNG modules (Dialog, Button, InputText, etc.)
- Uses `@HostListener` for scroll
- Mixed signals and regular properties

**Target (Pastoral Care):**
- Uses only `FormsModule` (two-way binding with ngModel)
- No PrimeNG modules - native HTML dialogs
- No scroll handling - uses pagination buttons
- All state in signals

### 2. Data Management

**Changes Needed:**
```typescript
// REMOVE: FormBuilder and ReactiveFormsModule
- eventForm!: FormGroup;
- private formBuilder: FormBuilder

// REPLACE WITH: Signal-based form
+ eventForm = signal<EventRequest>({...});
+ cancelForm = signal({ reason: '' });
```

### 3. UI State

**Changes Needed:**
```typescript
// REMOVE: Regular properties
- showAddDialog = false;
- showEditDialog = false;
- currentView: 'grid' | 'list' = 'grid';
- loading = signal<boolean>(false);

// REPLACE WITH: All signals
+ showAddDialog = signal(false);
+ showEditDialog = signal(false);
+ loading = signal(false);
+ error = signal<string | null>(null);
+ success = signal<string | null>(null);
```

### 4. Filtering

**Changes Needed:**
```typescript
// REMOVE: Mixed approach with if/else and applyFilters doing API calls
- applyFilters(): void {
    this.loadEvents(true);  // Makes API call
  }

// REPLACE WITH: Client-side filtering like Pastoral Care
+ applyFilters(): void {
    let filtered = this.events();
    // Filter in memory
    if (this.searchTerm()) {
      filtered = filtered.filter(...);
    }
    this.filteredEvents.set(filtered);
  }
```

### 5. Pagination

**Changes Needed:**
```typescript
// REMOVE: Infinite scroll with @HostListener
- @HostListener('window:scroll', [])
  onScroll(): void { ... }
- isLastPage = false;

// REPLACE WITH: Button-based pagination
+ previousPage(): void {
    if (this.currentPage() > 0) {
      this.currentPage.update(p => p - 1);
      this.loadEvents();
    }
  }
+ nextPage(): void { ... }
```

### 6. Dialogs

**Current:**
```html
<!-- PrimeNG Dialog -->
<p-dialog [(visible)]="showAddDialog" [modal]="true">
  <form [formGroup]="eventForm">
    <input pInputText formControlName="name" />
  </form>
</p-dialog>
```

**Target:**
```html
<!-- Native Dialog -->
@if (showAddDialog()) {
  <div class="dialog-overlay" (click)="closeDialogs()">
    <div class="dialog" (click)="$event.stopPropagation()">
      <div class="dialog-header">
        <h2>Add Event</h2>
        <button class="btn-close" (click)="closeDialogs()">
          <i class="pi pi-times"></i>
        </button>
      </div>
      <div class="dialog-body">
        <div class="form-group">
          <label for="name">Name *</label>
          <input type="text" id="name" class="form-input" [(ngModel)]="eventForm().name" />
        </div>
      </div>
      <div class="dialog-footer">
        <button class="btn-secondary" (click)="closeDialogs()">Cancel</button>
        <button class="btn-primary" (click)="submitAdd()">Create</button>
      </div>
    </div>
  </div>
}
```

### 7. Form Submission

**Changes Needed:**
```typescript
// REMOVE: FormGroup validation
- if (this.eventForm.invalid) {
    this.toastService.error(...);
  }
- const formValue = this.eventForm.value;

// REPLACE WITH: Direct signal access
+ const form = this.eventForm();
+ if (!form.name || !form.eventType) {
    this.error.set('Please fill in all required fields');
    return;
  }
```

### 8. Toast Notifications

**Changes Needed:**
```typescript
// REMOVE: ToastService
- this.toastService.success('Event created');
- this.toastService.error('Failed to create event');

// REPLACE WITH: Error/Success signals
+ this.success.set('Event created successfully');
+ setTimeout(() => this.success.set(null), 3000);
+ this.error.set('Failed to create event');
```

### 9. Dialog Management

**Add:**
```typescript
closeDialogs(): void {
  this.showAddDialog.set(false);
  this.showEditDialog.set(false);
  this.showDetailsDialog.set(false);
  this.showCancelDialog.set(false);
  this.showDeleteDialog.set(false);
  this.selectedEvent.set(null);
  this.error.set(null);
  this.success.set(null);
}
```

### 10. HTML Template Structure

**Target Structure:**
```html
<div class="page-container">
  <!-- Page Header -->
  <div class="page-header">
    <div class="header-content">
      <h1 class="page-title">Events</h1>
      <p class="page-subtitle">...</p>
    </div>
    <button class="btn-primary" (click)="openAddDialog()">
      <i class="pi pi-plus"></i> Add Event
    </button>
  </div>

  <!-- Success/Error Messages -->
  @if (success()) {
    <div class="alert alert-success">
      <i class="pi pi-check-circle"></i>
      {{ success() }}
    </div>
  }
  @if (error()) {
    <div class="alert alert-error">
      <i class="pi pi-times-circle"></i>
      {{ error() }}
    </div>
  }

  <!-- Stats Cards -->
  <div class="stats-row">...</div>

  <!-- Filters -->
  <div class="filters-section">...</div>

  <!-- Events Grid -->
  @if (loading()) {
    <div class="loading-spinner">...</div>
  } @else if (filteredEvents().length === 0) {
    <div class="empty-state">...</div>
  } @else {
    <div class="events-grid">...</div>

    <!-- Pagination -->
    @if (totalPages() > 1) {
      <div class="pagination">
        <button class="btn-secondary" (click)="previousPage()" [disabled]="currentPage() === 0">
          <i class="pi pi-chevron-left"></i> Previous
        </button>
        <span class="page-info">Page {{ currentPage() + 1 }} of {{ totalPages() }}</span>
        <button class="btn-secondary" (click)="nextPage()" [disabled]="currentPage() >= totalPages() - 1">
          Next <i class="pi pi-chevron-right"></i>
        </button>
      </div>
    }
  }
</div>

<!-- Dialogs -->
@if (showAddDialog()) { ... }
@if (showEditDialog()) { ... }
@if (showDetailsDialog()) { ... }
@if (showCancelDialog()) { ... }
@if (showDeleteDialog()) { ... }
```

---

## Files to Update

1. **events-page.ts** - Complete refactor (545 lines)
2. **events-page.html** - Complete refactor (346 lines)
3. **events-page.css** - Use pastoral-care-page.css as reference (453 lines current)

---

## Benefits of Standardization

1. **Consistency**: All pages follow same pattern
2. **Performance**: No heavy PrimeNG overhead
3. **Maintainability**: Simpler code, easier to understand
4. **Bundle Size**: Smaller production build
5. **Signals-First**: Modern Angular reactive patterns
6. **Accessibility**: Better control over HTML/CSS

---

## Implementation Steps

1. Remove PrimeNG imports
2. Convert FormGroup to signal-based form
3. Replace all boolean flags with signals
4. Add error/success signals
5. Replace Toast with inline alerts
6. Convert dialogs to native HTML with @if
7. Remove @HostListener, add pagination buttons
8. Update all methods to use signals
9. Add closeDialogs() helper
10. Update HTML template completely
11. Update CSS to match pastoral care
12. Test all CRUD operations
13. Test all dialogs
14. Test filtering and pagination

---

## Current Backup

A backup of the current events-page.ts has been saved to:
```
/home/reuben/Documents/workspace/past-care-spring-frontend/src/app/events-page/events-page.ts.backup
```

---

## Recommendation

Given the extensive changes needed (800+ lines across 3 files), this refactoring should be done:
1. **Gradually** - One dialog at a time
2. **With Testing** - Test each change
3. **In Separate PR** - Don't mix with other changes

**OR**

Complete rebuild following pastoral-care-page exactly as the template.
