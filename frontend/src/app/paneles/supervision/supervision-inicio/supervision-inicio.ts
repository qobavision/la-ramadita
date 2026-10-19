import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { ComprasService } from '../../../inventario/compras.service';
import { InsumosService } from '../../../inventario/insumos.service';
import { Dialogo } from '../../../shared/ui/dialogo/dialogo';
import { EncabezadoPagina } from '../../../shared/ui/encabezado-pagina/encabezado-pagina';
import { Icono, NombreIcono } from '../../../shared/ui/icono/icono';
import { OpcionSegmento, Segmento } from '../../../shared/ui/segmento/segmento';
import { ColumnaTabla, TablaListado } from '../../../shared/ui/tabla-listado/tabla-listado';
import { TurnosService } from '../../../turnos/turnos.service';
import { ZonasService } from '../../../zonas/zonas.service';
import { SupervisionDemo } from '../datos/supervision.demo';
import { PersonalTurno, estadoStockDe, formatoSoles } from '../datos/supervision.models';
import {
  CajaMonitoreo,
  FiltroMesasInicio,
  PedidoCocinaMonitoreo,
  cajasMonitoreoIniciales,
  claseEstadoCocinaInicio,
  cocinaMonitoreoInicial,
  filtrarMesasMonitoreo,
  mesasMonitoreoDe,
  resumenProductos,
  textoOpcional,
  totalMesa,
} from './inicio.monitoreo';

interface IndicadorInicio {
  etiqueta: string;
  valor: string;
  nota?: string;
  icono: NombreIcono;
}

@Component({
  selector: 'app-supervision-inicio',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [EncabezadoPagina, Icono, Segmento, TablaListado, Dialogo],
  templateUrl: './supervision-inicio.html',
})
export class SupervisionInicio {
  private readonly demo = inject(SupervisionDemo);
  private readonly turnosApi = inject(TurnosService);
  private readonly comprasApi = inject(ComprasService);
  private readonly insumosApi = inject(InsumosService);
  private readonly zonasApi = inject(ZonasService);

  constructor() {
    this.turnosApi.cargarPersonal().subscribe({ error: () => undefined });
    this.comprasApi.cargar().subscribe({ error: () => undefined });
    this.insumosApi.cargar().subscribe({ error: () => undefined });
    this.zonasApi.cargarMesas().subscribe({ error: () => undefined });
  }

  protected readonly cajas = signal<CajaMonitoreo[]>(cajasMonitoreoIniciales());
  protected readonly cocina = signal<PedidoCocinaMonitoreo[]>(cocinaMonitoreoInicial());
  protected readonly mesas = computed(() => mesasMonitoreoDe(this.zonasApi.mesas()));
  protected readonly filtroMesas = signal<FiltroMesasInicio>('Todas');
  protected readonly pedidoDetalle = signal<PedidoCocinaMonitoreo | null>(null);

  protected readonly formatoSoles = formatoSoles;
  protected readonly resumenProductos = resumenProductos;
  protected readonly textoOpcional = textoOpcional;
  protected readonly totalMesa = totalMesa;
  protected readonly claseEstadoCocinaInicio = claseEstadoCocinaInicio;

  protected readonly filtrosMesas: readonly OpcionSegmento<FiltroMesasInicio>[] = [
    { valor: 'Todas', etiqueta: 'Todas' },
    { valor: 'Libre', etiqueta: 'Libres' },
    { valor: 'Ocupada', etiqueta: 'Ocupadas' },
  ];

  protected readonly indicadores = computed<readonly IndicadorInicio[]>(() => {
    const personal = this.turnosApi.personal();
    const insumos = this.insumosApi.lista().filter((i) => i.activo !== false);
    const bajos = insumos.filter((i) => estadoStockDe(i) === 'STOCK BAJO').length;
    const agotados = insumos.filter((i) => estadoStockDe(i) === 'AGOTADO').length;
    const compras = this.comprasApi.lista();
    const pendientes = compras.filter((c) => c.estado === 'Pendiente').length;
    const parciales = compras.filter((c) => c.estado === 'Parcial').length;

    return [
      {
        etiqueta: 'Pedidos en curso',
        valor: String(this.demo.pedidosEnCurso()),
        icono: 'pedidos',
      },
      {
        etiqueta: 'Pedidos atendidos hoy',
        valor: '0',
        icono: 'historial',
      },
      {
        etiqueta: 'Personal activo',
        valor: String(personal.length),
        nota: detallePersonal(personal),
        icono: 'empleados',
      },
      {
        etiqueta: 'Compras por recibir',
        valor: String(pendientes + parciales),
        nota: pendientes + parciales > 0 ? detalleCompras(pendientes, parciales) : undefined,
        icono: 'compras',
      },
      {
        etiqueta: 'Alertas de stock',
        valor: String(bajos + agotados),
        nota: bajos + agotados > 0 ? detalleStock(bajos, agotados) : undefined,
        icono: 'alerta',
      },
      {
        etiqueta: 'Ventas del día',
        valor: formatoSoles(0),
        icono: 'ventas',
      },
    ];
  });

  protected readonly mesasVisibles = computed(() => filtrarMesasMonitoreo(this.mesas(), this.filtroMesas()));
  protected readonly vacioMesas = computed(() => this.mesasVisibles().length === 0);

  protected readonly columnasCajas: readonly ColumnaTabla[] = [
    { titulo: 'Caja' },
    { titulo: 'Zona' },
    { titulo: 'Responsable' },
    { titulo: 'Hora de apertura' },
    { titulo: 'Estado' },
    { titulo: 'Monto cobrado', numerica: true },
  ];

  protected readonly columnasCocina: readonly ColumnaTabla[] = [
    { titulo: 'Pedido' },
    { titulo: 'Mesa / Para llevar' },
    { titulo: 'Mesero' },
    { titulo: 'Contenido' },
    { titulo: 'Estado' },
    { titulo: 'Tiempo' },
    { titulo: 'Acciones', numerica: true },
  ];

  protected readonly columnasMesas: readonly ColumnaTabla[] = [
    { titulo: 'Mesa' },
    { titulo: 'Zona' },
    { titulo: 'Mesero' },
    { titulo: 'Pedido' },
    { titulo: 'Tiempo' },
    { titulo: 'Total actual', numerica: true },
  ];

  protected abrirDetalle(pedido: PedidoCocinaMonitoreo): void {
    this.pedidoDetalle.set(pedido);
  }

  protected cerrarDetalle(): void {
    this.pedidoDetalle.set(null);
  }
}

function detallePersonal(personal: readonly PersonalTurno[]): string {
  if (personal.length === 0) {
    return 'Sin personal en turno';
  }
  let administradores = 0;
  let supervisores = 0;
  let meseros = 0;
  let cocina = 0;
  let caja = 0;
  for (const p of personal) {
    const funcion = p.funcionActual.toUpperCase();
    if (funcion.includes('ADMINISTRADOR')) {
      administradores += 1;
    } else if (funcion.includes('SUPERVISOR')) {
      supervisores += 1;
    } else if (funcion.includes('CAJA')) {
      caja += 1;
    } else if (funcion.includes('COCINA')) {
      cocina += 1;
    } else {
      meseros += 1;
    }
  }
  const partes: string[] = [];
  if (administradores > 0) {
    partes.push(`${administradores} administrador${administradores === 1 ? '' : 'es'}`);
  }
  if (supervisores > 0) {
    partes.push(`${supervisores} supervisor${supervisores === 1 ? '' : 'es'}`);
  }
  partes.push(`${meseros} ${meseros === 1 ? 'mesero' : 'meseros'}`);
  partes.push(`${cocina} cocina`);
  partes.push(`${caja} caja`);
  return partes.join(' · ');
}

function detalleStock(bajos: number, agotados: number): string {
  const etiquetaBajo = bajos === 1 ? 'bajo' : 'bajos';
  const etiquetaAgotado = agotados === 1 ? 'agotado' : 'agotados';
  return `${bajos} ${etiquetaBajo} · ${agotados} ${etiquetaAgotado}`;
}

function detalleCompras(pendientes: number, parciales: number): string {
  const etiquetaPendiente = pendientes === 1 ? 'pendiente' : 'pendientes';
  const etiquetaParcial = parciales === 1 ? 'parcial' : 'parciales';
  return `${pendientes} ${etiquetaPendiente} · ${parciales} ${etiquetaParcial}`;
}
