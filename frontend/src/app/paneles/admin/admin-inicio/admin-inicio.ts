import { ChangeDetectionStrategy, Component } from '@angular/core';
import { EncabezadoPagina } from '../../../shared/ui/encabezado-pagina/encabezado-pagina';
import { Icono } from '../../../shared/ui/icono/icono';
import { TarjetaModulo } from '../../../shared/ui/tarjeta-modulo/tarjeta-modulo';
import { PANEL_ADMIN } from '../admin.panel';

/**
 * Inicio del panel de administración. Por ahora es una vista de ejemplo:
 * indicadores sin datos y accesos a los módulos que se irán habilitando.
 */
@Component({
  selector: 'app-admin-inicio',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [EncabezadoPagina, Icono, TarjetaModulo],
  templateUrl: './admin-inicio.html',
})
export class AdminInicio {
  /** Módulos del menú, sin la entrada "Inicio". */
  protected readonly modulos = PANEL_ADMIN.menu.filter((m) => m.ruta !== '');

  protected readonly indicadores = [
    { etiqueta: 'Ventas del día', icono: 'ventas', nota: 'Disponible con M11' },
    { etiqueta: 'Pedidos activos', icono: 'pedidos', nota: 'Disponible con M07' },
    { etiqueta: 'Mesas ocupadas', icono: 'mesas', nota: 'Disponible con M06' },
    { etiqueta: 'Alertas de inventario', icono: 'inventario', nota: 'Disponible con M04' },
  ] as const;
}
