import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, catchError, map, of, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { LoginRequest, LoginResponse, Usuario } from './auth.models';

/** Motivo con el que se redirige al login al cerrar la sesión. */
export type MotivoCierre = 'manual' | 'expirada';

/**
 * Gestiona la sesión del usuario frente a la API de M01.
 *
 * - El JWT se guarda en `sessionStorage` (vive mientras la pestaña esté abierta).
 * - El usuario autenticado se mantiene en memoria como señal y se recupera con
 *   `GET /auth/me` cuando la aplicación se recarga y aún existe un token.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private static readonly CLAVE_TOKEN = 'la-ramadita.token';

  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly baseUrl = `${environment.apiUrl}/auth`;

  private readonly usuarioActual = signal<Usuario | null>(null);

  /** Usuario con sesión iniciada, o `null`. */
  readonly usuario = this.usuarioActual.asReadonly();
  readonly autenticado = computed(() => this.usuarioActual() !== null);

  /** Token almacenado en la sesión del navegador, o `null` si no existe. */
  get token(): string | null {
    return sessionStorage.getItem(AuthService.CLAVE_TOKEN);
  }

  login(credenciales: LoginRequest): Observable<Usuario> {
    return this.http.post<LoginResponse>(`${this.baseUrl}/login`, credenciales).pipe(
      tap((respuesta) => {
        sessionStorage.setItem(AuthService.CLAVE_TOKEN, respuesta.token);
        this.usuarioActual.set(conFuncion(respuesta.usuario));
      }),
      map((respuesta) => conFuncion(respuesta.usuario)),
    );
  }

  /**
   * Recupera la sesión a partir del token guardado consultando `/auth/me`.
   * Devuelve el usuario o `null` si no hay token o éste ya no es válido
   * (expirado, usuario desactivado, etc.), en cuyo caso limpia el almacenamiento.
   */
  restaurarSesion(): Observable<Usuario | null> {
    const enMemoria = this.usuarioActual();
    if (enMemoria) {
      return of(enMemoria);
    }
    return this.recargarUsuario();
  }

  /** Vuelve a leer `/auth/me` para tomar una función temporal recién asignada. */
  recargarUsuario(): Observable<Usuario | null> {
    if (!this.token) {
      return of(null);
    }
    return this.http.get<Usuario>(`${this.baseUrl}/me`).pipe(
      tap((usuario) => this.usuarioActual.set(conFuncion(usuario))),
      catchError(() => {
        this.limpiarSesion();
        return of(null);
      }),
    );
  }

  /** Cierra la sesión localmente y vuelve a la pantalla de login. */
  logout(motivo: MotivoCierre = 'manual'): void {
    this.limpiarSesion();
    const queryParams = motivo === 'expirada' ? { motivo } : {};
    void this.router.navigate(['/login'], { queryParams });
  }

  private limpiarSesion(): void {
    sessionStorage.removeItem(AuthService.CLAVE_TOKEN);
    this.usuarioActual.set(null);
  }
}

function conFuncion(usuario: Usuario): Usuario {
  return { ...usuario, funcionActual: usuario.funcionActual || usuario.rol };
}
