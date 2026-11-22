import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../environments/environment';
import { Boleta, CajaSesion, Cuenta, CuentaResumen, MetodoPago, SolicitudPago } from './caja.models';

/** Llamadas HTTP de caja. Saldos, estados y reglas se calculan en el backend. */
@Injectable({ providedIn: 'root' })
export class CajaService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/caja`;

  metodos(): Observable<MetodoPago[]> {
    return this.http.get<MetodoPago[]>(`${this.base}/metodos`);
  }

  /** Mi caja abierta, o `null` si todavía no la abrí (204). */
  miCaja(): Observable<CajaSesion | null> {
    return this.http.get<CajaSesion | null>(`${this.base}/sesion`).pipe(map((s) => s ?? null));
  }

  abrir(montoInicial: number): Observable<CajaSesion> {
    return this.http.post<CajaSesion>(`${this.base}/sesion`, { montoInicial });
  }

  movimiento(tipo: 'INGRESO' | 'EGRESO', monto: number, motivo: string): Observable<CajaSesion> {
    return this.http.post<CajaSesion>(`${this.base}/sesion/movimientos`, { tipo, monto, motivo });
  }

  cerrar(efectivoContado: number, observacion: string | null): Observable<CajaSesion> {
    return this.http.post<CajaSesion>(`${this.base}/sesion/cerrar`, { efectivoContado, observacion });
  }

  sesiones(): Observable<CajaSesion[]> {
    return this.http.get<CajaSesion[]>(`${this.base}/sesiones`);
  }

  pendientes(): Observable<CuentaResumen[]> {
    return this.http.get<CuentaResumen[]>(`${this.base}/cuentas`);
  }

  cuenta(atencionId: number): Observable<Cuenta> {
    return this.http.get<Cuenta>(`${this.base}/cuentas/${atencionId}`);
  }

  pagar(atencionId: number, pago: SolicitudPago): Observable<Cuenta> {
    return this.http.post<Cuenta>(`${this.base}/cuentas/${atencionId}/pagos`, pago);
  }

  boleta(atencionId: number): Observable<Boleta> {
    return this.http.get<Boleta>(`${this.base}/cuentas/${atencionId}/boleta`);
  }

  /** Solo ADMINISTRADOR (lo valida el backend); el motivo queda como evidencia. */
  anularPago(pagoId: number, motivo: string): Observable<Cuenta> {
    return this.http.post<Cuenta>(`${this.base}/pagos/${pagoId}/anular`, { motivo });
  }
}
