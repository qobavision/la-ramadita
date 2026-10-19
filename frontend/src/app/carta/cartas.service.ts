import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Observable, map, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { unicosDetalles } from '../paneles/supervision/carta/carta.vista';
import { CartaDia, DetalleCarta, UnidadNegocio } from '../paneles/supervision/datos/supervision.models';
import { CartaApi, CartaDetalleApi, ambitoApi, mapearComponentes } from './carta.api';

@Injectable({ providedIn: 'root' })
export class CartasService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/cartas`;

  readonly lista = signal<CartaDia[]>([]);

  cargar(unidad: UnidadNegocio): Observable<CartaDia[]> {
    const params = new HttpParams().set('ambito', ambitoApi(unidad));
    return this.http.get<CartaApi[]>(this.base, { params }).pipe(
      map((items) => items.map((c) => mapearCarta(c, unidad))),
      tap((items) => this.lista.set(items)),
    );
  }

  crear(unidad: UnidadNegocio, nombre: string, detalles: DetalleCarta[] = []): Observable<CartaDia> {
    return this.http
      .post<CartaApi>(this.base, {
        nombre,
        ambito: ambitoApi(unidad),
        detalles: detalles.map(haciaDetalleRequest),
      })
      .pipe(map((c) => mapearCarta(c, unidad)));
  }

  guardar(carta: CartaDia): Observable<CartaDia> {
    return this.http
      .put<CartaApi>(`${this.base}/${carta.id}`, {
        nombre: carta.nombre,
        ambito: ambitoApi(carta.unidad),
        detalles: unicosDetalles(carta.detalles).map(haciaDetalleRequest),
      })
      .pipe(map((c) => mapearCarta(c, carta.unidad)));
  }

  marcarVigente(carta: CartaDia): Observable<CartaDia> {
    return this.http.patch<CartaApi>(`${this.base}/${carta.id}/vigente`, {}).pipe(map((c) => mapearCarta(c, carta.unidad)));
  }

  quitarDetalle(carta: CartaDia, detalleId: number): Observable<CartaDia> {
    return this.http
      .delete<CartaApi>(`${this.base}/${carta.id}/detalles/${detalleId}`)
      .pipe(map((c) => mapearCarta(c, carta.unidad)));
  }

  estadoDetalle(carta: CartaDia, detalleId: number, estado: 'DISPONIBLE' | 'AGOTADO'): Observable<CartaDia> {
    return this.http
      .patch<CartaApi>(`${this.base}/${carta.id}/detalles/${detalleId}/estado`, { estado })
      .pipe(map((c) => mapearCarta(c, carta.unidad)));
  }
}

function mapearCarta(api: CartaApi, unidad: UnidadNegocio): CartaDia {
  return {
    id: api.id,
    nombre: api.nombre,
    unidad,
    fecha: api.fecha,
    estado: api.estado,
    vigente: api.vigente,
    detalles: unicosDetalles(api.detalles.map(mapearDetalle)),
  };
}

function mapearDetalle(api: CartaDetalleApi): DetalleCarta {
  return {
    id: api.id,
    productoId: api.productoId,
    nombre: api.nombre,
    seccion: api.seccion,
    precioDia: api.precioDia,
    precioEfectivo: Number(api.precioEfectivo),
    estado: api.estado,
    componentes: mapearComponentes(api.componentes),
  };
}

function haciaDetalleRequest(d: DetalleCarta): { productoId: number; seccion: string; precioDia: number | null; estado: string } {
  return {
    productoId: d.productoId,
    seccion: d.seccion,
    precioDia: d.precioDia,
    estado: d.estado,
  };
}
