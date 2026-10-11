import { Rol } from '../../../auth/auth.models';

export type UnidadNegocio = 'Restaurante' | 'Pollería';
export type EstadoTurno = 'ACTIVO' | 'EN CIERRE' | 'CERRADO';
export type EstadoCaja = 'Abierta' | 'Pendiente de cierre' | 'Cerrada';
export type RolTemporal = 'MESERO' | 'COCINA' | 'CAJA';
export type EstadoMesaOp = 'Libre' | 'Ocupada';
export type EstadoPedido = 'En curso' | 'Pendiente cocina' | 'Preparado' | 'Modificado' | 'Cancelado';
export type EstadoCompra = 'Pendiente' | 'Parcial' | 'Recibida';
export type EstadoItemCompra = 'Pendiente' | 'Parcial' | 'Recibido';
export type EstadoStock = 'NORMAL' | 'STOCK BAJO' | 'AGOTADO';
export type TipoDocumento = 'RUC' | 'DNI';
export type UnidadInsumo = 'Unidad' | 'Kilogramo (kg)' | 'Gramo (g)' | 'Litro (L)' | 'Mililitro (ml)';
export type TipoMovimiento = 'ENTRADA' | 'CONSUMO' | 'AJUSTE';
export type DisponibilidadCarta = 'Disponible' | 'Agotado';

export const UNIDADES_INSUMO: readonly UnidadInsumo[] = [
  'Unidad',
  'Kilogramo (kg)',
  'Gramo (g)',
  'Litro (L)',
  'Mililitro (ml)',
];

export const ROLES_TEMPORALES: readonly RolTemporal[] = ['MESERO', 'COCINA', 'CAJA'];

export interface HorarioBase {
  unidad: UnidadNegocio;
  horaInicio: string;
  horaFin: string;
  configurado: boolean;
}

export interface TurnoOperativo {
  unidad: UnidadNegocio;
  horaInicio: string;
  horaFin: string;
  estado: EstadoTurno;
  caja: EstadoCaja;
  alerta: string | null;
}

export interface PendienteCierre {
  etiqueta: string;
  cantidad: number;
}

export interface PersonalTurno {
  id: number;
  empleado: string;
  username: string;
  rolPermanente: Rol;
  funcionActual: string;
  horaIngreso: string;
  estado: 'En turno' | 'Asignación temporal';
}

export interface AsignacionTemporal {
  id: number;
  empleado: string;
  username: string;
  rolPermanente: Rol;
  rolTemporal: RolTemporal | null;
  turno: UnidadNegocio;
  estado: 'Sin asignación' | 'Vigente';
}

export interface ZonaDemo {
  id: number;
  nombre: string;
  activa: boolean;
}

export interface MesaDemo {
  id: number;
  nombre: string;
  zonaId: number;
  zona: string;
  activa: boolean;
  estadoOp: EstadoMesaOp;
  atencion: string | null;
  mesero: string | null;
  tiempo: string | null;
}

export interface PedidoDemo {
  id: string;
  origen: string;
  estado: EstadoPedido;
  hora: string;
  responsable: string;
  cocina: string;
}

export interface ProveedorDemo {
  id: number;
  nombre: string;
  tipoDocumento: TipoDocumento;
  documento: string;
  telefono: string;
  contacto: string;
  activo: boolean;
}

export interface LineaCompra {
  id: number;
  insumoId: number;
  insumo: string;
  unidad: UnidadInsumo;
  cantidad: number;
  precioUnitario: number;
  recibido: number;
}

export interface CompraDemo {
  id: number;
  codigo: string;
  proveedorId: number;
  proveedor: string;
  fecha: string;
  estado: EstadoCompra;
  total: number;
  lineas: LineaCompra[];
}

export interface InsumoDemo {
  id: number;
  nombre: string;
  unidad: UnidadInsumo;
  stock: number;
  minimo: number;
  estado?: EstadoStock;
  activo?: boolean;
}

export interface MovimientoDemo {
  id: number;
  fecha: string;
  insumo: string;
  tipo: TipoMovimiento;
  cantidad: number;
  stockResultante: number;
  origen: string;
}

export interface RecetaInsumo {
  insumoId: number;
  insumo: string;
  unidad: UnidadInsumo;
  cantidad: number;
}

export interface RecetaDemo {
  id: number;
  nombre: string;
  insumos: RecetaInsumo[];
}

export interface ProductoDemo {
  id: number;
  nombre: string;
  categoria: string;
  precio: string;
  activo: boolean;
  unidades: UnidadNegocio[];
}

export interface ItemCartaDemo {
  id: number;
  productoId: number;
  producto: string;
  unidad: UnidadNegocio;
  precio: string;
  disponibilidad: DisponibilidadCarta;
}

export function claseEstadoTurno(estado: EstadoTurno): string {
  if (estado === 'ACTIVO') {
    return 'lr-estado--activo';
  }
  if (estado === 'EN CIERRE') {
    return 'lr-estado--abierta';
  }
  return 'lr-estado--inactivo';
}

export function estadoStockDe(insumo: Pick<InsumoDemo, 'stock' | 'minimo' | 'estado'>): EstadoStock {
  if (insumo.estado) {
    return insumo.estado;
  }
  const actual = Math.max(0, insumo.stock);
  if (actual === 0) {
    return 'AGOTADO';
  }
  if (actual <= insumo.minimo) {
    return 'STOCK BAJO';
  }
  return 'NORMAL';
}

export function claseEstadoStock(estado: EstadoStock): string {
  if (estado === 'NORMAL') {
    return 'lr-estado--activo';
  }
  if (estado === 'STOCK BAJO') {
    return 'lr-estado--abierta';
  }
  return 'lr-estado--alerta';
}

export function estadoItemCompraDe(linea: LineaCompra): EstadoItemCompra {
  if (linea.recibido <= 0) {
    return 'Pendiente';
  }
  if (linea.recibido < linea.cantidad) {
    return 'Parcial';
  }
  return 'Recibido';
}

export function estadoCompraDe(compra: CompraDemo): EstadoCompra {
  if (compra.estado) {
    return compra.estado;
  }
  if (compra.lineas.length === 0 || compra.lineas.every((l) => l.recibido <= 0)) {
    return 'Pendiente';
  }
  if (compra.lineas.every((l) => l.recibido >= l.cantidad)) {
    return 'Recibida';
  }
  return 'Parcial';
}

export function claseEstadoCompra(estado: EstadoCompra | EstadoItemCompra): string {
  if (estado === 'Recibida' || estado === 'Recibido') {
    return 'lr-estado--activo';
  }
  if (estado === 'Parcial') {
    return 'lr-estado--abierta';
  }
  return 'lr-estado';
}

export function totalCompraDe(compra: CompraDemo): number {
  if (typeof compra.total === 'number') {
    return compra.total;
  }
  return compra.lineas.reduce((suma, linea) => suma + linea.cantidad * linea.precioUnitario, 0);
}

export function resumenInsumosDe(lineas: readonly LineaCompra[], soloPendientes = false): string {
  const nombres = lineas
    .filter((l) => !soloPendientes || l.recibido < l.cantidad)
    .map((l) => l.insumo);
  if (nombres.length === 0) {
    return '—';
  }
  if (nombres.length <= 2) {
    return nombres.join(' · ');
  }
  return `${nombres[0]} · ${nombres[1]} · +${nombres.length - 2} más`;
}

export function abreviarUnidad(unidad: UnidadInsumo): string {
  if (unidad === 'Unidad') {
    return 'und';
  }
  if (unidad === 'Kilogramo (kg)') {
    return 'kg';
  }
  if (unidad === 'Gramo (g)') {
    return 'g';
  }
  if (unidad === 'Litro (L)') {
    return 'L';
  }
  return 'ml';
}

export function formatoSoles(valor: number): string {
  return `S/ ${valor.toFixed(2)}`;
}

export function formatoCantidad(valor: number): string {
  if (Number.isInteger(valor)) {
    return String(valor);
  }
  return String(Number(valor.toFixed(3)));
}

export function fechaHoyLocal(): string {
  const fecha = new Date();
  const dia = String(fecha.getDate()).padStart(2, '0');
  const mes = String(fecha.getMonth() + 1).padStart(2, '0');
  return `${dia}/${mes}/${fecha.getFullYear()}`;
}

export function pendienteDe(linea: LineaCompra): number {
  return Math.max(0, Number((linea.cantidad - linea.recibido).toFixed(3)));
}

export function clasePedido(estado: EstadoPedido): string {
  if (estado === 'Preparado') {
    return 'lr-estado--activo';
  }
  if (estado === 'Cancelado') {
    return 'lr-estado--inactivo';
  }
  if (estado === 'Modificado') {
    return 'lr-estado--alerta';
  }
  return 'lr-estado--abierta';
}
