import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { EncabezadoPagina } from '../../../shared/ui/encabezado-pagina/encabezado-pagina';
import { Icono } from '../../../shared/ui/icono/icono';
import { PANEL_MESERO } from '../mesero.panel';

/**
 * Inicio del panel de mesero, pensado para tablet: pocas opciones, botones
 * grandes y táctiles. Sin datos reales todavía.
 */
@Component({
  selector: 'app-mesero-inicio',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [EncabezadoPagina, Icono, RouterLink],
  templateUrl: './mesero-inicio.html',
})
export class MeseroInicio {
  protected readonly accesos = PANEL_MESERO.menu
    .filter((m) => m.ruta !== '')
    .map((m) => ({
      ...m,
      detalle:
        m.ruta === 'mesas'
          ? 'Ver el salón y abrir una atención'
          : m.ruta === 'pedidos'
            ? 'Tomar pedidos y enviarlos a cocina'
            : 'Pedir la cuenta y avisar a caja',
    }));
}
