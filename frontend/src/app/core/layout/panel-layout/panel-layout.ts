import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { ActivatedRoute, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { Rol, ROL_ETIQUETA } from '../../../auth/auth.models';
import { AuthService } from '../../../auth/auth.service';
import { rutaPanelDe } from '../../../auth/paneles';
import { Icono } from '../../../shared/ui/icono/icono';
import { ConfiguracionPanel } from './panel.config';

/**
 * Armazón común de todos los paneles por rol: barra lateral con el menú
 * (rail de iconos en tablet, barra inferior en móvil), cabecera con el
 * usuario y el cierre de sesión, y un `router-outlet` para las páginas.
 *
 * Se usa como `component` de la ruta raíz de cada panel, que debe aportar
 * `data: { panel: ConfiguracionPanel, rolesPanel: Rol[] }`.
 *
 * Si el usuario no pertenece a los roles del panel (caso del ADMINISTRADOR
 * revisando un panel operativo), la cabecera muestra un enlace para volver
 * a su propio panel.
 */
@Component({
  selector: 'app-panel-layout',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, Icono],
  templateUrl: './panel-layout.html',
  styleUrl: './panel-layout.scss',
})
export class PanelLayout {
  private readonly auth = inject(AuthService);
  private readonly ruta = inject(ActivatedRoute);

  protected readonly config: ConfiguracionPanel = this.ruta.snapshot.data['panel'] ?? {
    nombre: 'Panel',
    eyebrow: '',
    menu: [],
  };

  private readonly rolesPanel: readonly Rol[] = this.ruta.snapshot.data['rolesPanel'] ?? [];

  protected readonly usuario = this.auth.usuario;

  /** Ruta del panel propio del usuario cuando está visitando un panel de otro rol. */
  protected readonly panelPropio = computed(() => {
    const u = this.usuario();
    return u && !this.rolesPanel.includes(u.rol) ? rutaPanelDe(u.rol) : null;
  });

  protected readonly nombreCompleto = computed(() => {
    const u = this.usuario();
    return u ? [u.nombre, u.apellido].filter(Boolean).join(' ') : '';
  });

  protected readonly rolEtiqueta = computed(() => {
    const u = this.usuario();
    return u ? (ROL_ETIQUETA[u.rol] ?? u.rolNombre) : '';
  });

  protected readonly iniciales = computed(() => {
    const u = this.usuario();
    return u
      ? [u.nombre, u.apellido]
          .filter((p): p is string => !!p)
          .map((p) => p.charAt(0).toUpperCase())
          .join('')
          .slice(0, 2)
      : '';
  });

  protected cerrarSesion(): void {
    this.auth.logout();
  }
}
