import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Atencion, AtencionResumen, CartaMesero, MesaSalon } from './atencion.models';

/** Llamadas HTTP de atención de mesas y pedidos. Las reglas viven en el backend. */
@Injectable({ providedIn: 'root' })
export class AtencionesService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/atenciones`;

  salon(): Observable<MesaSalon[]> {
    return this.http.get<MesaSalon[]>(`${this.base}/salon`);
  }

  activas(): Observable<AtencionResumen[]> {
    return this.http.get<AtencionResumen[]>(this.base);
  }

  /** Carta vigente de la unidad de negocio de la atención. */
  carta(id: number): Observable<CartaMesero> {
    return this.http.get<CartaMesero>(`${this.base}/${id}/carta`);
  }

  abrirMesa(mesaId: number): Observable<Atencion> {
    return this.http.post<Atencion>(this.base, { tipo: 'MESA', mesaId });
  }

  abrirParaLlevar(): Observable<Atencion> {
    return this.http.post<Atencion>(this.base, { tipo: 'PARA_LLEVAR' });
  }

  obtener(id: number): Observable<Atencion> {
    return this.http.get<Atencion>(`${this.base}/${id}`);
  }

  agregar(id: number, cartaDetalleId: number, cantidad = 1, observacion: string | null = null): Observable<Atencion> {
    return this.http.post<Atencion>(`${this.base}/${id}/detalles`, { cartaDetalleId, cantidad, observacion });
  }

  actualizar(id: number, detalleId: number, cantidad: number, observacion: string | null): Observable<Atencion> {
    return this.http.patch<Atencion>(`${this.base}/${id}/detalles/${detalleId}`, { cantidad, observacion });
  }

  quitar(id: number, detalleId: number): Observable<Atencion> {
    return this.http.delete<Atencion>(`${this.base}/${id}/detalles/${detalleId}`);
  }

  enviar(id: number): Observable<Atencion> {
    return this.http.post<Atencion>(`${this.base}/${id}/enviar`, {});
  }

  pedirCuenta(id: number): Observable<Atencion> {
    return this.http.post<Atencion>(`${this.base}/${id}/cuenta`, {});
  }

  /** RN-07: vuelve a ABIERTA una cuenta pendiente de pago para agregar más productos. */
  reabrir(id: number): Observable<Atencion> {
    return this.http.post<Atencion>(`${this.base}/${id}/reabrir`, {});
  }

  liberar(id: number): Observable<Atencion> {
    return this.http.post<Atencion>(`${this.base}/${id}/liberar`, {});
  }
}
