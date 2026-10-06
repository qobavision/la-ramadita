import { ConfiguracionPanel } from '../../core/layout/panel-layout/panel.config';

/** Menú del panel de CAJA: cobros, pagos, división de cuenta y control de caja. */
export const PANEL_CAJA: ConfiguracionPanel = {
  nombre: 'Caja',
  eyebrow: 'Cobros y control',
  menu: [
    { etiqueta: 'Inicio', icono: 'inicio', ruta: '', disponible: true },
    { etiqueta: 'Cobros', icono: 'caja', ruta: 'cobros' },
    { etiqueta: 'Pagos', icono: 'pago', ruta: 'pagos' },
    { etiqueta: 'División de cuenta', icono: 'dividir', ruta: 'division' },
    { etiqueta: 'Control de caja', icono: 'reloj', ruta: 'control' },
  ],
};
