import { SeccionCarta } from '../paneles/supervision/datos/supervision.models';

/** Contratos de `/api/v1/atenciones` (M06 Mesas y atención + M07 Pedidos). */

export type TipoAtencion = 'MESA' | 'PARA_LLEVAR';
export type EstadoAtencion = 'ABIERTA' | 'PENDIENTE_PAGO' | 'CERRADA';
export type EstadoMesa = 'DISPONIBLE' | 'OCUPADA' | 'PENDIENTE_PAGO';

export interface AtencionResumen {
  id: number;
  tipo: TipoAtencion;
  identificacion: string;
  mesaId: number | null;
  zona: string | null;
  mesero: string;
  meseroUsername: string;
  estado: EstadoAtencion;
  inicio: string;
  total: number;
  itemsBorrador: number;
  itemsPendientes: number;
  itemsPreparados: number;
}

export interface MesaSalon {
  id: number;
  nombre: string;
  zonaId: number;
  zona: string;
  estado: EstadoMesa;
  atencion: AtencionResumen | null;
}

export interface LineaPedido {
  id: number;
  productoId: number;
  producto: string;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
  observacion: string | null;
  estadoCocina: 'PENDIENTE' | 'PREPARADO';
  estadoItem: 'ACTIVO' | 'CANCELADO';
}

export interface PedidoAtencion {
  id: number;
  estado: 'BORRADOR' | 'ENVIADO_COCINA' | 'CERRADO' | 'CANCELADO';
  fechaEnvio: string | null;
  total: number;
  detalles: LineaPedido[];
}

export interface Atencion {
  id: number;
  tipo: TipoAtencion;
  identificacion: string;
  mesaId: number | null;
  zona: string | null;
  mesero: string;
  meseroUsername: string;
  unidad: string;
  estado: EstadoAtencion;
  inicio: string;
  totalEnviado: number;
  totalBorrador: number;
  borrador: PedidoAtencion | null;
  enviados: PedidoAtencion[];
}

/** Línea de la carta vigente; `id` es el id de `carta_detalle`. */
export interface ProductoCarta {
  id: number;
  productoId: number;
  nombre: string;
  seccion: SeccionCarta;
  precioEfectivo: number;
  estado: 'DISPONIBLE' | 'AGOTADO';
}

export interface CartaMesero {
  id: number;
  nombre: string;
  ambito: string;
  detalles: ProductoCarta[];
}

export const ETIQUETA_SECCION: Record<SeccionCarta, string> = {
  MENU_CRIOLLO: 'Menú criollo',
  MENU_CRIOLLO_ENTRADA: 'Entradas del menú',
  MENU_CRIOLLO_SEGUNDO: 'Segundos del menú',
  MENU_EJECUTIVO: 'Menú ejecutivo',
  A_LA_CARTA: 'A la carta',
  BEBIDA: 'Bebidas',
  POSTRE: 'Postres',
  OTRO: 'Otros',
  BRASA: 'A la brasa',
  CHIFA: 'Chifa',
  EXTRA: 'Extras',
  COMBO: 'Combos',
};

export const ETIQUETA_ESTADO_MESA: Record<EstadoMesa, string> = {
  DISPONIBLE: 'Libre',
  OCUPADA: 'Ocupada',
  PENDIENTE_PAGO: 'Por cobrar',
};

/** Tiempo transcurrido desde `iso` (instante UTC del backend), p. ej. "8 min" o "1 h 05 min". */
export function tiempoDesde(iso: string | null | undefined, ahora = Date.now()): string {
  if (!iso) {
    return '—';
  }
  const minutos = Math.max(0, Math.floor((ahora - new Date(iso).getTime()) / 60000));
  if (minutos < 60) {
    return `${minutos} min`;
  }
  return `${Math.floor(minutos / 60)} h ${String(minutos % 60).padStart(2, '0')} min`;
}

/** Hora local (HH:mm) de un instante UTC del backend. */
export function horaDe(iso: string | null | undefined): string {
  if (!iso) {
    return '—';
  }
  const fecha = new Date(iso);
  return `${String(fecha.getHours()).padStart(2, '0')}:${String(fecha.getMinutes()).padStart(2, '0')}`;
}
