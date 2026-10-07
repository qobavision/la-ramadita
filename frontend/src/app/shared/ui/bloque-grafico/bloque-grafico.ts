import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { Icono, NombreIcono } from '../icono/icono';

export type TipoGrafico = 'barras' | 'linea' | 'dona' | 'lista';

/**
 * Tarjeta que reserva el lugar de un gráfico o ranking del dashboard.
 * No dibuja datos: muestra una silueta neutra del tipo de gráfico y el aviso
 * "Sin datos", para que la pantalla quede preparada sin inventar cifras.
 * Estilos en `styles/_datos.scss` (`.lr-bloque`, `.lr-grafico`).
 */
@Component({
  selector: 'app-bloque-grafico',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Icono],
  host: { class: 'lr-tarjeta lr-bloque' },
  template: `
    <header>
      <div>
        <h3>{{ titulo() }}</h3>
        @if (descripcion()) {
          <p>{{ descripcion() }}</p>
        }
      </div>
      @if (icono(); as ic) {
        <app-icono [nombre]="ic" [tamano]="18" />
      }
    </header>

    <div [class]="'lr-grafico lr-grafico--' + tipo()" aria-hidden="true">
      @for (s of siluetas(); track $index) {
        <i></i>
      }
      <div class="lr-grafico__aviso">
        {{ aviso() }}
        @if (nota()) {
          <small>{{ nota() }}</small>
        }
      </div>
    </div>

    <ng-content select="[pie]" />
  `,
})
export class BloqueGrafico {
  readonly titulo = input.required<string>();
  readonly descripcion = input<string>();
  readonly icono = input<NombreIcono>();
  readonly tipo = input<TipoGrafico>('barras');
  readonly aviso = input('Sin datos');
  readonly nota = input<string | undefined>('Disponible cuando existan ventas');

  /** Cantidad de siluetas según el tipo (barras/puntos/anillo/filas). */
  protected readonly siluetas = computed(() => {
    switch (this.tipo()) {
      case 'dona':
        return [0];
      case 'lista':
        return [0, 1, 2, 3, 4];
      default:
        return [0, 1, 2, 3, 4, 5, 6];
    }
  });
}
