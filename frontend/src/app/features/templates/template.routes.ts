import { Routes } from '@angular/router';

export const TEMPLATE_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/template-list.component').then(m => m.TemplateListComponent)
  },
  {
    path: 'create',
    loadComponent: () => import('./ckeditor-template-editor/ckeditor-template-editor.component').then(m => m.CkeditorTemplateEditorComponent)
  },
  {
    path: 'edit/:id',
    loadComponent: () => import('./ckeditor-template-editor/ckeditor-template-editor.component').then(m => m.CkeditorTemplateEditorComponent)
  },
  {
    path: 'simulate',
    loadComponent: () => import('./template-simulation/template-simulation.component').then(m => m.TemplateSimulationComponent)
  }
];