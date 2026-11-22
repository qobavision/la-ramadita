import { ConfiguracionPanel } from '../../core/layout/panel-layout/panel.config';
import { AvisosMesero } from './avisos/avisos-mesero';

/** Menú del panel de MESERO (uso en tablet): mesas, pedidos y cuentas. */
export const PANEL_MESERO: ConfiguracionPanel = {
  nombre: 'Atención en salón',
  eyebrow: 'Mesero',
  // Aviso de Cocina cuando un producto queda preparado (RN-13), en cualquier página del panel.
  complemento: AvisosMesero,
  menu: [
    { etiqueta: 'Inicio', icono: 'inicio', ruta: '', disponible: true },
    { etiqueta: 'Mesas', icono: 'mesas', ruta: 'mesas', disponible: true },
    { etiqueta: 'Pedidos', icono: 'pedidos', ruta: 'pedidos', disponible: true },
    { etiqueta: 'Cuentas', icono: 'ventas', ruta: 'cuentas', disponible: true },
  ],
};
