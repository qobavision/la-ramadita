import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Dialogo } from '../../../shared/ui/dialogo/dialogo';
import { EncabezadoPagina } from '../../../shared/ui/encabezado-pagina/encabezado-pagina';
import { Icono } from '../../../shared/ui/icono/icono';
import { OpcionSegmento, Segmento } from '../../../shared/ui/segmento/segmento';
import { ColumnaTabla, TablaListado } from '../../../shared/ui/tabla-listado/tabla-listado';
import { SupervisionDemo } from '../datos/supervision.demo';
import { ProductoDemo, UnidadNegocio } from '../datos/supervision.models';

type VistaCarta = 'productos' | 'carta';

@Component({
  selector: 'app-supervision-carta',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, EncabezadoPagina, Icono, Segmento, TablaListado, Dialogo],
  templateUrl: './supervision-carta.html',
})
export class SupervisionCarta {
  private readonly demo = inject(SupervisionDemo);

  protected readonly vista = signal<VistaCarta>('productos');
  protected readonly unidad = signal<UnidadNegocio | 'todas'>('Pollería');
  protected readonly productos = this.demo.productos;
  protected readonly carta = this.demo.carta;

  protected readonly vistas: readonly OpcionSegmento<VistaCarta>[] = [
    { valor: 'productos', etiqueta: 'Productos' },
    { valor: 'carta', etiqueta: 'Carta del día' },
  ];

  protected readonly unidades: readonly OpcionSegmento<UnidadNegocio | 'todas'>[] = [
    { valor: 'todas', etiqueta: 'Ambas' },
    { valor: 'Restaurante', etiqueta: 'Restaurante' },
    { valor: 'Pollería', etiqueta: 'Pollería' },
  ];

  protected readonly cartaFiltrada = computed(() => {
    const u = this.unidad();
    return u === 'todas' ? this.carta() : this.carta().filter((c) => c.unidad === u);
  });

  protected readonly dialogo = signal(false);
  protected readonly editando = signal<ProductoDemo | null>(null);
  protected readonly nombre = signal('');
  protected readonly categoria = signal('Platos');
  protected readonly precio = signal('');
  protected readonly activo = signal(true);

  protected readonly columnasProd: readonly ColumnaTabla[] = [
    { titulo: 'Producto' },
    { titulo: 'Categoría' },
    { titulo: 'Precio', numerica: true },
    { titulo: 'Unidad' },
    { titulo: 'Estado' },
    { titulo: 'Acciones', numerica: true },
  ];

  protected readonly columnasCarta: readonly ColumnaTabla[] = [
    { titulo: 'Producto' },
    { titulo: 'Unidad' },
    { titulo: 'Precio', numerica: true },
    { titulo: 'Disponibilidad' },
    { titulo: 'Acciones', numerica: true },
  ];

  protected abrirProducto(p?: ProductoDemo): void {
    this.editando.set(p ?? null);
    this.nombre.set(p?.nombre ?? '');
    this.categoria.set(p?.categoria ?? 'Platos');
    this.precio.set(p?.precio ?? '');
    this.activo.set(p?.activo ?? true);
    this.dialogo.set(true);
  }

  protected guardarProducto(): void {
    const nombre = this.nombre().trim();
    if (!nombre) {
      return;
    }
    this.demo.guardarProducto({
      id: this.editando()?.id,
      nombre,
      categoria: this.categoria(),
      precio: this.precio().startsWith('S/') ? this.precio() : `S/ ${this.precio()}`,
      activo: this.activo(),
      unidades: this.editando()?.unidades ?? ['Restaurante', 'Pollería'],
    });
    this.dialogo.set(false);
  }

  protected marcarAgotado(id: number, agotado: boolean): void {
    this.demo.marcarCarta(id, agotado ? 'Agotado' : 'Disponible');
  }
}
