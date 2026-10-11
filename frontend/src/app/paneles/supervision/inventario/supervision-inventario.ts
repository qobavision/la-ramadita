import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { mensajeDeError } from '../../../core/errores/api-error';
import {
  FiltroEstadoInsumo,
  etiquetaEstadoOperativo,
  muestraEstadoStock,
} from '../../../inventario/inventario.models';
import { InsumosService } from '../../../inventario/insumos.service';
import { MovimientosService } from '../../../inventario/movimientos.service';
import { Dialogo } from '../../../shared/ui/dialogo/dialogo';
import { EncabezadoPagina } from '../../../shared/ui/encabezado-pagina/encabezado-pagina';
import { EstadoVacio } from '../../../shared/ui/estado-vacio/estado-vacio';
import { Icono } from '../../../shared/ui/icono/icono';
import { OpcionSegmento, Segmento } from '../../../shared/ui/segmento/segmento';
import { ColumnaTabla, TablaListado } from '../../../shared/ui/tabla-listado/tabla-listado';
import { SupervisionDemo } from '../datos/supervision.demo';
import {
  InsumoDemo,
  RecetaDemo,
  RecetaInsumo,
  UNIDADES_INSUMO,
  UnidadInsumo,
  abreviarUnidad,
  claseEstadoStock,
  estadoStockDe,
  formatoCantidad,
} from '../datos/supervision.models';

type VistaInventario = 'insumos' | 'recetas' | 'movimientos';
type DialogoInventario = 'insumo' | 'receta' | 'eliminar-insumo' | 'eliminar-receta' | null;

interface LineaRecetaForm {
  clave: number;
  insumoId: number;
  cantidad: number | null;
}

const INSUMOS_EN_CARD = 3;

@Component({
  selector: 'app-supervision-inventario',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, EncabezadoPagina, Icono, Segmento, TablaListado, Dialogo, EstadoVacio],
  templateUrl: './supervision-inventario.html',
})
export class SupervisionInventario {
  private readonly demo = inject(SupervisionDemo);
  private readonly insumosApi = inject(InsumosService);
  private readonly movimientosApi = inject(MovimientosService);
  private siguienteClave = 1;

  protected readonly vista = signal<VistaInventario>('insumos');
  protected readonly insumos = this.insumosApi.lista;
  protected readonly movimientos = this.movimientosApi.lista;
  protected readonly recetas = this.demo.recetas;
  protected readonly unidades = UNIDADES_INSUMO;
  protected readonly claseStock = claseEstadoStock;
  protected readonly estadoDe = estadoStockDe;
  protected readonly formatoCantidad = formatoCantidad;
  protected readonly unidadCorta = abreviarUnidad;
  protected readonly busqueda = signal('');
  protected readonly filtroEstado = signal<FiltroEstadoInsumo>('ACTIVOS');
  protected readonly busquedaReceta = signal('');
  protected readonly etiquetaEstado = etiquetaEstadoOperativo;
  protected readonly muestraStock = muestraEstadoStock;
  protected readonly dialogo = signal<DialogoInventario>(null);
  protected readonly editando = signal<InsumoDemo | null>(null);
  protected readonly recetaEditando = signal<RecetaDemo | null>(null);
  protected readonly recetaBorrar = signal<RecetaDemo | null>(null);
  protected readonly insumoBorrar = signal<InsumoDemo | null>(null);
  protected readonly recetaAbierta = signal<number | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly enviando = signal(false);
  protected readonly nombre = signal('');
  protected readonly unidad = signal<UnidadInsumo>('Unidad');
  protected readonly stock = signal<number | null>(0);
  protected readonly minimo = signal<number | null>(0);
  protected readonly recetaNombre = signal('');
  protected readonly lineasReceta = signal<LineaRecetaForm[]>([]);

  protected readonly vistas: readonly OpcionSegmento<VistaInventario>[] = [
    { valor: 'insumos', etiqueta: 'Insumos' },
    { valor: 'recetas', etiqueta: 'Recetas' },
    { valor: 'movimientos', etiqueta: 'Movimientos' },
  ];

  protected readonly filtrosEstado: readonly OpcionSegmento<FiltroEstadoInsumo>[] = [
    { valor: 'ACTIVOS', etiqueta: 'Activos' },
    { valor: 'INACTIVOS', etiqueta: 'Inactivos' },
    { valor: 'TODOS', etiqueta: 'Todos' },
  ];

  protected readonly filtrados = computed(() => this.insumos());

  protected readonly vacioPorFiltro = computed(() => this.busqueda().trim().length > 0 && this.filtrados().length === 0);

  protected readonly vacioInactivos = computed(
    () => this.filtroEstado() === 'INACTIVOS' && this.busqueda().trim().length === 0 && this.filtrados().length === 0,
  );

  protected readonly insumosActivos = computed(() => this.insumos().filter((i) => i.activo !== false));

  protected readonly recetasFiltradas = computed(() => {
    const texto = this.busquedaReceta().trim().toLowerCase();
    return this.recetas().filter(
      (r) =>
        !texto ||
        r.nombre.toLowerCase().includes(texto) ||
        r.insumos.some((i) => i.insumo.toLowerCase().includes(texto)),
    );
  });

  protected readonly vacioRecetaPorFiltro = computed(
    () => this.recetas().length > 0 && this.recetasFiltradas().length === 0,
  );

  protected readonly columnasInsumos: readonly ColumnaTabla[] = [
    { titulo: 'Insumo' },
    { titulo: 'Unidad' },
    { titulo: 'Stock actual', numerica: true },
    { titulo: 'Estado' },
    { titulo: 'Acciones', numerica: true },
  ];

  protected readonly columnasMov: readonly ColumnaTabla[] = [
    { titulo: 'Fecha/hora' },
    { titulo: 'Insumo' },
    { titulo: 'Movimiento' },
    { titulo: 'Cantidad', numerica: true },
    { titulo: 'Stock resultante', numerica: true },
    { titulo: 'Origen' },
  ];

  constructor() {
    this.recargarInsumos();
    this.movimientosApi.cargar().subscribe({ error: () => undefined });
  }

  protected actualizarBusqueda(evento: Event): void {
    this.busqueda.set((evento.target as HTMLInputElement).value);
    this.recargarInsumos();
  }

  protected cambiarFiltro(estado: FiltroEstadoInsumo): void {
    this.filtroEstado.set(estado);
    this.error.set(null);
    this.recargarInsumos();
  }

  protected cambiarVista(vista: VistaInventario): void {
    this.vista.set(vista);
    if (vista === 'insumos') {
      this.recargarInsumos();
    } else if (vista === 'recetas') {
      this.insumosApi.cargar().subscribe({ error: () => undefined });
    }
  }

  protected actualizarBusquedaReceta(evento: Event): void {
    this.busquedaReceta.set((evento.target as HTMLInputElement).value);
  }

  protected abrirInsumo(insumo?: InsumoDemo): void {
    this.editando.set(insumo ?? null);
    this.nombre.set(insumo?.nombre ?? '');
    this.unidad.set(insumo?.unidad ?? 'Unidad');
    this.stock.set(insumo?.stock ?? 0);
    this.minimo.set(insumo?.minimo ?? 0);
    this.error.set(null);
    this.dialogo.set('insumo');
  }

  protected guardarInsumo(): void {
    this.enviando.set(true);
    this.error.set(null);
    const actual = this.editando();
    const peticion = actual
      ? this.insumosApi.actualizar(actual.id, {
          nombre: this.nombre(),
          unidad: this.unidad(),
          minimo: Number(this.minimo() ?? 0),
        })
      : this.insumosApi.crear({
          nombre: this.nombre(),
          unidad: this.unidad(),
          stock: Number(this.stock() ?? 0),
          minimo: Number(this.minimo() ?? 0),
        });
    peticion.subscribe({
      next: () => {
        this.enviando.set(false);
        this.cerrar();
      },
      error: (error) => {
        this.enviando.set(false);
        this.error.set(mensajeDeError(error, 'No se pudo guardar el insumo.'));
      },
    });
  }

  protected abrirReceta(receta?: RecetaDemo): void {
    this.recetaEditando.set(receta ?? null);
    this.recetaNombre.set(receta?.nombre ?? '');
    this.lineasReceta.set(
      receta
        ? receta.insumos.map((l) => ({
            clave: this.siguienteClave++,
            insumoId: l.insumoId,
            cantidad: l.cantidad,
          }))
        : [this.lineaRecetaVacia()],
    );
    this.error.set(null);
    this.dialogo.set('receta');
  }

  protected agregarLineaReceta(): void {
    this.lineasReceta.update((lista) => [...lista, this.lineaRecetaVacia()]);
  }

  protected quitarLineaReceta(clave: number): void {
    this.lineasReceta.update((lista) => (lista.length <= 1 ? lista : lista.filter((l) => l.clave !== clave)));
  }

  protected cambiarLineaReceta(clave: number, campo: 'insumoId' | 'cantidad', valor: number): void {
    this.lineasReceta.update((lista) => lista.map((l) => (l.clave === clave ? { ...l, [campo]: valor } : l)));
  }

  protected insumoDe(id: number): InsumoDemo | undefined {
    return this.insumosActivos().find((i) => i.id === id) ?? this.insumos().find((i) => i.id === id);
  }

  protected reactivarInsumo(insumo: InsumoDemo): void {
    this.enviando.set(true);
    this.error.set(null);
    this.insumosApi.reactivar(insumo.id).subscribe({
      next: () => this.enviando.set(false),
      error: (error) => {
        this.enviando.set(false);
        this.error.set(mensajeDeError(error, 'No se pudo reactivar el insumo.'));
      },
    });
  }

  protected guardarReceta(): void {
    const mensaje = this.demo.guardarReceta({
      id: this.recetaEditando()?.id,
      nombre: this.recetaNombre(),
      lineas: this.lineasReceta()
        .filter((l) => l.insumoId)
        .map((l) => ({
          insumoId: l.insumoId,
          cantidad: Number(l.cantidad ?? 0),
        })),
    });
    if (mensaje) {
      this.error.set(mensaje);
      return;
    }
    this.cerrar();
  }

  protected pedirEliminarInsumo(insumo: InsumoDemo): void {
    this.insumoBorrar.set(insumo);
    this.error.set(null);
    this.dialogo.set('eliminar-insumo');
  }

  protected confirmarEliminarInsumo(): void {
    const insumo = this.insumoBorrar();
    if (!insumo) {
      return;
    }
    this.enviando.set(true);
    this.insumosApi.eliminar(insumo.id).subscribe({
      next: () => {
        this.demo.quitarInsumoDeRecetas(insumo.id);
        this.enviando.set(false);
        this.cerrar();
      },
      error: (error) => {
        this.enviando.set(false);
        this.error.set(mensajeDeError(error, 'No se pudo eliminar el insumo.'));
      },
    });
  }

  protected pedirEliminar(receta: RecetaDemo): void {
    this.recetaBorrar.set(receta);
    this.dialogo.set('eliminar-receta');
  }

  protected confirmarEliminar(): void {
    const receta = this.recetaBorrar();
    if (!receta) {
      return;
    }
    this.demo.eliminarReceta(receta.id);
    if (this.recetaAbierta() === receta.id) {
      this.recetaAbierta.set(null);
    }
    this.cerrar();
  }

  protected insumosDeCard(receta: RecetaDemo): RecetaInsumo[] {
    if (this.recetaAbierta() === receta.id) {
      return receta.insumos;
    }
    return receta.insumos.slice(0, INSUMOS_EN_CARD);
  }

  protected extrasDe(receta: RecetaDemo): number {
    if (this.recetaAbierta() === receta.id) {
      return 0;
    }
    return Math.max(0, receta.insumos.length - INSUMOS_EN_CARD);
  }

  protected alternarIngredientes(id: number): void {
    this.recetaAbierta.update((actual) => (actual === id ? null : id));
  }

  protected cerrar(): void {
    this.dialogo.set(null);
    this.editando.set(null);
    this.recetaEditando.set(null);
    this.recetaBorrar.set(null);
    this.insumoBorrar.set(null);
    this.error.set(null);
    this.enviando.set(false);
  }

  private recargarInsumos(): void {
    this.insumosApi.cargar(this.busqueda(), this.filtroEstado()).subscribe({ error: () => undefined });
  }

  private lineaRecetaVacia(): LineaRecetaForm {
    return { clave: this.siguienteClave++, insumoId: 0, cantidad: null };
  }
}
