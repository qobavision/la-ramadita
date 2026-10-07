import { ConfiguracionPanel } from '../../core/layout/panel-layout/panel.config';

/**
 * Menú del ADMINISTRADOR. Solo contiene lo propio de la gestión general;
 * las funciones operativas (compras, carta, mesas, cobros...) viven en los
 * paneles de cada rol, a los que el administrador entra desde
 * "Accesos operativos" con su misma sesión.
 */
export const PANEL_ADMIN: ConfiguracionPanel = {
  nombre: 'Administración',
  eyebrow: 'Panel de control',
  menu: [
    { etiqueta: 'Dashboard', icono: 'tablero', ruta: '', disponible: true },
    { etiqueta: 'Empleados', icono: 'empleados', ruta: 'empleados', disponible: true },
    { etiqueta: 'Usuarios y accesos', icono: 'usuarios', ruta: 'usuarios', disponible: true },
    { etiqueta: 'Reportes / BI', icono: 'reportes', ruta: 'reportes' },
    { etiqueta: 'Control de caja', icono: 'caja', ruta: 'control-caja' },
    { etiqueta: 'Accesos operativos', icono: 'accesos', ruta: 'accesos', disponible: true },
  ],
};
