import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Comanda, HistorialCocina, TableroCocina } from './cocina.models';

/** Llamadas HTTP de Cocina (M08). Permisos y reglas se validan en el backend. */
@Injectable({ providedIn: 'root' })
export class CocinaService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/cocina`;

  tablero(): Observable<TableroCocina> {
    return this.http.get<TableroCocina>(`${this.base}/tablero`);
  }

  historial(): Observable<HistorialCocina> {
    return this.http.get<HistorialCocina>(`${this.base}/historial`);
  }

  prepararLinea(detalleId: number): Observable<Comanda> {
    return this.http.post<Comanda>(`${this.base}/detalles/${detalleId}/preparar`, {});
  }

  prepararComanda(pedidoId: number): Observable<Comanda> {
    return this.http.post<Comanda>(`${this.base}/comandas/${pedidoId}/preparar`, {});
  }
}
