// Add this route to your app.routes.ts file

import { CrisesPageComponent } from './pages/crises/crises-page.component';

// Add to your routes array:
{
  path: 'crises',
  component: CrisesPageComponent,
  canActivate: [authGuard]
}
