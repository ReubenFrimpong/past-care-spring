# Phase 3: Prayer Request Management - Angular Frontend

Complete Angular 21 frontend implementation for Prayer Request Management.

## Files Created

### Models (4 files)
- `prayer-category.enum.ts` - 17 prayer categories
- `prayer-priority.enum.ts` - 4 priority levels
- `prayer-request-status.enum.ts` - 4 statuses
- `prayer-request.interface.ts` - TypeScript interfaces

### Services (1 file)
- `prayer-request.service.ts` - Complete API integration (140 lines)

### Page Component (3 files - TO BE CREATED)
- `pages/prayer-requests/prayer-requests-page.ts` - Component logic
- `pages/prayer-requests/prayer-requests-page.html` - Template
- `pages/prayer-requests/prayer-requests-page.css` - Styles

## Integration Steps

### 1. Copy Files to Your Angular Project

```bash
# Copy models
cp angular-frontend-phase3/models/* src/app/models/

# Copy services
cp angular-frontend-phase3/services/* src/app/services/

# Copy page component (when created)
cp -r angular-frontend-phase3/pages/prayer-requests src/app/pages/
```

### 2. Add Route

In `src/app/app.routes.ts`:

```typescript
import { PrayerRequestsPageComponent } from './pages/prayer-requests/prayer-requests-page.component';

export const routes: Routes = [
  // ... existing routes
  {
    path: 'prayer-requests',
    component: PrayerRequestsPageComponent,
    canActivate: [authGuard]
  }
];
```

### 3. Add Navigation Link

In your side navigation component:

```html
<a routerLink="/prayer-requests" routerLinkActive="active">
  <i class="pi pi-heart"></i>
  <span>Prayer Requests</span>
</a>
```

## Features

### Prayer Request Management
- ✅ Submit prayer requests with 17 categories
- ✅ Set priority (LOW, NORMAL, HIGH, URGENT)
- ✅ Anonymous prayer option
- ✅ Expiration dates
- ✅ Public/private visibility

### Prayer Tracking
- ✅ Increment prayer count ("I Prayed" button)
- ✅ Track how many people have prayed
- ✅ Prayer statistics

### Testimonies
- ✅ Mark prayers as answered
- ✅ Add testimony when answered
- ✅ View answered prayers with testimonies

### Filters & Search
- Search by title/description
- Filter by status, category, priority
- Show only urgent prayers
- Show only public prayers
- Show only my requests

### Auto-archiving
- Prayer requests with expiration dates
- View expiring soon
- Auto-archive expired prayers

## API Endpoints

All endpoints use `/api/prayer-requests`:

- `POST /` - Create prayer request
- `GET /{id}` - Get by ID
- `GET /` - Get all (paginated)
- `PUT /{id}` - Update
- `DELETE /{id}` - Delete
- `POST /{id}/pray` - Increment prayer count
- `POST /{id}/answer` - Mark as answered with testimony
- `POST /{id}/archive` - Archive
- `GET /active` - Active prayers
- `GET /urgent` - Urgent prayers
- `GET /my-requests` - User's requests
- `GET /status/{status}` - By status
- `GET /category/{category}` - By category
- `GET /answered` - Answered with testimonies
- `GET /public` - Public prayers
- `GET /search` - Search
- `GET /stats` - Statistics
- `GET /expiring-soon` - Expiring within 7 days
- `POST /auto-archive` - Auto-archive expired

## Prayer Categories

1. HEALING - Physical/emotional healing
2. GUIDANCE - Direction and wisdom
3. PROVISION - Financial/material needs
4. PROTECTION - Safety and security
5. SALVATION - Spiritual salvation
6. RELATIONSHIPS - Family, friends, marriage
7. GRIEF - Loss and mourning
8. ADDICTION - Freedom from addiction
9. MENTAL_HEALTH - Mental well-being
10. EMPLOYMENT - Job-related needs
11. MINISTRY - Ministry work
12. TRAVEL - Safe travel
13. EXAMS - Academic success
14. PREGNANCY - Pregnancy and childbirth
15. BREAKTHROUGH - Spiritual breakthrough
16. THANKSGIVING - Gratitude and praise
17. OTHER - Other requests

## Priority Levels

- **URGENT** - Critical, immediate prayer needed
- **HIGH** - Important, requires attention
- **NORMAL** - Standard prayer request
- **LOW** - General prayer need

## Status Lifecycle

```
PENDING → ACTIVE → ANSWERED
                 ↓
              ARCHIVED
```

- **PENDING** - Newly submitted, awaiting approval
- **ACTIVE** - Approved and active for prayer
- **ANSWERED** - Marked as answered (with testimony)
- **ARCHIVED** - Closed/archived (expired or completed)

## Statistics Available

- Total Prayer Requests
- Pending Requests
- Active Requests
- Answered Requests (with testimonies)
- Urgent Requests
- Public Requests

## Environment Configuration

Ensure `environment.ts` has the API URL:

```typescript
export const environment = {
  apiUrl: 'http://localhost:8080/api'
};
```

## Dependencies

Required PrimeNG modules:
- CardModule
- ButtonModule
- DialogModule
- InputTextModule
- InputTextareaModule
- DropdownModule
- CalendarModule
- CheckboxModule
- TagModule
- ToastModule
- ConfirmDialogModule

## Next Steps

1. Create the page component files (TS, HTML, CSS)
2. Follow the pattern from Phase 2 (counseling-sessions-page)
3. Implement signals-based reactive state
4. Add prayer count animation on "Pray" button click
5. Display testimonies for answered prayers
6. Implement responsive grid layout
7. Add E2E tests

## Reference Implementation

See `angular-frontend-phase2/` for complete example of:
- Signals-based component
- PrimeNG integration
- Reactive forms
- Dialog management
- Statistics display
- Filtering and search
