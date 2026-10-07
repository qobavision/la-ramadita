import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { Icono, NombreIcono } from '../icono/icono';

/**
 * Estado vacío reutilizable: icono, título, explicación y un hueco para una
 * acción (`<button acciones>`). Estilos en `styles/_paneles.scss` (`.lr-vacio`).
 */
@Component({
  selector: 'app-estado-vacio',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Icono],
  host: { class: 'lr-vacio' },
  template: `
    <span class="lr-vacio__icono"><app-icono [nombre]="icono()" [tamano]="28" /></span>
    <h2>{{ titulo() }}</h2>
    @if (descripcion()) {
      <p>{{ descripcion() }}</p>
    }
    <ng-content select="[acciones]" />
  `,
  styles: `
    :host h2 {
      font-size: 20px;
    }
  `,
})
export class EstadoVacio {
  readonly titulo = input.required<string>();
  readonly descripcion = input<string>();
  readonly icono = input<NombreIcono>('lista');
}
