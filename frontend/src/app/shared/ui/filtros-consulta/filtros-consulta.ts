import { ChangeDetectionStrategy, Component, input, model } from '@angular/core';
import { OpcionSegmento, Segmento } from '../segmento/segmento';

export type Periodo = 'hoy' | 'ayer' | 'semana' | 'mes' | 'rango';
export type UnidadFiltro = 'consolidado' | 'restaurante' | 'polleria';

export const PERIODOS: readonly OpcionSegmento<Periodo>[] = [
  { valor: 'hoy', etiqueta: 'Hoy' },
  { valor: 'ayer', etiqueta: 'Ayer' },
  { valor: 'semana', etiqueta: 'Esta semana' },
  { valor: 'mes', etiqueta: 'Este mes' },
  { valor: 'rango', etiqueta: 'Rango personalizado' },
];

export const UNIDADES: readonly OpcionSegmento<UnidadFiltro>[] = [
  { valor: 'consolidado', etiqueta: 'Consolidado' },
  { valor: 'restaurante', etiqueta: 'Restaurante' },
  { valor: 'polleria', etiqueta: 'Pollería' },
];

/**
 * Filtros comunes de las pantallas de consulta del administrador:
 * periodo y unidad de negocio. Por ahora solo guardan la selección;
 * las consultas reales se conectarán cuando existan los módulos de datos.
 */
@Component({
  selector: 'app-filtros-consulta',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Segmento],
  host: { class: 'lr-filtros' },
  template: `
    <div class="lr-filtro">
      <span>Periodo</span>
      <app-segmento etiqueta="Periodo" [opciones]="periodos" [(valor)]="periodo" />
    </div>

    @if (periodo() === 'rango') {
      <div class="lr-filtro lr-filtro--rango" aria-label="Rango de fechas">
        <input class="form-control" type="date" aria-label="Desde" [value]="desde()" (change)="desde.set(valorDe($event))" />
        <span>hasta</span>
        <input class="form-control" type="date" aria-label="Hasta" [value]="hasta()" (change)="hasta.set(valorDe($event))" />
      </div>
    }

    @if (conUnidad()) {
      <div class="lr-filtro">
        <span>Unidad</span>
        <app-segmento etiqueta="Unidad de negocio" [opciones]="unidades" [(valor)]="unidad" />
      </div>
    }
  `,
})
export class FiltrosConsulta {
  protected readonly periodos = PERIODOS;
  protected readonly unidades = UNIDADES;

  readonly conUnidad = input(true);
  readonly periodo = model<Periodo>('hoy');
  readonly unidad = model<UnidadFiltro>('consolidado');
  readonly desde = model('');
  readonly hasta = model('');

  protected valorDe(evento: Event): string {
    return (evento.target as HTMLInputElement).value;
  }
}
