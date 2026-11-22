import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

/** Punto de una serie única (un día, una hora…). */
export interface PuntoGrafico {
  clave: string;
  etiqueta: string;
  valor: number;
  /** Texto secundario del tooltip, p. ej. "4 ventas". */
  detalle?: string;
}

const ETIQUETAS_MAXIMAS = 8;

/**
 * Columnas de una sola serie (magnitud a lo largo del tiempo). Sin librerías:
 * barras ≤ 24px con extremo redondeado de 4px, rejilla de una línea, tooltip
 * por columna (hover y foco con teclado) y la tabla de datos como alternativa
 * accesible. Una sola serie no lleva leyenda: el título de la tarjeta la nombra.
 * Estilos en `styles/_graficos.scss`.
 */
@Component({
  selector: 'app-grafico-columnas',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { class: 'lr-cols' },
  template: `
    @if (vacio()) {
      <p class="lr-grafico-vacio">{{ aviso() }}</p>
    } @else {
      <div class="lr-cols__plano">
        @for (t of ticks(); track t.valor) {
          <span class="lr-cols__tick" [style.bottom.%]="t.pct" aria-hidden="true">
            <small>{{ t.texto }}</small>
          </span>
        }
        <div class="lr-cols__area" [style.--columnas]="puntos().length">
          @for (p of puntos(); track p.clave) {
            <button type="button" class="lr-cols__col" [attr.aria-label]="p.etiqueta + ': ' + formato()(p.valor)">
              <span class="lr-cols__barra" [class.lr-cols__barra--cero]="p.valor === 0" [style.height.%]="altura(p.valor)" [style.background]="color()"></span>
              <span class="lr-cols__tip" role="tooltip">
                <strong>{{ formato()(p.valor) }}</strong>
                <small>{{ p.etiqueta }}@if (p.detalle) { · {{ p.detalle }}}</small>
              </span>
            </button>
          }
        </div>
      </div>
      <div class="lr-cols__x" [style.--columnas]="puntos().length" aria-hidden="true">
        @for (p of puntos(); track p.clave; let i = $index) {
          <span>{{ i % paso() === 0 ? p.etiqueta : '' }}</span>
        }
      </div>
      <details class="lr-grafico-tabla">
        <summary>Ver tabla</summary>
        <table>
          <tbody>
            @for (p of puntos(); track p.clave) {
              <tr>
                <th scope="row">{{ p.etiqueta }}</th>
                <td>{{ formato()(p.valor) }}</td>
                <td>{{ p.detalle ?? '' }}</td>
              </tr>
            }
          </tbody>
        </table>
      </details>
    }
  `,
})
export class GraficoColumnas {
  readonly puntos = input.required<readonly PuntoGrafico[]>();
  readonly formato = input<(valor: number) => string>((v) => String(v));
  readonly color = input('var(--lr-serie-principal)');
  readonly aviso = input('Sin datos en el periodo');

  protected readonly vacio = computed(() => this.puntos().every((p) => p.valor === 0));
  private readonly maximo = computed(() => techo(Math.max(0, ...this.puntos().map((p) => p.valor))));

  /** Tres líneas de referencia (0, mitad, máximo), con un solo estilo de número por eje. */
  protected readonly ticks = computed(() => {
    const max = this.maximo();
    const compacto = max >= 10_000;
    return [0, max / 2, max].map((valor) => ({
      valor,
      pct: (valor / max) * 100,
      texto: compacto ? `${Math.round(valor / 1000)}K` : Math.round(valor).toLocaleString('es-PE'),
    }));
  });

  protected readonly paso = computed(() => Math.max(1, Math.ceil(this.puntos().length / ETIQUETAS_MAXIMAS)));

  protected altura(valor: number): number {
    return (valor / this.maximo()) * 100;
  }
}

/** Redondea hacia arriba a 1, 2 o 5 × 10ⁿ para que el eje tenga números limpios. */
function techo(valor: number): number {
  if (valor <= 0) {
    return 1;
  }
  const escala = 10 ** Math.floor(Math.log10(valor));
  const f = valor / escala;
  return (f <= 1 ? 1 : f <= 2 ? 2 : f <= 5 ? 5 : 10) * escala;
}
