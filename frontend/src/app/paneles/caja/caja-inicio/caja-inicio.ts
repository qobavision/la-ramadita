import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { horaDe } from '../../../atencion/atencion.models';
import { CajaSesion, CuentaResumen } from '../../../caja/caja.models';
import { CajaService } from '../../../caja/caja.service';
import { TiempoRealService } from '../../../core/tiempo-real/tiempo-real.service';
import { EncabezadoPagina } from '../../../shared/ui/encabezado-pagina/encabezado-pagina';
import { Icono } from '../../../shared/ui/icono/icono';
import { TarjetaModulo } from '../../../shared/ui/tarjeta-modulo/tarjeta-modulo';
import { formatoSoles } from '../../supervision/datos/supervision.models';
import { PANEL_CAJA } from '../caja.panel';

/** Inicio del panel de caja: estado de mi caja, cuentas por cobrar y accesos. */
@Component({
  selector: 'app-caja-inicio',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [EncabezadoPagina, Icono, TarjetaModulo],
  templateUrl: './caja-inicio.html',
})
export class CajaInicio {
  private readonly api = inject(CajaService);

  protected readonly accesos = PANEL_CAJA.menu
    .filter((m) => m.ruta !== '')
    .map((m) => ({
      ...m,
      detalle: m.ruta === 'cobros' ? 'Cobrar cuentas, dividir y registrar pagos' : 'Apertura, movimientos, cierre y arqueo',
    }));

  protected readonly caja = signal<CajaSesion | null>(null);
  protected readonly cuentas = signal<CuentaResumen[]>([]);
  protected readonly porCobrar = computed(() => this.cuentas().reduce((t, c) => t + c.saldo, 0));
  protected readonly soles = formatoSoles;
  protected readonly hora = horaDe;

  constructor() {
    this.api.miCaja().subscribe({ next: (c) => this.caja.set(c), error: () => undefined });
    this.cargarCuentas();
    inject(TiempoRealService)
      .escuchar(['caja'])
      .pipe(takeUntilDestroyed())
      .subscribe((evento) => {
        if (['CUENTA_SOLICITADA', 'CUENTA_REABIERTA', 'CUENTA_PAGADA', 'CONECTADO'].includes(evento.tipo)) {
          this.cargarCuentas();
        }
      });
  }

  private cargarCuentas(): void {
    this.api.pendientes().subscribe({ next: (lista) => this.cuentas.set(lista), error: () => undefined });
  }
}
