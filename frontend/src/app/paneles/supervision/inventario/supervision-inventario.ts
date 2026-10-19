import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { mensajeDeError } from '../../../core/errores/api-error';
import {
  FiltroEstadoInsumo,
  convertirCantidadUnidad,
  etiquetaEstadoOperativo,
  factorConversionUnidad,
  mensajeUnidadIncompatible,
  muestraEstadoStock,
} from '../../../inventario/inventario.models';
import { RecetasService } from '../../../carta/recetas.service';
import { ProductosService } from '../../../carta/productos.service';
import { InsumosService } from '../../../inventario/insumos.service';
import { MovimientosService } from '../../../inventario/movimientos.service';
import { Dialogo } from '../../../shared/ui/dialogo/dialogo';
import { EncabezadoPagina } from '../../../shared/ui/encabezado-pagina/encabezado-pagina';
import { Icono } from '../../../shared/ui/icono/icono';
import { OpcionSegmento, Segmento } from '../../../shared/ui/segmento/segmento';
import { ColumnaTabla, TablaListado } from '../../../shared/ui/tabla-listado/tabla-listado';
import {
  AmbitoProducto,
  CATEGORIAS_RECETA,
  CategoriaProducto,
  InsumoDemo,
  Producto,
  RecetaDemo,
  RecetaInsumo,
  UNIDADES_INSUMO,
  UnidadInsumo,
  abreviarUnidad,
  claseEstadoStock,
  estadoStockDe,
  formatoCantidad,
} from '../datos/supervision.models';

type VistaInventario = 'insumos' | 'recetas' | 'combos' | 'movimientos';
type DialogoInventario = 'insumo' | 'receta' | 'combo' | 'eliminar-insumo' | 'eliminar-receta' | 'eliminar-combo' | null;

interface LineaRecetaForm {
  clave: number;
  insumoId: number;
  cantidad: number | null;
}

interface LineaComboForm {
  clave: number;
  productoId: number;
  cantidad: number | null;
}

const INSUMOS_EN_TABLA = 2;

@Component({
  selector: 'app-supervision-inventario',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, EncabezadoPagina, Icono, Segmento, TablaListado, Dialogo],
  templateUrl: './supervision-inventario.html',
})
export class SupervisionInventario {
  private readonly insumosApi = inject(InsumosService);
  private readonly movimientosApi = inject(MovimientosService);
  private readonly recetasApi = inject(RecetasService);
  private readonly productosApi = inject(ProductosService);
  private siguienteClave = 1;

  protected readonly vista = signal<VistaInventario>('insumos');
  protected readonly insumos = this.insumosApi.lista;
  protected readonly movimientos = this.movimientosApi.lista;
  protected readonly recetas = this.recetasApi.lista;
  protected readonly productos = this.productosApi.lista;
  protected readonly unidades = UNIDADES_INSUMO;
  protected readonly ambitos: readonly { valor: AmbitoProducto; etiqueta: string }[] = [
    { valor: 'RESTAURANTE', etiqueta: 'Restaurante' },
    { valor: 'POLLERIA', etiqueta: 'Pollería' },
    { valor: 'AMBOS', etiqueta: 'Ambos' },
  ];
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
  protected readonly comboEditando = signal<Producto | null>(null);
  protected readonly comboBorrar = signal<Producto | null>(null);
  protected readonly comboNombre = signal('');
  protected readonly comboAmbito = signal<AmbitoProducto>('POLLERIA');
  protected readonly lineasCombo = signal<LineaComboForm[]>([]);
  protected readonly busquedaCombo = signal('');
  protected readonly insumoBorrar = signal<InsumoDemo | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly enviando = signal(false);
  protected readonly nombre = signal('');
  protected readonly unidad = signal<UnidadInsumo>('Unidad');
  protected readonly stock = signal<number | null>(0);
  protected readonly minimo = signal<number | null>(0);
  protected readonly recetaNombre = signal('');
  protected readonly recetaCategoria = signal<CategoriaProducto>('Entrada');
  protected readonly recetaAmbito = signal<AmbitoProducto>('RESTAURANTE');
  protected readonly lineasReceta = signal<LineaRecetaForm[]>([]);

  protected readonly vistas: readonly OpcionSegmento<VistaInventario>[] = [
    { valor: 'insumos', etiqueta: 'Insumos' },
    { valor: 'recetas', etiqueta: 'Recetas' },
    { valor: 'combos', etiqueta: 'Combos' },
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

  protected readonly categoriasReceta = computed(() => {
    const actual = this.recetaCategoria();
    return CATEGORIAS_RECETA.includes(actual) ? CATEGORIAS_RECETA : [...CATEGORIAS_RECETA, actual];
  });

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

  protected readonly combos = computed(() =>
    this.productos().filter((p) => p.activo && p.categoria === 'Combo' && (p.componentes?.length ?? 0) > 0),
  );

  protected readonly combosFiltrados = computed(() => {
    const texto = this.busquedaCombo().trim().toLowerCase();
    return this.combos().filter(
      (c) =>
        !texto ||
        c.nombre.toLowerCase().includes(texto) ||
        (c.componentes ?? []).some((p) => p.nombre.toLowerCase().includes(texto)),
    );
  });

  protected readonly vacioComboPorFiltro = computed(
    () => this.combos().length > 0 && this.combosFiltrados().length === 0,
  );

  protected readonly recetasParaCombo = computed(() => this.recetas().filter((r) => r.insumos.length > 0));

  protected readonly columnasInsumos: readonly ColumnaTabla[] = [
    { titulo: 'Insumo' },
    { titulo: 'Unidad' },
    { titulo: 'Stock actual', numerica: true },
    { titulo: 'Estado' },
    { titulo: 'Acciones', numerica: true },
  ];

  protected readonly columnasRecetas: readonly ColumnaTabla[] = [
    { titulo: 'Receta' },
    { titulo: 'Categoría' },
    { titulo: 'Ámbito' },
    { titulo: 'Insumos' },
    { titulo: 'Acciones', numerica: true },
  ];

  protected readonly columnasCombos: readonly ColumnaTabla[] = [
    { titulo: 'Combo' },
    { titulo: 'Ámbito' },
    { titulo: 'Recetas' },
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
    this.recetasApi.cargar().subscribe({ error: () => undefined });
    this.productosApi.cargar().subscribe({ error: () => undefined });
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
      this.recetasApi.cargar().subscribe({ error: (error) => this.error.set(mensajeDeError(error, 'No se pudieron cargar las recetas.')) });
    } else if (vista === 'combos') {
      this.recetasApi.cargar().subscribe({ error: () => undefined });
      this.productosApi.cargar().subscribe({ error: (error) => this.error.set(mensajeDeError(error, 'No se pudieron cargar los combos.')) });
    }
  }

  protected actualizarBusquedaReceta(evento: Event): void {
    this.busquedaReceta.set((evento.target as HTMLInputElement).value);
  }

  protected actualizarBusquedaCombo(evento: Event): void {
    this.busquedaCombo.set((evento.target as HTMLInputElement).value);
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

  protected cambiarUnidadInsumo(nueva: UnidadInsumo): void {
    const anterior = this.unidad();
    if (nueva === anterior) {
      return;
    }
    if (!this.editando()) {
      this.error.set(null);
      this.unidad.set(nueva);
      return;
    }
    const factor = factorConversionUnidad(anterior, nueva);
    if (factor == null) {
      this.error.set(mensajeUnidadIncompatible(anterior, nueva));
      return;
    }
    this.error.set(null);
    this.unidad.set(nueva);
    if (factor !== 1) {
      this.minimo.set(convertirCantidadUnidad(Number(this.minimo() ?? 0), factor));
      this.stock.set(convertirCantidadUnidad(Number(this.stock() ?? 0), factor));
    }
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
        this.recetasApi.cargar().subscribe({ error: () => undefined });
        this.cerrar();
      },
      error: (error) => {
        this.enviando.set(false);
        this.error.set(mensajeDeError(error, 'No se pudo guardar el insumo.'));
      },
    });
  }

  protected abrirReceta(receta?: RecetaDemo): void {
    this.insumosApi.cargar().subscribe({ error: () => undefined });
    const actual = receta ? this.recetas().find((r) => r.id === receta.id) ?? receta : undefined;
    this.recetaEditando.set(actual ?? null);
    this.recetaNombre.set(actual?.nombre ?? '');
    this.recetaCategoria.set(actual?.categoria ?? 'Entrada');
    this.recetaAmbito.set(actual?.ambito ?? 'RESTAURANTE');
    this.lineasReceta.set(
      actual
        ? actual.insumos.map((l) => ({
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
    const lineas = this.lineasReceta().filter((l) => l.insumoId);
    const ids = lineas.map((l) => l.insumoId);
    if (lineas.length === 0) {
      this.error.set('Agrega al menos un insumo.');
      return;
    }
    if (new Set(ids).size !== ids.length) {
      this.error.set('No repitas el mismo insumo en la receta.');
      return;
    }
    if (lineas.some((l) => Number(l.cantidad ?? 0) <= 0)) {
      this.error.set('Indica una cantidad mayor a 0 en cada insumo.');
      return;
    }
    this.enviando.set(true);
    this.error.set(null);
    this.recetasApi
      .guardar({
        id: this.recetaEditando()?.id,
        nombre: this.recetaNombre(),
        categoria: this.recetaCategoria(),
        ambito: this.recetaAmbito(),
        precio: this.recetaEditando()?.precio ?? 0,
        lineas: lineas.map((l) => ({
          insumoId: l.insumoId,
          cantidad: Number(l.cantidad),
        })),
      })
      .subscribe({
        next: () => {
          this.enviando.set(false);
          this.cerrar();
        },
        error: (error) => {
          this.enviando.set(false);
          this.error.set(mensajeDeError(error, 'No se pudo guardar la receta.'));
        },
      });
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
        this.recetasApi.cargar().subscribe({ error: () => undefined });
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
    this.enviando.set(true);
    this.recetasApi.eliminar(receta.id).subscribe({
      next: () => {
        this.enviando.set(false);
        this.cerrar();
      },
      error: (error) => {
        this.enviando.set(false);
        this.error.set(mensajeDeError(error, 'No se pudo eliminar la receta.'));
      },
    });
  }

  protected etiquetaAmbito(ambito: AmbitoProducto | undefined): string {
    return this.ambitos.find((a) => a.valor === ambito)?.etiqueta ?? '—';
  }

  protected abrirCombo(combo?: Producto): void {
    this.recetasApi.cargar().subscribe({ error: () => undefined });
    const actual = combo ? this.combos().find((c) => c.id === combo.id) ?? combo : undefined;
    this.comboEditando.set(actual ?? null);
    this.comboNombre.set(actual?.nombre ?? '');
    this.comboAmbito.set(actual?.ambito ?? 'POLLERIA');
    this.lineasCombo.set(
      actual?.componentes?.length
        ? actual.componentes.map((c) => ({
            clave: this.siguienteClave++,
            productoId: c.productoId,
            cantidad: c.cantidad,
          }))
        : [this.lineaComboVacia(), this.lineaComboVacia()],
    );
    this.error.set(null);
    this.dialogo.set('combo');
  }

  protected agregarLineaCombo(): void {
    this.lineasCombo.update((lista) => [...lista, this.lineaComboVacia()]);
  }

  protected quitarLineaCombo(clave: number): void {
    this.lineasCombo.update((lista) => (lista.length <= 2 ? lista : lista.filter((l) => l.clave !== clave)));
  }

  protected cambiarLineaCombo(clave: number, campo: 'productoId' | 'cantidad', valor: number): void {
    this.lineasCombo.update((lista) => lista.map((l) => (l.clave === clave ? { ...l, [campo]: valor } : l)));
  }

  protected recetasComboDisponibles(actualId: number): RecetaDemo[] {
    const tomadas = new Set(this.lineasCombo().filter((l) => l.productoId !== actualId).map((l) => l.productoId));
    return this.recetasParaCombo().filter((r) => !tomadas.has(r.id));
  }

  protected guardarCombo(): void {
    const lineas = this.lineasCombo().filter((l) => l.productoId);
    const ids = lineas.map((l) => l.productoId);
    if (lineas.length < 2) {
      this.error.set('Un combo se arma con al menos dos recetas.');
      return;
    }
    if (new Set(ids).size !== ids.length) {
      this.error.set('No repitas la misma receta en el combo.');
      return;
    }
    if (lineas.some((l) => Number(l.cantidad ?? 0) <= 0)) {
      this.error.set('Indica una cantidad mayor a 0 en cada receta.');
      return;
    }
    this.enviando.set(true);
    this.error.set(null);
    const datos = {
      nombre: this.comboNombre(),
      categoria: 'Combo' as const,
      ambito: this.comboAmbito(),
      precio: this.comboEditando()?.precio ?? 0,
      componentes: lineas.map((l) => ({ productoId: l.productoId, cantidad: Number(l.cantidad) })),
    };
    const actual = this.comboEditando();
    const peticion = actual ? this.productosApi.actualizar(actual.id, datos) : this.productosApi.crear(datos);
    peticion.subscribe({
      next: () => {
        this.enviando.set(false);
        this.productosApi.cargar().subscribe({ error: () => undefined });
        this.cerrar();
      },
      error: (error) => {
        this.enviando.set(false);
        this.error.set(mensajeDeError(error, 'No se pudo guardar el combo.'));
      },
    });
  }

  protected pedirEliminarCombo(combo: Producto): void {
    this.comboBorrar.set(combo);
    this.error.set(null);
    this.dialogo.set('eliminar-combo');
  }

  protected confirmarEliminarCombo(): void {
    const combo = this.comboBorrar();
    if (!combo) {
      return;
    }
    this.enviando.set(true);
    this.productosApi.cambiarActivo(combo.id, false).subscribe({
      next: () => {
        this.enviando.set(false);
        this.cerrar();
      },
      error: (error) => {
        this.enviando.set(false);
        this.error.set(mensajeDeError(error, 'No se pudo eliminar el combo.'));
      },
    });
  }

  protected resumenCombo(combo: Producto): string {
    const piezas = combo.componentes ?? [];
    if (piezas.length === 0) {
      return '—';
    }
    const nombres = piezas.slice(0, INSUMOS_EN_TABLA).map((p) => p.nombre);
    const resto = piezas.length - nombres.length;
    return resto > 0 ? `${nombres.join(' + ')} +${resto}` : nombres.join(' + ');
  }

  protected resumenInsumos(insumos: readonly RecetaInsumo[]): string {
    if (insumos.length === 0) {
      return '—';
    }
    const nombres = insumos.slice(0, INSUMOS_EN_TABLA).map((i) => i.insumo);
    const resto = insumos.length - nombres.length;
    const cantidad = insumos.length === 1 ? '1 insumo' : `${insumos.length} insumos`;
    return resto > 0 ? `${cantidad}: ${nombres.join(', ')} +${resto}` : `${cantidad}: ${nombres.join(', ')}`;
  }

  protected cerrar(): void {
    this.dialogo.set(null);
    this.editando.set(null);
    this.recetaEditando.set(null);
    this.recetaBorrar.set(null);
    this.comboEditando.set(null);
    this.comboBorrar.set(null);
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

  private lineaComboVacia(): LineaComboForm {
    return { clave: this.siguienteClave++, productoId: 0, cantidad: 1 };
  }
}
