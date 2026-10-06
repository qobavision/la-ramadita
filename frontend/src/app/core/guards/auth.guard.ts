import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map } from 'rxjs';
import { AuthService } from '../../auth/auth.service';

/**
 * Protege rutas que requieren sesión. Si existe un token guardado pero el
 * usuario aún no está en memoria (recarga de página), intenta recuperarlo
 * mediante `/auth/me` antes de decidir.
 */
export const authGuard: CanActivateFn = (_route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  return auth.restaurarSesion().pipe(
    map((usuario) =>
      usuario ? true : router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } }),
    ),
  );
};

/** Evita mostrar el login a quien ya tiene sesión: lo envía a `/inicio`. */
export const invitadoGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  return auth.restaurarSesion().pipe(
    map((usuario) => (usuario ? router.createUrlTree(['/inicio']) : true)),
  );
};
