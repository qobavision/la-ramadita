import { Routes } from '@angular/router';
import { rutasPanel } from '../../core/layout/panel-layout/panel.routes';
import { CajaInicio } from './caja-inicio/caja-inicio';
import { PANEL_CAJA } from './caja.panel';
import { CajaCobro } from './cobro/caja-cobro';
import { CajaCobros } from './cobros/caja-cobros';
import { CajaControl } from './control/caja-control';

export const CAJA_ROUTES: Routes = rutasPanel(PANEL_CAJA, ['CAJA'], [
  { path: '', component: CajaInicio, title: 'Caja · La Ramadita' },
  { path: 'cobros', component: CajaCobros, title: 'Cobros · La Ramadita' },
  { path: 'cobros/:id', component: CajaCobro, title: 'Cobrar cuenta · La Ramadita' },
  { path: 'control', component: CajaControl, title: 'Control de caja · La Ramadita' },
]);
