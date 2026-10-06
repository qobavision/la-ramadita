import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../../auth/auth.service';
import { environment } from '../../../environments/environment';

const RUTA_LOGIN = `${environment.apiUrl}/auth/login`;

/**
 * - Agrega `Authorization: Bearer <token>` a las llamadas dirigidas a la API.
 * - Si la API responde 401 fuera del login, el token ya no sirve (expiró o el
 *   usuario fue desactivado): se cierra la sesión y se vuelve al login.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const esLlamadaApi = req.url.startsWith(environment.apiUrl);
  const token = auth.token;

  const peticion =
    esLlamadaApi && token
      ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
      : req;

  return next(peticion).pipe(
    catchError((error: unknown) => {
      if (
        error instanceof HttpErrorResponse &&
        error.status === 401 &&
        esLlamadaApi &&
        req.url !== RUTA_LOGIN &&
        auth.autenticado()
      ) {
        auth.logout('expirada');
      }
      return throwError(() => error);
    }),
  );
};
