import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map } from 'rxjs';
import { AuthService } from '../../auth/auth.service';
import { rutaInicioDe } from '../../auth/paneles';

/**
 * Protege rutas que solo requieren sesión (sin distinguir rol). Si existe un
 * token guardado pero el usuario aún no está en memoria (recarga de página),
 * intenta recuperarlo mediante `/auth/me` antes de decidir.
 *
 * Para rutas de un panel concreto usar `rolGuard`.
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

/** Evita mostrar el login a quien ya tiene sesión: lo envía a su panel. */
export const invitadoGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  return auth.restaurarSesion().pipe(
    map((usuario) => (usuario ? router.parseUrl(rutaInicioDe(usuario)) : true)),
  );
};

/**
 * Guard de la raíz (`/`): nunca muestra contenido, solo decide a dónde ir:
 * al panel del rol si hay sesión, o al login si no la hay.
 */
export const inicioGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  return auth.restaurarSesion().pipe(
    map((usuario) => router.parseUrl(usuario ? rutaInicioDe(usuario) : '/login')),
  );
};
