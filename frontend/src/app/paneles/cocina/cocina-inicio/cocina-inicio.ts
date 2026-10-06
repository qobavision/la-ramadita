import { ChangeDetectionStrategy, Component } from '@angular/core';
import { EncabezadoPagina } from '../../../shared/ui/encabezado-pagina/encabezado-pagina';

/**
 * Inicio del panel de cocina: tablero por estado (pendiente, en preparación,
 * listo) vacío hasta que exista el módulo M08 y la conexión por WebSocket.
 */
@Component({
  selector: 'app-cocina-inicio',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [EncabezadoPagina],
  templateUrl: './cocina-inicio.html',
})
export class CocinaInicio {
  protected readonly columnas = [
    { clave: 'pendiente', titulo: 'Pendientes', detalle: 'Pedidos enviados por los meseros que aún no se toman.' },
    { clave: 'proceso', titulo: 'En preparación', detalle: 'Pedidos que la cocina ya está preparando.' },
    { clave: 'listo', titulo: 'Listos', detalle: 'Pedidos terminados a la espera de ser servidos.' },
  ] as const;
}
