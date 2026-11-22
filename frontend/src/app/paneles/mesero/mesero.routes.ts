import { Routes } from '@angular/router';
import { rutasPanel } from '../../core/layout/panel-layout/panel.routes';
import { MeseroAtencion } from './atencion/mesero-atencion';
import { MeseroCuentas } from './cuentas/mesero-cuentas';
import { MeseroMesas } from './mesas/mesero-mesas';
import { MeseroInicio } from './mesero-inicio/mesero-inicio';
import { PANEL_MESERO } from './mesero.panel';
import { MeseroPedidos } from './pedidos/mesero-pedidos';

export const MESERO_ROUTES: Routes = rutasPanel(PANEL_MESERO, ['MESERO'], [
  { path: '', component: MeseroInicio, title: 'Mesero · La Ramadita' },
  { path: 'mesas', component: MeseroMesas, title: 'Mesas · La Ramadita' },
  { path: 'pedidos', component: MeseroPedidos, title: 'Pedidos · La Ramadita' },
  { path: 'atencion/:id', component: MeseroAtencion, title: 'Atención · La Ramadita' },
  { path: 'cuentas', component: MeseroCuentas, title: 'Cuentas · La Ramadita' },
]);
