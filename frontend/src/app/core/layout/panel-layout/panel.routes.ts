import { Routes } from '@angular/router';
import { Rol } from '../../../auth/auth.models';
import { rolGuard } from '../../guards/rol.guard';
import { PanelLayout } from './panel-layout';
import { ConfiguracionPanel } from './panel.config';

/**
 * Construye las rutas de un panel: una ruta raíz con el `PanelLayout`,
 * protegida por sesión y rol (también para las rutas hijas), y las páginas
 * del panel como hijas.
 *
 * `roles` son los roles a los que pertenece el panel; el layout los usa para
 * detectar cuándo lo visita un usuario de otro rol (el administrador) y
 * ofrecerle el regreso a su propio panel.
 */
export function rutasPanel(config: ConfiguracionPanel, roles: readonly Rol[], hijos: Routes): Routes {
  const guard = rolGuard(roles);
  return [
    {
      path: '',
      component: PanelLayout,
      canActivate: [guard],
      canActivateChild: [guard],
      data: { panel: config, rolesPanel: roles },
      children: hijos,
    },
  ];
}
