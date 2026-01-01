# Claude Code Project Rules for PastCare

## Critical Rules (MUST ALWAYS FOLLOW)

### 1. Definition of Done
**A feature is ONLY complete when ALL of the following pass:**
- [ ] Backend compiles successfully (`./mvnw compile`)
- [ ] Frontend compiles successfully (`cd past-care-spring-frontend && ng build`)
- [ ] Backend tests pass (`./mvnw test`)
- [ ] Frontend unit tests pass (`cd past-care-spring-frontend && ng test --watch=false`)
- [ ] E2E tests pass (`cd past-care-spring-frontend && npx playwright test`)

**DO NOT mark any task as complete until all checks pass.**

### 2. Port Cleanup (CONDITIONAL)
**Port 8080 should ONLY be cleaned up if you actually started a server on it during the task.**
- Only run cleanup if you started the backend server (`./mvnw spring-boot:run`) or similar
- Do NOT clean up port 8080 if you only ran compile/build commands without starting a server
- When cleanup is needed, run: `lsof -ti:8080 | xargs kill -9 2>/dev/null || true`
- This prevents accidentally killing unrelated processes

### 3. Test Requirements
- Every E2E and unit test MUST test ALL user roles
- Tests MUST run assertions on what each role is expected to see AND not see
- This is non-negotiable and must be enforced when writing any test

### 4. Verification Commands
Before declaring any feature complete, run:
```bash
# Backend
./mvnw compile
./mvnw test

# Frontend
cd past-care-spring-frontend
ng build --configuration=production
ng test --watch=false --browsers=ChromeHeadless
npx playwright test
```

### 5. User Roles in System
The application has 7 user roles that must be tested:
1. SUPERADMIN - Platform-level access
2. ADMIN - Church-level full access
3. PASTOR - Pastoral care and member oversight
4. TREASURER - Financial operations
5. MEMBER_MANAGER - Member data management
6. FELLOWSHIP_LEADER - Fellowship-scoped access
7. MEMBER - Limited personal access

### 6. Frontend Separation Rule (CRITICAL)
**The Angular frontend MUST be kept separate from the backend project.**

⚠️ **NEVER**:
- Add `past-care-spring-frontend/` directory to the backend project at `/home/reuben/Documents/workspace/pastcare-spring/`
- Copy frontend files into the backend repository
- Create Angular files (package.json, angular.json, tsconfig.json, etc.) inside the backend directory
- Commit frontend code to the backend Git repository

✅ **ALWAYS**:
- Keep frontend at: `/home/reuben/Documents/workspace/past-care-spring-frontend/`
- Make ALL frontend changes in the standalone frontend directory
- Run `ng serve` from the standalone frontend directory
- Deploy frontend and backend separately

**Rationale**: Clean separation of concerns, easier deployment, faster development, better Git workflow.

**Enforcement**: The backend `.gitignore` has rules to prevent accidental frontend commits. See `FRONTEND_LOCATION.md` for details.

## Project Locations

### Absolute Paths
- **Backend Project**: `/home/reuben/Documents/workspace/pastcare-spring`
- **Frontend Project**: `/home/reuben/Documents/workspace/past-care-spring-frontend` ⚠️ **SEPARATE DIRECTORY**

### Project Structure
- **Backend**: Spring Boot application at `/home/reuben/Documents/workspace/pastcare-spring`
  - ❌ **NO Angular/frontend files allowed in this directory**
- **Frontend**: Angular application at `/home/reuben/Documents/workspace/past-care-spring-frontend`
  - ✅ **ALL frontend work happens here**
- **E2E Tests**: Playwright tests at `/home/reuben/Documents/workspace/past-care-spring-frontend/e2e/`
- **Integration Tests**: `/home/reuben/Documents/workspace/pastcare-spring/src/test/java/com/reuben/pastcare_spring/integration/`
- **Security Tests**: `/home/reuben/Documents/workspace/pastcare-spring/src/test/java/com/reuben/pastcare_spring/security/`

**IMPORTANT**: Frontend and Backend are in SEPARATE directories. Never mix them. See Rule #6 above.

## Common Commands

```bash
# Run all backend tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=RoleBasedAccessControlTest

# Run frontend in dev mode
cd past-care-spring-frontend && ng serve

# Run E2E tests with UI
cd past-care-spring-frontend && npx playwright test --ui

# Run specific E2E test
cd past-care-spring-frontend && npx playwright test critical-path-01
```

## Pricing Model

**CRITICAL: There is NO free plan. NEVER create a free/trial/starter plan.**

### Subscription Plans
- **STANDARD Plan ONLY**: GHC 150.00/month
  - 2GB base storage
  - Unlimited members and users
  - All features included
  - This is the ONLY subscription plan

### Important Rules
1. **NO FREE PLAN** - Do not create STARTER, FREE, TRIAL, or any $0 plans
2. **NO TRIAL PERIOD** - No automatic grace periods or free trials
3. **Single Plan Model** - Only STANDARD plan at GHC 150/month exists
4. **Partnership Codes** - Free access is ONLY granted through partnership/promotional codes
5. **Payment Required** - Churches must pay GHC 150/month via Paystack to use the system

Refer to `PRICING_MODEL_REVISED.md` for complete pricing information.

## Frontend Styling Guidelines

All frontend components MUST follow the established styling patterns from the members-page component. These rules ensure visual consistency across the application.

### Color Palette (MUST USE THESE EXACT COLORS)

**Primary Colors:**
- **Primary Purple Gradient**: `linear-gradient(135deg, #667eea 0%, #764ba2 100%)`
  - Use for: Primary buttons, FAB, main actions, toolbars
- **Secondary Purple Gradient**: `linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%)`
  - Use for: Advanced features, secondary actions
- **Accent Green**: `#10b981`, `#34d399` (gradient: `135deg, #34d399 0%, #10b981 100%`)
  - Use for: Success states, quick add actions, verified badges
- **Accent Orange**: `#f59e0b`, `#f97316`
  - Use for: Warnings, bulk import, pending states
- **Accent Blue**: `#3b82f6`, `#2563eb`
  - Use for: Information, location features
- **Danger Red**: `#ef4444`, `#dc2626`
  - Use for: Delete, errors, critical actions

**Neutral Colors:**
- Text primary: `#1f2937`
- Text secondary: `#6b7280`
- Text muted: `#9ca3af`
- Border default: `#e5e7eb`
- Border light: `#d1d5db`
- Background light: `#f9fafb`
- Background lighter: `#f3f4f6`

### Typography Standards

**Font Sizes (use rem units):**
- Page titles: `1.875rem` (30px), bold, color `#1f2937`
- Section titles: `1.125rem` (18px), font-weight 600
- Subtitles: `0.875rem` (14px), color `#6b7280`
- Form labels: `0.875rem`, font-weight 500, color `#374151`
- Body text: `0.9375rem` (15px)
- Small text: `0.75rem` (12px), color `#6b7280`
- Tags/badges: `0.625rem` (10px), bold

**Font Weights:**
- Titles: 700 (bold)
- Section headers: 600 (semibold)
- Labels: 500 (medium)
- Body: 400 (normal)

### Spacing System (CRITICAL - USE CONSISTENTLY)

**Padding:**
- Page container: `1.5rem` (24px)
- Cards/sections: `1.25rem` to `1.5rem`
- Form fields: `0.75rem` (12px)
- Primary buttons: `0.75rem 1.5rem` (vertical horizontal)
- Secondary buttons: `0.625rem 1.25rem`
- Dialog content: `1rem` to `1.5rem`

**Margins:**
- Page header bottom: `2rem` (32px)
- Section spacing: `1.5rem` to `2rem`
- Form field spacing: `1.25rem`
- Item spacing: `0.625rem` to `1rem`

**Gaps (flexbox/grid):**
- Primary gap: `1rem` (16px) - **DEFAULT**
- Compact gap: `0.5rem` (8px) - for icon+text
- Wide gap: `1.5rem` to `2rem` - for major sections

### Border Radius Hierarchy

**MUST follow this pattern:**
- Page containers/sections: `1.25rem` (20px)
- Cards: `1rem` (16px)
- Form inputs: `0.75rem` (12px)
- Buttons: `0.75rem` (12px)
- Tags/badges: `0.5rem` (8px)
- Pills (status badges): `9999px`
- Circular (avatars): `50%`

### Shadow Patterns

**Box Shadows (use exact values):**
- Subtle (default): `0 1px 3px rgba(0,0,0,0.05), 0 4px 12px rgba(0,0,0,0.04)`
- Card hover: `0 8px 20px rgba(102, 126, 234, 0.15)` (purple tint)
- Button hover: `0 4px 12px rgba(X, Y, Z, 0.3)` (color-matched)
- Dialog/panel: `0 4px 12px rgba(0,0,0,0.1)`
- Focus state: `0 0 0 3px rgba(139, 92, 246, 0.1)` (purple inner glow)

### Button Styles (MANDATORY PATTERNS)

**Primary Button:**
```css
background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
color: white;
padding: 0.75rem 1.5rem;
border-radius: 0.75rem;
border: none;
transition: all 0.2s ease;
```
- Hover: `transform: translateY(-2px)` + shadow boost
- Disabled: `opacity: 0.5`, `cursor: not-allowed`

**Secondary Button:**
```css
background: #f3f4f6;
color: #4b5563;
border: 1px solid #e5e7eb;
padding: 0.625rem 1.25rem;
border-radius: 0.75rem;
```
- Hover: `background: #e5e7eb`

**Danger Button:**
```css
background: #fef2f2;
color: #ef4444;
border: 1px solid #fee2e2;
```
- Hover: `background: #ef4444`, `color: white`

**Icon Button (small):**
- Size: `36px x 36px` minimum
- Border: `2px solid #e5e7eb`
- Hover: border → `#667eea`, background → `#667eea15`

### Form Controls

**Input Fields:**
```css
padding: 0.75rem 1rem;
border: 1px solid #d1d5db;
border-radius: 0.75rem;
font-size: 0.9375rem;
```
- Focus: `border-color: #8b5cf6`, `box-shadow: 0 0 0 3px rgba(139,92,246,0.1)`

**Search Box (larger):**
```css
border: 2px solid #e5e7eb;
border-radius: 0.75rem;
padding: 0.75rem 1rem;
```

**Required Field Marker:**
```html
<span class="required" style="color: #ef4444;">*</span>
```

### Layout Patterns

**Page Container:**
```css
.page-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 1.5rem;
}
```

**Stats Grid (auto-responsive):**
```css
display: grid;
grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
gap: 1rem;
```

**Card Grid:**
```css
display: grid;
grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
gap: 1.25rem;
```

**Form Row (2-column):**
```css
display: grid;
grid-template-columns: 1fr 1fr;
gap: 1rem;

@media (max-width: 768px) {
  grid-template-columns: 1fr;
}
```

### Responsive Breakpoints

**Mobile (≤640px):**
- Single column layouts
- Full-width buttons
- Padding reduced to `1rem`
- Stack flex layouts vertically

**Tablet (≤768px):**
- Form rows → single column
- Hide non-essential text in buttons
- Adjust grid columns

**Desktop (≥1024px):**
- Full multi-column layouts
- All features visible

### Animation Standards

**Transitions:**
- Standard: `0.2s ease`
- Important states: `0.3s ease`
- Never exceed `0.3s`

**Hover Effects:**
- Lift: `transform: translateY(-2px)` or `translateY(-4px)`
- Scale: `transform: scale(1.1)` (for FAB)

**Keyframe Animations:**
```css
@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
```

### Avatar Patterns

**Member Avatar (card):**
- Size: `64px x 64px`
- Border-radius: `1rem` (rounded square)
- Background: `linear-gradient(135deg, #667eea 0%, #764ba2 100%)`
- Text: white, bold, `1.5rem`

**Profile Avatar (large):**
- Size: `80px` to `150px`
- Border-radius: `50%` (circle)
- Border: `3px solid` (color-matched)

**Verified Badge:**
- Size: `24px` circle
- Background: `#10b981`
- Position: absolute, top-right of avatar

### Empty State Pattern

```css
.empty-state {
  background: linear-gradient(to bottom, #ffffff, #f9fafb);
  border: 2px dashed #e5e7eb;
  border-radius: 1.25rem;
  padding: 4rem 2rem;
  text-align: center;
}

.empty-icon {
  width: 120px;
  height: 120px;
  background: linear-gradient(135deg, #667eea15 0%, #764ba215 100%);
  border-radius: 50%;
}
```

### Dialog/Modal Standards

**Dialog Width:**
- Small: `450px`
- Medium: `600px`
- Large: `900px`
- Max: `95vw` (responsive)

**Dialog Structure:**
```css
.dialog-header {
  padding: 1.5rem;
  border-bottom: 1px solid #e5e7eb;
}

.dialog-content {
  padding: 1.5rem;
}

.dialog-footer {
  padding: 1rem 1.5rem;
  border-top: 1px solid #e5e7eb;
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
}
```

### Badge & Tag Patterns

**Fellowship Tag:**
```css
background: #eff6ff;
color: #2563eb;
padding: 0.25rem 0.625rem;
border-radius: 0.5rem;
font-size: 0.625rem;
font-weight: bold;
```

**Status Badge (pill):**
```css
background: linear-gradient(135deg, [color1] 0%, [color2] 100%);
color: white;
padding: 0.375rem 0.875rem;
border-radius: 9999px;
display: inline-flex;
align-items: center;
gap: 0.5rem;
```

### PrimeNG Integration

**MUST override PrimeNG styles using `::ng-deep`:**
- Match border-radius with design system
- Apply consistent padding
- Use theme colors for focus states
- Maintain shadow patterns

**Common Overrides:**
```css
::ng-deep .p-inputswitch {
  width: 3rem;
  height: 1.75rem;
}

::ng-deep .p-select,
::ng-deep .p-chips {
  font-size: 0.9375rem;
}

::ng-deep .p-focus {
  box-shadow: 0 0 0 3px rgba(139, 92, 246, 0.1) !important;
}
```

### CSS Class Naming

**Follow BEM-inspired conventions:**
- Block: `.member-card`
- Element: `.member-card__header`
- Modifier: `.member-card--selected`

**State classes:**
- `.is-active`, `.is-selected`, `.is-disabled`
- `.has-error`, `.has-warning`

**Utility classes:**
- `.hidden`, `.required`, `.error-message`

### Accessibility Requirements

- All form labels MUST be associated with inputs
- Required fields MUST have red asterisk
- Focus states MUST be clearly visible
- Error messages MUST use semantic color + icon
- Alt text MUST be provided for images
- Proper ARIA attributes for PrimeNG components

### Critical Rules for Developers

1. **ALWAYS use the purple gradient** (`#667eea` to `#764ba2`) for primary actions
2. **ALWAYS maintain 1rem base gap** for spacing between elements
3. **NEVER create custom colors** - use only the defined palette
4. **ALWAYS include focus states** with the purple shadow
5. **NEVER exceed 0.3s** for animations
6. **ALWAYS follow border-radius hierarchy** (1.25rem → 1rem → 0.75rem → 0.5rem)
7. **ALWAYS use flexbox/grid gaps** instead of margins between items
8. **ALWAYS make components responsive** using defined breakpoints
9. **ALWAYS match PrimeNG components** to the design system
10. **NEVER use inline styles** - use component CSS files

### Testing Style Consistency

Before submitting any frontend component:
1. Verify all colors match the palette
2. Check all border-radius values follow hierarchy
3. Confirm spacing uses the defined system
4. Test all button hover states
5. Validate focus states are visible
6. Test responsive behavior at all breakpoints
7. Verify PrimeNG overrides are applied
