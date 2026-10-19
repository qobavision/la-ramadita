import { formatearFechaHora } from '../personal/personal.models';
import {
  CompraDemo,
  EstadoCompra,
  EstadoStock,
  InsumoDemo,
  LineaCompra,
  MovimientoDemo,
  ProveedorDemo,
  TipoDocumento,
  TipoMovimiento,
  UnidadInsumo,
  estadoStockDe,
} from '../paneles/supervision/datos/supervision.models';

export interface InsumoApi {
  id: number;
  nombre: string;
  unidadMedida: string;
  stockActual: number;
  stockMinimo: number;
  estado: 'NORMAL' | 'STOCK_BAJO' | 'AGOTADO';
  activo: boolean;
}

export interface ProveedorApi {
  id: number;
  nombreRazonSocial: string;
  tipoDocumento: TipoDocumento;
  documento: string;
  celular: string | null;
  personaContacto: string | null;
  activo: boolean;
  archivado?: boolean;
}

export interface CompraDetalleApi {
  id: number;
  insumoId: number;
  insumo: string;
  unidadMedida: string;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
  cantidadRecibida: number;
  cantidadPendiente: number;
  estado: string;
}

export interface CompraApi {
  id: number;
  codigo: string;
  proveedorId: number;
  proveedor: string;
  fechaRegistro: string;
  estado: string;
  total: number;
  registradoPor: string | null;
  detalles: CompraDetalleApi[];
}

export interface MovimientoApi {
  id: number;
  fechaHora: string;
  insumoId: number;
  insumo: string;
  unidadMedida: string;
  tipo: 'ENTRADA' | 'CONSUMO' | 'AJUSTE';
  cantidad: number;
  stockAnterior: number;
  stockResultante: number;
  origen: string | null;
  usuario: string;
}

export function unidadDesdeApi(unidad: string): UnidadInsumo {
  switch (unidad) {
    case 'KILOGRAMO':
      return 'Kilogramo (kg)';
    case 'GRAMO':
      return 'Gramo (g)';
    case 'LITRO':
      return 'Litro (L)';
    case 'MILILITRO':
      return 'Mililitro (ml)';
    default:
      return 'Unidad';
  }
}

export function unidadHaciaApi(unidad: UnidadInsumo): string {
  switch (unidad) {
    case 'Kilogramo (kg)':
      return 'KILOGRAMO';
    case 'Gramo (g)':
      return 'GRAMO';
    case 'Litro (L)':
      return 'LITRO';
    case 'Mililitro (ml)':
      return 'MILILITRO';
    default:
      return 'UNIDAD';
  }
}

const FACTORES_UNIDAD: Record<string, number> = {
  'Kilogramo (kg)>Gramo (g)': 1000,
  'Gramo (g)>Kilogramo (kg)': 0.001,
  'Litro (L)>Mililitro (ml)': 1000,
  'Mililitro (ml)>Litro (L)': 0.001,
};

export function factorConversionUnidad(desde: UnidadInsumo, hacia: UnidadInsumo): number | null {
  if (desde === hacia) {
    return 1;
  }
  return FACTORES_UNIDAD[`${desde}>${hacia}`] ?? null;
}

export function convertirCantidadUnidad(cantidad: number, factor: number): number {
  return Math.round(cantidad * factor * 1000) / 1000;
}

export function mensajeUnidadIncompatible(desde: UnidadInsumo, hacia: UnidadInsumo): string {
  return `No se puede cambiar de ${desde} a ${hacia}: no hay conversión entre esas unidades. Quita el insumo de las recetas, elimínalo y créalo de nuevo con la unidad correcta.`;
}

export function estadoStockDesdeApi(estado: InsumoApi['estado']): EstadoStock {
  if (estado === 'STOCK_BAJO') {
    return 'STOCK BAJO';
  }
  return estado;
}

export function estadoCompraDesdeApi(estado: string): EstadoCompra {
  if (estado === 'RECIBIDA') {
    return 'Recibida';
  }
  if (estado === 'PARCIAL') {
    return 'Parcial';
  }
  return 'Pendiente';
}

export function tipoMovimientoDesdeApi(tipo: MovimientoApi['tipo']): TipoMovimiento {
  if (tipo === 'CONSUMO') {
    return 'CONSUMO';
  }
  if (tipo === 'AJUSTE') {
    return 'AJUSTE';
  }
  return 'ENTRADA';
}

export type FiltroEstadoInsumo = 'ACTIVOS' | 'INACTIVOS' | 'TODOS';

export function mapearInsumo(api: InsumoApi): InsumoDemo {
  return {
    id: api.id,
    nombre: api.nombre,
    unidad: unidadDesdeApi(api.unidadMedida),
    stock: Number(api.stockActual),
    minimo: Number(api.stockMinimo),
    estado: estadoStockDesdeApi(api.estado),
    activo: api.activo,
  };
}

export function coincideFiltroInsumo(
  insumo: Pick<InsumoDemo, 'nombre' | 'unidad' | 'activo'>,
  filtro: FiltroEstadoInsumo,
  busqueda = '',
): boolean {
  const texto = busqueda.trim().toLowerCase();
  const coincideTexto =
    !texto || insumo.nombre.toLowerCase().includes(texto) || insumo.unidad.toLowerCase().includes(texto);
  if (filtro === 'ACTIVOS') {
    return coincideTexto && insumo.activo !== false;
  }
  if (filtro === 'INACTIVOS') {
    return coincideTexto && insumo.activo === false;
  }
  return coincideTexto;
}

export function etiquetaEstadoOperativo(insumo: Pick<InsumoDemo, 'activo' | 'stock' | 'minimo' | 'estado'>): string {
  return insumo.activo === false ? 'Inactivo' : estadoStockDe(insumo);
}

export function muestraEstadoStock(insumo: Pick<InsumoDemo, 'activo'>): boolean {
  return insumo.activo !== false;
}

export function mapearProveedor(api: ProveedorApi): ProveedorDemo {
  return {
    id: api.id,
    nombre: api.nombreRazonSocial,
    tipoDocumento: api.tipoDocumento,
    documento: api.documento,
    telefono: api.celular ?? '',
    contacto: api.personaContacto ?? '',
    activo: api.activo,
  };
}

export function mapearLinea(api: CompraDetalleApi): LineaCompra {
  return {
    id: api.id,
    insumoId: api.insumoId,
    insumo: api.insumo,
    unidad: unidadDesdeApi(api.unidadMedida),
    cantidad: Number(api.cantidad),
    precioUnitario: Number(api.precioUnitario),
    recibido: Number(api.cantidadRecibida),
  };
}

export function mapearCompra(api: CompraApi): CompraDemo {
  return {
    id: api.id,
    codigo: api.codigo,
    proveedorId: api.proveedorId,
    proveedor: api.proveedor,
    fecha: formatearFechaLocal(api.fechaRegistro),
    estado: estadoCompraDesdeApi(api.estado),
    total: Number(api.total),
    lineas: api.detalles.map(mapearLinea),
  };
}

export function mapearMovimiento(api: MovimientoApi): MovimientoDemo {
  return {
    id: api.id,
    fecha: formatearFechaHora(api.fechaHora),
    insumo: api.insumo,
    tipo: tipoMovimientoDesdeApi(api.tipo),
    cantidad: Number(api.cantidad),
    stockResultante: Number(api.stockResultante),
    origen: api.origen ?? '—',
  };
}

function formatearFechaLocal(iso: string): string {
  const texto = formatearFechaHora(iso);
  return texto === '—' ? texto : texto.slice(0, 10);
}
