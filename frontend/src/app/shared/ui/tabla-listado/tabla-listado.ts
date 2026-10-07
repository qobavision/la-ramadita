import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { EstadoVacio } from '../estado-vacio/estado-vacio';
import { NombreIcono } from '../icono/icono';

/** Definición visual de una columna. */
export interface ColumnaTabla {
  titulo: string;
  /** Alinea a la derecha (montos, cantidades). */
  numerica?: boolean;
}

/**
 * Tabla responsive con cabecera fija y estado vacío integrado.
 *
 * Las filas se proyectan con `<tr filas>`; mientras `total` sea 0 se muestra
 * el estado vacío ocupando toda la tabla. Estilos en `styles/_datos.scss`.
 */
@Component({
  selector: 'app-tabla-listado',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [EstadoVacio],
  host: { class: 'lr-tarjeta lr-tabla-marco' },
  template: `
    <table class="lr-tabla">
      <thead>
        <tr>
          @for (col of columnas(); track col.titulo) {
            <th scope="col" [class.num]="col.numerica">{{ col.titulo }}</th>
          }
        </tr>
      </thead>
      <tbody>
        <ng-content select="[filas]" />
        @if (total() === 0) {
          <tr>
            <td class="lr-tabla__vacia" [attr.colspan]="columnas().length">
              <app-estado-vacio [titulo]="vacioTitulo()" [descripcion]="vacioDescripcion()" [icono]="vacioIcono()">
                <ng-content select="[acciones]" />
              </app-estado-vacio>
            </td>
          </tr>
        }
      </tbody>
    </table>
  `,
  styles: `
    :host {
      display: block;
    }
  `,
})
export class TablaListado {
  readonly columnas = input.required<readonly ColumnaTabla[]>();
  readonly total = input(0);
  readonly vacioTitulo = input('Sin registros');
  readonly vacioDescripcion = input<string>();
  readonly vacioIcono = input<NombreIcono>('lista');
}
