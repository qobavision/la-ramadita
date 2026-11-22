import { ConfiguracionPanel } from '../../core/layout/panel-layout/panel.config';

/**
 * Menú del panel de CAJA. Pagos y división de cuenta se hacen dentro de
 * "Cobros" (al abrir una cuenta), por eso no tienen entrada propia.
 */
export const PANEL_CAJA: ConfiguracionPanel = {
  nombre: 'Caja',
  eyebrow: 'Cobros y control',
  menu: [
    { etiqueta: 'Inicio', icono: 'inicio', ruta: '', disponible: true },
    { etiqueta: 'Cobros', icono: 'caja', ruta: 'cobros', disponible: true },
    { etiqueta: 'Control de caja', icono: 'reloj', ruta: 'control', disponible: true },
  ],
};
