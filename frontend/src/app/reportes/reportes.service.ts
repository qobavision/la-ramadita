import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { FiltroReporte, ResumenVentas, VentaDetalle, VentaResumen } from './reportes.models';

/** Consultas de Reportes / BI (M12) y ventas históricas (M11). Los cálculos se hacen en el backend. */
@Injectable({ providedIn: 'root' })
export class ReportesService {
  private readonly http = inject(HttpClient);
  private readonly api = environment.apiUrl;

  resumen(filtro: FiltroReporte): Observable<ResumenVentas> {
    return this.http.get<ResumenVentas>(`${this.api}/reportes/resumen`, { params: parametros(filtro) });
  }

  ventas(filtro: FiltroReporte): Observable<VentaResumen[]> {
    return this.http.get<VentaResumen[]>(`${this.api}/ventas`, { params: parametros(filtro) });
  }

  venta(atencionId: number): Observable<VentaDetalle> {
    return this.http.get<VentaDetalle>(`${this.api}/ventas/${atencionId}`);
  }
}

function parametros(f: FiltroReporte): HttpParams {
  let p = new HttpParams().set('desde', f.desde).set('hasta', f.hasta);
  if (f.unidad !== 'consolidado') {
    p = p.set('unidad', f.unidad.toUpperCase());
  }
  return p;
}
