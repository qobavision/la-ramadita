import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Dialogo } from '../../../shared/ui/dialogo/dialogo';
import { EncabezadoPagina } from '../../../shared/ui/encabezado-pagina/encabezado-pagina';
import { Icono } from '../../../shared/ui/icono/icono';
import { OpcionSegmento, Segmento } from '../../../shared/ui/segmento/segmento';
import { ColumnaTabla, TablaListado } from '../../../shared/ui/tabla-listado/tabla-listado';
import { SupervisionDemo } from '../datos/supervision.demo';
import {
  InsumoDemo,
  RecetaDemo,
  UNIDADES_INSUMO,
  UnidadInsumo,
  claseEstadoStock,
  estadoStockDe,
  formatoCantidad,
} from '../datos/supervision.models';

type VistaInventario = 'insumos' | 'recetas' | 'movimientos';

@Component({
  selector: 'app-supervision-inventario',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, EncabezadoPagina, Icono, Segmento, TablaListado, Dialogo],
  templateUrl: './supervision-inventario.html',
})
export class SupervisionInventario {
  private readonly demo = inject(SupervisionDemo);

  protected readonly vista = signal<VistaInventario>('insumos');
  protected readonly insumos = this.demo.insumos;
  protected readonly movimientos = this.demo.movimientos;
  protected readonly recetas = this.demo.recetas;
  protected readonly unidades = UNIDADES_INSUMO;
  protected readonly claseStock = claseEstadoStock;
  protected readonly estadoDe = estadoStockDe;
  protected readonly formatoCantidad = formatoCantidad;
  protected readonly recetaSel = signal<RecetaDemo | null>(null);
  protected readonly busqueda = signal('');
  protected readonly dialogo = signal(false);
  protected readonly editando = signal<InsumoDemo | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly nombre = signal('');
  protected readonly unidad = signal<UnidadInsumo>('Unidad');
  protected readonly stock = signal<number | null>(0);
  protected readonly minimo = signal<number | null>(0);

  protected readonly vistas: readonly OpcionSegmento<VistaInventario>[] = [
    { valor: 'insumos', etiqueta: 'Insumos' },
    { valor: 'recetas', etiqueta: 'Recetas' },
    { valor: 'movimientos', etiqueta: 'Movimientos' },
  ];

  protected readonly filtrados = computed(() => {
    const texto = this.busqueda().trim().toLowerCase();
    return this.insumos().filter((i) => !texto || i.nombre.toLowerCase().includes(texto) || i.unidad.toLowerCase().includes(texto));
  });

  protected readonly vacioPorFiltro = computed(() => this.insumos().length > 0 && this.filtrados().length === 0);

  protected readonly columnasInsumos: readonly ColumnaTabla[] = [
    { titulo: 'Insumo' },
    { titulo: 'Unidad' },
    { titulo: 'Stock actual', numerica: true },
    { titulo: 'Estado' },
    { titulo: 'Acciones', numerica: true },
  ];

  protected readonly columnasMov: readonly ColumnaTabla[] = [
    { titulo: 'Fecha' },
    { titulo: 'Insumo' },
    { titulo: 'Tipo' },
    { titulo: 'Cantidad', numerica: true },
    { titulo: 'Motivo' },
    { titulo: 'Responsable' },
  ];

  protected readonly columnasRecetas: readonly ColumnaTabla[] = [
    { titulo: 'Producto' },
    { titulo: 'Insumos asociados', numerica: true },
    { titulo: 'Estado' },
    { titulo: 'Acciones', numerica: true },
  ];

  protected actualizarBusqueda(evento: Event): void {
    this.busqueda.set((evento.target as HTMLInputElement).value);
  }

  protected abrirInsumo(insumo?: InsumoDemo): void {
    this.editando.set(insumo ?? null);
    this.nombre.set(insumo?.nombre ?? '');
    this.unidad.set(insumo?.unidad ?? 'Unidad');
    this.stock.set(insumo?.stock ?? 0);
    this.minimo.set(insumo?.minimo ?? 0);
    this.error.set(null);
    this.dialogo.set(true);
  }

  protected guardarInsumo(): void {
    const mensaje = this.demo.guardarInsumo({
      id: this.editando()?.id,
      nombre: this.nombre(),
      unidad: this.unidad(),
      stock: Number(this.stock() ?? 0),
      minimo: Number(this.minimo() ?? 0),
    });
    if (mensaje) {
      this.error.set(mensaje);
      return;
    }
    this.dialogo.set(false);
  }

  protected ver(receta: RecetaDemo): void {
    this.recetaSel.set(receta);
  }
}
