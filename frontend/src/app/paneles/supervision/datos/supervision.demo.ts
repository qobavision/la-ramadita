import { computed, Injectable, signal } from '@angular/core';
import {
  asignacionesIniciales,
  cartaInicial,
  comprasIniciales,
  horariosBaseIniciales,
  insumosIniciales,
  mesasIniciales,
  movimientosIniciales,
  pedidosIniciales,
  pendientesCierreIniciales,
  personalInicial,
  productosIniciales,
  proveedoresIniciales,
  recetasIniciales,
  turnoInicial,
  zonasIniciales,
} from './supervision.mocks';
import {
  CompraDemo,
  DisponibilidadCarta,
  estadoCompraDe,
  estadoStockDe,
  LineaCompra,
  MesaDemo,
  pendienteDe,
  ProductoDemo,
  ProveedorDemo,
  RecetaDemo,
  RolTemporal,
  UnidadInsumo,
  ZonaDemo,
} from './supervision.models';

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
  readonly proveedores = signal(proveedoresIniciales());
  readonly compras = signal(comprasIniciales());
  readonly insumos = signal(insumosIniciales());
  readonly movimientos = signal(movimientosIniciales());
  readonly recetas = signal(recetasIniciales());
  readonly productos = signal(productosIniciales());
  readonly carta = signal(cartaInicial());

  readonly mesasLibres = computed(() => this.mesas().filter((m) => m.activa && m.estadoOp === 'Libre').length);
  readonly mesasOcupadas = computed(() => this.mesas().filter((m) => m.activa && m.estadoOp === 'Ocupada').length);
  readonly pedidosEnCurso = computed(() => this.pedidos().filter((p) => p.estado === 'En curso' || p.estado === 'Pendiente cocina').length);
  readonly pedidosCocina = computed(() => this.pedidos().filter((p) => p.estado === 'Pendiente cocina').length);
  readonly pedidosPreparados = computed(() => this.pedidos().filter((p) => p.estado === 'Preparado').length);
  readonly atencionesAbiertas = computed(() => this.mesas().filter((m) => m.atencion).length);
  readonly comprasPorRecibir = computed(
    () => this.compras().filter((c) => estadoCompraDe(c) !== 'Recibida').length,
  );
  readonly productosAgotados = computed(() => this.carta().filter((c) => c.disponibilidad === 'Agotado').length);
  readonly stockBajo = computed(
    () => this.insumos().filter((i) => estadoStockDe(i.stock, i.minimo) !== 'NORMAL').length,
  );
  readonly personalActivo = computed(() => this.personal().length);
  readonly proveedoresActivos = computed(() => this.proveedores().filter((p) => p.activo));

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
    if (entrada.id) {
      this.zonas.update((lista) =>
        lista.map((z) => (z.id === entrada.id ? { ...z, nombre: entrada.nombre, activa: entrada.activa } : z)),
      );
      this.mesas.update((lista) =>
        lista.map((m) => (m.zonaId === entrada.id ? { ...m, zona: entrada.nombre } : m)),
      );
      return;
    }
    const id = Math.max(0, ...this.zonas().map((z) => z.id)) + 1;
    this.zonas.update((lista) => [...lista, { id, nombre: entrada.nombre, activa: entrada.activa }]);
  }

  alternarZona(zona: ZonaDemo): void {
    this.zonas.update((lista) => lista.map((z) => (z.id === zona.id ? { ...z, activa: !z.activa } : z)));
  }

  guardarMesa(entrada: { id?: number; nombre: string; zonaId: number; activa: boolean }): void {
    const zona = this.zonas().find((z) => z.id === entrada.zonaId);
    if (!zona) {
      return;
    }
    if (entrada.id) {
      this.mesas.update((lista) =>
        lista.map((m) =>
          m.id === entrada.id ? { ...m, nombre: entrada.nombre, zonaId: zona.id, zona: zona.nombre, activa: entrada.activa } : m,
        ),
      );
      return;
    }
    const id = Math.max(0, ...this.mesas().map((m) => m.id)) + 1;
    const nueva: MesaDemo = {
      id,
      nombre: entrada.nombre,
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

  guardarInsumo(entrada: {
    id?: number;
    nombre: string;
    unidad: UnidadInsumo;
    stock: number;
    minimo: number;
  }): string | null {
    const nombre = entrada.nombre.trim();
    if (!nombre) {
      return 'Ingresa el nombre del insumo.';
    }
    const stock = Math.max(0, entrada.stock);
    const minimo = Math.max(0, entrada.minimo);
    if (entrada.id) {
      this.insumos.update((lista) =>
        lista.map((i) =>
          i.id === entrada.id ? { ...i, nombre, unidad: entrada.unidad, stock, minimo } : i,
        ),
      );
      return null;
    }
    const id = Math.max(0, ...this.insumos().map((i) => i.id)) + 1;
    this.insumos.update((lista) => [...lista, { id, nombre, unidad: entrada.unidad, stock, minimo }]);
    return null;
  }

  guardarProveedor(entrada: Omit<ProveedorDemo, 'id'> & { id?: number }): string | null {
    const nombre = entrada.nombre.trim();
    if (!nombre) {
      return 'Ingresa el nombre o razón social.';
    }
    const documento = entrada.documento.trim();
    if (!documento) {
      return 'Ingresa el número de documento.';
    }
    if (entrada.id) {
      this.proveedores.update((lista) =>
        lista.map((p) => (p.id === entrada.id ? { ...p, ...entrada, id: entrada.id, nombre, documento } : p)),
      );
      return null;
    }
    const id = Math.max(0, ...this.proveedores().map((p) => p.id)) + 1;
    this.proveedores.update((lista) => [...lista, { ...entrada, id, nombre, documento }]);
    return null;
  }

  alternarProveedor(fila: ProveedorDemo): void {
    this.proveedores.update((lista) => lista.map((p) => (p.id === fila.id ? { ...p, activo: !p.activo } : p)));
  }

  guardarCompra(entrada: {
    id?: string;
    proveedorId: number;
    fecha: string;
    lineas: Omit<LineaCompra, 'id' | 'recibido' | 'insumo' | 'unidad'>[];
  }): string | null {
    const proveedor = this.proveedores().find((p) => p.id === entrada.proveedorId && p.activo);
    if (!proveedor) {
      return 'Selecciona un proveedor activo.';
    }
    if (entrada.lineas.length === 0) {
      return 'Agrega al menos un insumo.';
    }
    const lineas: LineaCompra[] = [];
    for (const [indice, linea] of entrada.lineas.entries()) {
      const insumo = this.insumos().find((i) => i.id === linea.insumoId);
      if (!insumo) {
        return 'Selecciona un insumo registrado.';
      }
      if (linea.cantidad <= 0) {
        return 'La cantidad debe ser mayor a 0.';
      }
      if (linea.precioUnitario < 0) {
        return 'El precio no puede ser negativo.';
      }
      if (lineas.some((l) => l.insumoId === insumo.id)) {
        return 'No repitas el mismo insumo en la compra.';
      }
      lineas.push({
        id: indice + 1,
        insumoId: insumo.id,
        insumo: insumo.nombre,
        unidad: insumo.unidad,
        cantidad: linea.cantidad,
        precioUnitario: linea.precioUnitario,
        recibido: 0,
      });
    }
    if (entrada.id) {
      const actual = this.compras().find((c) => c.id === entrada.id);
      if (!actual) {
        return 'No se encontró la compra.';
      }
      if (estadoCompraDe(actual) !== 'Pendiente') {
        return 'Solo se puede editar una compra pendiente.';
      }
      this.compras.update((lista) =>
        lista.map((c) =>
          c.id === entrada.id
            ? { ...c, proveedorId: proveedor.id, proveedor: proveedor.nombre, fecha: entrada.fecha, lineas }
            : c,
        ),
      );
      return null;
    }
    const siguiente = this.compras().reduce((max, c) => {
      const n = Number(c.id.replace('COMP-', ''));
      return Number.isFinite(n) ? Math.max(max, n) : max;
    }, 0) + 1;
    const codigo = `COMP-${String(siguiente).padStart(3, '0')}`;
    this.compras.update((lista) => [
      {
        id: codigo,
        proveedorId: proveedor.id,
        proveedor: proveedor.nombre,
        fecha: entrada.fecha,
        lineas,
      },
      ...lista,
    ]);
    return null;
  }

  recibirLinea(compraId: string, lineaId: number, cantidad: number): string | null {
    const compra = this.compras().find((c) => c.id === compraId);
    if (!compra) {
      return 'No se encontró la compra.';
    }
    const linea = compra.lineas.find((l) => l.id === lineaId);
    if (!linea) {
      return 'No se encontró el insumo de la compra.';
    }
    const pendiente = pendienteDe(linea);
    if (cantidad <= 0) {
      return 'Ingresa una cantidad mayor a 0.';
    }
    if (cantidad > pendiente) {
      return `No puedes recibir más de ${pendiente} pendientes.`;
    }
    this.compras.update((lista) =>
      lista.map((c) =>
        c.id !== compraId
          ? c
          : {
              ...c,
              lineas: c.lineas.map((l) =>
                l.id === lineaId ? { ...l, recibido: Number((l.recibido + cantidad).toFixed(3)) } : l,
              ),
            },
      ),
    );
    this.insumos.update((lista) =>
      lista.map((i) =>
        i.id === linea.insumoId ? { ...i, stock: Number((i.stock + cantidad).toFixed(3)) } : i,
      ),
    );
    return null;
  }

  guardarProducto(entrada: Omit<ProductoDemo, 'id'> & { id?: number }): void {
    if (entrada.id) {
      this.productos.update((lista) => lista.map((p) => (p.id === entrada.id ? { ...p, ...entrada, id: entrada.id } : p)));
      return;
    }
    const id = Math.max(0, ...this.productos().map((p) => p.id)) + 1;
    this.productos.update((lista) => [...lista, { ...entrada, id }]);
  }

  marcarCarta(id: number, disponibilidad: DisponibilidadCarta): void {
    this.carta.update((lista) => lista.map((c) => (c.id === id ? { ...c, disponibilidad } : c)));
  }

  recetaDe(id: number): RecetaDemo | undefined {
    return this.recetas().find((r) => r.id === id);
  }
}
