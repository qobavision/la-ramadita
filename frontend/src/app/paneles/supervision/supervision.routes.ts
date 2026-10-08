import { Routes } from '@angular/router';
import { rutasPanel } from '../../core/layout/panel-layout/panel.routes';
import { PANEL_SUPERVISION } from './supervision.panel';
import { SupervisionInicio } from './supervision-inicio/supervision-inicio';
import { SupervisionTurnos } from './turnos/supervision-turnos';
import { SupervisionOperacion } from './operacion/supervision-operacion';
import { SupervisionCompras } from './compras/supervision-compras';
import { SupervisionInventario } from './inventario/supervision-inventario';
import { SupervisionCarta } from './carta/supervision-carta';

export const SUPERVISION_ROUTES: Routes = rutasPanel(PANEL_SUPERVISION, ['SUPERVISOR'], [
  { path: '', component: SupervisionInicio, title: 'Supervisión · La Ramadita' },
  { path: 'turnos', component: SupervisionTurnos, title: 'Turnos y personal · La Ramadita' },
  { path: 'operacion', component: SupervisionOperacion, title: 'Operación · La Ramadita' },
  { path: 'compras', component: SupervisionCompras, title: 'Compras y recepción · La Ramadita' },
  { path: 'inventario', component: SupervisionInventario, title: 'Inventario · La Ramadita' },
  { path: 'productos', component: SupervisionCarta, title: 'Carta y productos · La Ramadita' },
  { path: 'ventas', redirectTo: '' },
  { path: 'reportes', redirectTo: '' },
]);
