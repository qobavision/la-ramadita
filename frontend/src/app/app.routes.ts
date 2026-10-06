import { Routes } from '@angular/router';
import { inicioGuard, invitadoGuard } from './core/guards/auth.guard';

/**
 * Rutas principales.
 *
 * - `/` decide según la sesión: panel del rol o login.
 * - Cada panel se carga de forma perezosa y protege sus rutas (sesión + rol)
 *   mediante `rutasPanel(...)` en su propio archivo de rutas.
 */
export const routes: Routes = [
  { path: '', pathMatch: 'full', canActivate: [inicioGuard], children: [] },
  {
    path: 'login',
    title: 'Iniciar sesión · La Ramadita',
    canActivate: [invitadoGuard],
    loadComponent: () => import('./auth/login/login').then((m) => m.Login),
  },
  {
    path: 'admin',
    loadChildren: () => import('./paneles/admin/admin.routes').then((m) => m.ADMIN_ROUTES),
  },
  {
    path: 'supervision',
    loadChildren: () => import('./paneles/supervision/supervision.routes').then((m) => m.SUPERVISION_ROUTES),
  },
  {
    path: 'mesero',
    loadChildren: () => import('./paneles/mesero/mesero.routes').then((m) => m.MESERO_ROUTES),
  },
  {
    path: 'cocina',
    loadChildren: () => import('./paneles/cocina/cocina.routes').then((m) => m.COCINA_ROUTES),
  },
  {
    path: 'caja',
    loadChildren: () => import('./paneles/caja/caja.routes').then((m) => m.CAJA_ROUTES),
  },
  // Compatibilidad con la antigua pantalla de inicio
  { path: 'inicio', redirectTo: '' },
  { path: '**', redirectTo: '' },
];
