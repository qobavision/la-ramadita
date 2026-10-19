import { Routes } from '@angular/router';
import { rutasPanel } from '../../core/layout/panel-layout/panel.routes';
import { PANEL_SUPERVISION } from './supervision.panel';
import { SupervisionInicio } from './supervision-inicio/supervision-inicio';
import { SupervisionTurnos } from './turnos/supervision-turnos';
import { SupervisionZonas } from './zonas/supervision-zonas';
import { SupervisionCompras } from './compras/supervision-compras';
import { SupervisionInventario } from './inventario/supervision-inventario';
import { SupervisionCarta } from './carta/supervision-carta';

export const SUPERVISION_ROUTES: Routes = rutasPanel(PANEL_SUPERVISION, ['SUPERVISOR'], [
  { path: '', component: SupervisionInicio, title: 'Supervisión · La Ramadita' },
  { path: 'turnos', component: SupervisionTurnos, title: 'Turnos y personal · La Ramadita' },
  { path: 'zonas', component: SupervisionZonas, title: 'Zonas · La Ramadita' },
  { path: 'operacion', redirectTo: 'zonas' },
  { path: 'compras', component: SupervisionCompras, title: 'Compras y recepción · La Ramadita' },
  { path: 'inventario', component: SupervisionInventario, title: 'Inventario · La Ramadita' },
  { path: 'productos', component: SupervisionCarta, title: 'Carta y combos · La Ramadita' },
  { path: 'ventas', redirectTo: '' },
  { path: 'reportes', redirectTo: '' },
]);
