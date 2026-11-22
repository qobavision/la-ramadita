/** Contratos de `/api/v1/cocina` (M08 Cocina). */

export type EstadoComanda = 'PENDIENTE' | 'PREPARADO' | 'CANCELADO';

export interface LineaComanda {
  id: number;
  producto: string;
  cantidad: number;
  observacion: string | null;
  estado: EstadoComanda;
  /** Cambió o se canceló después del envío (RN-06). */
  modificado: boolean;
  /** Cantidad antes del último cambio, para mostrar "antes 2". */
  cantidadAnterior: number | null;
  /** Descripción del último cambio o de la cancelación. */
  cambio: string | null;
  preparadoPor: string | null;
  preparadoEn: string | null;
}

export interface Comanda {
  pedidoId: number;
  atencionId: number;
  tipo: 'MESA' | 'PARA_LLEVAR';
  identificacion: string;
  zona: string | null;
  mesero: string;
  enviado: string;
  estado: EstadoComanda;
  modificado: boolean;
  /** Unidades que faltan preparar. */
  pendientes: number;
  /** Unidades activas (sin contar canceladas). */
  unidades: number;
  listo: string | null;
  lineas: LineaComanda[];
}

export interface TableroCocina {
  pendientes: Comanda[];
  preparados: Comanda[];
}

export interface HistorialCocina {
  turno: string | null;
  comandas: Comanda[];
}

/** Avisos del canal `cocina` que llegan por WebSocket. */
export type TipoAvisoCocina = 'PEDIDO_NUEVO' | 'PEDIDO_MODIFICADO' | 'PEDIDO_CANCELADO';

export const ETIQUETA_AVISO: Record<TipoAvisoCocina, string> = {
  PEDIDO_NUEVO: 'Pedido nuevo',
  PEDIDO_MODIFICADO: 'Pedido modificado',
  PEDIDO_CANCELADO: 'Pedido cancelado',
};

export const ETIQUETA_ESTADO_COMANDA: Record<EstadoComanda, string> = {
  PENDIENTE: 'Pendiente',
  PREPARADO: 'Preparado',
  CANCELADO: 'Cancelado',
};

/** Minutos de espera a partir de los cuales una comanda pendiente se resalta. */
export const MINUTOS_DEMORA = 15;

export function esAvisoCocina(tipo: string): tipo is TipoAvisoCocina {
  return tipo in ETIQUETA_AVISO;
}

/** Minutos entre dos instantes UTC del backend (o hasta ahora). */
export function minutosEntre(desde: string | null, hasta: string | number | null = Date.now()): number {
  if (!desde || hasta === null) {
    return 0;
  }
  const fin = typeof hasta === 'number' ? hasta : new Date(hasta).getTime();
  return Math.max(0, Math.floor((fin - new Date(desde).getTime()) / 60000));
}
