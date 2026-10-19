import { computed, Injectable, signal } from '@angular/core';
import {
  asignacionesIniciales,
  horariosBaseIniciales,
  mesasIniciales,
  pedidosIniciales,
  pendientesCierreIniciales,
  personalInicial,
  turnoInicial,
  zonasIniciales,
} from './supervision.mocks';
import { HorarioBase, MesaDemo, RolTemporal, ZonaDemo } from './supervision.models';
import { normalizarTextoDescriptivo } from '../../../shared/texto/texto-descriptivo';

/**
 * Estado visual del panel Supervisor. Solo vive en memoria del navegador
 * para poder revisar pantallas. Más adelante se sustituye por HTTP.
 */
@Injectable({ providedIn: 'root' })
export class SupervisionDemo {
  readonly horarios = signal(horariosBaseIniciales());
  readonly turno = signal(turnoInicial());
  readonly pendientesCierre = signal(pendientesCierreIniciales());
  readonly personal = signal(personalInicial());
  readonly asignaciones = signal(asignacionesIniciales());
  readonly zonas = signal(zonasIniciales());
  readonly mesas = signal(mesasIniciales());
  readonly pedidos = signal(pedidosIniciales());

  readonly mesasLibres = computed(() => this.mesas().filter((m) => m.activa && m.estadoOp === 'Libre').length);
  readonly mesasOcupadas = computed(() => this.mesas().filter((m) => m.activa && m.estadoOp === 'Ocupada').length);
  readonly pedidosEnCurso = computed(() => this.pedidos().filter((p) => p.estado === 'En curso' || p.estado === 'Pendiente cocina').length);
  readonly pedidosCocina = computed(() => this.pedidos().filter((p) => p.estado === 'Pendiente cocina').length);
  readonly pedidosPreparados = computed(() => this.pedidos().filter((p) => p.estado === 'Preparado').length);
  readonly atencionesAbiertas = computed(() => this.mesas().filter((m) => m.atencion).length);
  readonly personalActivo = computed(() => this.personal().length);

  actualizarHorario(unidad: HorarioBase['unidad'], horaInicio: string, horaFin: string): void {
    this.horarios.update((lista) =>
      lista.map((h) => (h.unidad === unidad ? { ...h, horaInicio, horaFin, configurado: true } : h)),
    );
  }

  iniciarTurnoAnticipado(responsable: string): void {
    const actual = this.turno();
    const siguiente = actual.unidad === 'Pollería' ? this.horarios()[0] : this.horarios()[1];
    const ahora = new Date();
    this.turno.set({
      id: null,
      unidad: siguiente.unidad,
      horaInicio: horaActual(ahora),
      horaFin: siguiente.horaFin,
      estado: 'ACTIVO',
      caja: 'Abierta',
      alerta: null,
      responsable: responsable || actual.responsable,
      cerradoPor: '',
    });
  }

  prepararCierre(): void {
    this.turno.update((t) => ({
      ...t,
      estado: 'EN CIERRE',
      caja: 'Pendiente de cierre',
      alerta: 'Prepare el cierre de caja. Meseros y cocina deben terminar lo pendiente.',
    }));
  }

  finalizarTurno(): void {
    this.turno.update((t) => ({
      ...t,
      estado: 'CERRADO',
      caja: 'Cerrada',
      alerta: `Turno ${t.unidad} cerrado. Las asignaciones temporales se retiran al terminar el turno.`,
    }));
    this.asignaciones.update((lista) =>
      lista.map((a) => ({ ...a, rolTemporal: null, estado: 'Sin asignación' as const })),
    );
    this.personal.update((lista) =>
      lista.map((p) => ({
        ...p,
        funcionActual: p.rolPermanente,
        estado: 'En turno' as const,
      })),
    );
  }

  asignarTemporal(id: number, rol: RolTemporal): void {
    this.asignaciones.update((lista) =>
      lista.map((a) => (a.id === id ? { ...a, rolTemporal: rol, estado: 'Vigente' as const } : a)),
    );
    const fila = this.asignaciones().find((a) => a.id === id);
    if (!fila) {
      return;
    }
    this.personal.update((lista) =>
      lista.map((p) =>
        p.username === fila.username
          ? { ...p, funcionActual: `${rol} (temporal)`, estado: 'Asignación temporal' as const }
          : p,
      ),
    );
  }

  quitarTemporal(id: number): void {
    const fila = this.asignaciones().find((a) => a.id === id);
    this.asignaciones.update((lista) =>
      lista.map((a) => (a.id === id ? { ...a, rolTemporal: null, estado: 'Sin asignación' as const } : a)),
    );
    if (!fila) {
      return;
    }
    this.personal.update((lista) =>
      lista.map((p) =>
        p.username === fila.username
          ? { ...p, funcionActual: fila.rolPermanente, estado: 'En turno' as const }
          : p,
      ),
    );
  }

  asignarFuncionTemporal(personalId: number, rol: RolTemporal | null): void {
    const persona = this.personal().find((p) => p.id === personalId);
    if (!persona) {
      return;
    }
    const temporal = rol && rol !== persona.rolPermanente ? rol : null;
    this.personal.update((lista) =>
      lista.map((p) =>
        p.id === personalId
          ? {
              ...p,
              funcionActual: temporal ? `${temporal} (temporal)` : p.rolPermanente,
              estado: temporal ? ('Asignación temporal' as const) : ('En turno' as const),
            }
          : p,
      ),
    );
    this.asignaciones.update((lista) => {
      const indice = lista.findIndex((a) => a.username === persona.username);
      const siguiente = {
        empleado: persona.empleado,
        username: persona.username,
        rolPermanente: persona.rolPermanente,
        rolTemporal: temporal,
        turno: this.turno().unidad,
        estado: temporal ? ('Vigente' as const) : ('Sin asignación' as const),
      };
      if (indice < 0) {
        if (!temporal) {
          return lista;
        }
        const id = Math.max(0, ...lista.map((a) => a.id)) + 1;
        return [...lista, { id, ...siguiente }];
      }
      return lista.map((a, i) => (i === indice ? { ...a, ...siguiente } : a));
    });
  }

  guardarZona(entrada: {
    id?: number;
    nombre: string;
    activa?: boolean;
    mesas: number;
    cajas: number;
    cocinas: number;
  }): void {
    const nombre = normalizarTextoDescriptivo(entrada.nombre);
    if (!nombre) {
      return;
    }
    const mesas = Math.max(0, Math.floor(entrada.mesas) || 0);
    const cajas = Math.max(0, Math.floor(entrada.cajas) || 0);
    const cocinas = Math.max(0, Math.floor(entrada.cocinas) || 0);
    if (entrada.id) {
      this.zonas.update((lista) =>
        lista.map((z) =>
          z.id === entrada.id
            ? { ...z, nombre, mesas, cajas, cocinas, activa: entrada.activa ?? z.activa }
            : z,
        ),
      );
      this.mesas.update((lista) =>
        lista.map((m) => (m.zonaId === entrada.id ? { ...m, zona: nombre } : m)),
      );
      return;
    }
    const id = Math.max(0, ...this.zonas().map((z) => z.id)) + 1;
    this.zonas.update((lista) => [
      ...lista,
      { id, nombre, activa: entrada.activa ?? true, mesas, cajas, cocinas },
    ]);
  }

  alternarZona(zona: ZonaDemo): void {
    if (zona.activa && this.mesas().some((m) => m.zonaId === zona.id && m.activa && m.estadoOp === 'Ocupada')) {
      return;
    }
    this.zonas.update((lista) => lista.map((z) => (z.id === zona.id ? { ...z, activa: !z.activa } : z)));
  }

  eliminarZona(zona: ZonaDemo): void {
    const tieneUso = this.mesas().some((m) => m.zonaId === zona.id);
    if (tieneUso) {
      return;
    }
    this.zonas.update((lista) => lista.filter((z) => z.id !== zona.id));
  }

  guardarMesa(entrada: { id?: number; nombre: string; zonaId: number; activa: boolean }): void {
    const zona = this.zonas().find((z) => z.id === entrada.zonaId);
    if (!zona) {
      return;
    }
    const nombre = normalizarTextoDescriptivo(entrada.nombre);
    if (!nombre) {
      return;
    }
    if (entrada.id) {
      this.mesas.update((lista) =>
        lista.map((m) =>
          m.id === entrada.id ? { ...m, nombre, zonaId: zona.id, zona: zona.nombre, activa: entrada.activa } : m,
        ),
      );
      return;
    }
    const id = Math.max(0, ...this.mesas().map((m) => m.id)) + 1;
    const nueva: MesaDemo = {
      id,
      nombre,
      zonaId: zona.id,
      zona: zona.nombre,
      activa: entrada.activa,
      estadoOp: 'Libre',
      atencion: null,
      mesero: null,
      tiempo: null,
    };
    this.mesas.update((lista) => [...lista, nueva]);
  }

  alternarMesa(mesa: MesaDemo): void {
    this.mesas.update((lista) => lista.map((m) => (m.id === mesa.id ? { ...m, activa: !m.activa } : m)));
  }
}

function horaActual(ahora: Date): string {
  return `${String(ahora.getHours()).padStart(2, '0')}:${String(ahora.getMinutes()).padStart(2, '0')}`;
}
