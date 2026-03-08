import { Routes } from '@angular/router';
import { AuthGuard } from '../../utils/AuthGuard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./dashboard.component').then(m => m.DashboardComponent),
    canActivate: [AuthGuard],
    data: {
      title: $localize`Dashboard`
    }
  }
];

