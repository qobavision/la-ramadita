import { Routes } from '@angular/router';
import { authGuard, invitadoGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'inicio' },
  {
    path: 'login',
    title: 'Iniciar sesión · La Ramadita',
    canActivate: [invitadoGuard],
    loadComponent: () => import('./auth/login/login').then((m) => m.Login),
  },
  {
    path: 'inicio',
    title: 'Inicio · La Ramadita',
    canActivate: [authGuard],
    loadComponent: () => import('./inicio/inicio').then((m) => m.Inicio),
  },
  { path: '**', redirectTo: 'inicio' },
];
