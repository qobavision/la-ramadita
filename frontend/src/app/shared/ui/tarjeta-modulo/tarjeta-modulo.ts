import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Icono, NombreIcono } from '../icono/icono';

/**
 * Tarjeta de acceso a un módulo del panel. Mientras el módulo no exista,
 * se muestra con la etiqueta "Próximamente" y sin datos de negocio.
 */
@Component({
  selector: 'app-tarjeta-modulo',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Icono, RouterLink],
  template: `
    <a class="tarjeta" [routerLink]="ruta()" [class.tarjeta--pendiente]="!disponible()">
      <span class="tarjeta__icono"><app-icono [nombre]="icono()" [tamano]="24" /></span>
      <span class="tarjeta__texto">
        <strong>{{ titulo() }}</strong>
        <small>{{ descripcion() }}</small>
        @if (!disponible()) {
          <em class="tarjeta__etiqueta">Próximamente</em>
        }
      </span>
      <app-icono class="tarjeta__flecha" nombre="flecha" [tamano]="18" />
    </a>
  `,
  styles: `
    :host {
      display: block;
    }

    .tarjeta {
      display: grid;
      grid-template-columns: auto 1fr auto;
      align-items: center;
      gap: 16px;
      height: 100%;
      padding: 18px;
      border: 1px solid var(--lr-linea);
      border-radius: 16px;
      background: var(--lr-papel);
      box-shadow: var(--lr-sombra);
      color: inherit;
      text-decoration: none;
      transition: transform 0.15s ease, border-color 0.15s ease;

      &:hover,
      &:focus-visible {
        border-color: var(--lr-naranja);
        transform: translateY(-2px);
        outline: none;
      }
    }

    .tarjeta__icono {
      display: grid;
      place-items: center;
      width: 52px;
      height: 52px;
      border-radius: 14px;
      background: #fff0e4;
      color: var(--lr-naranja);
    }

    .tarjeta--pendiente .tarjeta__icono {
      background: #eef1ec;
      color: var(--lr-verde);
    }

    .tarjeta__texto {
      min-width: 0;

      strong {
        display: block;
        font-size: 17px;
        letter-spacing: -0.01em;
      }

      small {
        display: block;
        margin-top: 2px;
        font-size: 13px;
        color: var(--lr-gris);
      }
    }

    .tarjeta__etiqueta {
      display: inline-block;
      margin-top: 8px;
      padding: 3px 9px;
      border-radius: 999px;
      background: #eee9e2;
      color: var(--lr-gris);
      font-size: 10px;
      font-style: normal;
      font-weight: 800;
      letter-spacing: 0.08em;
      text-transform: uppercase;
    }

    .tarjeta__flecha {
      color: #bbb;
    }
  `,
})
export class TarjetaModulo {
  readonly titulo = input.required<string>();
  readonly descripcion = input.required<string>();
  readonly icono = input.required<NombreIcono>();
  readonly ruta = input.required<string | readonly string[]>();
  readonly disponible = input(false);
}
