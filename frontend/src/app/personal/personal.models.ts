import { Rol } from '../auth/auth.models';

/** Contratos de M01 / empleados alineados con los DTOs del backend. */

export interface Cargo {
  id: number;
  nombre: string;
}

export interface UsuarioResumen {
  id: number;
  username: string;
  rol: Rol;
  activo: boolean;
}

export interface Empleado {
  id: number;
  nombres: string;
  apellidos: string;
  dni: string | null;
  telefono: string | null;
  correoPersonal: string | null;
  cargo: Cargo;
  activo: boolean;
  archivado: boolean;
  fechaCreacion: string;
  fechaArchivado: string | null;
  usuario: UsuarioResumen | null;
}

export interface EmpleadoResumen {
  id: number;
  nombres: string;
  apellidos: string;
  dni: string | null;
  correoPersonal: string | null;
  cargo: string;
  activo: boolean;
}

export interface UsuarioAdmin {
  id: number;
  empleadoId: number;
  username: string;
  nombre: string;
  apellido: string | null;
  rol: Rol;
  rolNombre: string;
  activo: boolean;
  archivado: boolean;
  fechaCreacion: string;
  fechaArchivado: string | null;
  fechaUltimoAcceso: string | null;
  empleado: EmpleadoResumen | null;
}

export interface EmpleadoRequest {
  nombres: string;
  apellidos: string;
  dni: string | null;
  telefono: string | null;
  correoPersonal: string | null;
  cargoId: number;
  activo: boolean;
}

export interface UsuarioAltaRequest {
  empleadoId: number;
  username: string;
  password: string;
  rol: Rol;
  activo: boolean;
}

export interface UsuarioEdicionRequest {
  username: string;
  password?: string | null;
  rol: Rol;
  activo: boolean;
}

export type FiltroEstadoPersonal = 'todos' | 'activos' | 'inactivos';

export function nombreCompletoDe(persona: { nombres: string; apellidos: string }): string {
  return `${persona.nombres} ${persona.apellidos}`.trim();
}

export function etiquetaEstadoDe(item: { activo: boolean; archivado: boolean }): string {
  if (item.archivado) {
    return 'Archivado';
  }
  return item.activo ? 'Activo' : 'Inactivo';
}

export function claseEstadoDe(item: { activo: boolean; archivado: boolean }): string {
  if (item.archivado) {
    return 'lr-estado--archivado';
  }
  return item.activo ? 'lr-estado--activo' : 'lr-estado--inactivo';
}

export function coincideFiltroEstado(
  item: { activo: boolean; archivado: boolean },
  filtro: FiltroEstadoPersonal,
): boolean {
  if (filtro === 'todos') {
    return !item.archivado;
  }
  if (filtro === 'activos') {
    return item.activo && !item.archivado;
  }
  return !item.activo || item.archivado;
}

export function compararInactivos<T extends { activo: boolean; archivado: boolean }>(a: T, b: T): number {
  return Number(a.archivado) - Number(b.archivado);
}

export function formatearFechaHora(iso: string | null | undefined): string {
  if (!iso) {
    return '—';
  }
  const fecha = new Date(iso);
  if (Number.isNaN(fecha.getTime())) {
    return '—';
  }
  const dia = String(fecha.getDate()).padStart(2, '0');
  const mes = String(fecha.getMonth() + 1).padStart(2, '0');
  const anio = fecha.getFullYear();
  const hora = String(fecha.getHours()).padStart(2, '0');
  const minuto = String(fecha.getMinutes()).padStart(2, '0');
  return `${dia}/${mes}/${anio} ${hora}:${minuto}`;
}

export function formatearUltimoAcceso(iso: string | null | undefined): string {
  return formatearFechaHora(iso);
}
