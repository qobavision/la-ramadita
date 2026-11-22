import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Router } from '@angular/router';
import { TiempoRealService } from '../../../core/tiempo-real/tiempo-real.service';
import { ETIQUETA_ESTADO_MESA, MesaSalon, tiempoDesde } from '../../../atencion/atencion.models';
import { AtencionesService } from '../../../atencion/atenciones.service';
import { mensajeDeError } from '../../../core/errores/api-error';
import { EncabezadoPagina } from '../../../shared/ui/encabezado-pagina/encabezado-pagina';
import { EstadoVacio } from '../../../shared/ui/estado-vacio/estado-vacio';
import { Icono } from '../../../shared/ui/icono/icono';
import { OpcionSegmento, Segmento } from '../../../shared/ui/segmento/segmento';
import { formatoSoles } from '../../supervision/datos/supervision.models';

type FiltroMesas = 'TODAS' | 'LIBRES' | 'OCUPADAS';

/** Salón del mesero (M06): mesas por zona, apertura de atención y pedido para llevar. */
@Component({
  selector: 'app-mesero-mesas',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [EncabezadoPagina, EstadoVacio, Icono, Segmento],
  templateUrl: './mesero-mesas.html',
})
export class MeseroMesas {
  private readonly api = inject(AtencionesService);
  private readonly router = inject(Router);

  protected readonly mesas = signal<MesaSalon[]>([]);
  protected readonly cargando = signal(true);
  protected readonly ocupado = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly filtro = signal<FiltroMesas>('TODAS');
  protected readonly filtros: OpcionSegmento<FiltroMesas>[] = [
    { valor: 'TODAS', etiqueta: 'Todas' },
    { valor: 'LIBRES', etiqueta: 'Libres' },
    { valor: 'OCUPADAS', etiqueta: 'Ocupadas' },
  ];
  protected readonly etiquetaEstado = ETIQUETA_ESTADO_MESA;
  protected readonly soles = formatoSoles;
  protected readonly tiempo = tiempoDesde;

  protected readonly libres = computed(() => this.mesas().filter((m) => m.estado === 'DISPONIBLE').length);
  protected readonly zonas = computed(() => {
    const filtro = this.filtro();
    const grupos = new Map<string, MesaSalon[]>();
    for (const mesa of this.mesas()) {
      const libre = mesa.estado === 'DISPONIBLE';
      if (filtro === 'TODAS' || (filtro === 'LIBRES') === libre) {
        grupos.set(mesa.zona, [...(grupos.get(mesa.zona) ?? []), mesa]);
      }
    }
    return [...grupos.entries()].map(([zona, mesas]) => ({ zona, mesas }));
  });

  constructor() {
    this.cargar();
    // Tiempo real (AGENTS.md §10): mesas abiertas, cobradas o liberadas por otros.
    inject(TiempoRealService)
      .escuchar(['salon'])
      .pipe(takeUntilDestroyed())
      .subscribe((evento) => {
        if (['SALON_ACTUALIZADO', 'CONECTADO'].includes(evento.tipo)) {
          this.cargar();
        }
      });
  }

  protected cargar(): void {
    this.api.salon().subscribe({
      next: (mesas) => {
        this.mesas.set(mesas);
        this.cargando.set(false);
      },
      error: (error) => {
        this.cargando.set(false);
        this.error.set(mensajeDeError(error, 'No se pudieron cargar las mesas.'));
      },
    });
  }

  protected seleccionar(mesa: MesaSalon): void {
    if (mesa.atencion) {
      this.irA(mesa.atencion.id);
      return;
    }
    this.ocupado.set(true);
    this.error.set(null);
    this.api.abrirMesa(mesa.id).subscribe({
      next: (atencion) => this.irA(atencion.id),
      error: (error) => {
        this.ocupado.set(false);
        this.error.set(mensajeDeError(error, 'No se pudo abrir la mesa.'));
        this.cargar();
      },
    });
  }

  protected paraLlevar(): void {
    this.ocupado.set(true);
    this.error.set(null);
    this.api.abrirParaLlevar().subscribe({
      next: (atencion) => this.irA(atencion.id),
      error: (error) => {
        this.ocupado.set(false);
        this.error.set(mensajeDeError(error, 'No se pudo iniciar el pedido para llevar.'));
      },
    });
  }

  private irA(id: number): void {
    void this.router.navigate(['/mesero', 'atencion', id]);
  }
}
