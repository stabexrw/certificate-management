import { Routes } from '@angular/router';
import { AuthGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: '/dashboard',
    pathMatch: 'full'
  },
  {
    path: 'auth',
    loadChildren: () => import('./features/auth/auth.routes').then(m => m.AUTH_ROUTES)
  },
  {
    path: 'dashboard',
    loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent),
    canActivate: [AuthGuard]
  },
  {
    path: 'templates',
    loadChildren: () => import('./features/templates/template.routes').then(m => m.TEMPLATE_ROUTES),
    canActivate: [AuthGuard]
  },
  {
    path: 'verify/:uniqueId',
    loadComponent: () => import('./features/verify/verify.component').then(m => m.VerifyComponent)
  },
  {
    path: 'certificates',
    loadChildren: () => import('./features/certificates/certificate.routes').then(m => m.CERTIFICATE_ROUTES),
    canActivate: [AuthGuard]
  },
  {
    path: '**',
    redirectTo: '/dashboard'
  }
];