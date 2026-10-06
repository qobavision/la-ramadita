import { Routes } from '@angular/router';
import { rutasPanel } from '../../core/layout/panel-layout/panel.routes';
import { rutaProximamente } from '../../shared/paginas/proximamente/proximamente.routes';
import { CocinaInicio } from './cocina-inicio/cocina-inicio';
import { PANEL_COCINA } from './cocina.panel';

export const COCINA_ROUTES: Routes = rutasPanel(PANEL_COCINA, ['COCINA'], [
  { path: '', component: CocinaInicio, title: 'Cocina · La Ramadita' },
  rutaProximamente('pedidos', {
    titulo: 'Pedidos en cocina',
    descripcion: 'Pedidos recibidos en tiempo real, cambio de estado y aviso al mesero.',
    modulo: 'M08',
  }),
  rutaProximamente('historial', {
    titulo: 'Historial',
    descripcion: 'Pedidos preparados durante el turno.',
    modulo: 'M08',
  }),
]);
