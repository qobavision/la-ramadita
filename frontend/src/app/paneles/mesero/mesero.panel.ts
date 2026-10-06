import { ConfiguracionPanel } from '../../core/layout/panel-layout/panel.config';

/** Menú del panel de MESERO (uso en tablet): mesas, pedidos y cuentas. */
export const PANEL_MESERO: ConfiguracionPanel = {
  nombre: 'Atención en salón',
  eyebrow: 'Mesero',
  menu: [
    { etiqueta: 'Inicio', icono: 'inicio', ruta: '', disponible: true },
    { etiqueta: 'Mesas', icono: 'mesas', ruta: 'mesas' },
    { etiqueta: 'Pedidos', icono: 'pedidos', ruta: 'pedidos' },
    { etiqueta: 'Cuentas', icono: 'ventas', ruta: 'cuentas' },
  ],
};
