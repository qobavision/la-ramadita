import { Routes } from '@angular/router';
import { rutasPanel } from '../../core/layout/panel-layout/panel.routes';
import { rutaProximamente } from '../../shared/paginas/proximamente/proximamente.routes';
import { MeseroInicio } from './mesero-inicio/mesero-inicio';
import { PANEL_MESERO } from './mesero.panel';

export const MESERO_ROUTES: Routes = rutasPanel(PANEL_MESERO, ['MESERO'], [
  { path: '', component: MeseroInicio, title: 'Mesero · La Ramadita' },
  rutaProximamente('mesas', {
    titulo: 'Mesas',
    descripcion: 'Mapa de mesas con su estado y apertura de atención.',
    modulo: 'M06',
  }),
  rutaProximamente('pedidos', {
    titulo: 'Pedidos',
    descripcion: 'Toma de pedidos, envío a cocina y seguimiento.',
    modulo: 'M07',
  }),
  rutaProximamente('cuentas', {
    titulo: 'Cuentas',
    descripcion: 'Precuenta y solicitud de cobro a caja.',
    modulo: 'M09',
  }),
]);
