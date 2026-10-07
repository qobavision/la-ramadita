import { ChangeDetectionStrategy, Component, computed, signal } from '@angular/core';
import { BloqueGrafico, TipoGrafico } from '../../../shared/ui/bloque-grafico/bloque-grafico';
import { EncabezadoPagina } from '../../../shared/ui/encabezado-pagina/encabezado-pagina';
import { FiltrosConsulta, Periodo, UnidadFiltro } from '../../../shared/ui/filtros-consulta/filtros-consulta';
import { Icono, NombreIcono } from '../../../shared/ui/icono/icono';
import { ColumnaTabla, TablaListado } from '../../../shared/ui/tabla-listado/tabla-listado';

type Pestana =
  | 'resumen'
  | 'historico'
  | 'evolucion'
  | 'platos'
  | 'horarios'
  | 'metodos'
  | 'unidades'
  | 'rentabilidad';

interface DefinicionPestana {
  id: Pestana;
  etiqueta: string;
  titulo: string;
  descripcion: string;
  icono: NombreIcono;
  tipo: TipoGrafico;
  nota: string;
}

/**
 * Reportes / BI: reúne el análisis (M12) y la consulta histórica de ventas
 * (M11) en una sola sección con pestañas. Estructura visual sin cálculos.
 */
@Component({
  selector: 'app-admin-reportes',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [EncabezadoPagina, FiltrosConsulta, BloqueGrafico, Icono, TablaListado],
  templateUrl: './admin-reportes.html',
})
export class AdminReportes {
  protected readonly periodo = signal<Periodo>('mes');
  protected readonly unidad = signal<UnidadFiltro>('consolidado');
  protected readonly pestana = signal<Pestana>('resumen');

  protected readonly pestanas: readonly DefinicionPestana[] = [
    {
      id: 'resumen',
      etiqueta: 'Resumen',
      titulo: 'Resumen del periodo',
      descripcion: 'Indicadores principales de ventas, ticket promedio y participación por unidad.',
      icono: 'tablero',
      tipo: 'barras',
      nota: 'Disponible cuando existan ventas',
    },
    {
      id: 'historico',
      etiqueta: 'Ventas e históricos',
      titulo: 'Ventas e históricos',
      descripcion: 'Consulta de ventas por día, semana, mes o rango de fechas, con detalle por unidad y turno.',
      icono: 'historial',
      tipo: 'lista',
      nota: 'Disponible con ventas e historial (M11)',
    },
    {
      id: 'evolucion',
      etiqueta: 'Evolución',
      titulo: 'Evolución de ventas',
      descripcion: 'Tendencia de las ventas a lo largo del periodo seleccionado.',
      icono: 'tendencia',
      tipo: 'linea',
      nota: 'Disponible cuando existan ventas',
    },
    {
      id: 'platos',
      etiqueta: 'Platos más vendidos',
      titulo: 'Platos más vendidos',
      descripcion: 'Ranking de productos por cantidad e importe vendido.',
      icono: 'productos',
      tipo: 'lista',
      nota: 'Disponible cuando existan ventas',
    },
    {
      id: 'horarios',
      etiqueta: 'Horarios pico',
      titulo: 'Horarios de mayor venta',
      descripcion: 'Distribución de las ventas por franja horaria y día de la semana.',
      icono: 'reloj',
      tipo: 'barras',
      nota: 'Disponible cuando existan ventas',
    },
    {
      id: 'metodos',
      etiqueta: 'Métodos de pago',
      titulo: 'Ventas por método de pago',
      descripcion: 'Participación de Efectivo, Yape, Plin, Tarjeta y Transferencia.',
      icono: 'pago',
      tipo: 'dona',
      nota: 'Disponible cuando existan pagos registrados',
    },
    {
      id: 'unidades',
      etiqueta: 'Restaurante vs Pollería',
      titulo: 'Restaurante vs Pollería',
      descripcion: 'Comparación de ventas, consumo y resultado entre ambas unidades de negocio.',
      icono: 'dona',
      tipo: 'barras',
      nota: 'Disponible cuando existan ventas',
    },
    {
      id: 'rentabilidad',
      etiqueta: 'Rentabilidad',
      titulo: 'Rentabilidad',
      descripcion: 'Margen por producto y por unidad, calculado a partir de recetas y costos de insumos.',
      icono: 'ventas',
      tipo: 'barras',
      nota: 'Disponible cuando existan costos suficientes (M03/M04)',
    },
  ];

  protected readonly actual = computed(
    () => this.pestanas.find((p) => p.id === this.pestana()) ?? this.pestanas[0],
  );

  /** Columnas previstas del detalle histórico de ventas. */
  protected readonly columnasHistorico: readonly ColumnaTabla[] = [
    { titulo: 'Fecha' },
    { titulo: 'Unidad' },
    { titulo: 'Turno' },
    { titulo: 'Pedidos', numerica: true },
    { titulo: 'Total vendido', numerica: true },
  ];

  protected readonly indicadoresResumen = [
    'Ventas del periodo',
    'Pedidos atendidos',
    'Ticket promedio',
    'Participación Pollería',
  ] as const;
}
