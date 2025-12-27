# Pastoral Care Module - Phase 2: Counseling Sessions Frontend

Complete Angular frontend implementation for Phase 2 of the Pastoral Care module.

## Overview

This implementation provides a full-featured Angular application for managing counseling sessions, including:

- 15 counseling session types (Individual, Couples, Family, Group, Youth, etc.)
- 6 session statuses (Scheduled, In Progress, Completed, etc.)
- 7 session outcomes (Positive, Needs Follow-up, Needs Referral, etc.)
- Complete session lifecycle management
- Professional referral system
- Follow-up scheduling
- Confidentiality support
- Comprehensive statistics dashboard

## File Structure

```
angular-frontend-phase2/
├── models/                                    # TypeScript interfaces and enums
│   ├── counseling-session.interface.ts       # Main session interfaces
│   ├── counseling-type.enum.ts               # 15 counseling types
│   ├── counseling-status.enum.ts             # 6 session statuses
│   └── session-outcome.enum.ts               # 7 session outcomes
│
├── services/                                  # Angular services
│   └── counseling-session.service.ts         # HTTP client service (20+ methods)
│
├── pages/counseling-sessions/                 # Main page component
│   ├── counseling-sessions-page.ts           # Component logic (signals-based)
│   ├── counseling-sessions-page.html         # Template (PrimeNG components)
│   └── counseling-sessions-page.css          # Styles (matches pastoral-care-page)
│
├── app-routes-addition.ts                     # Route configuration example
├── side-nav-addition.html                     # Navigation menu addition
└── README.md                                  # This file
```

## Installation Instructions

### Step 1: Copy Model Files

Copy the TypeScript interfaces and enums to your Angular project:

```bash
# From your Angular project root
cp angular-frontend-phase2/models/* src/app/models/
```

**Files to copy:**
- `counseling-session.interface.ts`
- `counseling-type.enum.ts`
- `counseling-status.enum.ts`
- `session-outcome.enum.ts`

### Step 2: Copy Service File

Copy the service to your Angular project:

```bash
cp angular-frontend-phase2/services/counseling-session.service.ts src/app/services/
```

**Update the environment import path** in the service file if needed:
```typescript
import { environment } from '../../environments/environment';
```

### Step 3: Copy Page Component

Copy the page component files:

```bash
# Create directory
mkdir -p src/app/pages/counseling-sessions

# Copy files
cp angular-frontend-phase2/pages/counseling-sessions/* src/app/pages/counseling-sessions/
```

**Files:**
- `counseling-sessions-page.ts`
- `counseling-sessions-page.html`
- `counseling-sessions-page.css`

### Step 4: Add Route

Add the counseling sessions route to your `app.routes.ts`:

```typescript
import { CounselingSessionsPageComponent } from './pages/counseling-sessions/counseling-sessions-page';

export const routes: Routes = [
  // ... existing routes ...
  {
    path: 'counseling-sessions',
    component: CounselingSessionsPageComponent,
    canActivate: [authGuard]
  },
  // ... more routes ...
];
```

See `app-routes-addition.ts` for a complete example.

### Step 5: Update Navigation

Add the counseling sessions link to your side navigation (`side-nav.html` or similar):

```html
<a
  routerLink="/counseling-sessions"
  routerLinkActive="active"
  class="nav-link"
>
  <i class="pi pi-comments"></i>
  <span>Counseling Sessions</span>
</a>
```

See `side-nav-addition.html` for complete examples (desktop and mobile).

### Step 6: Verify Dependencies

Ensure you have the required PrimeNG modules installed:

```bash
npm install primeng primeicons
```

**Required PrimeNG modules** (already imported in the component):
- CardModule
- ButtonModule
- DialogModule
- DropdownModule
- CalendarModule
- InputTextModule
- InputTextareaModule
- CheckboxModule
- ToastModule
- ConfirmDialogModule

## Backend API Endpoints

The service expects the following backend endpoints to be available at `/api/counseling-sessions`:

### CRUD Operations
- `POST /api/counseling-sessions` - Create session
- `GET /api/counseling-sessions/{id}` - Get by ID
- `GET /api/counseling-sessions` - Get all (paginated)
- `PUT /api/counseling-sessions/{id}` - Update session
- `DELETE /api/counseling-sessions/{id}` - Delete session

### Session Actions
- `POST /api/counseling-sessions/{id}/complete` - Complete with outcome
- `POST /api/counseling-sessions/{id}/follow-up` - Schedule follow-up
- `POST /api/counseling-sessions/{id}/referral` - Create referral

### Query Methods
- `GET /api/counseling-sessions/my-sessions` - Current counselor's sessions
- `GET /api/counseling-sessions/counselor/{id}` - By counselor
- `GET /api/counseling-sessions/status/{status}` - By status
- `GET /api/counseling-sessions/type/{type}` - By type
- `GET /api/counseling-sessions/upcoming` - Upcoming sessions
- `GET /api/counseling-sessions/my-upcoming` - Counselor's upcoming
- `GET /api/counseling-sessions/follow-ups` - Requiring follow-up
- `GET /api/counseling-sessions/member/{id}` - By member
- `GET /api/counseling-sessions/care-need/{id}` - By care need
- `GET /api/counseling-sessions/search?searchTerm=...` - Search sessions
- `GET /api/counseling-sessions/stats` - Statistics

**Note:** All backend endpoints are already implemented. See `PASTORAL_CARE_PHASES_2_3_4_BACKEND_COMPLETE.md` in the project root.

## Features Implemented

### Core Features
1. **Session Management**
   - Create, read, update, delete counseling sessions
   - 15 counseling session types
   - 6 session statuses with lifecycle tracking
   - Date and time scheduling

2. **Session Completion**
   - Mark sessions as completed
   - Record session outcomes (7 types)
   - Add outcomes description
   - Schedule follow-up if needed

3. **Follow-up Management**
   - Schedule follow-up sessions
   - Track required follow-ups
   - Add follow-up notes

4. **Professional Referrals**
   - Create referrals to professional counselors
   - Track referral details
   - Flag sessions requiring professional help

5. **Filtering and Search**
   - Search by member name, counselor name, or purpose
   - Filter by status (6 options)
   - Filter by type (15 options)
   - Clear all filters

6. **Statistics Dashboard**
   - Total sessions count
   - Scheduled sessions count
   - Completed sessions count
   - Sessions requiring follow-up count

### UI/UX Features
1. **Responsive Design**
   - Mobile-first approach
   - Adapts to all screen sizes
   - Touch-friendly buttons

2. **Card-based Layout**
   - Consistent with pastoral-care-page
   - Hover effects and shadows
   - Professional gradient headers

3. **Status Badges**
   - Color-coded status indicators
   - Outcome badges with semantic colors
   - Confidential, follow-up, and referral flags

4. **Dialogs**
   - Add/Edit session dialog
   - View session details dialog
   - Complete session dialog
   - Schedule follow-up dialog
   - Create referral dialog

5. **Validation**
   - Required field validation
   - Form-level validation
   - Real-time feedback

## Technical Implementation

### Angular Version
- **Angular 21** (standalone components)
- **TypeScript** (strict mode)
- **Signals-based** reactive state management

### State Management
```typescript
// Signals for reactive state
sessions = signal<CounselingSessionResponse[]>([]);
stats = signal<CounselingSessionStatsResponse | null>(null);
loading = signal(false);

// Computed filtered sessions
filteredSessions = computed(() => {
  // Filtering logic based on search and filters
});
```

### Form Management
- **Reactive Forms** with FormBuilder
- **Validators** for required fields
- **Conditional fields** (follow-up date shows when checkbox checked)

### HTTP Client
- **Observable-based** API calls
- **Pagination support** on all list endpoints
- **Error handling** with toast notifications

## Styling

The CSS follows the exact same patterns as `pastoral-care-page`:

### Color Scheme
- **Primary gradient:** `#667eea` to `#764ba2`
- **Stats cards:** Different gradients for each stat type
- **Status badges:** Semantic colors (blue, yellow, green, red, etc.)

### Layout
- **Card border-radius:** 12px (matches fellowship cards)
- **Grid layout:** Auto-fill with min 380px cards
- **Responsive breakpoints:** Mobile at 768px

### Components
- **No animations** (per user preference)
- **Pill-shaped badges** with proper padding
- **Hover effects** on cards (translateY and shadow)

## Usage Examples

### Creating a New Session

1. Click "New Session" button
2. Fill in required fields:
   - Member ID
   - Counselor ID
   - Session Type
   - Session Date
   - Status
3. Optionally add:
   - Care Need ID (to link to existing care need)
   - Start/End Time
   - Purpose
   - Notes
   - Confidential flag
   - Follow-up required flag
4. Click "Save"

### Completing a Session

1. Find the session card
2. Click "Complete Session" button
3. Select outcome (required)
4. Add outcomes description
5. Check "Requires Follow-up" if needed
6. Set follow-up date
7. Click "Complete Session"

### Scheduling a Follow-up

1. Find the session card
2. Click "Schedule Follow-up" button
3. Select follow-up date (required)
4. Add notes
5. Click "Schedule"

### Creating a Referral

1. Find the session card
2. Click "Create Referral" button
3. Enter referral details (name, contact, specialty, etc.)
4. Click "Create Referral"

## Integration with Backend

The frontend is designed to work seamlessly with the Phase 2 backend implementation:

### Request/Response Mapping
- **CounselingSessionRequest** → Backend DTO
- **CounselingSessionResponse** → Backend DTO
- **CounselingSessionStatsResponse** → Backend DTO

### Enum Synchronization
- **CounselingType** → Matches backend enum (15 types)
- **CounselingStatus** → Matches backend enum (6 statuses)
- **SessionOutcome** → Matches backend enum (7 outcomes)

### Date Formatting
- Frontend sends dates as ISO strings: `YYYY-MM-DD`
- Backend expects `LocalDate` format
- Service automatically formats dates before sending

## Testing Checklist

- [ ] Create a new counseling session
- [ ] Edit an existing session
- [ ] View session details
- [ ] Delete a session
- [ ] Complete a session with outcome
- [ ] Schedule a follow-up
- [ ] Create a professional referral
- [ ] Filter by status
- [ ] Filter by type
- [ ] Search by member name
- [ ] Clear all filters
- [ ] Verify statistics update correctly
- [ ] Test responsive layout on mobile
- [ ] Test form validation
- [ ] Test error handling

## Browser Compatibility

- Chrome (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)
- Mobile browsers (iOS Safari, Chrome Mobile)

## Accessibility

- Semantic HTML elements
- ARIA labels on interactive elements
- Keyboard navigation support
- Screen reader friendly
- Color contrast compliance

## Performance Considerations

1. **Lazy Loading:** Component is standalone and can be lazy loaded
2. **Pagination:** Backend pagination support (default 20 items per page)
3. **Computed Signals:** Efficient filtering without manual subscriptions
4. **OnPush Strategy:** Can be added for better performance
5. **Tree-shakeable:** PrimeNG modules imported individually

## Future Enhancements

Potential improvements for future iterations:

1. **Member/Counselor Autocomplete:** Replace ID inputs with searchable dropdowns
2. **Calendar View:** Visual calendar for scheduling sessions
3. **Session Notes Editor:** Rich text editor for detailed notes
4. **Attachment Support:** Upload documents/forms related to sessions
5. **Email Notifications:** Automated reminders for upcoming sessions
6. **Session Templates:** Pre-defined session types with default settings
7. **Reporting:** Export sessions to PDF/Excel
8. **Analytics:** Charts and graphs for session trends

## Support

For issues or questions:
1. Check backend is running on correct port
2. Verify all backend endpoints are accessible
3. Check browser console for errors
4. Ensure environment.apiUrl is correct

## License

Part of the PastCare Spring application.

## Authors

Generated for Pastoral Care Module - Phase 2 Frontend Implementation
Date: December 26, 2025
