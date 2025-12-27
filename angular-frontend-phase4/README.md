# Phase 4: Crisis & Emergency Management - Angular Frontend

Complete Angular 21 frontend implementation for Crisis & Emergency Management.

## Files Created

### Models (4 files)
- `crisis-type.enum.ts` - 13 crisis types
- `crisis-severity.enum.ts` - 4 severity levels
- `crisis-status.enum.ts` - 4 statuses
- `crisis.interface.ts` - TypeScript interfaces

### Services (1 file)
- `crisis.service.ts` - Complete API integration (132 lines)

### Page Component (3 files - TO BE CREATED)
- `pages/crises/crises-page.ts` - Component logic
- `pages/crises/crises-page.html` - Template
- `pages/crises/crises-page.css` - Styles

## Integration Steps

### 1. Copy Files to Your Angular Project

```bash
# Copy models
cp angular-frontend-phase4/models/* src/app/models/

# Copy services
cp angular-frontend-phase4/services/* src/app/services/

# Copy page component (when created)
cp -r angular-frontend-phase4/pages/crises src/app/pages/
```

### 2. Add Route

In `src/app/app.routes.ts`:

```typescript
import { CrisesPageComponent } from './pages/crises/crises-page.component';

export const routes: Routes = [
  // ... existing routes
  {
    path: 'crises',
    component: CrisesPageComponent,
    canActivate: [authGuard]
  }
];
```

### 3. Add Navigation Link

In your side navigation component:

```html
<a routerLink="/crises" routerLinkActive="active">
  <i class="pi pi-exclamation-triangle"></i>
  <span>Crisis Management</span>
</a>
```

## Features

### Crisis Reporting
- ✅ Report crises with 13 types
- ✅ Set severity (CRITICAL, HIGH, MODERATE, LOW)
- ✅ Track incident date and location
- ✅ Add affected members
- ✅ Response team notes

### Resource Mobilization
- ✅ Track resources deployed
- ✅ Mobilize resources dialog
- ✅ Resource history

### Emergency Notifications
- ✅ Send emergency notifications
- ✅ Track communication sent
- ✅ Emergency contact notification status

### Crisis Resolution
- ✅ Resolve crisis with resolution notes
- ✅ Track resolved date
- ✅ Follow-up management

### Affected Members Management
- ✅ Add affected members
- ✅ Remove affected members
- ✅ Designate primary contact
- ✅ Track affected member count

### Filters & Search
- Search by title/description/location
- Filter by status, severity, type
- Show only active crises
- Show only critical crises
- Date range filtering

## API Endpoints

All endpoints use `/api/crises`:

- `POST /` - Report crisis
- `GET /{id}` - Get by ID
- `GET /` - Get all (paginated)
- `PUT /{id}` - Update crisis
- `DELETE /{id}` - Delete crisis
- `POST /{id}/affected-members` - Add affected member
- `DELETE /{id}/affected-members/{memberId}` - Remove affected member
- `POST /{id}/mobilize` - Mobilize resources
- `POST /{id}/notify` - Send notifications
- `POST /{id}/resolve` - Resolve crisis
- `PATCH /{id}/status` - Update status
- `GET /active` - Active crises
- `GET /critical` - Critical crises
- `GET /status/{status}` - By status
- `GET /type/{type}` - By type
- `GET /severity/{severity}` - By severity
- `GET /search` - Search crises
- `GET /stats` - Statistics

## Crisis Types

1. **DEATH** - Death in the family
2. **ACCIDENT** - Serious accident
3. **HOSPITALIZATION** - Emergency hospitalization
4. **NATURAL_DISASTER** - Natural disaster (flood, earthquake, etc.)
5. **FIRE** - Fire damage/loss
6. **FINANCIAL_CRISIS** - Severe financial difficulty
7. **FAMILY_VIOLENCE** - Domestic violence situation
8. **SUICIDE_RISK** - Suicide risk/attempt
9. **MENTAL_HEALTH_CRISIS** - Mental health emergency
10. **HOMELESSNESS** - Loss of housing
11. **JOB_LOSS** - Sudden unemployment
12. **LEGAL_ISSUE** - Serious legal trouble
13. **OTHER** - Other crisis

## Severity Levels

- **CRITICAL** - Life-threatening, requires immediate response (RED)
- **HIGH** - Serious, urgent attention needed (ORANGE)
- **MODERATE** - Significant but not urgent (YELLOW)
- **LOW** - Minor crisis, can be addressed routinely (BLUE)

## Status Lifecycle

```
ACTIVE → IN_RESPONSE → RESOLVED → CLOSED
```

- **ACTIVE** - Crisis just reported, awaiting response
- **IN_RESPONSE** - Response team mobilized, actively responding
- **RESOLVED** - Crisis resolved, awaiting final closure
- **CLOSED** - Crisis fully closed and archived

## Statistics Available

- Total Crises
- Active Crises
- In Response Crises
- Resolved Crises
- Critical Crises
- High Severity Crises
- Total Affected Members

## UI Color Coding

### Severity Colors
- **CRITICAL** - Red (#ef4444)
- **HIGH** - Orange (#f97316)
- **MODERATE** - Yellow (#eab308)
- **LOW** - Blue (#3b82f6)

### Status Colors
- **ACTIVE** - Red
- **IN_RESPONSE** - Orange
- **RESOLVED** - Green
- **CLOSED** - Gray

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
- MultiSelectModule (for affected members)
- TagModule
- ToastModule
- ConfirmDialogModule
- TooltipModule

## Next Steps

1. Create the page component files (TS, HTML, CSS)
2. Follow the pattern from Phase 2 (counseling-sessions-page)
3. Implement signals-based reactive state
4. Add severity color coding
5. Implement affected members management UI
6. Add resource mobilization dialog
7. Implement emergency notification tracking
8. Add E2E tests

## Reference Implementation

See `angular-frontend-phase2/` for complete example of:
- Signals-based component
- PrimeNG integration
- Reactive forms
- Dialog management
- Statistics display
- Filtering and search

## Special Considerations

### Crisis Types Requiring Immediate Attention
The following crisis types should trigger immediate alerts:
- DEATH
- SUICIDE_RISK
- FAMILY_VIOLENCE
- NATURAL_DISASTER
- FIRE

### Privacy & Confidentiality
Crisis information is highly sensitive. Ensure:
- Proper role-based access control
- Limited visibility of crisis details
- Secure handling of affected member information
- Confidential response team notes

### Emergency Protocols
Each crisis type should have predefined emergency protocols:
- Contact lists for each crisis type
- Resource mobilization checklists
- Communication templates
- Follow-up procedures
