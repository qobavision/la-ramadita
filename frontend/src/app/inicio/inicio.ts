import { Component, computed, inject } from '@angular/core';
import { ROL_ETIQUETA } from '../auth/auth.models';
import { AuthService } from '../auth/auth.service';

/**
 * Página temporal posterior al login. Muestra quién inició sesión y su rol.
 * Los paneles por rol (mesero, cocina, caja, administración) se agregarán
 * en módulos posteriores.
 */
@Component({
  selector: 'app-inicio',
  templateUrl: './inicio.html',
  styleUrl: './inicio.scss',
})
export class Inicio {
  private readonly auth = inject(AuthService);

  readonly usuario = this.auth.usuario;

  readonly nombreCompleto = computed(() => {
    const u = this.usuario();
    return u ? [u.nombre, u.apellido].filter(Boolean).join(' ') : '';
  });

  readonly rolEtiqueta = computed(() => {
    const u = this.usuario();
    return u ? (ROL_ETIQUETA[u.rol] ?? u.rolNombre) : '';
  });

  readonly iniciales = computed(() => {
    const u = this.usuario();
    if (!u) {
      return '';
    }
    return [u.nombre, u.apellido]
      .filter(Boolean)
      .map((parte) => parte!.charAt(0).toUpperCase())
      .join('')
      .slice(0, 2);
  });

  cerrarSesion(): void {
    this.auth.logout();
  }
}
