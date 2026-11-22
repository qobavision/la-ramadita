import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { TiempoRealService } from '../../../core/tiempo-real/tiempo-real.service';
import { AtencionResumen, tiempoDesde } from '../../../atencion/atencion.models';
import { AtencionesService } from '../../../atencion/atenciones.service';
import { AuthService } from '../../../auth/auth.service';
import { EncabezadoPagina } from '../../../shared/ui/encabezado-pagina/encabezado-pagina';
import { Icono } from '../../../shared/ui/icono/icono';
import { formatoSoles } from '../../supervision/datos/supervision.models';
import { PANEL_MESERO } from '../mesero.panel';

/**
 * Inicio del panel de mesero, pensado para tablet: pocas opciones, botones
 * grandes y táctiles, y las atenciones en curso del mesero.
 */
@Component({
  selector: 'app-mesero-inicio',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [EncabezadoPagina, Icono, RouterLink],
  templateUrl: './mesero-inicio.html',
})
export class MeseroInicio {
  private readonly username = inject(AuthService).usuario()?.username;

  protected readonly accesos = PANEL_MESERO.menu
    .filter((m) => m.ruta !== '')
    .map((m) => ({
      ...m,
      detalle:
        m.ruta === 'mesas'
          ? 'Ver el salón y abrir una atención'
          : m.ruta === 'pedidos'
            ? 'Tomar pedidos y enviarlos a cocina'
            : 'Pedir la cuenta y avisar a caja',
    }));

  protected readonly atenciones = signal<AtencionResumen[]>([]);
  protected readonly mias = computed(() => this.atenciones().filter((a) => a.meseroUsername === this.username));
  protected readonly soles = formatoSoles;
  protected readonly tiempo = tiempoDesde;

  private readonly api = inject(AtencionesService);

  constructor() {
    this.cargar();
    inject(TiempoRealService)
      .escuchar(['salon'])
      .pipe(takeUntilDestroyed())
      .subscribe((evento) => {
        if (['SALON_ACTUALIZADO', 'PRODUCTO_PREPARADO', 'CONECTADO'].includes(evento.tipo)) {
          this.cargar();
        }
      });
  }

  private cargar(): void {
    this.api.activas().subscribe({ next: (lista) => this.atenciones.set(lista), error: () => undefined });
  }
}
