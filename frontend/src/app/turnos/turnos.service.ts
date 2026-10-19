import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Observable, map, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { Rol } from '../auth/auth.models';
import { EstadoCaja, EstadoTurno, HorarioBase, PersonalTurno, RolTemporal, TurnoOperativo, UnidadNegocio } from '../paneles/supervision/datos/supervision.models';
import { turnoInicial } from '../paneles/supervision/datos/supervision.mocks';

interface HorarioApi {
  ambito: string;
  unidad: string;
  horaInicio: string;
  horaFin: string;
}

interface TurnoApi {
  id: number | null;
  ambito: string;
  unidad: string;
  estado: string;
  horaInicio: string;
  horaFin: string;
  responsable: string;
  cerradoPor?: string;
}

interface PersonalApi {
  id: number;
  empleado: string;
  username: string;
  rolPermanente: string;
  funcionActual: string;
  horaIngreso: string;
}

@Injectable({ providedIn: 'root' })
export class TurnosService {
  private readonly http = inject(HttpClient);

  readonly horarios = signal<HorarioBase[]>([]);
  readonly turno = signal<TurnoOperativo>(turnoInicial());
  readonly personal = signal<PersonalTurno[]>([]);

  cargarHorarios(): Observable<HorarioBase[]> {
    return this.http.get<HorarioApi[]>(`${environment.apiUrl}/horarios`).pipe(
      map((items) => items.map(mapearHorario)),
      tap((items) => this.horarios.set(items)),
    );
  }

  actualizarHorario(unidad: UnidadNegocio, horaInicio: string, horaFin: string): Observable<HorarioBase> {
    return this.http
      .put<HorarioApi>(`${environment.apiUrl}/horarios/${ambitoDe(unidad)}`, {
        horaInicio: horaCorta(horaInicio),
        horaFin: horaCorta(horaFin),
      })
      .pipe(
        map(mapearHorario),
        tap(() => this.cargarHorarios().subscribe({ error: () => undefined })),
      );
  }

  cargarTurno(): Observable<TurnoOperativo> {
    return this.http.get<TurnoApi>(`${environment.apiUrl}/turnos/actual`).pipe(
      map(mapearTurno),
      tap((turno) => this.turno.set(turno)),
    );
  }

  cargarPersonal(): Observable<PersonalTurno[]> {
    return this.http.get<PersonalApi[]>(`${environment.apiUrl}/turnos/actual/personal`).pipe(
      map((items) => items.map(mapearPersonal)),
      tap((items) => this.personal.set(items)),
    );
  }

  asignarFuncion(usuarioId: number, funcionActual: RolTemporal | null): Observable<PersonalTurno> {
    return this.http
      .patch<PersonalApi>(`${environment.apiUrl}/turnos/actual/personal/${usuarioId}`, { funcionActual })
      .pipe(
        map(mapearPersonal),
        tap(() => this.cargarPersonal().subscribe({ error: () => undefined })),
      );
  }

  expulsar(usuarioId: number): Observable<void> {
    return this.http.delete<void>(`${environment.apiUrl}/turnos/actual/personal/${usuarioId}`).pipe(
      tap(() => this.cargarPersonal().subscribe({ error: () => undefined })),
    );
  }

  iniciar(unidad: UnidadNegocio): Observable<TurnoOperativo> {
    return this.http.post<TurnoApi>(`${environment.apiUrl}/turnos`, { ambito: ambitoDe(unidad) }).pipe(
      map(mapearTurno),
      tap((turno) => {
        this.turno.set(turno);
        this.cargarPersonal().subscribe({ error: () => undefined });
      }),
    );
  }

  prepararCierre(id: number): Observable<TurnoOperativo> {
    return this.http.patch<TurnoApi>(`${environment.apiUrl}/turnos/${id}/cierre`, {}).pipe(
      map(mapearTurno),
      tap((turno) => this.turno.set(turno)),
    );
  }

  cerrar(id: number): Observable<TurnoOperativo> {
    return this.http.patch<TurnoApi>(`${environment.apiUrl}/turnos/${id}/cerrar`, {}).pipe(
      map(mapearTurno),
      tap(() => this.cargarTurno().subscribe({ error: () => undefined })),
    );
  }
}

function ambitoDe(unidad: UnidadNegocio): string {
  return unidad === 'Pollería' ? 'POLLERIA' : 'RESTAURANTE';
}

function horaCorta(valor: string): string {
  return valor.slice(0, 5);
}

function mapearHorario(api: HorarioApi): HorarioBase {
  return {
    unidad: api.ambito === 'POLLERIA' ? 'Pollería' : 'Restaurante',
    horaInicio: horaCorta(api.horaInicio),
    horaFin: horaCorta(api.horaFin),
    configurado: true,
  };
}

function mapearTurno(api: TurnoApi): TurnoOperativo {
  const estado: EstadoTurno = api.estado === 'ABIERTO' ? 'ACTIVO' : api.estado === 'EN_CIERRE' ? 'EN CIERRE' : 'CERRADO';
  const caja: EstadoCaja = estado === 'ACTIVO' ? 'Abierta' : estado === 'EN CIERRE' ? 'Pendiente de cierre' : 'Cerrada';
  return {
    id: api.id,
    unidad: api.ambito === 'POLLERIA' ? 'Pollería' : 'Restaurante',
    horaInicio: horaCorta(api.horaInicio),
    horaFin: horaCorta(api.horaFin),
    estado,
    caja,
    alerta: null,
    responsable: api.responsable ?? '',
    cerradoPor: api.cerradoPor ?? '',
  };
}

function mapearPersonal(api: PersonalApi): PersonalTurno {
  return {
    id: api.id,
    empleado: api.empleado,
    username: api.username,
    rolPermanente: api.rolPermanente as Rol,
    funcionActual: api.funcionActual,
    horaIngreso: api.horaIngreso,
    estado: api.funcionActual !== api.rolPermanente ? 'Asignación temporal' : 'En turno',
  };
}
