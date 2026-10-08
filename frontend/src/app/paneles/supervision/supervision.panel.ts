import { ConfiguracionPanel } from '../../core/layout/panel-layout/panel.config';

/**
 * Menú del panel de SUPERVISOR: operación diaria. No administra usuarios
 * ni roles permanentes. Ventas históricas y BI viven en Administración.
 */
export const PANEL_SUPERVISION: ConfiguracionPanel = {
  nombre: 'Supervisión',
  eyebrow: 'Control operativo',
  menu: [
    { etiqueta: 'Inicio', icono: 'inicio', ruta: '', disponible: true },
    { etiqueta: 'Turnos y personal', icono: 'reloj', ruta: 'turnos', disponible: true },
    { etiqueta: 'Operación', icono: 'operacion', ruta: 'operacion', disponible: true },
    { etiqueta: 'Compras y recepción', icono: 'compras', ruta: 'compras', disponible: true },
    { etiqueta: 'Inventario', icono: 'inventario', ruta: 'inventario', disponible: true },
    { etiqueta: 'Carta y productos', icono: 'carta', ruta: 'productos', disponible: true },
  ],
};
