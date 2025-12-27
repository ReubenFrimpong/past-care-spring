// Add this route to your app.routes.ts file

import { PrayerRequestsPageComponent } from './pages/prayer-requests/prayer-requests-page.component';

// Add to your routes array:
{
  path: 'prayer-requests',
  component: PrayerRequestsPageComponent,
  canActivate: [authGuard]
}
