import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, map, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { InsumoDemo, UnidadInsumo, estadoStockDe } from '../paneles/supervision/datos/supervision.models';
import { FiltroEstadoInsumo, InsumoApi, mapearInsumo, unidadHaciaApi } from './inventario.models';

@Injectable({ providedIn: 'root' })
export class InsumosService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/insumos`;
  private ultimaBusqueda = '';
  private ultimoEstado: FiltroEstadoInsumo = 'ACTIVOS';

  readonly lista = signal<InsumoDemo[]>([]);
  readonly stockBajo = computed(() =>
    this.lista().filter((i) => i.activo !== false && estadoStockDe(i) !== 'NORMAL').length,
  );

  cargar(busqueda = '', estado: FiltroEstadoInsumo = 'ACTIVOS'): Observable<InsumoApi[]> {
    this.ultimaBusqueda = busqueda;
    this.ultimoEstado = estado;
    let params = new HttpParams().set('estado', estado);
    if (busqueda.trim()) {
      params = params.set('q', busqueda.trim());
    }
    return this.http.get<InsumoApi[]>(this.base, { params }).pipe(
      tap((items) => this.lista.set(items.map(mapearInsumo))),
    );
  }

  crear(datos: { nombre: string; unidad: UnidadInsumo; stock: number; minimo: number }): Observable<InsumoApi> {
    return this.http
      .post<InsumoApi>(this.base, {
        nombre: datos.nombre,
        unidadMedida: unidadHaciaApi(datos.unidad),
        stockInicial: datos.stock,
        stockMinimo: datos.minimo,
      })
      .pipe(tap(() => this.refrescar()));
  }

  actualizar(id: number, datos: { nombre: string; unidad: UnidadInsumo; minimo: number }): Observable<InsumoApi> {
    return this.http
      .put<InsumoApi>(`${this.base}/${id}`, {
        nombre: datos.nombre,
        unidadMedida: unidadHaciaApi(datos.unidad),
        stockMinimo: datos.minimo,
      })
      .pipe(tap(() => this.refrescar()));
  }

  eliminar(id: number): Observable<InsumoApi | null> {
    return this.http.delete<InsumoApi>(`${this.base}/${id}`, { observe: 'response' }).pipe(
      map((respuesta) => respuesta.body),
      tap(() => this.refrescar()),
    );
  }

  reactivar(id: number): Observable<InsumoApi> {
    return this.http.patch<InsumoApi>(`${this.base}/${id}/reactivar`, {}).pipe(tap(() => this.refrescar()));
  }

  private refrescar(): void {
    this.cargar(this.ultimaBusqueda, this.ultimoEstado).subscribe();
  }
}
