import { computed, inject, Injectable, signal } from '@angular/core';
import { InsumosService } from '../../../inventario/insumos.service';
import {
  asignacionesIniciales,
  cartaInicial,
  horariosBaseIniciales,
  mesasIniciales,
  pedidosIniciales,
  pendientesCierreIniciales,
  personalInicial,
  productosIniciales,
  recetasIniciales,
  turnoInicial,
  zonasIniciales,
} from './supervision.mocks';
import {
  DisponibilidadCarta,
  MesaDemo,
  ProductoDemo,
  RecetaDemo,
  RolTemporal,
  ZonaDemo,
} from './supervision.models';
import { normalizarTextoDescriptivo } from '../../../shared/texto/texto-descriptivo';

/**
 * Estado visual del panel Supervisor. Solo vive en memoria del navegador
 * para poder revisar pantallas. Más adelante se sustituye por HTTP.
 */
@Injectable({ providedIn: 'root' })
export class SupervisionDemo {
  private readonly insumosApi = inject(InsumosService);

  readonly horarios = signal(horariosBaseIniciales());
  readonly turno = signal(turnoInicial());
  readonly pendientesCierre = signal(pendientesCierreIniciales());
  readonly personal = signal(personalInicial());
  readonly asignaciones = signal(asignacionesIniciales());
  readonly zonas = signal(zonasIniciales());
  readonly mesas = signal(mesasIniciales());
  readonly pedidos = signal(pedidosIniciales());
  readonly recetas = signal(recetasIniciales());
  readonly productos = signal(productosIniciales());
  readonly carta = signal(cartaInicial());

  readonly mesasLibres = computed(() => this.mesas().filter((m) => m.activa && m.estadoOp === 'Libre').length);
  readonly mesasOcupadas = computed(() => this.mesas().filter((m) => m.activa && m.estadoOp === 'Ocupada').length);
  readonly pedidosEnCurso = computed(() => this.pedidos().filter((p) => p.estado === 'En curso' || p.estado === 'Pendiente cocina').length);
  readonly pedidosCocina = computed(() => this.pedidos().filter((p) => p.estado === 'Pendiente cocina').length);
  readonly pedidosPreparados = computed(() => this.pedidos().filter((p) => p.estado === 'Preparado').length);
  readonly atencionesAbiertas = computed(() => this.mesas().filter((m) => m.atencion).length);
  readonly productosAgotados = computed(() => this.carta().filter((c) => c.disponibilidad === 'Agotado').length);
  readonly personalActivo = computed(() => this.personal().length);

  iniciarTurnoAnticipado(): void {
    const actual = this.turno();
    const siguiente = actual.unidad === 'Pollería' ? this.horarios()[0] : this.horarios()[1];
    this.turno.set({
      unidad: siguiente.unidad,
      horaInicio: siguiente.horaInicio,
      horaFin: siguiente.horaFin,
      estado: 'ACTIVO',
      caja: 'Abierta',
      alerta: `Turno ${siguiente.unidad} iniciado de forma anticipada (simulado).`,
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

  guardarZona(entrada: { id?: number; nombre: string; activa: boolean }): void {
    const nombre = normalizarTextoDescriptivo(entrada.nombre);
    if (!nombre) {
      return;
    }
    if (entrada.id) {
      this.zonas.update((lista) =>
        lista.map((z) => (z.id === entrada.id ? { ...z, nombre, activa: entrada.activa } : z)),
      );
      this.mesas.update((lista) =>
        lista.map((m) => (m.zonaId === entrada.id ? { ...m, zona: nombre } : m)),
      );
      return;
    }
    const id = Math.max(0, ...this.zonas().map((z) => z.id)) + 1;
    this.zonas.update((lista) => [...lista, { id, nombre, activa: entrada.activa }]);
  }

  alternarZona(zona: ZonaDemo): void {
    this.zonas.update((lista) => lista.map((z) => (z.id === zona.id ? { ...z, activa: !z.activa } : z)));
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

  quitarInsumoDeRecetas(id: number): void {
    this.recetas.update((lista) =>
      lista.map((r) => ({
        ...r,
        insumos: r.insumos.filter((l) => l.insumoId !== id),
      })),
    );
  }

  guardarProducto(entrada: Omit<ProductoDemo, 'id'> & { id?: number }): void {
    const nombre = normalizarTextoDescriptivo(entrada.nombre);
    if (!nombre) {
      return;
    }
    if (entrada.id) {
      this.productos.update((lista) =>
        lista.map((p) => (p.id === entrada.id ? { ...p, ...entrada, id: entrada.id, nombre } : p)),
      );
      this.carta.update((lista) =>
        lista.map((c) => (c.productoId === entrada.id ? { ...c, producto: nombre } : c)),
      );
      return;
    }
    const id = Math.max(0, ...this.productos().map((p) => p.id)) + 1;
    this.productos.update((lista) => [...lista, { ...entrada, id, nombre }]);
  }

  marcarCarta(id: number, disponibilidad: DisponibilidadCarta): void {
    this.carta.update((lista) => lista.map((c) => (c.id === id ? { ...c, disponibilidad } : c)));
  }

  recetaDe(id: number): RecetaDemo | undefined {
    return this.recetas().find((r) => r.id === id);
  }

  guardarReceta(entrada: {
    id?: number;
    nombre: string;
    lineas: { insumoId: number; cantidad: number }[];
  }): string | null {
    const nombre = normalizarTextoDescriptivo(entrada.nombre);
    if (!nombre) {
      return 'Ingresa el nombre de la receta.';
    }
    if (entrada.lineas.length === 0) {
      return 'Agrega al menos un insumo.';
    }
    const insumos: RecetaDemo['insumos'] = [];
    for (const linea of entrada.lineas) {
      const insumo = this.insumosApi.lista().find((i) => i.id === linea.insumoId);
      if (!insumo) {
        return 'Selecciona un insumo registrado.';
      }
      if (linea.cantidad <= 0) {
        return 'La cantidad debe ser mayor a 0.';
      }
      if (insumos.some((l) => l.insumoId === insumo.id)) {
        return 'No repitas el mismo insumo en la receta.';
      }
      insumos.push({
        insumoId: insumo.id,
        insumo: insumo.nombre,
        unidad: insumo.unidad,
        cantidad: Number(linea.cantidad.toFixed(3)),
      });
    }
    if (entrada.id) {
      const actual = this.recetas().find((r) => r.id === entrada.id);
      if (!actual) {
        return 'No se encontró la receta.';
      }
      this.recetas.update((lista) =>
        lista.map((r) => (r.id === entrada.id ? { ...r, nombre, insumos } : r)),
      );
      return null;
    }
    const id = Math.max(0, ...this.recetas().map((r) => r.id)) + 1;
    this.recetas.update((lista) => [{ id, nombre, insumos }, ...lista]);
    return null;
  }

  eliminarReceta(id: number): void {
    this.recetas.update((lista) => lista.filter((r) => r.id !== id));
  }
}
