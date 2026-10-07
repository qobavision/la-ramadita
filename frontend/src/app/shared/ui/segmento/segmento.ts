import { ChangeDetectionStrategy, Component, input, model } from '@angular/core';

/** Opción de un selector segmentado. */
export interface OpcionSegmento<T extends string = string> {
  valor: T;
  etiqueta: string;
}

/**
 * Selector de una sola opción con aspecto de "píldoras" (Hoy / Ayer /
 * Esta semana...). Solo maneja estado visual; quien lo usa decide qué hacer
 * con el valor elegido. Estilos en `styles/_datos.scss` (`.lr-segmento`).
 */
@Component({
  selector: 'app-segmento',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { class: 'lr-segmento', role: 'group', '[attr.aria-label]': 'etiqueta()' },
  template: `
    @for (op of opciones(); track op.valor) {
      <button type="button" [attr.aria-pressed]="op.valor === valor()" (click)="valor.set(op.valor)">
        {{ op.etiqueta }}
      </button>
    }
  `,
})
export class Segmento<T extends string = string> {
  readonly opciones = input.required<readonly OpcionSegmento<T>[]>();
  readonly etiqueta = input<string>();
  readonly valor = model<T>();
}
