# Church Registration Form - Button Fix Requirements

**Date**: December 31, 2025
**Status**: PENDING - Form Component Not Found in Src

---

## Problems Reported

### Issue 1: Incorrect Button Text
**Problem**: When joining an existing church, the submit button says "Add New Church" instead of "Join Church"

**Expected Behavior**:
- **New Church Mode**: Button says "Register Church" or "Create Church"
- **Join Church Mode**: Button says "Join Church" or "Request to Join"

### Issue 2: Validate Button Stretching
**Problem**: The "Validate" button (likely for partnership/invitation code validation) is stretching unusually

**Expected Behavior**:
- Button should have fixed or max width
- Should not stretch to fill container
- Should maintain consistent padding

---

## Current State of Registration

### E2E Test Evidence

From [critical-path-01-church-registration.spec.ts](/home/reuben/Documents/workspace/pastcare-spring/past-care-spring-frontend/e2e/tests/critical-path-01-church-registration.spec.ts):

The registration flow includes:
1. Church info: name, email, phone, address
2. Admin info: name, email, password, confirm password
3. Submit button (text unknown - needs verification)
4. Partnership code application (after registration, on subscription page)

### Page Object

From [registration.page.ts](/home/reuben/Documents/workspace/pastcare-spring/past-care-spring-frontend/e2e/pages/auth/registration.page.ts):

```typescript
private get submitButton() {
    return '[data-testid="register-button"], button[type="submit"]';
}
```

**This suggests**:
- Button should have `data-testid="register-button"`
- Falls back to any `button[type="submit"]`
- But actual text content is not specified

### Frontend Structure

**Investigation Result**:
- Registration component **NOT FOUND** in `src/app/` directory
- The app currently has:
  - billing-page
  - guards
  - models
  - pricing-section
  - services
  - user-management

**Missing Components**:
- Registration/Signup component
- Landing page (with registration)
- Auth components (login, register, forgot password)

---

## Two Scenarios

### Scenario A: Registration is on Landing Page

The registration form might be embedded in the landing page or a modal.

**Files to check**:
- `index.html` (root)
- Landing page component (if exists outside `src/app`)
- External registration form (hosted separately?)

### Scenario B: Registration Component Not Yet Created

The registration might be:
1. Planned but not implemented in Angular
2. Using backend-only registration (API direct)
3. Part of a separate frontend app

---

## Where Registration Should Be

Based on the E2E tests expecting `/register` route:

```
past-care-spring-frontend/src/app/
├── auth/                          # Auth module (MISSING)
│   ├── register/
│   │   ├── register.component.ts
│   │   ├── register.component.html  ← FIX NEEDED HERE
│   │   └── register.component.css
│   ├── login/
│   │   ├── login.component.ts
│   │   ├── login.component.html
│   │   └── login.component.css
│   └── auth.module.ts
```

---

## Fix Implementation (When Component is Found)

### Fix 1: Dynamic Button Text Based on Mode

**HTML** (register.component.html):
```html
<!-- BEFORE (Wrong) -->
<button type="submit" class="submit-btn">
  Add New Church
</button>

<!-- AFTER (Correct) -->
<button
  type="submit"
  class="submit-btn"
  data-testid="register-button"
  [disabled]="isSubmitting || registrationForm.invalid">
  {{ isJoiningMode ? 'Join Church' : 'Register Church' }}
  <i *ngIf="isSubmitting" class="spinner-icon"></i>
</button>
```

**TypeScript** (register.component.ts):
```typescript
export class RegisterComponent {
  isJoiningMode: boolean = false;  // Set based on invitation code presence
  isSubmitting: boolean = false;

  ngOnInit() {
    // Detect if user has invitation code
    const invitationCode = this.route.snapshot.queryParams['invitationCode'];
    this.isJoiningMode = !!invitationCode;
  }

  onSubmit() {
    this.isSubmitting = true;
    if (this.isJoiningMode) {
      // Join existing church logic
    } else {
      // Register new church logic
    }
  }
}
```

### Fix 2: Constrain Validate Button Width

**HTML** (likely on partnership code input):
```html
<!-- BEFORE (Stretching) -->
<div class="code-input-group">
  <input type="text" placeholder="Enter partnership code" [(ngModel)]="partnershipCode">
  <button class="validate-btn">Validate</button>  <!-- Stretches -->
</div>

<!-- AFTER (Fixed) -->
<div class="code-input-group">
  <input
    type="text"
    class="code-input"
    placeholder="Enter partnership code"
    [(ngModel)]="partnershipCode">
  <button
    type="button"
    class="validate-btn"
    (click)="validateCode()"
    [disabled]="!partnershipCode || isValidating">
    {{ isValidating ? 'Validating...' : 'Validate' }}
  </button>
</div>
```

**CSS**:
```css
/* BEFORE (Stretching) */
.code-input-group {
  display: flex;
  gap: 1rem;
}

.validate-btn {
  /* No width constraint - stretches to fill */
}

/* AFTER (Fixed) */
.code-input-group {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.code-input {
  flex: 1;  /* Input takes available space */
  min-width: 200px;
}

.validate-btn {
  flex-shrink: 0;        /* Prevent shrinking */
  width: auto;           /* Content-based width */
  min-width: 120px;      /* Minimum width */
  max-width: 150px;      /* Maximum width */
  padding: 0.75rem 1.5rem;
  white-space: nowrap;   /* Prevent text wrapping */
}

/* Responsive adjustment */
@media (max-width: 640px) {
  .code-input-group {
    flex-direction: column;  /* Stack on mobile */
  }

  .validate-btn {
    width: 100%;           /* Full width on mobile */
    max-width: none;
  }
}
```

---

## Alternative: If Using Grid Layout

```css
.code-input-group {
  display: grid;
  grid-template-columns: 1fr auto;  /* Input takes space, button is auto-sized */
  gap: 1rem;
  align-items: center;
}

.validate-btn {
  padding: 0.75rem 1.5rem;
  white-space: nowrap;
}
```

---

## Action Items

### Immediate
1. ✅ **Locate the actual registration component**
   - Check if it exists outside `src/app`
   - Check if registration is on landing page
   - Check if it's a standalone HTML file

2. **Once Found**: Apply the fixes above

3. **Test**:
   - Button shows "Register Church" for new registration
   - Button shows "Join Church" when invitation code present
   - Validate button has constrained width
   - Responsive design works on mobile

### Additional Improvements

While fixing, consider:

1. **Add Loading States**:
   ```html
   <button [disabled]="isSubmitting">
     <span *ngIf="!isSubmitting">{{ buttonText }}</span>
     <span *ngIf="isSubmitting">
       <i class="spinner"></i> Processing...
     </span>
   </button>
   ```

2. **Add Success/Error Feedback**:
   ```typescript
   showSuccessMessage() {
     this.messageService.success('Partnership code validated!');
   }

   showErrorMessage(error: string) {
     this.messageService.error(error);
   }
   ```

3. **Add Keyboard Support**:
   ```html
   <input
     (keyup.enter)="validateCode()"
     placeholder="Enter code">
   <button (click)="validateCode()">Validate</button>
   ```

---

## User Request Summary

**Original Issue**:
> "When joining an existing church the submit button should not be saying add new church and also fix the validate button which is unusually stretching"

**Root Causes**:
1. Submit button text is hardcoded instead of conditional
2. Validate button lacks width constraints in CSS

**Fixes**:
1. Make button text dynamic based on mode (join vs create)
2. Add CSS constraints to validate button (min-width, max-width, flex-shrink: 0)

**Status**: Awaiting component location to apply fixes

---

## Next Steps

**User action needed**: Please help locate the registration form:

1. Is it on the landing page?
2. Is it a separate app/page outside Angular?
3. Is it in a different repository?
4. Can you navigate to it in the browser and inspect the HTML?

Once we know where the component is, I can apply the exact fixes needed.

---

## Screenshots/Inspection Needed

To help debug, we would need:
1. Screenshot of the registration form
2. Browser DevTools inspect of the submit button
3. Browser DevTools inspect of the validate button
4. The actual HTML/CSS of these buttons

This would help us identify:
- Where the form is located
- What CSS classes are being used
- Why the validate button is stretching
- Current button text implementation
