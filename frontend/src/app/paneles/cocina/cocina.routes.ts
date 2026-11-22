import { Routes } from '@angular/router';
import { rutasPanel } from '../../core/layout/panel-layout/panel.routes';
import { CocinaInicio } from './cocina-inicio/cocina-inicio';
import { PANEL_COCINA } from './cocina.panel';
import { CocinaHistorial } from './historial/cocina-historial';

export const COCINA_ROUTES: Routes = rutasPanel(PANEL_COCINA, ['COCINA'], [
  { path: '', component: CocinaInicio, title: 'Cocina · La Ramadita' },
  { path: 'historial', component: CocinaHistorial, title: 'Historial de cocina · La Ramadita' },
  // Enlaces antiguos a /cocina/pedidos llevan al monitor.
  { path: 'pedidos', redirectTo: '', pathMatch: 'full' },
]);
