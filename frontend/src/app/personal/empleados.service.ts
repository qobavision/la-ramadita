import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Cargo, Empleado, EmpleadoRequest } from './personal.models';

@Injectable({ providedIn: 'root' })
export class EmpleadosService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}`;

  listarCargos(): Observable<Cargo[]> {
    return this.http.get<Cargo[]>(`${this.base}/cargos`);
  }

  listar(): Observable<Empleado[]> {
    return this.http.get<Empleado[]>(`${this.base}/empleados`);
  }

  obtener(id: number): Observable<Empleado> {
    return this.http.get<Empleado>(`${this.base}/empleados/${id}`);
  }

  crear(datos: EmpleadoRequest): Observable<Empleado> {
    return this.http.post<Empleado>(`${this.base}/empleados`, datos);
  }

  actualizar(id: number, datos: EmpleadoRequest): Observable<Empleado> {
    return this.http.put<Empleado>(`${this.base}/empleados/${id}`, datos);
  }

  archivar(id: number): Observable<Empleado> {
    return this.http.patch<Empleado>(`${this.base}/empleados/${id}/archivar`, {});
  }
}
