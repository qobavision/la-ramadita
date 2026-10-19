import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Observable, map, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { unidadDesdeApi } from '../inventario/inventario.models';
import { AmbitoProducto, CategoriaProducto, RecetaDemo } from '../paneles/supervision/datos/supervision.models';
import { RecetaApi, ambitoDesdeApi, categoriaDesdeApi, categoriaHaciaApi } from './carta.api';

@Injectable({ providedIn: 'root' })
export class RecetasService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/recetas`;

  readonly lista = signal<RecetaDemo[]>([]);

  cargar(): Observable<RecetaDemo[]> {
    return this.http.get<RecetaApi[]>(this.base).pipe(
      map((items) => items.map(mapearReceta)),
      tap((items) => this.lista.set(items)),
    );
  }

  guardar(entrada: {
    id?: number;
    nombre: string;
    categoria: CategoriaProducto;
    ambito: AmbitoProducto;
    precio: number;
    lineas: { insumoId: number; cantidad: number }[];
  }): Observable<RecetaDemo> {
    const cuerpo = {
      nombre: entrada.nombre,
      categoria: categoriaHaciaApi(entrada.categoria),
      ambito: entrada.ambito,
      precio: entrada.precio,
      insumos: entrada.lineas.map((l) => ({ insumoId: l.insumoId, cantidad: l.cantidad })),
    };
    const peticion = entrada.id
      ? this.http.put<RecetaApi>(`${this.base}/${entrada.id}`, cuerpo)
      : this.http.post<RecetaApi>(this.base, cuerpo);
    return peticion.pipe(
      map(mapearReceta),
      tap((receta) => {
        this.lista.update((lista) => {
          const indice = lista.findIndex((r) => r.id === receta.id);
          if (indice < 0) {
            return [...lista, receta];
          }
          return lista.map((r, i) => (i === indice ? receta : r));
        });
        this.cargar().subscribe({ error: () => undefined });
      }),
    );
  }

  eliminar(productoId: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${productoId}`).pipe(tap(() => this.cargar().subscribe()));
  }
}

function mapearReceta(api: RecetaApi): RecetaDemo {
  return {
    id: api.productoId,
    nombre: api.nombre,
    categoria: categoriaDesdeApi(api.categoria),
    ambito: ambitoDesdeApi(api.ambito),
    precio: Number(api.precio),
    insumos: (api.insumos ?? []).map((i) => ({
      insumoId: i.insumoId,
      insumo: i.insumo,
      unidad: unidadDesdeApi(i.unidadMedida),
      cantidad: Number(i.cantidad),
    })),
  };
}
