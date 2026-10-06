import { Routes } from '@angular/router';
import { Rol } from '../../../auth/auth.models';
import { rolGuard } from '../../guards/rol.guard';
import { PanelLayout } from './panel-layout';
import { ConfiguracionPanel } from './panel.config';

/**
 * Construye las rutas de un panel: una ruta raíz con el `PanelLayout`,
 * protegida por sesión y rol (también para las rutas hijas), y las páginas
 * del panel como hijas.
 */
export function rutasPanel(config: ConfiguracionPanel, roles: readonly Rol[], hijos: Routes): Routes {
  const guard = rolGuard(roles);
  return [
    {
      path: '',
      component: PanelLayout,
      canActivate: [guard],
      canActivateChild: [guard],
      data: { panel: config },
      children: hijos,
    },
  ];
}
