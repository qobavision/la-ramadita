import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { EMPTY, catchError, switchMap } from 'rxjs';
import { horaDe } from '../../../atencion/atencion.models';
import { CajaSesion } from '../../../caja/caja.models';
import { CajaService } from '../../../caja/caja.service';
import { mensajeDeError } from '../../../core/errores/api-error';
import { unidadDesdeApi } from '../../../inventario/inventario.models';
import { COLOR_UNIDAD, ResumenVentas, etiquetaDia, porcentaje, rangoDe } from '../../../reportes/reportes.models';
import { ReportesService } from '../../../reportes/reportes.service';
import { EncabezadoPagina } from '../../../shared/ui/encabezado-pagina/encabezado-pagina';
import {
  FiltrosConsulta,
  PERIODOS,
  Periodo,
  UNIDADES,
  UnidadFiltro,
} from '../../../shared/ui/filtros-consulta/filtros-consulta';
import { GraficoColumnas, PuntoGrafico } from '../../../shared/ui/graficos/grafico-columnas';
import { GraficoParticipacion, SegmentoParticipacion } from '../../../shared/ui/graficos/grafico-participacion';
import { FilaRanking, GraficoRanking } from '../../../shared/ui/graficos/grafico-ranking';
import { Icono } from '../../../shared/ui/icono/icono';
import { formatoSoles } from '../../supervision/datos/supervision.models';

/**
 * Dashboard ejecutivo del administrador (pantalla inicial de `/admin`):
 * ventas cobradas del periodo (M11/M12), cierres de caja (M10) y alertas de
 * stock (M04). Todas las cifras vienen del backend.
 */
@Component({
  selector: 'app-admin-dashboard',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [EncabezadoPagina, FiltrosConsulta, GraficoColumnas, GraficoParticipacion, GraficoRanking, Icono],
  templateUrl: './admin-dashboard.html',
})
export class AdminDashboard {
  private readonly reportes = inject(ReportesService);

  protected readonly periodo = signal<Periodo>('hoy');
  protected readonly unidad = signal<UnidadFiltro>('consolidado');
  protected readonly desde = signal('');
  protected readonly hasta = signal('');

  protected readonly resumen = signal<ResumenVentas | null>(null);
  protected readonly cargando = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly cierres = signal<CajaSesion[] | null>(null);

  protected readonly soles = (v: number) => formatoSoles(v);
  protected readonly hora = horaDe;
  /** Mismo formato que Inventario ("Kilogramo (kg)"). */
  protected readonly unidadMedida = unidadDesdeApi;

  protected readonly contexto = computed(() => {
    const p = PERIODOS.find((o) => o.valor === this.periodo())?.etiqueta ?? '';
    const u = UNIDADES.find((o) => o.valor === this.unidad())?.etiqueta ?? '';
    return `${p} · ${u}`;
  });

  private readonly filtro = computed(() => ({
    ...rangoDe(this.periodo(), this.desde(), this.hasta()),
    unidad: this.unidad(),
  }));

  protected readonly porDia = computed<PuntoGrafico[]>(() =>
    (this.resumen()?.porDia ?? []).map((d) => ({
      clave: d.clave,
      etiqueta: etiquetaDia(d.clave),
      valor: d.total,
      detalle: ventasTexto(d.cantidad),
    })),
  );

  protected readonly porHora = computed<PuntoGrafico[]>(() =>
    (this.resumen()?.porHora ?? []).map((h) => ({
      clave: h.clave,
      etiqueta: h.etiqueta,
      valor: h.total,
      detalle: ventasTexto(h.cantidad),
    })),
  );

  protected readonly unidades = computed<SegmentoParticipacion[]>(() =>
    (this.resumen()?.porUnidad ?? []).map((u) => ({
      clave: u.clave,
      etiqueta: u.etiqueta,
      valor: u.total,
      texto: formatoSoles(u.total),
      color: COLOR_UNIDAD[u.clave],
    })),
  );

  protected readonly platos = computed<FilaRanking[]>(() =>
    (this.resumen()?.topProductos ?? []).slice(0, 5).map((p) => ({
      clave: p.producto,
      etiqueta: p.producto,
      valor: p.cantidad,
      texto: `${p.cantidad} und.`,
      detalle: formatoSoles(p.importe),
    })),
  );

  protected readonly metodos = computed<FilaRanking[]>(() => {
    const r = this.resumen();
    return (r?.porMetodo ?? []).map((m) => ({
      clave: m.clave,
      etiqueta: m.etiqueta,
      valor: m.total,
      texto: formatoSoles(m.total),
      detalle: porcentaje(m.total, r?.total ?? 0),
    }));
  });

  /** Últimos cinco cierres de caja con su diferencia de arqueo. */
  protected readonly ultimosCierres = computed(() =>
    (this.cierres() ?? [])
      .filter((s) => s.estado === 'CERRADA' && s.cierre)
      .sort((a, b) => (b.cierre ?? '').localeCompare(a.cierre ?? ''))
      .slice(0, 5),
  );

  constructor() {
    toObservable(this.filtro)
      .pipe(
        switchMap((f) => {
          this.cargando.set(true);
          this.error.set(null);
          return this.reportes.resumen(f).pipe(
            catchError((e: unknown) => {
              this.error.set(mensajeDeError(e, 'No se pudo cargar el resumen de ventas.'));
              this.cargando.set(false);
              return EMPTY;
            }),
          );
        }),
        takeUntilDestroyed(),
      )
      .subscribe((r) => {
        this.resumen.set(r);
        this.cargando.set(false);
      });

    inject(CajaService)
      .sesiones()
      .subscribe({ next: (s) => this.cierres.set(s), error: () => this.cierres.set([]) });
  }

  protected participacion(clave: string): string {
    const r = this.resumen();
    const total = r?.porUnidad.find((u) => u.clave === clave)?.total ?? 0;
    return porcentaje(total, r?.total ?? 0);
  }

  protected totalUnidad(clave: string): number | null {
    return this.resumen()?.porUnidad.find((u) => u.clave === clave)?.total ?? null;
  }
}

function ventasTexto(n: number): string {
  return n === 1 ? '1 venta' : `${n} ventas`;
}
