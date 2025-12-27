# Quick Start Guide - Counseling Sessions Frontend

5-minute setup guide for integrating the Counseling Sessions frontend into your Angular application.

## Prerequisites

- Angular 21+ project
- PrimeNG installed
- Backend API running on `/api/counseling-sessions`

## Installation (5 Steps)

### 1. Copy Files (2 minutes)

```bash
# From your Angular project root

# Copy models
cp angular-frontend-phase2/models/* src/app/models/

# Copy service
cp angular-frontend-phase2/services/counseling-session.service.ts src/app/services/

# Copy page component
mkdir -p src/app/pages/counseling-sessions
cp angular-frontend-phase2/pages/counseling-sessions/* src/app/pages/counseling-sessions/
```

### 2. Add Route (30 seconds)

Add to `src/app/app.routes.ts`:

```typescript
import { CounselingSessionsPageComponent } from './pages/counseling-sessions/counseling-sessions-page';

export const routes: Routes = [
  // ... existing routes
  {
    path: 'counseling-sessions',
    component: CounselingSessionsPageComponent,
    canActivate: [authGuard]
  }
];
```

### 3. Update Navigation (30 seconds)

Add to `src/app/shared/components/side-nav/side-nav.html`:

```html
<a routerLink="/counseling-sessions" routerLinkActive="active" class="nav-link">
  <i class="pi pi-comments"></i>
  <span>Counseling Sessions</span>
</a>
```

### 4. Verify Environment (30 seconds)

Check `src/environments/environment.ts`:

```typescript
export const environment = {
  apiUrl: 'http://localhost:8080/api'  // Adjust as needed
};
```

### 5. Test (1 minute)

```bash
# Start Angular dev server
ng serve

# Navigate to http://localhost:4200/counseling-sessions
```

## Verification Checklist

- [ ] Page loads without errors
- [ ] Stats cards display (may show 0 if no data)
- [ ] "New Session" button visible
- [ ] Console shows no TypeScript errors
- [ ] Backend API accessible

## Common Issues

### Issue: "Module not found"
**Solution:** Ensure file paths match your project structure. Update imports if needed.

### Issue: "PrimeNG module errors"
**Solution:** Install PrimeNG:
```bash
npm install primeng primeicons
```

### Issue: "API calls fail (404/500)"
**Solution:** Check backend is running and `environment.apiUrl` is correct.

### Issue: "Auth guard errors"
**Solution:** Ensure `authGuard` exists or remove `canActivate` from route temporarily.

### Issue: "Member/Counselor IDs required"
**Solution:** For testing, use existing member/user IDs from your database.

## Quick Test Data

To test the UI, you can use Postman/curl to create a test session:

```bash
curl -X POST http://localhost:8080/api/counseling-sessions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "memberId": 1,
    "counselorId": 1,
    "sessionDate": "2025-01-15",
    "type": "INDIVIDUAL",
    "status": "SCHEDULED",
    "purpose": "Initial consultation",
    "isConfidential": false,
    "requiresFollowUp": false,
    "referredToProfessional": false
  }'
```

## Next Steps

After basic setup works:

1. **Enhance Forms:**
   - Replace member/counselor ID inputs with autocomplete dropdowns
   - Add member search component integration

2. **Add Validation:**
   - Custom validators for date ranges
   - Business rule validation (e.g., no past dates for new sessions)

3. **Improve UX:**
   - Add loading skeletons
   - Add pagination controls
   - Add bulk actions

4. **Write Tests:**
   - Unit tests for component
   - E2E tests for workflows

## File Structure Reference

```
src/app/
├── models/
│   ├── counseling-session.interface.ts
│   ├── counseling-type.enum.ts
│   ├── counseling-status.enum.ts
│   └── session-outcome.enum.ts
├── services/
│   └── counseling-session.service.ts
└── pages/
    └── counseling-sessions/
        ├── counseling-sessions-page.ts
        ├── counseling-sessions-page.html
        └── counseling-sessions-page.css
```

## API Endpoints Used

The component calls these endpoints (all at `/api/counseling-sessions`):

- `GET /` - Load all sessions
- `GET /stats` - Load statistics
- `POST /` - Create session
- `PUT /{id}` - Update session
- `DELETE /{id}` - Delete session
- `POST /{id}/complete` - Complete session
- `POST /{id}/follow-up` - Schedule follow-up
- `POST /{id}/referral` - Create referral

All endpoints must be accessible and return proper DTOs.

## Support

For detailed information, see:
- `README.md` - Full documentation
- `IMPLEMENTATION_SUMMARY.md` - Technical details
- Backend documentation in project root

## Ready to Go!

If all checks pass, you're ready to start using the Counseling Sessions management page!

Navigate to: `http://localhost:4200/counseling-sessions`

---

**Setup Time:** ~5 minutes
**Difficulty:** Easy
**Dependencies:** PrimeNG, Angular 21+
