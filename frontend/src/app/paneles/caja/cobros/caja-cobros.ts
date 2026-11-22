import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { tiempoDesde } from '../../../atencion/atencion.models';
import { CuentaResumen } from '../../../caja/caja.models';
import { CajaService } from '../../../caja/caja.service';
import { mensajeDeError } from '../../../core/errores/api-error';
import { TiempoRealService } from '../../../core/tiempo-real/tiempo-real.service';
import { EncabezadoPagina } from '../../../shared/ui/encabezado-pagina/encabezado-pagina';
import { EstadoVacio } from '../../../shared/ui/estado-vacio/estado-vacio';
import { formatoSoles } from '../../supervision/datos/supervision.models';

/** Cuentas pendientes de pago (M09): mesas y pedidos para llevar que el mesero envió a caja. */
@Component({
  selector: 'app-caja-cobros',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, EncabezadoPagina, EstadoVacio],
  templateUrl: './caja-cobros.html',
})
export class CajaCobros {
  private readonly api = inject(CajaService);

  protected readonly cuentas = signal<CuentaResumen[]>([]);
  protected readonly cargando = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly soles = formatoSoles;
  protected readonly tiempo = tiempoDesde;

  constructor() {
    this.cargar();
    // Tiempo real (AGENTS.md §10): aparece la cuenta apenas el mesero la pide.
    inject(TiempoRealService)
      .escuchar(['caja'])
      .pipe(takeUntilDestroyed())
      .subscribe((evento) => {
        if (['CUENTA_SOLICITADA', 'CUENTA_REABIERTA', 'CUENTA_PAGADA', 'CONECTADO'].includes(evento.tipo)) {
          this.cargar();
        }
      });
  }

  private cargar(): void {
    this.api.pendientes().subscribe({
      next: (lista) => {
        this.cuentas.set(lista);
        this.cargando.set(false);
      },
      error: (error) => {
        this.cargando.set(false);
        this.error.set(mensajeDeError(error, 'No se pudieron cargar las cuentas.'));
      },
    });
  }
}
