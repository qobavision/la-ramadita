import { ConfiguracionPanel } from '../../core/layout/panel-layout/panel.config';

/**
 * Menú del panel de SUPERVISOR: control operativo, compras, inventario y
 * carta (levantamiento con el cliente §20) más la gestión de usuarios que
 * M01 le permite (sin tocar administradores).
 */
export const PANEL_SUPERVISION: ConfiguracionPanel = {
  nombre: 'Supervisión',
  eyebrow: 'Control operativo',
  menu: [
    { etiqueta: 'Inicio', icono: 'inicio', ruta: '', disponible: true },
    { etiqueta: 'Operación', icono: 'operacion', ruta: 'operacion' },
    { etiqueta: 'Compras', icono: 'compras', ruta: 'compras' },
    { etiqueta: 'Inventario', icono: 'inventario', ruta: 'inventario' },
    { etiqueta: 'Productos y carta', icono: 'carta', ruta: 'productos' },
    { etiqueta: 'Usuarios', icono: 'usuarios', ruta: 'usuarios' },
    { etiqueta: 'Ventas', icono: 'ventas', ruta: 'ventas' },
    { etiqueta: 'Reportes', icono: 'reportes', ruta: 'reportes' },
  ],
};
