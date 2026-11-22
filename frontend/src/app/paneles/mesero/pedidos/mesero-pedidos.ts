import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Router, RouterLink } from '@angular/router';
import { TiempoRealService } from '../../../core/tiempo-real/tiempo-real.service';
import { AtencionResumen, tiempoDesde } from '../../../atencion/atencion.models';
import { AtencionesService } from '../../../atencion/atenciones.service';
import { AuthService } from '../../../auth/auth.service';
import { mensajeDeError } from '../../../core/errores/api-error';
import { EncabezadoPagina } from '../../../shared/ui/encabezado-pagina/encabezado-pagina';
import { EstadoVacio } from '../../../shared/ui/estado-vacio/estado-vacio';
import { Icono } from '../../../shared/ui/icono/icono';
import { OpcionSegmento, Segmento } from '../../../shared/ui/segmento/segmento';
import { formatoSoles } from '../../supervision/datos/supervision.models';

type FiltroPedidos = 'MIAS' | 'TODAS';

/** Atenciones en curso (mesa y para llevar) con el estado de sus pedidos (M07). */
@Component({
  selector: 'app-mesero-pedidos',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, EncabezadoPagina, EstadoVacio, Icono, Segmento],
  templateUrl: './mesero-pedidos.html',
})
export class MeseroPedidos {
  private readonly api = inject(AtencionesService);
  private readonly router = inject(Router);
  private readonly username = inject(AuthService).usuario()?.username;

  protected readonly atenciones = signal<AtencionResumen[]>([]);
  protected readonly cargando = signal(true);
  protected readonly ocupado = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly filtro = signal<FiltroPedidos>('MIAS');
  protected readonly filtros: OpcionSegmento<FiltroPedidos>[] = [
    { valor: 'MIAS', etiqueta: 'Mis atenciones' },
    { valor: 'TODAS', etiqueta: 'Todas' },
  ];
  protected readonly soles = formatoSoles;
  protected readonly tiempo = tiempoDesde;

  protected readonly visibles = computed(() =>
    this.filtro() === 'TODAS'
      ? this.atenciones()
      : this.atenciones().filter((a) => a.meseroUsername === this.username),
  );

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
    this.api.activas().subscribe({
      next: (lista) => {
        this.atenciones.set(lista);
        this.cargando.set(false);
      },
      error: (error) => {
        this.cargando.set(false);
        this.error.set(mensajeDeError(error, 'No se pudieron cargar los pedidos.'));
      },
    });
  }

  protected paraLlevar(): void {
    this.ocupado.set(true);
    this.error.set(null);
    this.api.abrirParaLlevar().subscribe({
      next: (a) => void this.router.navigate(['/mesero', 'atencion', a.id]),
      error: (error) => {
        this.ocupado.set(false);
        this.error.set(mensajeDeError(error, 'No se pudo iniciar el pedido para llevar.'));
      },
    });
  }
}
