import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { mensajeDeError } from '../../../core/errores/api-error';
import { ComprasService } from '../../../inventario/compras.service';
import { InsumosService } from '../../../inventario/insumos.service';
import { MovimientosService } from '../../../inventario/movimientos.service';
import { ProveedoresService } from '../../../inventario/proveedores.service';
import { LONGITUD_CELULAR, longitudDocumento, recortarDigitos } from '../../../shared/texto/documento-peru';
import { Dialogo } from '../../../shared/ui/dialogo/dialogo';
import { EncabezadoPagina } from '../../../shared/ui/encabezado-pagina/encabezado-pagina';
import { Icono } from '../../../shared/ui/icono/icono';
import { OpcionSegmento, Segmento } from '../../../shared/ui/segmento/segmento';
import { ColumnaTabla, TablaListado } from '../../../shared/ui/tabla-listado/tabla-listado';
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

interface LineaRecepcionForm {
  lineaId: number;
  cantidadAhora: number | null;
  completo: boolean;
}

@Component({
  selector: 'app-supervision-compras',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, EncabezadoPagina, Icono, Segmento, TablaListado, Dialogo],
  templateUrl: './supervision-compras.html',
})
export class SupervisionCompras {
  private readonly comprasApi = inject(ComprasService);
  private readonly proveedoresApi = inject(ProveedoresService);
  private readonly insumosApi = inject(InsumosService);
  private readonly movimientosApi = inject(MovimientosService);
  private siguienteClave = 1;

  protected readonly vista = signal<VistaCompras>('compras');
  protected readonly filtroRecepcion = signal<FiltroRecepcion>('pendientes');
  protected readonly compras = this.comprasApi.lista;
  protected readonly proveedores = this.proveedoresApi.lista;
  protected readonly proveedoresActivos = this.proveedoresApi.activos;
  protected readonly insumos = this.insumosApi.lista;
  protected readonly claseCompra = claseEstadoCompra;
  protected readonly estadoDe = estadoCompraDe;
  protected readonly estadoItem = estadoItemCompraDe;
  protected readonly resumenDe = resumenInsumosDe;
  protected readonly totalDe = totalCompraDe;
  protected readonly soles = formatoSoles;
  protected readonly cant = formatoCantidad;
  protected readonly unidadDe = abreviarUnidad;
  protected readonly pendienteDe = pendienteDe;
  protected readonly enviando = signal(false);

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

  protected readonly dialogo = signal<'compra' | 'detalle' | 'proveedor' | 'recepcion' | 'eliminar-compra' | null>(null);
  protected readonly compraId = signal<number | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly compraSel = computed(() => this.compras().find((c) => c.id === this.compraId()) ?? null);
  protected readonly lineasRecepcion = signal<LineaRecepcionForm[]>([]);
  protected readonly compraBorrar = signal<CompraDemo | null>(null);

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

  protected readonly totalForm = computed(() =>
    this.lineasForm().reduce((s, l) => s + Number(l.cantidad ?? 0) * Number(l.precio ?? 0), 0),
  );

  protected readonly proveedoresParaCompra = computed(() => {
    const activos = this.proveedoresActivos();
    const actual = this.proveedores().find((p) => p.id === this.compraProveedorId());
    if (actual && !actual.activo && !activos.some((p) => p.id === actual.id)) {
      return [actual, ...activos];
    }
    return activos;
  });

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
    { titulo: 'Celular' },
    { titulo: 'Contacto' },
    { titulo: 'Estado' },
    { titulo: 'Acciones', numerica: true },
  ];

  constructor() {
    this.comprasApi.cargar().subscribe({ error: () => undefined });
    this.proveedoresApi.cargar().subscribe({ error: () => undefined });
    this.insumosApi.cargar().subscribe({ error: () => undefined });
  }

  protected sinRecepcion(compra: CompraDemo): boolean {
    return compra.lineas.every((l) => l.recibido <= 0);
  }

  protected puedeEditar(compra: CompraDemo): boolean {
    return estadoCompraDe(compra) === 'Pendiente' && this.sinRecepcion(compra);
  }

  protected puedeEliminar(compra: CompraDemo): boolean {
    return this.puedeEditar(compra);
  }

  protected documentoDe(p: ProveedorDemo): string {
    return `${p.tipoDocumento} ${p.documento}`;
  }

  protected longitudDoc(): number {
    return longitudDocumento(this.proveedorTipo());
  }

  protected ayudaDocumento(): string {
    return this.proveedorTipo() === 'RUC' ? '11 dígitos' : '8 dígitos';
  }

  protected cambiarTipoDocumento(tipo: TipoDocumento): void {
    this.proveedorTipo.set(tipo);
    this.proveedorDoc.set(recortarDigitos(this.proveedorDoc(), longitudDocumento(tipo)));
  }

  protected cambiarDocumento(valor: string): void {
    this.proveedorDoc.set(recortarDigitos(valor, this.longitudDoc()));
  }

  protected cambiarCelular(valor: string): void {
    this.proveedorTel.set(recortarDigitos(valor, LONGITUD_CELULAR));
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
    this.enviando.set(true);
    this.error.set(null);
    this.comprasApi
      .guardar({
        id: this.compraId() ?? undefined,
        proveedorId: this.compraProveedorId(),
        lineas: this.lineasForm()
          .filter((l) => l.insumoId)
          .map((l) => ({
            insumoId: l.insumoId,
            cantidad: Number(l.cantidad ?? 0),
            precioUnitario: Number(l.precio ?? 0),
          })),
      })
      .subscribe({
        next: () => {
          this.enviando.set(false);
          this.cerrar();
        },
        error: (error) => {
          this.enviando.set(false);
          this.error.set(mensajeDeError(error, 'No se pudo guardar la compra.'));
        },
      });
  }

  protected ver(compra: CompraDemo): void {
    this.compraId.set(compra.id);
    this.dialogo.set('detalle');
  }

  protected pedirEliminarCompra(compra: CompraDemo): void {
    this.compraBorrar.set(compra);
    this.error.set(null);
    this.dialogo.set('eliminar-compra');
  }

  protected confirmarEliminarCompra(): void {
    const compra = this.compraBorrar();
    if (!compra) {
      return;
    }
    this.enviando.set(true);
    this.comprasApi.cancelar(compra.id).subscribe({
      next: () => {
        this.enviando.set(false);
        this.cerrar();
      },
      error: (error) => {
        this.enviando.set(false);
        this.error.set(mensajeDeError(error, 'No se pudo cancelar la compra.'));
      },
    });
  }

  protected abrirRecepcion(compra: CompraDemo): void {
    this.compraId.set(compra.id);
    this.error.set(null);
    this.lineasRecepcion.set(
      compra.lineas.map((l) => {
        const pendiente = pendienteDe(l);
        return {
          lineaId: l.id,
          cantidadAhora: pendiente > 0 ? pendiente : 0,
          completo: pendiente > 0,
        };
      }),
    );
    this.dialogo.set('recepcion');
  }

  protected formRecepcion(lineaId: number): LineaRecepcionForm | undefined {
    return this.lineasRecepcion().find((l) => l.lineaId === lineaId);
  }

  protected marcarCompleto(linea: LineaCompra, marcado: boolean): void {
    const pendiente = pendienteDe(linea);
    this.lineasRecepcion.update((lista) =>
      lista.map((l) =>
        l.lineaId === linea.id
          ? { ...l, completo: marcado, cantidadAhora: marcado ? pendiente : l.cantidadAhora }
          : l,
      ),
    );
  }

  protected cambiarCantidadAhora(linea: LineaCompra, valor: number): void {
    const pendiente = pendienteDe(linea);
    const cantidad = Number(valor);
    this.lineasRecepcion.update((lista) =>
      lista.map((l) =>
        l.lineaId === linea.id
          ? { ...l, cantidadAhora: valor, completo: cantidad === pendiente && cantidad > 0 }
          : l,
      ),
    );
  }

  protected confirmarRecepcion(): void {
    const compra = this.compraSel();
    if (!compra) {
      return;
    }
    this.enviando.set(true);
    this.error.set(null);
    this.comprasApi
      .recibir(
        compra.id,
        this.lineasRecepcion().map((l) => ({
          compraDetalleId: l.lineaId,
          cantidad: Number(l.cantidadAhora ?? 0),
        })),
      )
      .subscribe({
        next: () => {
          this.insumosApi.cargar().subscribe({ error: () => undefined });
          this.movimientosApi.cargar().subscribe({ error: () => undefined });
          this.enviando.set(false);
          this.cerrar();
        },
        error: (error) => {
          this.enviando.set(false);
          this.error.set(mensajeDeError(error, 'No se pudo confirmar la recepción.'));
        },
      });
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
    this.enviando.set(true);
    this.error.set(null);
    this.proveedoresApi
      .guardar({
        id: this.proveedorSel()?.id,
        nombre: this.proveedorNombre(),
        tipoDocumento: this.proveedorTipo(),
        documento: this.proveedorDoc(),
        telefono: this.proveedorTel().trim(),
        contacto: this.proveedorContacto().trim(),
        activo: this.proveedorActivo(),
      })
      .subscribe({
        next: () => {
          this.enviando.set(false);
          this.cerrar();
        },
        error: (error) => {
          this.enviando.set(false);
          this.error.set(mensajeDeError(error, 'No se pudo guardar el proveedor.'));
        },
      });
  }

  protected alternarProveedor(fila: ProveedorDemo): void {
    this.proveedoresApi.alternarActivo(fila.id).subscribe({
      error: (error) => this.error.set(mensajeDeError(error, 'No se pudo cambiar el estado del proveedor.')),
    });
  }

  protected cerrar(): void {
    this.dialogo.set(null);
    this.compraId.set(null);
    this.compraBorrar.set(null);
    this.lineasRecepcion.set([]);
    this.proveedorSel.set(null);
    this.error.set(null);
    this.enviando.set(false);
  }

  private lineaVacia(): LineaFormulario {
    return { clave: this.siguienteClave++, insumoId: 0, cantidad: null, precio: null };
  }
}
