// Add this route to your app.routes.ts file
// Place it in the appropriate section with other pastoral care routes

import { Routes } from '@angular/router';
import { CounselingSessionsPageComponent } from './pages/counseling-sessions/counseling-sessions-page';
import { authGuard } from './guards/auth.guard'; // Adjust path as needed

// Add this to your existing routes array:
export const counselingSessionRoute = {
  path: 'counseling-sessions',
  component: CounselingSessionsPageComponent,
  canActivate: [authGuard],
  data: {
    title: 'Counseling Sessions',
    breadcrumb: 'Counseling Sessions'
  }
};

// Example of how to add it to your routes array:
/*
export const routes: Routes = [
  // ... existing routes ...
  {
    path: 'pastoral-care',
    component: PastoralCarePageComponent,
    canActivate: [authGuard]
  },
  {
    path: 'counseling-sessions',  // <-- Add this route
    component: CounselingSessionsPageComponent,
    canActivate: [authGuard]
  },
  {
    path: 'visits',
    component: VisitsPageComponent,
    canActivate: [authGuard]
  },
  // ... more routes ...
];
*/
