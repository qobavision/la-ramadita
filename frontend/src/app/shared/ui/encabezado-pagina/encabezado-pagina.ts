import { ChangeDetectionStrategy, Component, input } from '@angular/core';

/**
 * Cabecera estándar de una página dentro de un panel:
 * rótulo pequeño (eyebrow), título y descripción, con un hueco a la derecha
 * para acciones (`<button acciones>` o cualquier elemento con el atributo `acciones`).
 */
@Component({
  selector: 'app-encabezado-pagina',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <header class="encabezado">
      <div>
        @if (eyebrow()) {
          <span class="eyebrow">{{ eyebrow() }}</span>
        }
        <h1>{{ titulo() }}</h1>
        @if (descripcion()) {
          <p>{{ descripcion() }}</p>
        }
      </div>
      <div class="encabezado__acciones">
        <ng-content select="[acciones]" />
      </div>
    </header>
  `,
  styles: `
    .encabezado {
      display: flex;
      align-items: flex-end;
      justify-content: space-between;
      gap: 16px 24px;
      flex-wrap: wrap;
      margin-bottom: 24px;

      h1 {
        margin: 4px 0;
        font-size: clamp(26px, 3vw, 34px);
        font-weight: 800;
        letter-spacing: -0.02em;
      }

      p {
        margin: 0;
        font-size: 14px;
        color: var(--lr-gris);
      }
    }

    .encabezado__acciones {
      display: flex;
      gap: 8px;
      flex-wrap: wrap;
    }
  `,
})
export class EncabezadoPagina {
  readonly titulo = input.required<string>();
  readonly eyebrow = input<string>();
  readonly descripcion = input<string>();
}
