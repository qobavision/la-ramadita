import { ChangeDetectionStrategy, Component, signal } from '@angular/core';
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
  imports: [EncabezadoPagina, FiltrosConsulta, Icono, Segmento, TablaListado],
  templateUrl: './admin-control-caja.html',
})
export class AdminControlCaja {
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

  protected readonly detalle = [
    'Ingresos',
    'Egresos',
    'Efectivo esperado',
    'Efectivo contado',
    'Diferencia',
  ] as const;
}
