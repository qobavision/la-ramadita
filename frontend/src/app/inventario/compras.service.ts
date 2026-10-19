import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { CompraDemo } from '../paneles/supervision/datos/supervision.models';
import { CompraApi, mapearCompra } from './inventario.models';

@Injectable({ providedIn: 'root' })
export class ComprasService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/compras`;

  readonly lista = signal<CompraDemo[]>([]);
  readonly porRecibir = computed(() => this.lista().filter((c) => c.estado !== 'Recibida').length);

  cargar(): Observable<CompraApi[]> {
    return this.http.get<CompraApi[]>(this.base).pipe(tap((items) => this.lista.set(items.map(mapearCompra))));
  }

  guardar(entrada: {
    id?: number;
    proveedorId: number;
    lineas: { insumoId: number; cantidad: number; precioUnitario: number }[];
  }): Observable<CompraApi> {
    const cuerpo = {
      proveedorId: entrada.proveedorId,
      detalles: entrada.lineas.map((l) => ({
        insumoId: l.insumoId,
        cantidad: l.cantidad,
        precioUnitario: l.precioUnitario,
      })),
    };
    const peticion = entrada.id
      ? this.http.put<CompraApi>(`${this.base}/${entrada.id}`, cuerpo)
      : this.http.post<CompraApi>(this.base, cuerpo);
    return peticion.pipe(tap(() => this.cargar().subscribe()));
  }

  cancelar(id: number): Observable<CompraApi> {
    return this.http.patch<CompraApi>(`${this.base}/${id}/cancelar`, {}).pipe(tap(() => this.cargar().subscribe()));
  }

  recibir(id: number, detalles: { compraDetalleId: number; cantidad: number }[]): Observable<CompraApi> {
    return this.http
      .post<CompraApi>(`${this.base}/${id}/recepciones`, { detalles })
      .pipe(tap(() => this.cargar().subscribe()));
  }
}
