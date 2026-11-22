import { Periodo, UnidadFiltro } from '../shared/ui/filtros-consulta/filtros-consulta';

/** Contratos de `/api/v1/reportes` (M12) y `/api/v1/ventas` (M11). */

export interface SerieReporte {
  clave: string;
  etiqueta: string;
  total: number;
  cantidad: number;
}

export interface ProductoRanking {
  producto: string;
  cantidad: number;
  importe: number;
}

export interface AlertaStock {
  insumoId: number;
  insumo: string;
  stockActual: number;
  stockMinimo: number;
  unidadMedida: string;
  estado: 'STOCK_BAJO' | 'AGOTADO';
}

export interface ResumenVentas {
  desde: string;
  hasta: string;
  unidad: string;
  total: number;
  ventas: number;
  ticketPromedio: number;
  porUnidad: SerieReporte[];
  porMetodo: SerieReporte[];
  porDia: SerieReporte[];
  porHora: SerieReporte[];
  topProductos: ProductoRanking[];
  alertasStock: AlertaStock[];
}

export interface MontoMetodo {
  metodo: string;
  monto: number;
}

export interface VentaResumen {
  atencionId: number;
  fecha: string;
  unidad: string;
  tipo: 'MESA' | 'PARA_LLEVAR';
  identificacion: string;
  zona: string | null;
  mesero: string;
  consumo: number;
  cobrado: number;
  metodos: MontoMetodo[];
}

export interface VentaDetalle {
  venta: VentaResumen;
  productos: { producto: string; cantidad: number; precioUnitario: number; subtotal: number; observacion: string | null }[];
  pagos: { metodo: string; monto: number; fecha: string; cajero: string; estado: string }[];
  comprobantes: {
    tipo: string;
    serie: string;
    numero: number;
    total: number;
    fecha: string;
    cliente: string | null;
    estado: 'EMITIDO' | 'ANULADO';
  }[];
}

/** Filtro común de las consultas del administrador (fechas AAAA-MM-DD en hora local de Lima). */
export interface FiltroReporte {
  desde: string;
  hasta: string;
  unidad: UnidadFiltro;
}

/**
 * Color fijo por unidad de negocio (sigue a la entidad, nunca al orden).
 * Par validado con el script de dataviz sobre la superficie de tarjeta #fffdf9.
 */
export const COLOR_UNIDAD: Record<string, string> = {
  RESTAURANTE: 'var(--lr-serie-restaurante)',
  POLLERIA: 'var(--lr-serie-polleria)',
};

/** Convierte el periodo elegido en fechas concretas. La semana empieza el lunes. */
export function rangoDe(periodo: Periodo, desde: string, hasta: string, hoy = new Date()): { desde: string; hasta: string } {
  const dia = (d: Date) =>
    `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
  const base = new Date(hoy.getFullYear(), hoy.getMonth(), hoy.getDate());
  switch (periodo) {
    case 'ayer': {
      const ayer = new Date(base);
      ayer.setDate(base.getDate() - 1);
      return { desde: dia(ayer), hasta: dia(ayer) };
    }
    case 'semana': {
      const lunes = new Date(base);
      lunes.setDate(base.getDate() - ((base.getDay() + 6) % 7));
      return { desde: dia(lunes), hasta: dia(base) };
    }
    case 'mes':
      return { desde: dia(new Date(base.getFullYear(), base.getMonth(), 1)), hasta: dia(base) };
    case 'rango': {
      const inicio = desde || dia(base);
      const fin = hasta || inicio;
      return inicio <= fin ? { desde: inicio, hasta: fin } : { desde: fin, hasta: inicio };
    }
    default:
      return { desde: dia(base), hasta: dia(base) };
  }
}

/** "2026-11-22" → "22 nov" para ejes y tablas. */
export function etiquetaDia(iso: string): string {
  const [a, m, d] = iso.split('-').map(Number);
  return new Date(a, m - 1, d).toLocaleDateString('es-PE', { day: 'numeric', month: 'short' }).replace('.', '');
}

/** Día local (hora de Lima en el equipo) de un instante UTC del backend: "22 nov". */
export function fechaCorta(iso: string | null | undefined): string {
  if (!iso) {
    return '—';
  }
  return new Date(iso).toLocaleDateString('es-PE', { day: 'numeric', month: 'short' }).replace('.', '');
}

export function porcentaje(parte: number, total: number): string {
  return total > 0 ? `${Math.round((parte / total) * 100)} %` : '0 %';
}
