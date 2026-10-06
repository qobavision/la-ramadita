import { Routes } from '@angular/router';
import { rutasPanel } from '../../core/layout/panel-layout/panel.routes';
import { rutaProximamente } from '../../shared/paginas/proximamente/proximamente.routes';
import { AdminInicio } from './admin-inicio/admin-inicio';
import { PANEL_ADMIN } from './admin.panel';

export const ADMIN_ROUTES: Routes = rutasPanel(PANEL_ADMIN, ['ADMINISTRADOR'], [
  { path: '', component: AdminInicio, title: 'Administración · La Ramadita' },
  rutaProximamente('usuarios', {
    titulo: 'Usuarios',
    descripcion: 'Alta, edición, activación y desactivación de cuentas del personal.',
    modulo: 'M01',
  }),
  rutaProximamente('productos', {
    titulo: 'Productos y carta',
    descripcion: 'Catálogo de productos, precios y carta diaria por unidad de negocio.',
    modulo: 'M05',
  }),
  rutaProximamente('compras', {
    titulo: 'Compras',
    descripcion: 'Registro de compras y confirmación de recepción de insumos.',
    modulo: 'M03',
  }),
  rutaProximamente('inventario', {
    titulo: 'Inventario',
    descripcion: 'Existencias, movimientos y recetas.',
    modulo: 'M04',
  }),
  rutaProximamente('mesas', {
    titulo: 'Mesas y atención',
    descripcion: 'Configuración de mesas y seguimiento de la atención.',
    modulo: 'M06',
  }),
  rutaProximamente('ventas', {
    titulo: 'Ventas',
    descripcion: 'Historial de ventas por unidad de negocio y turno.',
    modulo: 'M11',
  }),
  rutaProximamente('reportes', {
    titulo: 'Reportes',
    descripcion: 'Indicadores y reportes consolidados del negocio.',
    modulo: 'M12',
  }),
]);
