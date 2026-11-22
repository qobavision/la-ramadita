import { ConfiguracionPanel } from '../../core/layout/panel-layout/panel.config';

/** Menú del panel de COCINA: monitor de comandas en tiempo real e historial del turno. */
export const PANEL_COCINA: ConfiguracionPanel = {
  nombre: 'Cocina',
  eyebrow: 'Monitor de producción',
  menu: [
    { etiqueta: 'Pedidos', icono: 'cocina', ruta: '', disponible: true },
    { etiqueta: 'Historial', icono: 'historial', ruta: 'historial', disponible: true },
  ],
};
