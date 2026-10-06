import { ConfiguracionPanel } from '../../core/layout/panel-layout/panel.config';

/** Menú del panel de COCINA: monitor de pedidos e historial. */
export const PANEL_COCINA: ConfiguracionPanel = {
  nombre: 'Cocina',
  eyebrow: 'Monitor de producción',
  menu: [
    { etiqueta: 'Inicio', icono: 'inicio', ruta: '', disponible: true },
    { etiqueta: 'Pedidos', icono: 'cocina', ruta: 'pedidos' },
    { etiqueta: 'Historial', icono: 'historial', ruta: 'historial' },
  ],
};
