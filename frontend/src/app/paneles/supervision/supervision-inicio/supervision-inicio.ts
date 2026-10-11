import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { ComprasService } from '../../../inventario/compras.service';
import { InsumosService } from '../../../inventario/insumos.service';
import { EncabezadoPagina } from '../../../shared/ui/encabezado-pagina/encabezado-pagina';
import { Icono, NombreIcono } from '../../../shared/ui/icono/icono';
import { TarjetaModulo } from '../../../shared/ui/tarjeta-modulo/tarjeta-modulo';
import { SupervisionDemo } from '../datos/supervision.demo';
import { claseEstadoTurno } from '../datos/supervision.models';
import { PANEL_SUPERVISION } from '../supervision.panel';

@Component({
  selector: 'app-supervision-inicio',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [EncabezadoPagina, Icono, TarjetaModulo],
  templateUrl: './supervision-inicio.html',
})
export class SupervisionInicio {
  private readonly demo = inject(SupervisionDemo);
  private readonly comprasApi = inject(ComprasService);
  private readonly insumosApi = inject(InsumosService);

  constructor() {
    this.comprasApi.cargar().subscribe({ error: () => undefined });
    this.insumosApi.cargar().subscribe({ error: () => undefined });
  }

  protected readonly turno = this.demo.turno;
  protected readonly claseTurno = claseEstadoTurno;
  protected readonly ventasDelDia = 'S/ 1 842';

  protected readonly modulos = PANEL_SUPERVISION.menu.filter((m) => m.ruta !== '');

  protected readonly descripcionDe: Record<string, string> = {
    turnos: 'Horario base, turno vigente y asignaciones temporales.',
    operacion: 'Mesas, pedidos y cocina. Solo monitoreo.',
    compras: 'Compras, recepción de insumos y proveedores.',
    inventario: 'Existencias, movimientos y recetas.',
    productos: 'Catálogo, carta del día y productos agotados.',
  };

  protected indicadores(): readonly { etiqueta: string; valor: string; nota: string; icono: NombreIcono }[] {
    const t = this.turno();
    return [
      { etiqueta: 'Pedidos en curso', valor: String(this.demo.pedidosEnCurso()), nota: 'Salón y para llevar', icono: 'pedidos' },
      { etiqueta: 'Compras por recibir', valor: String(this.comprasApi.porRecibir()), nota: 'Aún no ingresan a stock', icono: 'compras' },
      { etiqueta: 'Productos agotados', valor: String(this.demo.productosAgotados()), nota: 'En la carta vigente', icono: 'carta' },
      { etiqueta: 'Stock bajo', valor: String(this.insumosApi.stockBajo()), nota: 'Mínimo o sin stock', icono: 'inventario' },
      { etiqueta: 'Turno actual', valor: t.unidad, nota: `${t.horaInicio} – ${t.horaFin}`, icono: 'reloj' },
      { etiqueta: 'Personal activo', valor: String(this.demo.personalActivo()), nota: 'Ingresaron a este turno', icono: 'empleados' },
      { etiqueta: 'Ventas del día', valor: this.ventasDelDia, nota: 'Dato rápido, no es un reporte', icono: 'ventas' },
    ];
  }
}
