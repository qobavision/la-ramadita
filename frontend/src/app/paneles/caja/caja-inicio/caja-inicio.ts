import { ChangeDetectionStrategy, Component } from '@angular/core';
import { EncabezadoPagina } from '../../../shared/ui/encabezado-pagina/encabezado-pagina';
import { Icono } from '../../../shared/ui/icono/icono';
import { TarjetaModulo } from '../../../shared/ui/tarjeta-modulo/tarjeta-modulo';
import { PANEL_CAJA } from '../caja.panel';

/** Inicio del panel de caja: estado del turno (sin datos) y accesos. */
@Component({
  selector: 'app-caja-inicio',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [EncabezadoPagina, Icono, TarjetaModulo],
  templateUrl: './caja-inicio.html',
})
export class CajaInicio {
  protected readonly accesos = PANEL_CAJA.menu
    .filter((m) => m.ruta !== '')
    .map((m) => ({
      ...m,
      detalle:
        m.ruta === 'cobros'
          ? 'Cuentas pendientes de cobro'
          : m.ruta === 'pagos'
            ? 'Efectivo, tarjeta y billeteras'
            : m.ruta === 'division'
              ? 'Dividir una cuenta entre varias personas'
              : 'Apertura, cierre y arqueo',
    }));
}
