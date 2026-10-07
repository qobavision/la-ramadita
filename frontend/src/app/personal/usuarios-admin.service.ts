import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { UsuarioAdmin, UsuarioAltaRequest, UsuarioEdicionRequest } from './personal.models';

@Injectable({ providedIn: 'root' })
export class UsuariosAdminService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/usuarios`;

  listar(): Observable<UsuarioAdmin[]> {
    return this.http.get<UsuarioAdmin[]>(this.base);
  }

  obtener(id: number): Observable<UsuarioAdmin> {
    return this.http.get<UsuarioAdmin>(`${this.base}/${id}`);
  }

  crear(datos: UsuarioAltaRequest): Observable<UsuarioAdmin> {
    return this.http.post<UsuarioAdmin>(this.base, datos);
  }

  actualizar(id: number, datos: UsuarioEdicionRequest): Observable<UsuarioAdmin> {
    return this.http.put<UsuarioAdmin>(`${this.base}/${id}`, datos);
  }

  restablecerPassword(id: number, password: string): Observable<UsuarioAdmin> {
    return this.http.patch<UsuarioAdmin>(`${this.base}/${id}/password`, { password });
  }

  archivar(id: number): Observable<UsuarioAdmin> {
    return this.http.patch<UsuarioAdmin>(`${this.base}/${id}/archivar`, {});
  }
}
