import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { MovimientoDemo } from '../paneles/supervision/datos/supervision.models';
import { MovimientoApi, mapearMovimiento } from './inventario.models';

@Injectable({ providedIn: 'root' })
export class MovimientosService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/inventario/movimientos`;

  readonly lista = signal<MovimientoDemo[]>([]);

  cargar(insumoId?: number): Observable<MovimientoApi[]> {
    let params = new HttpParams();
    if (insumoId) {
      params = params.set('insumoId', insumoId);
    }
    return this.http.get<MovimientoApi[]>(this.base, { params }).pipe(
      tap((items) => this.lista.set(items.map(mapearMovimiento))),
    );
  }
}
