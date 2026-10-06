import { ChangeDetectionStrategy, Component } from '@angular/core';
import { EncabezadoPagina } from '../../../shared/ui/encabezado-pagina/encabezado-pagina';
import { Icono } from '../../../shared/ui/icono/icono';
import { TarjetaModulo } from '../../../shared/ui/tarjeta-modulo/tarjeta-modulo';
import { PANEL_SUPERVISION } from '../supervision.panel';

/** Inicio del panel de supervisión: estructura visual sin datos reales. */
@Component({
  selector: 'app-supervision-inicio',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [EncabezadoPagina, Icono, TarjetaModulo],
  templateUrl: './supervision-inicio.html',
})
export class SupervisionInicio {
  protected readonly modulos = PANEL_SUPERVISION.menu.filter((m) => m.ruta !== '');

  protected readonly indicadores = [
    { etiqueta: 'Pedidos en curso', icono: 'pedidos', nota: 'Disponible con M07' },
    { etiqueta: 'Compras por recibir', icono: 'compras', nota: 'Disponible con M03' },
    { etiqueta: 'Productos agotados', icono: 'carta', nota: 'Disponible con M05' },
    { etiqueta: 'Stock bajo mínimo', icono: 'inventario', nota: 'Disponible con M04' },
  ] as const;
}
