import { Routes } from '@angular/router';
import { rutasPanel } from '../../core/layout/panel-layout/panel.routes';
import { rutaProximamente } from '../../shared/paginas/proximamente/proximamente.routes';
import { SupervisionInicio } from './supervision-inicio/supervision-inicio';
import { PANEL_SUPERVISION } from './supervision.panel';

export const SUPERVISION_ROUTES: Routes = rutasPanel(PANEL_SUPERVISION, ['SUPERVISOR'], [
  { path: '', component: SupervisionInicio, title: 'Supervisión · La Ramadita' },
  rutaProximamente('operacion', {
    titulo: 'Operación',
    descripcion: 'Estado de mesas, pedidos en curso y cocina en tiempo real.',
    modulo: 'M06',
  }),
  rutaProximamente('compras', {
    titulo: 'Compras',
    descripcion: 'Registro de compras y confirmación de recepción de insumos.',
    modulo: 'M03',
  }),
  rutaProximamente('inventario', {
    titulo: 'Inventario',
    descripcion: 'Existencias, movimientos y alertas de stock mínimo.',
    modulo: 'M04',
  }),
  rutaProximamente('productos', {
    titulo: 'Productos y carta',
    descripcion: 'Disponibilidad diaria y productos agotados.',
    modulo: 'M05',
  }),
  rutaProximamente('ventas', {
    titulo: 'Ventas',
    descripcion: 'Historial de ventas por unidad de negocio y turno.',
    modulo: 'M11',
  }),
  rutaProximamente('reportes', {
    titulo: 'Reportes',
    descripcion: 'Indicadores operativos y de inventario.',
    modulo: 'M12',
  }),
]);
