import { ChangeDetectionStrategy, Component, computed, signal } from '@angular/core';
import { BloqueGrafico } from '../../../shared/ui/bloque-grafico/bloque-grafico';
import { EncabezadoPagina } from '../../../shared/ui/encabezado-pagina/encabezado-pagina';
import {
  FiltrosConsulta,
  PERIODOS,
  Periodo,
  UNIDADES,
  UnidadFiltro,
} from '../../../shared/ui/filtros-consulta/filtros-consulta';
import { Icono, NombreIcono } from '../../../shared/ui/icono/icono';
import { METODOS_PAGO } from '../admin.catalogos';

interface Indicador {
  etiqueta: string;
  icono: NombreIcono;
  nota: string;
}

/**
 * Dashboard ejecutivo del administrador (pantalla inicial de `/admin`).
 * Estructura visual preparada para M11/M12: filtros de periodo y unidad,
 * indicadores, gráficos y listas. Todavía sin datos: muestra "—" y "Sin datos".
 */
@Component({
  selector: 'app-admin-dashboard',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [EncabezadoPagina, FiltrosConsulta, BloqueGrafico, Icono],
  templateUrl: './admin-dashboard.html',
})
export class AdminDashboard {
  protected readonly periodo = signal<Periodo>('hoy');
  protected readonly unidad = signal<UnidadFiltro>('consolidado');

  protected readonly metodosPago = METODOS_PAGO;

  /** Texto del filtro activo, para que el resumen indique qué se está mirando. */
  protected readonly contexto = computed(() => {
    const p = PERIODOS.find((o) => o.valor === this.periodo())?.etiqueta ?? '';
    const u = UNIDADES.find((o) => o.valor === this.unidad())?.etiqueta ?? '';
    return `${p} · ${u}`;
  });

  protected readonly indicadores: readonly Indicador[] = [
    { etiqueta: 'Ventas del periodo', icono: 'ventas', nota: 'Disponible cuando existan ventas' },
    { etiqueta: 'Ventas Restaurante', icono: 'productos', nota: 'Disponible cuando existan ventas' },
    { etiqueta: 'Ventas Pollería', icono: 'llama', nota: 'Disponible cuando existan ventas' },
    { etiqueta: 'Estado general de caja', icono: 'caja', nota: 'Disponible con control de caja (M10)' },
  ];
}
