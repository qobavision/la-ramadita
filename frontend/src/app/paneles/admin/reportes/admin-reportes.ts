import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { EMPTY, catchError, filter, switchMap } from 'rxjs';
import { horaDe } from '../../../atencion/atencion.models';
import { mensajeDeError } from '../../../core/errores/api-error';
import {
  COLOR_UNIDAD,
  ResumenVentas,
  VentaDetalle,
  VentaResumen,
  etiquetaDia,
  fechaCorta,
  porcentaje,
  rangoDe,
} from '../../../reportes/reportes.models';
import { ReportesService } from '../../../reportes/reportes.service';
import { BloqueGrafico } from '../../../shared/ui/bloque-grafico/bloque-grafico';
import { Dialogo } from '../../../shared/ui/dialogo/dialogo';
import { EncabezadoPagina } from '../../../shared/ui/encabezado-pagina/encabezado-pagina';
import { FiltrosConsulta, Periodo, UnidadFiltro } from '../../../shared/ui/filtros-consulta/filtros-consulta';
import { GraficoColumnas, PuntoGrafico } from '../../../shared/ui/graficos/grafico-columnas';
import { GraficoParticipacion, SegmentoParticipacion } from '../../../shared/ui/graficos/grafico-participacion';
import { FilaRanking, GraficoRanking } from '../../../shared/ui/graficos/grafico-ranking';
import { Icono } from '../../../shared/ui/icono/icono';
import { ColumnaTabla, TablaListado } from '../../../shared/ui/tabla-listado/tabla-listado';
import { formatoSoles } from '../../supervision/datos/supervision.models';

type Pestana = 'resumen' | 'historico' | 'evolucion' | 'platos' | 'horarios' | 'metodos' | 'unidades' | 'rentabilidad';

/**
 * Reportes / BI: análisis (M12) y consulta histórica de ventas (M11) en una
 * sola sección con pestañas. Periodo y unidad aplican a todas; los cálculos
 * se hacen en el backend sobre las ventas cobradas.
 */
@Component({
  selector: 'app-admin-reportes',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    EncabezadoPagina,
    FiltrosConsulta,
    BloqueGrafico,
    GraficoColumnas,
    GraficoParticipacion,
    GraficoRanking,
    Icono,
    TablaListado,
    Dialogo,
  ],
  templateUrl: './admin-reportes.html',
})
export class AdminReportes {
  private readonly api = inject(ReportesService);

  protected readonly periodo = signal<Periodo>('mes');
  protected readonly unidad = signal<UnidadFiltro>('consolidado');
  protected readonly desde = signal('');
  protected readonly hasta = signal('');
  protected readonly pestana = signal<Pestana>('resumen');

  protected readonly resumen = signal<ResumenVentas | null>(null);
  protected readonly ventas = signal<VentaResumen[] | null>(null);
  protected readonly detalle = signal<VentaDetalle | null>(null);
  protected readonly busqueda = signal('');
  protected readonly error = signal<string | null>(null);

  protected readonly soles = (v: number) => formatoSoles(v);
  protected readonly hora = horaDe;
  protected readonly dia = etiquetaDia;
  protected readonly fecha = fechaCorta;

  protected readonly pestanas: readonly { id: Pestana; etiqueta: string }[] = [
    { id: 'resumen', etiqueta: 'Resumen' },
    { id: 'historico', etiqueta: 'Ventas e históricos' },
    { id: 'evolucion', etiqueta: 'Evolución' },
    { id: 'platos', etiqueta: 'Platos más vendidos' },
    { id: 'horarios', etiqueta: 'Horarios pico' },
    { id: 'metodos', etiqueta: 'Métodos de pago' },
    { id: 'unidades', etiqueta: 'Restaurante vs Pollería' },
    { id: 'rentabilidad', etiqueta: 'Rentabilidad' },
  ];

  protected readonly columnasHistorico: readonly ColumnaTabla[] = [
    { titulo: 'Fecha' },
    { titulo: 'Unidad' },
    { titulo: 'Atención' },
    { titulo: 'Mesero' },
    { titulo: 'Métodos de pago' },
    { titulo: 'Total cobrado', numerica: true },
    { titulo: '' },
  ];

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
    (this.resumen()?.topProductos ?? []).map((p) => ({
      clave: p.producto,
      etiqueta: p.producto,
      valor: p.cantidad,
      texto: `${p.cantidad} und.`,
      detalle: formatoSoles(p.importe),
    })),
  );

  protected readonly metodos = computed<FilaRanking[]>(() => {
    const total = this.resumen()?.total ?? 0;
    return (this.resumen()?.porMetodo ?? []).map((m) => ({
      clave: m.clave,
      etiqueta: m.etiqueta,
      valor: m.total,
      texto: formatoSoles(m.total),
      detalle: `${porcentaje(m.total, total)} · ${ventasTexto(m.cantidad)}`,
    }));
  });

  protected readonly ventasFiltradas = computed(() => {
    const texto = this.busqueda().trim().toLowerCase();
    const lista = this.ventas() ?? [];
    return texto
      ? lista.filter((v) =>
          `${v.identificacion} ${v.zona ?? ''} ${v.mesero} ${v.unidad} ${v.metodos.map((m) => m.metodo).join(' ')}`
            .toLowerCase()
            .includes(texto),
        )
      : lista;
  });

  protected readonly totalListado = computed(() => this.ventasFiltradas().reduce((s, v) => s + v.cobrado, 0));

  constructor() {
    toObservable(this.filtro)
      .pipe(
        switchMap((f) => {
          this.error.set(null);
          return this.api.resumen(f).pipe(catchError((e: unknown) => this.fallo(e)));
        }),
        takeUntilDestroyed(),
      )
      .subscribe((r) => this.resumen.set(r));

    // El listado histórico solo se consulta cuando se abre su pestaña.
    toObservable(computed(() => ({ f: this.filtro(), activa: this.pestana() === 'historico' })))
      .pipe(
        filter(({ activa }) => activa),
        switchMap(({ f }) => {
          this.ventas.set(null);
          return this.api.ventas(f).pipe(catchError((e: unknown) => this.fallo(e)));
        }),
        takeUntilDestroyed(),
      )
      .subscribe((v) => this.ventas.set(v));
  }

  protected verDetalle(venta: VentaResumen): void {
    this.api.venta(venta.atencionId).subscribe({
      next: (d) => this.detalle.set(d),
      error: (e: unknown) => this.error.set(mensajeDeError(e, 'No se pudo cargar el detalle de la venta.')),
    });
  }

  protected metodosTexto(v: VentaResumen): string {
    return v.metodos.map((m) => `${etiquetaMetodo(m.metodo)} ${formatoSoles(m.monto)}`).join(' · ');
  }

  protected metodo = etiquetaMetodo;

  protected ticketUnidad(clave: string): string {
    const u = this.resumen()?.porUnidad.find((x) => x.clave === clave);
    return u && u.cantidad > 0 ? formatoSoles(u.total / u.cantidad) : '—';
  }

  private fallo(e: unknown) {
    this.error.set(mensajeDeError(e, 'No se pudieron cargar los reportes.'));
    return EMPTY;
  }
}

function ventasTexto(n: number): string {
  return n === 1 ? '1 venta' : `${n} ventas`;
}

function etiquetaMetodo(nombre: string): string {
  return nombre ? nombre.charAt(0) + nombre.slice(1).toLowerCase() : nombre;
}
