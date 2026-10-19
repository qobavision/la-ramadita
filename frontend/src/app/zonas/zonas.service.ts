import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Observable, map, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { MesaDemo, ZonaDemo } from '../paneles/supervision/datos/supervision.models';

interface ZonaApi {
  id: number;
  nombre: string;
  activa: boolean;
  cantidadMesas: number;
  cajas: number;
  cocinas: number;
  uso: string;
}

interface MesaApi {
  id: number;
  zonaId: number;
  zona: string;
  numero: string;
  nombre: string;
  estado: string;
  activa: boolean;
}

@Injectable({ providedIn: 'root' })
export class ZonasService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/zonas`;

  readonly lista = signal<ZonaDemo[]>([]);
  readonly mesas = signal<MesaDemo[]>([]);

  cargar(): Observable<ZonaDemo[]> {
    return this.http.get<ZonaApi[]>(this.base).pipe(
      map((items) => items.map(mapearZona)),
      tap((items) => this.lista.set(items)),
    );
  }

  cargarMesas(): Observable<MesaDemo[]> {
    return this.http.get<MesaApi[]>(`${environment.apiUrl}/mesas`).pipe(
      map((items) => items.map(mapearMesa)),
      tap((items) => this.mesas.set(items)),
    );
  }

  guardar(entrada: {
    id?: number;
    nombre: string;
    cantidadMesas: number;
    cajas: number;
    cocinas: number;
  }): Observable<ZonaDemo> {
    const cuerpo = {
      nombre: entrada.nombre,
      cantidadMesas: entrada.cantidadMesas,
      cajas: entrada.cajas,
      cocinas: entrada.cocinas,
    };
    const peticion = entrada.id
      ? this.http.put<ZonaApi>(`${this.base}/${entrada.id}`, cuerpo)
      : this.http.post<ZonaApi>(this.base, cuerpo);
    return peticion.pipe(
      map(mapearZona),
      tap(() => this.refrescar()),
    );
  }

  alternar(id: number): Observable<ZonaDemo> {
    return this.http.patch<ZonaApi>(`${this.base}/${id}/activo`, {}).pipe(
      map(mapearZona),
      tap(() => this.refrescar()),
    );
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`).pipe(tap(() => this.refrescar()));
  }

  private refrescar(): void {
    this.cargar().subscribe({ error: () => undefined });
    this.cargarMesas().subscribe({ error: () => undefined });
  }
}

function mapearZona(api: ZonaApi): ZonaDemo {
  const uso: NonNullable<ZonaDemo['uso']> =
    api.uso === 'OPERACION_ACTIVA' ? 'operacion-activa' : api.uso === 'HISTORIAL' ? 'historial' : 'sin-uso';
  return {
    id: api.id,
    nombre: api.nombre,
    activa: api.activa,
    mesas: api.cantidadMesas,
    cajas: api.cajas,
    cocinas: api.cocinas,
    uso,
  };
}

function mapearMesa(api: MesaApi): MesaDemo {
  return {
    id: api.id,
    nombre: api.nombre,
    zonaId: api.zonaId,
    zona: api.zona,
    activa: api.activa,
    estadoOp: api.estado === 'DISPONIBLE' ? 'Libre' : 'Ocupada',
    atencion: null,
    mesero: null,
    tiempo: null,
  };
}
