import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Observable, map, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { AmbitoProducto, CategoriaProducto, Producto, UnidadNegocio } from '../paneles/supervision/datos/supervision.models';
import { ProductoApi, ambitoApi, ambitoDesdeApi, categoriaDesdeApi, categoriaHaciaApi, mapearComponentes } from './carta.api';

@Injectable({ providedIn: 'root' })
export class ProductosService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/productos`;

  readonly lista = signal<Producto[]>([]);

  cargar(unidad?: UnidadNegocio, soloActivos = true): Observable<Producto[]> {
    let params = new HttpParams().set('activos', String(soloActivos));
    if (unidad) {
      params = params.set('ambito', ambitoApi(unidad));
    }
    return this.http.get<ProductoApi[]>(this.base, { params }).pipe(
      map((items) => unicosPorId(items.map(mapearProducto))),
      tap((items) => this.lista.set(items)),
    );
  }

  crear(datos: {
    nombre: string;
    categoria: CategoriaProducto;
    ambito: AmbitoProducto;
    precio: number;
    componentes?: { productoId: number; cantidad: number }[];
  }): Observable<Producto> {
    return this.http
      .post<ProductoApi>(this.base, {
        nombre: datos.nombre,
        categoria: categoriaHaciaApi(datos.categoria),
        ambito: datos.ambito,
        precio: datos.precio,
        componentes: datos.componentes ?? [],
      })
      .pipe(map(mapearProducto), tap((producto) => this.reemplazar(producto)));
  }

  actualizar(
    id: number,
    datos: {
      nombre: string;
      categoria: CategoriaProducto;
      ambito: AmbitoProducto;
      precio: number;
      componentes?: { productoId: number; cantidad: number }[];
    },
  ): Observable<Producto> {
    return this.http
      .put<ProductoApi>(`${this.base}/${id}`, {
        nombre: datos.nombre,
        categoria: categoriaHaciaApi(datos.categoria),
        ambito: datos.ambito,
        precio: datos.precio,
        componentes: datos.componentes ?? [],
      })
      .pipe(map(mapearProducto), tap((producto) => this.reemplazar(producto)));
  }

  cambiarActivo(id: number, activo: boolean): Observable<Producto> {
    return this.http
      .patch<ProductoApi>(`${this.base}/${id}/activo`, {}, { params: new HttpParams().set('valor', String(activo)) })
      .pipe(map(mapearProducto), tap((producto) => this.reemplazar(producto)));
  }

  buscarPorNombre(nombre: string): Producto | undefined {
    const limpio = nombre.trim().toLowerCase();
    return this.lista().find((p) => p.nombre.toLowerCase() === limpio);
  }

  private reemplazar(producto: Producto): void {
    this.lista.update((lista) => {
      const indice = lista.findIndex((p) => p.id === producto.id);
      if (indice < 0) {
        return producto.activo ? [...lista, producto] : lista;
      }
      if (!producto.activo) {
        return lista.filter((p) => p.id !== producto.id);
      }
      return lista.map((p, i) => (i === indice ? producto : p));
    });
  }
}

function unicosPorId(productos: Producto[]): Producto[] {
  const vistos = new Set<number>();
  return productos.filter((p) => {
    if (vistos.has(p.id)) {
      return false;
    }
    vistos.add(p.id);
    return true;
  });
}

function mapearProducto(api: ProductoApi): Producto {
  return {
    id: api.id,
    nombre: api.nombre,
    categoria: categoriaDesdeApi(api.categoria),
    ambito: ambitoDesdeApi(api.ambito),
    precio: Number(api.precio),
    activo: api.activo,
    componentes: mapearComponentes(api.componentes),
  };
}
