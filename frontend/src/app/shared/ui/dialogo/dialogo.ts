import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { Icono } from '../icono/icono';

/**
 * Panel superpuesto para formularios y fichas. Estilos en `styles/_datos.scss`.
 */
@Component({
  selector: 'app-dialogo',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Icono],
  template: `
    <div class="lr-dialogo" role="presentation">
      <button type="button" class="lr-dialogo__fondo" aria-label="Cerrar" (click)="cerrar.emit()"></button>
      <div
        class="lr-dialogo__panel"
        role="dialog"
        aria-modal="true"
        [attr.aria-labelledby]="tituloId"
        [class.lr-dialogo__panel--ancho]="ancho() === 'ancho'"
      >
        <header class="lr-dialogo__cabecera">
          <div>
            @if (eyebrow()) {
              <span class="eyebrow">{{ eyebrow() }}</span>
            }
            <h2 [id]="tituloId">{{ titulo() }}</h2>
            @if (descripcion()) {
              <p>{{ descripcion() }}</p>
            }
          </div>
          <button type="button" class="lr-accion" (click)="cerrar.emit()" aria-label="Cerrar">
            <app-icono nombre="cerrar" [tamano]="18" />
          </button>
        </header>
        <div class="lr-dialogo__cuerpo">
          <ng-content />
        </div>
      </div>
    </div>
  `,
  host: {
    '(document:keydown.escape)': 'cerrar.emit()',
  },
})
export class Dialogo {
  readonly titulo = input.required<string>();
  readonly eyebrow = input<string>();
  readonly descripcion = input<string>();
  readonly ancho = input<'normal' | 'ancho'>('normal');
  readonly cerrar = output<void>();

  protected readonly tituloId = `dialogo-${Math.random().toString(36).slice(2, 8)}`;
}
