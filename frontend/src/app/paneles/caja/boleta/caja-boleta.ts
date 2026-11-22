import { ChangeDetectionStrategy, Component, OnInit, inject, input, output, signal } from '@angular/core';
import { Boleta, etiquetaMetodo } from '../../../caja/caja.models';
import { CajaService } from '../../../caja/caja.service';
import { mensajeDeError } from '../../../core/errores/api-error';
import { formatearFechaHora } from '../../../personal/personal.models';
import { Dialogo } from '../../../shared/ui/dialogo/dialogo';
import { formatoSoles } from '../../supervision/datos/supervision.models';

/**
 * Boleta simple (M10, sin SUNAT) de una cuenta pagada. El backend la emite al
 * llegar el saldo a cero; aquí solo se muestra y se imprime.
 */
@Component({
  selector: 'app-caja-boleta',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Dialogo],
  template: `
    <app-dialogo eyebrow="Comprobante" titulo="Boleta" (cerrar)="cerrar.emit()">
      @if (error(); as texto) {
        <div class="alert alert-warning py-2 small" role="alert">{{ texto }}</div>
      }
      @if (boleta(); as b) {
        <article class="lr-boleta">
          <header class="lr-boleta__cabecera">
            <strong>La Ramadita · {{ b.unidad }}</strong>
            <span>Boleta de venta simple</span>
            <span class="lr-boleta__codigo">{{ b.codigo }}</span>
            @if (b.estado === 'ANULADO') {
              <span class="lr-boleta__anulada">ANULADA</span>
            }
          </header>
          <dl class="lr-boleta__datos">
            <div><dt>Fecha</dt><dd>{{ fecha(b.fechaEmision) }}</dd></div>
            <div><dt>Atención</dt><dd>{{ b.identificacion }}</dd></div>
            <div><dt>Mesero</dt><dd>{{ b.mesero }}</dd></div>
            <div><dt>Cajero</dt><dd>{{ b.emitidoPor }}</dd></div>
          </dl>
          <table class="lr-boleta__tabla">
            <thead>
              <tr><th>Cant.</th><th>Producto</th><th class="text-end">P. unit.</th><th class="text-end">Importe</th></tr>
            </thead>
            <tbody>
              @for (l of b.lineas; track $index) {
                <tr>
                  <td>{{ l.cantidad }}</td>
                  <td>{{ l.producto }}</td>
                  <td class="text-end">{{ soles(l.precioUnitario) }}</td>
                  <td class="text-end">{{ soles(l.subtotal) }}</td>
                </tr>
              }
            </tbody>
          </table>
          <div class="lr-boleta__total"><span>Total</span><strong>{{ soles(b.total) }}</strong></div>
          <ul class="lr-boleta__pagos">
            @for (p of b.pagos; track $index) {
              <li><span>{{ etiquetaMetodo(p.metodo) }}</span><span>{{ soles(p.monto) }}</span></li>
            }
          </ul>
          <p class="lr-boleta__pie">Comprobante simulado, sin valor tributario. ¡Gracias por su visita!</p>
        </article>
        <div class="lr-formulario__pie">
          <button type="button" class="btn btn-outline-secondary" (click)="cerrar.emit()">Cerrar</button>
          <button type="button" class="btn btn-primary" (click)="imprimir()">Imprimir</button>
        </div>
      }
    </app-dialogo>
  `,
})
export class CajaBoleta implements OnInit {
  private readonly api = inject(CajaService);

  readonly atencionId = input.required<number>();
  readonly cerrar = output<void>();

  protected readonly boleta = signal<Boleta | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly soles = formatoSoles;
  protected readonly fecha = formatearFechaHora;
  protected readonly etiquetaMetodo = etiquetaMetodo;

  ngOnInit(): void {
    this.api.boleta(this.atencionId()).subscribe({
      next: (b) => this.boleta.set(b),
      error: (error) => this.error.set(mensajeDeError(error, 'No se pudo cargar la boleta.')),
    });
  }

  protected imprimir(): void {
    window.print();
  }
}
