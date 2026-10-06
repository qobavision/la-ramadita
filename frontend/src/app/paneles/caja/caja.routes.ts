import { Routes } from '@angular/router';
import { rutasPanel } from '../../core/layout/panel-layout/panel.routes';
import { rutaProximamente } from '../../shared/paginas/proximamente/proximamente.routes';
import { CajaInicio } from './caja-inicio/caja-inicio';
import { PANEL_CAJA } from './caja.panel';

export const CAJA_ROUTES: Routes = rutasPanel(PANEL_CAJA, ['CAJA'], [
  { path: '', component: CajaInicio, title: 'Caja · La Ramadita' },
  rutaProximamente('cobros', {
    titulo: 'Cobros pendientes',
    descripcion: 'Cuentas listas para cobrar enviadas desde el salón.',
    modulo: 'M09',
  }),
  rutaProximamente('pagos', {
    titulo: 'Pagos',
    descripcion: 'Registro de pagos con uno o varios métodos hasta saldar la cuenta.',
    modulo: 'M09',
  }),
  rutaProximamente('division', {
    titulo: 'División de cuenta',
    descripcion: 'Separar una cuenta entre varios comensales.',
    modulo: 'M09',
  }),
  rutaProximamente('control', {
    titulo: 'Control de caja',
    descripcion: 'Apertura, cierre y arqueo del turno de caja.',
    modulo: 'M10',
  }),
]);
