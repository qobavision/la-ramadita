import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { horaDe } from '../../../atencion/atencion.models';
import { CajaSesion, Cuenta, LineaCuenta, MetodoPago, SolicitudPago, etiquetaMetodo } from '../../../caja/caja.models';
import { CajaService } from '../../../caja/caja.service';
import { mensajeDeError } from '../../../core/errores/api-error';
import { EncabezadoPagina } from '../../../shared/ui/encabezado-pagina/encabezado-pagina';
import { Icono } from '../../../shared/ui/icono/icono';
import { OpcionSegmento, Segmento } from '../../../shared/ui/segmento/segmento';
import { formatoSoles } from '../../supervision/datos/supervision.models';
import { CajaBoleta } from '../boleta/caja-boleta';

type ModoPago = 'MONTO' | 'PRODUCTOS';

/**
 * Cobro de una cuenta (M09): pagos parciales con distintos métodos (RN-14,
 * RN-15) y división por montos o por productos (RN-17). Saldo, estado y cierre
 * de la atención los calcula el backend; aquí solo se arma la solicitud.
 */
@Component({
  selector: 'app-caja-cobro',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, EncabezadoPagina, Icono, Segmento, CajaBoleta],
  templateUrl: './caja-cobro.html',
})
export class CajaCobro {
  private readonly api = inject(CajaService);
  private readonly id = Number(inject(ActivatedRoute).snapshot.paramMap.get('id'));

  protected readonly cuenta = signal<Cuenta | null>(null);
  protected readonly caja = signal<CajaSesion | null>(null);
  protected readonly metodos = signal<OpcionSegmento[]>([]);
  protected readonly metodo = signal('EFECTIVO');
  protected readonly modo = signal<ModoPago>('MONTO');
  protected readonly monto = signal<number | null>(null);
  protected readonly recibido = signal<number | null>(null);
  protected readonly observacion = signal('');
  protected readonly seleccion = signal<Record<number, number>>({});
  protected readonly ocupado = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly verBoleta = signal(false);
  protected readonly modos: OpcionSegmento<ModoPago>[] = [
    { valor: 'MONTO', etiqueta: 'Por monto' },
    { valor: 'PRODUCTOS', etiqueta: 'Por productos' },
  ];
  protected readonly soles = formatoSoles;
  protected readonly hora = horaDe;
  protected readonly etiquetaMetodo = etiquetaMetodo;

  protected readonly pendiente = computed(() => this.cuenta()?.estado === 'PENDIENTE');
  protected readonly montoProductos = computed(() => {
    const lineas = this.cuenta()?.lineas ?? [];
    const sel = this.seleccion();
    return redondear(lineas.reduce((t, l) => t + (sel[l.pedidoDetalleId] ?? 0) * l.precioUnitario, 0));
  });
  protected readonly montoAPagar = computed(() =>
    this.modo() === 'PRODUCTOS' ? this.montoProductos() : redondear(this.monto() ?? 0),
  );
  protected readonly vuelto = computed(() => {
    const recibido = this.recibido();
    return this.metodo() === 'EFECTIVO' && recibido != null ? redondear(recibido - this.montoAPagar()) : null;
  });
  protected readonly puedePagar = computed(() => {
    const cuenta = this.cuenta();
    const monto = this.montoAPagar();
    const vuelto = this.vuelto();
    return (
      !!cuenta && !!this.caja() && this.pendiente() && !this.ocupado() && monto > 0 && monto <= cuenta.saldo &&
      (vuelto == null || vuelto >= 0)
    );
  });

  constructor() {
    this.api.cuenta(this.id).subscribe({
      next: (c) => this.asignar(c),
      error: (error) => this.error.set(mensajeDeError(error, 'No se pudo cargar la cuenta.')),
    });
    this.api.miCaja().subscribe({ next: (c) => this.caja.set(c), error: () => undefined });
    this.api.metodos().subscribe({
      next: (lista: MetodoPago[]) =>
        this.metodos.set(lista.map((m) => ({ valor: m.nombre, etiqueta: etiquetaMetodo(m.nombre) }))),
      error: () => undefined,
    });
  }

  protected numero(evento: Event): number | null {
    const valor = (evento.target as HTMLInputElement).value;
    return valor === '' ? null : Number(valor);
  }

  /** División por montos: saldo completo o partes iguales. */
  protected dividir(partes: number): void {
    const saldo = this.cuenta()?.saldo ?? 0;
    this.modo.set('MONTO');
    this.monto.set(redondear(saldo / partes));
  }

  protected cantidad(linea: LineaCuenta): number {
    return this.seleccion()[linea.pedidoDetalleId] ?? 0;
  }

  protected cambiarCantidad(linea: LineaCuenta, delta: number): void {
    const nueva = Math.min(linea.cantidadPendiente, Math.max(0, this.cantidad(linea) + delta));
    this.seleccion.update((sel) => ({ ...sel, [linea.pedidoDetalleId]: nueva }));
  }

  protected pagar(): void {
    if (!this.puedePagar()) {
      return;
    }
    const solicitud: SolicitudPago = {
      metodo: this.metodo(),
      observacion: this.observacion().trim() || null,
    };
    if (this.modo() === 'PRODUCTOS') {
      solicitud.lineas = Object.entries(this.seleccion())
        .filter(([, cantidad]) => cantidad > 0)
        .map(([id, cantidad]) => ({ pedidoDetalleId: Number(id), cantidad }));
    } else {
      solicitud.monto = this.montoAPagar();
    }
    this.ocupado.set(true);
    this.error.set(null);
    this.api.pagar(this.id, solicitud).subscribe({
      next: (c) => {
        this.ocupado.set(false);
        this.asignar(c);
      },
      error: (error) => {
        this.ocupado.set(false);
        this.error.set(mensajeDeError(error, 'No se pudo registrar el pago.'));
      },
    });
  }

  private asignar(cuenta: Cuenta): void {
    this.cuenta.set(cuenta);
    this.seleccion.set({});
    this.monto.set(cuenta.saldo > 0 ? cuenta.saldo : null);
    this.recibido.set(null);
    this.observacion.set('');
  }
}

function redondear(valor: number): number {
  return Math.round(valor * 100) / 100;
}
