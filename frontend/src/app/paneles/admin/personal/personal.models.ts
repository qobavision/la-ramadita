import { Rol } from '../../../auth/auth.models';
import { Cargo } from '../admin.catalogos';

/**
 * Modelos visuales de personal. No son el contrato de la API: viven solo
 * en el frontend mientras no exista persistencia de empleados ni la
 * vinculación empleado → usuario en el backend.
 */

export interface EmpleadoVisual {
  id: number;
  nombres: string;
  apellidos: string;
  dni: string;
  telefono: string;
  correoPersonal: string;
  cargo: Cargo;
  activo: boolean;
}

export interface AccesoVisual {
  id: number;
  empleadoId: number;
  username: string;
  rol: Rol;
  activo: boolean;
}

export interface EmpleadoConAcceso extends EmpleadoVisual {
  acceso: AccesoVisual | null;
}

export interface AccesoConEmpleado extends AccesoVisual {
  empleado: EmpleadoVisual | null;
}

export function nombreCompletoDe(empleado: Pick<EmpleadoVisual, 'nombres' | 'apellidos'>): string {
  return `${empleado.nombres} ${empleado.apellidos}`.trim();
}
