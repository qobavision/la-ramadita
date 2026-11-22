import { Injectable, inject } from '@angular/core';
import { Observable, share } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthService } from '../../auth/auth.service';

/**
 * Aviso recibido por WebSocket. Además de los eventos del backend
 * (`PEDIDO_NUEVO`, `PRODUCTO_PREPARADO`, …) se emiten `CONECTADO` y
 * `DESCONECTADO` para que cada pantalla muestre el estado y vuelva a
 * consultar la API al reconectarse (pudo perderse algún aviso).
 */
export interface EventoTiempoReal {
  tipo: string;
  [dato: string]: unknown;
}

const ESPERA_INICIAL_MS = 1_000;
const ESPERA_MAXIMA_MS = 15_000;

/**
 * Conexión de tiempo real con el backend (AGENTS.md §10). Los avisos son
 * señales: la información siempre se vuelve a leer por la API REST.
 *
 * La URL se arma a partir de `environment.apiUrl` y la dirección de la página,
 * nunca con `localhost` fijo (AGENTS.md §13): una tablet de la red local se
 * conecta al mismo host desde el que abrió la aplicación.
 */
@Injectable({ providedIn: 'root' })
export class TiempoRealService {
  private readonly auth = inject(AuthService);
  private readonly conexiones = new Map<string, Observable<EventoTiempoReal>>();

  /**
   * Abre una conexión mientras haya suscriptores y se reconecta sola si se
   * corta. `canales` limita los avisos generales que se reciben (p. ej.
   * `cocina`); los avisos dirigidos al usuario llegan siempre. Quienes piden
   * los mismos canales comparten una sola conexión.
   */
  escuchar(canales: readonly string[] = []): Observable<EventoTiempoReal> {
    const clave = [...canales].sort().join(',');
    let conexion = this.conexiones.get(clave);
    if (!conexion) {
      conexion = this.conectar(canales).pipe(share({ resetOnRefCountZero: true }));
      this.conexiones.set(clave, conexion);
    }
    return conexion;
  }

  private conectar(canales: readonly string[]): Observable<EventoTiempoReal> {
    return new Observable<EventoTiempoReal>((suscriptor) => {
      let socket: WebSocket | null = null;
      let reintento: ReturnType<typeof setTimeout> | undefined;
      let espera = ESPERA_INICIAL_MS;
      let terminado = false;

      const abrir = () => {
        const token = this.auth.token;
        if (terminado || !token) {
          return;
        }
        socket = new WebSocket(urlTiempoReal(token, canales));
        socket.onopen = () => {
          espera = ESPERA_INICIAL_MS;
          suscriptor.next({ tipo: 'CONECTADO' });
        };
        socket.onmessage = (mensaje) => {
          try {
            suscriptor.next(JSON.parse(String(mensaje.data)) as EventoTiempoReal);
          } catch {
            // Mensaje no reconocido: se ignora.
          }
        };
        socket.onclose = () => {
          socket = null;
          if (terminado) {
            return;
          }
          suscriptor.next({ tipo: 'DESCONECTADO' });
          reintento = setTimeout(abrir, espera);
          espera = Math.min(espera * 2, ESPERA_MAXIMA_MS);
        };
      };

      abrir();

      return () => {
        terminado = true;
        clearTimeout(reintento);
        socket?.close();
      };
    });
  }
}

function urlTiempoReal(token: string, canales: readonly string[]): string {
  const url = new URL(`${environment.apiUrl}/tiempo-real`, window.location.href);
  url.protocol = url.protocol === 'https:' ? 'wss:' : 'ws:';
  url.searchParams.set('token', token);
  if (canales.length > 0) {
    url.searchParams.set('canales', canales.join(','));
  }
  return url.toString();
}
