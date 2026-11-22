import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

export interface SegmentoParticipacion {
  clave: string;
  etiqueta: string;
  valor: number;
  texto: string;
  /** Color fijo de la entidad (no del orden). */
  color: string;
}

/**
 * Parte de un todo con pocas categorías (Restaurante vs Pollería): una barra
 * al 100 % con 2px de separación entre segmentos y leyenda con monto y
 * porcentaje, para que la identidad nunca dependa solo del color.
 */
@Component({
  selector: 'app-grafico-participacion',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (total() === 0) {
      <p class="lr-grafico-vacio">{{ aviso() }}</p>
    } @else {
      <div class="lr-participacion" role="img" [attr.aria-label]="descripcion()">
        @for (s of segmentos(); track s.clave) {
          @if (s.valor > 0) {
            <span [style.flex-grow]="s.valor" [style.background]="s.color" [title]="s.etiqueta + ': ' + s.texto + ' (' + pct(s.valor) + ')'"></span>
          }
        }
      </div>
    }
    <ul class="lr-participacion__leyenda">
      @for (s of segmentos(); track s.clave) {
        <li>
          <i [style.background]="s.color" aria-hidden="true"></i>
          <span>{{ s.etiqueta }}</span>
          <strong>{{ s.texto }}</strong>
          <small>{{ pct(s.valor) }}</small>
        </li>
      }
    </ul>
  `,
})
export class GraficoParticipacion {
  readonly segmentos = input.required<readonly SegmentoParticipacion[]>();
  readonly aviso = input('Sin ventas en el periodo');

  protected readonly total = computed(() => this.segmentos().reduce((s, x) => s + x.valor, 0));
  protected readonly descripcion = computed(() =>
    this.segmentos()
      .map((s) => `${s.etiqueta} ${s.texto} (${this.pct(s.valor)})`)
      .join(', '),
  );

  protected pct(valor: number): string {
    const total = this.total();
    return total > 0 ? `${Math.round((valor / total) * 100)} %` : '0 %';
  }
}
