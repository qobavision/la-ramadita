import { ConfiguracionPanel } from '../../core/layout/panel-layout/panel.config';

/**
 * Menú del panel de SUPERVISOR: control operativo, compras, inventario y
 * carta (levantamiento con el cliente §20). Por decisión del equipo el
 * SUPERVISOR no administra usuarios ni roles permanentes: eso corresponde
 * al ADMINISTRADOR (ver AGENTS.md §7).
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
    { etiqueta: 'Ventas', icono: 'ventas', ruta: 'ventas' },
    { etiqueta: 'Reportes', icono: 'reportes', ruta: 'reportes' },
  ],
};
