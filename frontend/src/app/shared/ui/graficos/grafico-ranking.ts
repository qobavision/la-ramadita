import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

/** Fila de un ranking (plato, método de pago…). */
export interface FilaRanking {
  clave: string;
  etiqueta: string;
  valor: number;
  /** Valor ya formateado, p. ej. "S/ 120.00" o "18 und." */
  texto: string;
  detalle?: string;
}

/**
 * Ranking con barras horizontales finas: el valor va siempre escrito en la
 * punta, así que el dato nunca depende del color ni del hover. Estilos en
 * `styles/_graficos.scss`.
 */
@Component({
  selector: 'app-grafico-ranking',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (vacio()) {
      <p class="lr-grafico-vacio">{{ aviso() }}</p>
    } @else {
      <ol class="lr-ranking" [class.lr-ranking--numerado]="numerado()">
        @for (f of filas(); track f.clave) {
          <li>
            <span class="lr-ranking__etiqueta">{{ f.etiqueta }}</span>
            <span class="lr-ranking__valor">
              <strong>{{ f.texto }}</strong>
              @if (f.detalle) {
                <small>{{ f.detalle }}</small>
              }
            </span>
            <span class="lr-ranking__pista" aria-hidden="true">
              <span class="lr-ranking__barra" [style.width.%]="ancho(f.valor)" [style.background]="color()"></span>
            </span>
          </li>
        }
      </ol>
    }
  `,
})
export class GraficoRanking {
  readonly filas = input.required<readonly FilaRanking[]>();
  readonly color = input('var(--lr-serie-principal)');
  readonly numerado = input(false);
  readonly aviso = input('Sin datos en el periodo');

  protected readonly vacio = computed(() => this.filas().every((f) => f.valor === 0));
  private readonly maximo = computed(() => Math.max(1, ...this.filas().map((f) => f.valor)));

  protected ancho(valor: number): number {
    return (valor / this.maximo()) * 100;
  }
}
