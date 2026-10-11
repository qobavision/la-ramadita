import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { ProveedorDemo } from '../paneles/supervision/datos/supervision.models';
import { ProveedorApi, mapearProveedor } from './inventario.models';

@Injectable({ providedIn: 'root' })
export class ProveedoresService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/proveedores`;

  readonly lista = signal<ProveedorDemo[]>([]);
  readonly activos = computed(() => this.lista().filter((p) => p.activo));

  cargar(): Observable<ProveedorApi[]> {
    return this.http.get<ProveedorApi[]>(this.base, { params: new HttpParams() }).pipe(
      tap((items) => this.lista.set(items.map(mapearProveedor))),
    );
  }

  guardar(datos: Omit<ProveedorDemo, 'id'> & { id?: number }): Observable<ProveedorApi> {
    const cuerpo = {
      nombreRazonSocial: datos.nombre,
      tipoDocumento: datos.tipoDocumento,
      documento: datos.documento,
      celular: datos.telefono.trim() || null,
      personaContacto: datos.contacto.trim() || null,
      activo: datos.activo,
    };
    const peticion = datos.id
      ? this.http.put<ProveedorApi>(`${this.base}/${datos.id}`, cuerpo)
      : this.http.post<ProveedorApi>(this.base, cuerpo);
    return peticion.pipe(tap(() => this.cargar().subscribe()));
  }

  alternarActivo(id: number): Observable<ProveedorApi> {
    return this.http.patch<ProveedorApi>(`${this.base}/${id}/activo`, {}).pipe(tap(() => this.cargar().subscribe()));
  }
}
