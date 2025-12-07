import { Routes } from '@angular/router';

export const CERTIFICATE_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/certificate-list.component').then(m => m.CertificateListComponent)
  },
  {
    path: 'generate',
    loadComponent: () => import('./generate/certificate-generate.component').then(m => m.CertificateGenerateComponent)
  },
  {
    path: ':uniqueId',
    loadComponent: () => import('./detail/certificate-detail.component').then(m => m.CertificateDetailComponent)
  }
];