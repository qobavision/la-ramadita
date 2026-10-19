import { inject } from '@angular/core';
import { CanActivateChildFn, CanActivateFn, Router } from '@angular/router';
import { map } from 'rxjs';
import { Rol } from '../../auth/auth.models';
import { AuthService } from '../../auth/auth.service';
import { puedeEntrarAlPanel, rutaInicioDe } from '../../auth/paneles';

/**
 * Crea un guard que exige sesión iniciada **y** acceso al panel según el rol.
 *
 * - Sin sesión → `/login` conservando la URL solicitada.
 * - Con sesión y rol permitido (o rol de control general, ver
 *   `puedeEntrarAlPanel`) → pasa.
 * - Con sesión pero sin acceso → se redirige al panel propio del usuario
 *   (un MESERO que escribe `/admin` termina en `/mesero`).
 *
 * Sirve tanto para `canActivate` como para `canActivateChild`.
 *
 * Recordatorio AGENTS.md §7: esto solo oculta pantallas; la autorización real
 * la impone el backend en cada endpoint.
 */
export function rolGuard(rolesPermitidos: readonly Rol[]): CanActivateFn & CanActivateChildFn {
  return (_route, state) => {
    const auth = inject(AuthService);
    const router = inject(Router);

    return auth.recargarUsuario().pipe(
      map((usuario) => {
        if (!usuario) {
          return router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
        }
        if (puedeEntrarAlPanel(usuario, rolesPermitidos)) {
          return true;
        }
        return router.parseUrl(rutaInicioDe(usuario));
      }),
    );
  };
}
