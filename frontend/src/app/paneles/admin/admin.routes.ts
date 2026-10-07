import { Routes } from '@angular/router';
import { rutasPanel } from '../../core/layout/panel-layout/panel.routes';
import { AdminAccesos } from './accesos/admin-accesos';
import { PANEL_ADMIN } from './admin.panel';
import { AdminControlCaja } from './control-caja/admin-control-caja';
import { AdminDashboard } from './dashboard/admin-dashboard';
import { AdminEmpleados } from './empleados/admin-empleados';
import { AdminReportes } from './reportes/admin-reportes';
import { AdminUsuarios } from './usuarios/admin-usuarios';

export const ADMIN_ROUTES: Routes = rutasPanel(PANEL_ADMIN, ['ADMINISTRADOR'], [
  { path: '', component: AdminDashboard, title: 'Dashboard · La Ramadita' },
  { path: 'empleados', component: AdminEmpleados, title: 'Empleados · La Ramadita' },
  { path: 'usuarios', component: AdminUsuarios, title: 'Usuarios y accesos · La Ramadita' },
  { path: 'reportes', component: AdminReportes, title: 'Reportes / BI · La Ramadita' },
  { path: 'control-caja', component: AdminControlCaja, title: 'Control de caja · La Ramadita' },
  { path: 'accesos', component: AdminAccesos, title: 'Accesos operativos · La Ramadita' },
]);
