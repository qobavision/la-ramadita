import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { Observable } from 'rxjs';
import { horaDe } from '../../../atencion/atencion.models';
import { CajaSesion, etiquetaMetodo } from '../../../caja/caja.models';
import { CajaService } from '../../../caja/caja.service';
import { mensajeDeError } from '../../../core/errores/api-error';
import { Dialogo } from '../../../shared/ui/dialogo/dialogo';
import { EncabezadoPagina } from '../../../shared/ui/encabezado-pagina/encabezado-pagina';
import { Icono } from '../../../shared/ui/icono/icono';
import { OpcionSegmento, Segmento } from '../../../shared/ui/segmento/segmento';
import { formatoSoles } from '../../supervision/datos/supervision.models';

type TipoMovimiento = 'INGRESO' | 'EGRESO';

/**
 * Control de caja del cajero (M10): apertura con monto inicial, ingresos y
 * egresos, acumulado por método y cierre con efectivo esperado, contado y
 * diferencia. Los cálculos oficiales los hace el backend.
 */
@Component({
  selector: 'app-caja-control',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [EncabezadoPagina, Icono, Segmento, Dialogo],
  templateUrl: './caja-control.html',
})
export class CajaControl {
  private readonly api = inject(CajaService);

  protected readonly caja = signal<CajaSesion | null>(null);
  protected readonly cerrada = signal<CajaSesion | null>(null);
  protected readonly cargando = signal(true);
  protected readonly ocupado = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly montoInicial = signal<number | null>(0);
  protected readonly tipo = signal<TipoMovimiento>('INGRESO');
  protected readonly montoMovimiento = signal<number | null>(null);
  protected readonly motivo = signal('');
  protected readonly contado = signal<number | null>(null);
  protected readonly observacion = signal('');
  protected readonly confirmarCierre = signal(false);
  protected readonly tipos: OpcionSegmento<TipoMovimiento>[] = [
    { valor: 'INGRESO', etiqueta: 'Ingreso' },
    { valor: 'EGRESO', etiqueta: 'Egreso' },
  ];
  protected readonly soles = formatoSoles;
  protected readonly hora = horaDe;
  protected readonly etiquetaMetodo = etiquetaMetodo;

  /** Vista previa; la diferencia oficial la guarda el backend al cerrar. */
  protected readonly diferencia = computed(() => {
    const caja = this.caja();
    const contado = this.contado();
    return caja && contado != null ? Math.round((contado - caja.efectivoEsperado) * 100) / 100 : null;
  });

  constructor() {
    this.api.miCaja().subscribe({
      next: (c) => {
        this.caja.set(c);
        this.cargando.set(false);
      },
      error: (error) => {
        this.cargando.set(false);
        this.error.set(mensajeDeError(error, 'No se pudo consultar la caja del turno.'));
      },
    });
  }

  protected numero(evento: Event): number | null {
    const valor = (evento.target as HTMLInputElement).value;
    return valor === '' ? null : Number(valor);
  }

  protected abrir(): void {
    this.ejecutar(this.api.abrir(this.montoInicial() ?? 0), (c) => {
      this.caja.set(c);
      this.cerrada.set(null);
    });
  }

  protected registrarMovimiento(): void {
    const monto = this.montoMovimiento();
    if (!monto || monto <= 0 || !this.motivo().trim()) {
      return;
    }
    this.ejecutar(this.api.movimiento(this.tipo(), monto, this.motivo().trim()), (c) => {
      this.caja.set(c);
      this.montoMovimiento.set(null);
      this.motivo.set('');
    });
  }

  protected cerrar(): void {
    this.confirmarCierre.set(false);
    this.ejecutar(this.api.cerrar(this.contado() ?? 0, this.observacion().trim() || null), (c) => {
      this.cerrada.set(c);
      this.caja.set(null);
      this.contado.set(null);
      this.observacion.set('');
    });
  }

  private ejecutar(peticion: Observable<CajaSesion>, alTerminar: (c: CajaSesion) => void): void {
    this.ocupado.set(true);
    this.error.set(null);
    peticion.subscribe({
      next: (c) => {
        this.ocupado.set(false);
        alTerminar(c);
      },
      error: (error) => {
        this.ocupado.set(false);
        this.error.set(mensajeDeError(error, 'No se pudo completar la operación.'));
      },
    });
  }
}
