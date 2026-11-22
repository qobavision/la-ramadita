import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { CajaSesion, PagoCaja, etiquetaMetodo } from '../../../caja/caja.models';
import { CajaService } from '../../../caja/caja.service';
import { mensajeDeError } from '../../../core/errores/api-error';
import { formatearFechaHora } from '../../../personal/personal.models';
import { formatoSoles } from '../../supervision/datos/supervision.models';
import { Dialogo } from '../../../shared/ui/dialogo/dialogo';
import { EncabezadoPagina } from '../../../shared/ui/encabezado-pagina/encabezado-pagina';
import { FiltrosConsulta, Periodo, UnidadFiltro } from '../../../shared/ui/filtros-consulta/filtros-consulta';
import { Icono } from '../../../shared/ui/icono/icono';
import { OpcionSegmento, Segmento } from '../../../shared/ui/segmento/segmento';
import { ColumnaTabla, TablaListado } from '../../../shared/ui/tabla-listado/tabla-listado';
import { METODOS_PAGO } from '../admin.catalogos';

type FiltroCaja = 'todas' | 'abiertas' | 'cerradas';

/**
 * Control de caja del administrador: consulta y control de las cajas
 * abiertas y cerradas (M10). NO es la pantalla operativa de cobro, que
 * pertenece al rol CAJA. Estructura visual con estados vacíos.
 */
@Component({
  selector: 'app-admin-control-caja',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Dialogo, EncabezadoPagina, FiltrosConsulta, Icono, Segmento, TablaListado],
  templateUrl: './admin-control-caja.html',
})
export class AdminControlCaja {
  private readonly api = inject(CajaService);

  protected readonly sesiones = signal<CajaSesion[]>([]);
  protected readonly seleccion = signal<CajaSesion | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly soles = formatoSoles;
  protected readonly fechaHora = formatearFechaHora;
  protected readonly etiquetaMetodo = etiquetaMetodo;
  protected readonly anulando = signal<PagoCaja | null>(null);
  protected readonly motivo = signal('');
  protected readonly errorAnular = signal<string | null>(null);
  protected readonly ocupado = signal(false);

  /** Cajas del periodo, unidad y estado elegidos (datos reales de M10). */
  protected readonly visibles = computed(() => {
    const unidad = this.unidad();
    const estado = this.filtroCaja();
    return this.sesiones().filter(
      (s) =>
        enPeriodo(s.apertura, this.periodo()) &&
        (unidad === 'consolidado' || s.unidad.toLowerCase().startsWith(unidad.slice(0, 5))) &&
        (estado === 'todas' || (estado === 'abiertas') === (s.estado === 'ABIERTA')),
    );
  });
  protected readonly valores = computed(() => {
    const lista = this.visibles();
    return [
      String(this.sesiones().filter((s) => s.estado === 'ABIERTA').length),
      String(lista.filter((s) => s.estado === 'CERRADA').length),
      formatoSoles(lista.reduce((t, s) => t + s.totalCobrado, 0)),
      formatoSoles(lista.reduce((t, s) => t + (s.diferencia ?? 0), 0)),
    ];
  });

  constructor() {
    this.api.sesiones().subscribe({
      next: (lista) => this.sesiones.set(lista),
      error: (error) => this.error.set(mensajeDeError(error, 'No se pudieron cargar las cajas.')),
    });
  }

  protected pedirAnular(pago: PagoCaja): void {
    this.motivo.set('');
    this.errorAnular.set(null);
    this.anulando.set(pago);
  }

  /** Solo ADMINISTRADOR (lo valida el backend). El pago queda ANULADO con su evidencia. */
  protected anular(): void {
    const pago = this.anulando();
    if (!pago || !this.motivo().trim()) {
      return;
    }
    this.ocupado.set(true);
    this.errorAnular.set(null);
    this.api.anularPago(pago.id, this.motivo().trim()).subscribe({
      next: () => {
        this.ocupado.set(false);
        this.anulando.set(null);
        this.recargar();
      },
      error: (error) => {
        this.ocupado.set(false);
        this.errorAnular.set(mensajeDeError(error, 'No se pudo anular el pago.'));
      },
    });
  }

  private recargar(): void {
    const id = this.seleccion()?.id;
    this.api.sesiones().subscribe({
      next: (lista) => {
        this.sesiones.set(lista);
        this.seleccion.set(lista.find((s) => s.id === id) ?? null);
      },
      error: (error) => this.error.set(mensajeDeError(error, 'No se pudieron cargar las cajas.')),
    });
  }

  protected valorDetalle(campo: string, s: CajaSesion): string {
    switch (campo) {
      case 'Ingresos':
        return formatoSoles(s.ingresos);
      case 'Egresos':
        return formatoSoles(s.egresos);
      case 'Efectivo esperado':
        return formatoSoles(s.efectivoEsperado);
      case 'Efectivo contado':
        return s.efectivoContado == null ? '—' : formatoSoles(s.efectivoContado);
      default:
        return s.diferencia == null ? '—' : formatoSoles(s.diferencia);
    }
  }

  protected totalMetodo(metodo: string, s: CajaSesion): string {
    const fila = s.porMetodo.find((m) => m.metodo.toUpperCase() === metodo.toUpperCase());
    return formatoSoles(fila?.total ?? 0);
  }

  protected readonly periodo = signal<Periodo>('hoy');
  protected readonly unidad = signal<UnidadFiltro>('consolidado');
  protected readonly filtroCaja = signal<FiltroCaja>('todas');

  protected readonly metodosPago = METODOS_PAGO;

  protected readonly filtrosCaja: readonly OpcionSegmento<FiltroCaja>[] = [
    { valor: 'todas', etiqueta: 'Todas' },
    { valor: 'abiertas', etiqueta: 'Abiertas' },
    { valor: 'cerradas', etiqueta: 'Cerradas' },
  ];

  protected readonly indicadores = [
    { etiqueta: 'Cajas abiertas', nota: 'En este momento' },
    { etiqueta: 'Cajas cerradas', nota: 'En el periodo' },
    { etiqueta: 'Total registrado', nota: 'Suma de pagos del periodo' },
    { etiqueta: 'Diferencia acumulada', nota: 'Efectivo contado − esperado' },
  ] as const;

  protected readonly columnas: readonly ColumnaTabla[] = [
    { titulo: 'Estado' },
    { titulo: 'Responsable' },
    { titulo: 'Turno / unidad' },
    { titulo: 'Apertura' },
    { titulo: 'Cierre' },
    { titulo: 'Total registrado', numerica: true },
    { titulo: 'Efectivo esperado', numerica: true },
    { titulo: 'Efectivo contado', numerica: true },
    { titulo: 'Diferencia', numerica: true },
    { titulo: 'Detalle', numerica: true },
  ];

  protected readonly columnasPagos: readonly ColumnaTabla[] = [
    { titulo: 'Fecha' },
    { titulo: 'Atención' },
    { titulo: 'Método' },
    { titulo: 'Registró' },
    { titulo: 'Monto', numerica: true },
    { titulo: 'Acción', numerica: true },
  ];

  protected readonly columnasAnulados: readonly ColumnaTabla[] = [
    { titulo: 'Atención' },
    { titulo: 'Pago' },
    { titulo: 'Registró' },
    { titulo: 'Anuló' },
    { titulo: 'Fecha de anulación' },
    { titulo: 'Motivo' },
  ];

  protected readonly detalle = [
    'Ingresos',
    'Egresos',
    'Efectivo esperado',
    'Efectivo contado',
    'Diferencia',
  ] as const;
}

/** Si la fecha de apertura cae en el periodo elegido (rango: sin filtro de fechas). */
function enPeriodo(iso: string, periodo: Periodo): boolean {
  const dia = new Date(iso);
  dia.setHours(0, 0, 0, 0);
  const hoy = new Date();
  hoy.setHours(0, 0, 0, 0);
  const dias = Math.round((hoy.getTime() - dia.getTime()) / 86_400_000);
  switch (periodo) {
    case 'hoy':
      return dias === 0;
    case 'ayer':
      return dias === 1;
    case 'semana':
      return dias < 7;
    case 'mes':
      return dia.getMonth() === hoy.getMonth() && dia.getFullYear() === hoy.getFullYear();
    default:
      return true;
  }
}
