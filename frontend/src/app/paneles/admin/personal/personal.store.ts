import { computed, Injectable, signal } from '@angular/core';
import { Rol } from '../../../auth/auth.models';
import { Cargo } from '../admin.catalogos';
import {
  AccesoConEmpleado,
  AccesoVisual,
  EmpleadoConAcceso,
  EmpleadoVisual,
} from './personal.models';

export interface AltaEmpleado {
  nombres: string;
  apellidos: string;
  dni: string;
  telefono: string;
  correoPersonal: string;
  cargo: Cargo;
  activo: boolean;
}

export interface AltaAcceso {
  empleadoId: number;
  username: string;
  rol: Rol;
  activo: boolean;
}

/**
 * Estado visual de empleados y cuentas de acceso.
 *
 * Solo vive en memoria de esta sesión del navegador: no llama al backend,
 * no usa sessionStorage y se pierde al recargar. Sirve para recorrer las
 * pantallas de Empleados y Usuarios hasta que exista el CRUD real.
 */
@Injectable({ providedIn: 'root' })
export class PersonalVisualStore {
  private siguienteEmpleado = 1;
  private siguienteAcceso = 1;

  private readonly empleadosEstado = signal<EmpleadoVisual[]>([]);
  private readonly accesosEstado = signal<AccesoVisual[]>([]);

  readonly empleados = this.empleadosEstado.asReadonly();
  readonly accesos = this.accesosEstado.asReadonly();

  readonly empleadosConAcceso = computed<EmpleadoConAcceso[]>(() => {
    const accesos = this.accesosEstado();
    return this.empleadosEstado().map((empleado) => ({
      ...empleado,
      acceso: accesos.find((acceso) => acceso.empleadoId === empleado.id) ?? null,
    }));
  });

  readonly accesosConEmpleado = computed<AccesoConEmpleado[]>(() => {
    const empleados = this.empleadosEstado();
    return this.accesosEstado().map((acceso) => ({
      ...acceso,
      empleado: empleados.find((empleado) => empleado.id === acceso.empleadoId) ?? null,
    }));
  });

  registrarEmpleado(alta: AltaEmpleado): EmpleadoVisual {
    const empleado: EmpleadoVisual = { id: this.siguienteEmpleado++, ...alta };
    this.empleadosEstado.update((lista) => [empleado, ...lista]);
    return empleado;
  }

  actualizarEmpleado(id: number, cambios: AltaEmpleado): void {
    this.empleadosEstado.update((lista) =>
      lista.map((empleado) => (empleado.id === id ? { ...empleado, ...cambios } : empleado)),
    );
  }

  cambiarEstadoEmpleado(id: number, activo: boolean): void {
    this.empleadosEstado.update((lista) =>
      lista.map((empleado) => (empleado.id === id ? { ...empleado, activo } : empleado)),
    );
  }

  dniEnUso(dni: string, exceptoId?: number): boolean {
    return this.empleadosEstado().some(
      (empleado) => empleado.dni === dni && empleado.id !== exceptoId,
    );
  }

  crearAcceso(alta: AltaAcceso): AccesoVisual | null {
    if (this.accesoDeEmpleado(alta.empleadoId)) {
      return null;
    }
    const acceso: AccesoVisual = { id: this.siguienteAcceso++, ...alta };
    this.accesosEstado.update((lista) => [acceso, ...lista]);
    return acceso;
  }

  actualizarAcceso(id: number, cambios: Pick<AltaAcceso, 'username' | 'rol' | 'activo'>): void {
    this.accesosEstado.update((lista) =>
      lista.map((acceso) => (acceso.id === id ? { ...acceso, ...cambios } : acceso)),
    );
  }

  cambiarEstadoAcceso(id: number, activo: boolean): void {
    this.accesosEstado.update((lista) =>
      lista.map((acceso) => (acceso.id === id ? { ...acceso, activo } : acceso)),
    );
  }

  usernameEnUso(username: string, exceptoId?: number): boolean {
    const normalizado = username.trim().toLowerCase();
    return this.accesosEstado().some(
      (acceso) => acceso.username.toLowerCase() === normalizado && acceso.id !== exceptoId,
    );
  }

  accesoDeEmpleado(empleadoId: number): AccesoVisual | undefined {
    return this.accesosEstado().find((acceso) => acceso.empleadoId === empleadoId);
  }

  empleadoPorId(id: number): EmpleadoVisual | undefined {
    return this.empleadosEstado().find((empleado) => empleado.id === id);
  }

  accesoPorId(id: number): AccesoVisual | undefined {
    return this.accesosEstado().find((acceso) => acceso.id === id);
  }

  empleadosSinAcceso(incluirId?: number): EmpleadoVisual[] {
    const ocupados = new Set(this.accesosEstado().map((acceso) => acceso.empleadoId));
    return this.empleadosEstado().filter(
      (empleado) => empleado.id === incluirId || !ocupados.has(empleado.id),
    );
  }
}
