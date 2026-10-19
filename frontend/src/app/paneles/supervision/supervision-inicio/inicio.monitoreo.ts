import { EstadoMesaOp, MesaDemo, formatoSoles } from '../datos/supervision.models';

export type FiltroMesasInicio = 'Ocupada' | 'Libre' | 'Todas';
export type EstadoCocinaInicio = 'En preparación' | 'Preparado';

export interface CajaMonitoreo {
  id: number;
  nombre: string;
  zona: string;
  responsable: string;
  horaApertura: string;
  estado: 'Abierta' | 'Cerrada';
  monto: number;
}

export interface PedidoCocinaMonitoreo {
  id: string;
  origen: string;
  mesero: string;
  productos: readonly string[];
  estado: EstadoCocinaInicio;
  tiempo: string;
}

export interface MesaMonitoreo {
  id: number;
  nombre: string;
  zona: string;
  estado: EstadoMesaOp;
  mesero: string | null;
  pedido: string | null;
  tiempo: string | null;
  total: number | null;
}

export function cajasMonitoreoIniciales(): CajaMonitoreo[] {
  return [];
}

export function cocinaMonitoreoInicial(): PedidoCocinaMonitoreo[] {
  return [];
}

export function mesasMonitoreoIniciales(): MesaMonitoreo[] {
  return [];
}

export function mesasMonitoreoDe(mesas: readonly MesaDemo[]): MesaMonitoreo[] {
  return mesas
    .filter((mesa) => mesa.activa)
    .map((mesa) => ({
      id: mesa.id,
      nombre: mesa.nombre,
      zona: mesa.zona,
      estado: mesa.estadoOp,
      mesero: mesa.mesero,
      pedido: mesa.atencion,
      tiempo: mesa.tiempo,
      total: null,
    }));
}

export function resumenProductos(productos: readonly string[]): string {
  if (productos.length === 0) {
    return '—';
  }
  if (productos.length <= 2) {
    return productos.join(' + ');
  }
  return `${productos[0]} + ${productos[1]} +${productos.length - 2} más`;
}

export function resumenMesas(mesas: readonly MesaMonitoreo[]): string {
  const ocupadas = mesas.filter((m) => m.estado === 'Ocupada').length;
  const libres = mesas.filter((m) => m.estado === 'Libre').length;
  return `${ocupadas} ocupadas · ${libres} libres`;
}

export function filtrarMesasMonitoreo(mesas: readonly MesaMonitoreo[], filtro: FiltroMesasInicio): MesaMonitoreo[] {
  if (filtro === 'Todas') {
    return [...mesas];
  }
  return mesas.filter((m) => m.estado === filtro);
}

export function textoOpcional(valor: string | null | undefined): string {
  return valor?.trim() ? valor : '—';
}

export function totalMesa(mesa: MesaMonitoreo): string {
  return mesa.total == null ? '—' : formatoSoles(mesa.total);
}

export function claseEstadoCocinaInicio(estado: EstadoCocinaInicio): string {
  return estado === 'Preparado' ? 'lr-estado--activo' : 'lr-estado--alerta';
}
