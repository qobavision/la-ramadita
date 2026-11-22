/** Contratos de `/api/v1/caja` (M09 cobros y M10 control de caja). */

export interface MetodoPago {
  id: number;
  nombre: string;
}

export interface PagoCaja {
  id: number;
  atencionId: number;
  identificacion: string;
  metodo: string;
  monto: number;
  cajero: string;
  fecha: string;
  observacion: string | null;
  porProductos: boolean;
  estado: 'CONFIRMADO' | 'ANULADO';
  anuladoPor: string | null;
  fechaAnulacion: string | null;
  motivoAnulacion: string | null;
}

export interface MovimientoCaja {
  id: number;
  tipo: 'INGRESO' | 'EGRESO';
  monto: number;
  motivo: string;
  usuario: string;
  fecha: string;
}

export interface CajaSesion {
  id: number;
  estado: 'ABIERTA' | 'CERRADA';
  responsable: string;
  unidad: string;
  turnoId: number;
  apertura: string;
  cierre: string | null;
  montoInicial: number;
  totalCobrado: number;
  porMetodo: { metodo: string; total: number }[];
  ingresos: number;
  egresos: number;
  efectivoEsperado: number;
  efectivoContado: number | null;
  diferencia: number | null;
  observacionCierre: string | null;
  movimientos: MovimientoCaja[];
  pagos: PagoCaja[];
  /** Pagos anulados por el administrador: evidencia de quién, cuándo y por qué. */
  anulados: PagoCaja[];
}

export interface CuentaResumen {
  atencionId: number;
  tipo: 'MESA' | 'PARA_LLEVAR';
  identificacion: string;
  zona: string | null;
  mesero: string;
  inicio: string;
  total: number;
  pagado: number;
  saldo: number;
}

export interface LineaCuenta {
  pedidoDetalleId: number;
  producto: string;
  observacion: string | null;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
  cantidadPagada: number;
  cantidadPendiente: number;
}

export interface Cuenta extends CuentaResumen {
  unidad: string;
  estado: 'PENDIENTE' | 'PAGADO';
  lineas: LineaCuenta[];
  pagos: PagoCaja[];
}

export interface SolicitudPago {
  metodo: string;
  monto?: number | null;
  observacion?: string | null;
  lineas?: { pedidoDetalleId: number; cantidad: number }[];
}

/** Etiqueta legible del método (los nombres vienen en mayúsculas desde la BD). */
export function etiquetaMetodo(nombre: string): string {
  return nombre.charAt(0) + nombre.slice(1).toLowerCase();
}

/** Boleta simple (M10, sin SUNAT) emitida al quedar pagada la cuenta. */
export interface Boleta {
  id: number;
  tipo: 'BOLETA';
  codigo: string;
  estado: 'EMITIDO' | 'ANULADO';
  fechaEmision: string;
  unidad: string;
  identificacion: string;
  mesero: string;
  emitidoPor: string;
  total: number;
  lineas: { producto: string; cantidad: number; precioUnitario: number; subtotal: number }[];
  pagos: { metodo: string; monto: number }[];
}
