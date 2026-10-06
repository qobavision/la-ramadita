import { ConfiguracionPanel } from '../../core/layout/panel-layout/panel.config';

/** Menú y textos del panel de ADMINISTRADOR. */
export const PANEL_ADMIN: ConfiguracionPanel = {
  nombre: 'Administración',
  eyebrow: 'Panel de control',
  menu: [
    { etiqueta: 'Inicio', icono: 'inicio', ruta: '', disponible: true },
    { etiqueta: 'Usuarios', icono: 'usuarios', ruta: 'usuarios' },
    { etiqueta: 'Productos y carta', icono: 'productos', ruta: 'productos' },
    { etiqueta: 'Compras', icono: 'compras', ruta: 'compras' },
    { etiqueta: 'Inventario', icono: 'inventario', ruta: 'inventario' },
    { etiqueta: 'Mesas y atención', icono: 'mesas', ruta: 'mesas' },
    { etiqueta: 'Ventas', icono: 'ventas', ruta: 'ventas' },
    { etiqueta: 'Reportes', icono: 'reportes', ruta: 'reportes' },
  ],
};
