import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Dialogo } from '../../../shared/ui/dialogo/dialogo';
import { EncabezadoPagina } from '../../../shared/ui/encabezado-pagina/encabezado-pagina';
import { Icono } from '../../../shared/ui/icono/icono';
import { OpcionSegmento, Segmento } from '../../../shared/ui/segmento/segmento';
import { ColumnaTabla, TablaListado } from '../../../shared/ui/tabla-listado/tabla-listado';
import { SupervisionDemo } from '../datos/supervision.demo';
import {
  CompraDemo,
  LineaCompra,
  ProveedorDemo,
  TipoDocumento,
  abreviarUnidad,
  claseEstadoCompra,
  estadoCompraDe,
  estadoItemCompraDe,
  fechaHoyLocal,
  formatoCantidad,
  formatoSoles,
  pendienteDe,
  resumenInsumosDe,
  totalCompraDe,
} from '../datos/supervision.models';

type VistaCompras = 'compras' | 'recepcion' | 'proveedores';
type FiltroRecepcion = 'pendientes' | 'recibidas';

interface LineaFormulario {
  clave: number;
  insumoId: number;
  cantidad: number | null;
  precio: number | null;
}

@Component({
  selector: 'app-supervision-compras',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, EncabezadoPagina, Icono, Segmento, TablaListado, Dialogo],
  templateUrl: './supervision-compras.html',
})
export class SupervisionCompras {
  private readonly demo = inject(SupervisionDemo);
  private siguienteClave = 1;

  protected readonly vista = signal<VistaCompras>('compras');
  protected readonly filtroRecepcion = signal<FiltroRecepcion>('pendientes');
  protected readonly compras = this.demo.compras;
  protected readonly proveedores = this.demo.proveedores;
  protected readonly proveedoresActivos = this.demo.proveedoresActivos;
  protected readonly insumos = this.demo.insumos;
  protected readonly claseCompra = claseEstadoCompra;
  protected readonly estadoDe = estadoCompraDe;
  protected readonly estadoItem = estadoItemCompraDe;
  protected readonly resumenDe = resumenInsumosDe;
  protected readonly totalDe = totalCompraDe;
  protected readonly soles = formatoSoles;
  protected readonly cant = formatoCantidad;
  protected readonly unidadDe = abreviarUnidad;
  protected readonly pendienteDe = pendienteDe;

  protected readonly vistas: readonly OpcionSegmento<VistaCompras>[] = [
    { valor: 'compras', etiqueta: 'Compras' },
    { valor: 'recepcion', etiqueta: 'Recepciones' },
    { valor: 'proveedores', etiqueta: 'Proveedores' },
  ];

  protected readonly filtrosRecepcion: readonly OpcionSegmento<FiltroRecepcion>[] = [
    { valor: 'pendientes', etiqueta: 'Por recibir' },
    { valor: 'recibidas', etiqueta: 'Recibidas' },
  ];

  protected readonly pendientes = computed(() => this.compras().filter((c) => estadoCompraDe(c) !== 'Recibida'));
  protected readonly recibidas = computed(() => this.compras().filter((c) => estadoCompraDe(c) === 'Recibida'));
  protected readonly listaRecepcion = computed(() =>
    this.filtroRecepcion() === 'recibidas' ? this.recibidas() : this.pendientes(),
  );

  protected readonly dialogo = signal<'compra' | 'detalle' | 'proveedor' | 'recepcion' | 'recibir' | null>(null);
  protected readonly compraId = signal<string | null>(null);
  protected readonly lineaId = signal<number | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly compraSel = computed(() => this.compras().find((c) => c.id === this.compraId()) ?? null);
  protected readonly lineaSel = computed(() => this.compraSel()?.lineas.find((l) => l.id === this.lineaId()) ?? null);

  protected readonly proveedorSel = signal<ProveedorDemo | null>(null);
  protected readonly proveedorNombre = signal('');
  protected readonly proveedorTipo = signal<TipoDocumento>('RUC');
  protected readonly proveedorDoc = signal('');
  protected readonly proveedorTel = signal('');
  protected readonly proveedorContacto = signal('');
  protected readonly proveedorActivo = signal(true);

  protected readonly compraProveedorId = signal(0);
  protected readonly compraFecha = signal(fechaHoyLocal());
  protected readonly lineasForm = signal<LineaFormulario[]>([]);
  protected readonly cantidadAhora = signal<number | null>(null);

  protected readonly totalForm = computed(() =>
    this.lineasForm().reduce((s, l) => s + Number(l.cantidad ?? 0) * Number(l.precio ?? 0), 0),
  );

  protected readonly columnasCompras: readonly ColumnaTabla[] = [
    { titulo: 'Código' },
    { titulo: 'Proveedor' },
    { titulo: 'Contenido' },
    { titulo: 'Fecha' },
    { titulo: 'Total', numerica: true },
    { titulo: 'Estado' },
    { titulo: 'Acciones', numerica: true },
  ];

  protected readonly columnasRecepcion: readonly ColumnaTabla[] = [
    { titulo: 'Compra' },
    { titulo: 'Proveedor' },
    { titulo: 'Por recibir' },
    { titulo: 'Estado' },
    { titulo: 'Acción', numerica: true },
  ];

  protected readonly columnasProv: readonly ColumnaTabla[] = [
    { titulo: 'Nombre / Razón social' },
    { titulo: 'Documento' },
    { titulo: 'Teléfono' },
    { titulo: 'Contacto' },
    { titulo: 'Estado' },
    { titulo: 'Acciones', numerica: true },
  ];

  protected puedeEditar(compra: CompraDemo): boolean {
    return estadoCompraDe(compra) === 'Pendiente';
  }

  protected documentoDe(p: ProveedorDemo): string {
    return `${p.tipoDocumento} ${p.documento}`;
  }

  protected insumoDe(id: number) {
    return this.insumos().find((i) => i.id === id);
  }

  protected subtotal(linea: { cantidad: number | null; precio: number | null }): number {
    return Number(linea.cantidad ?? 0) * Number(linea.precio ?? 0);
  }

  protected abrirCompra(compra?: CompraDemo): void {
    this.error.set(null);
    this.compraId.set(compra?.id ?? null);
    this.compraProveedorId.set(compra?.proveedorId ?? this.proveedoresActivos()[0]?.id ?? 0);
    this.compraFecha.set(compra?.fecha ?? fechaHoyLocal());
    this.lineasForm.set(
      compra
        ? compra.lineas.map((l) => ({
            clave: this.siguienteClave++,
            insumoId: l.insumoId,
            cantidad: l.cantidad,
            precio: l.precioUnitario,
          }))
        : [this.lineaVacia()],
    );
    this.dialogo.set('compra');
  }

  protected agregarLinea(): void {
    this.lineasForm.update((lista) => [...lista, this.lineaVacia()]);
  }

  protected quitarLinea(clave: number): void {
    this.lineasForm.update((lista) => (lista.length <= 1 ? lista : lista.filter((l) => l.clave !== clave)));
  }

  protected cambiarLinea(clave: number, campo: 'insumoId' | 'cantidad' | 'precio', valor: number): void {
    this.lineasForm.update((lista) => lista.map((l) => (l.clave === clave ? { ...l, [campo]: valor } : l)));
  }

  protected guardarCompra(): void {
    const mensaje = this.demo.guardarCompra({
      id: this.compraId() ?? undefined,
      proveedorId: this.compraProveedorId(),
      fecha: this.compraFecha(),
      lineas: this.lineasForm()
        .filter((l) => l.insumoId)
        .map((l) => ({
          insumoId: l.insumoId,
          cantidad: Number(l.cantidad ?? 0),
          precioUnitario: Number(l.precio ?? 0),
        })),
    });
    if (mensaje) {
      this.error.set(mensaje);
      return;
    }
    this.cerrar();
  }

  protected ver(compra: CompraDemo): void {
    this.compraId.set(compra.id);
    this.dialogo.set('detalle');
  }

  protected abrirRecepcion(compra: CompraDemo): void {
    this.compraId.set(compra.id);
    this.error.set(null);
    this.dialogo.set('recepcion');
  }

  protected abrirRecibir(linea: LineaCompra): void {
    this.lineaId.set(linea.id);
    this.cantidadAhora.set(pendienteDe(linea));
    this.error.set(null);
    this.dialogo.set('recibir');
  }

  protected confirmarRecepcion(): void {
    const compra = this.compraSel();
    const linea = this.lineaSel();
    if (!compra || !linea) {
      return;
    }
    const mensaje = this.demo.recibirLinea(compra.id, linea.id, Number(this.cantidadAhora() ?? 0));
    if (mensaje) {
      this.error.set(mensaje);
      return;
    }
    this.lineaId.set(null);
    this.dialogo.set('recepcion');
  }

  protected abrirProveedor(fila?: ProveedorDemo): void {
    this.proveedorSel.set(fila ?? null);
    this.proveedorNombre.set(fila?.nombre ?? '');
    this.proveedorTipo.set(fila?.tipoDocumento ?? 'RUC');
    this.proveedorDoc.set(fila?.documento ?? '');
    this.proveedorTel.set(fila?.telefono ?? '');
    this.proveedorContacto.set(fila?.contacto ?? '');
    this.proveedorActivo.set(fila?.activo ?? true);
    this.error.set(null);
    this.dialogo.set('proveedor');
  }

  protected guardarProveedor(): void {
    const mensaje = this.demo.guardarProveedor({
      id: this.proveedorSel()?.id,
      nombre: this.proveedorNombre(),
      tipoDocumento: this.proveedorTipo(),
      documento: this.proveedorDoc(),
      telefono: this.proveedorTel().trim(),
      contacto: this.proveedorContacto().trim(),
      activo: this.proveedorActivo(),
    });
    if (mensaje) {
      this.error.set(mensaje);
      return;
    }
    this.cerrar();
  }

  protected alternarProveedor(fila: ProveedorDemo): void {
    this.demo.alternarProveedor(fila);
  }

  protected volverRecepcion(): void {
    this.lineaId.set(null);
    this.error.set(null);
    this.dialogo.set('recepcion');
  }

  protected cerrar(): void {
    this.dialogo.set(null);
    this.compraId.set(null);
    this.lineaId.set(null);
    this.proveedorSel.set(null);
    this.error.set(null);
  }

  private lineaVacia(): LineaFormulario {
    return { clave: this.siguienteClave++, insumoId: 0, cantidad: null, precio: null };
  }
}
