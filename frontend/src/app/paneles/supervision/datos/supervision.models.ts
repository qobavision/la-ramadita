import { Rol, ROL_ETIQUETA } from '../../../auth/auth.models';

export type UnidadNegocio = 'Restaurante' | 'Pollería';
export type EstadoTurno = 'ACTIVO' | 'EN CIERRE' | 'CERRADO';
export type EstadoCaja = 'Abierta' | 'Pendiente de cierre' | 'Cerrada';
export type RolTemporal = 'SUPERVISOR' | 'MESERO' | 'COCINA' | 'CAJA';
export type EstadoMesaOp = 'Libre' | 'Ocupada';
export type EstadoPedido = 'En curso' | 'Pendiente cocina' | 'Preparado' | 'Modificado' | 'Cancelado';
export type EstadoCompra = 'Pendiente' | 'Parcial' | 'Recibida';
export type EstadoItemCompra = 'Pendiente' | 'Parcial' | 'Recibido';
export type EstadoStock = 'NORMAL' | 'STOCK BAJO' | 'AGOTADO';
export type TipoDocumento = 'RUC' | 'DNI';
export type UnidadInsumo = 'Unidad' | 'Kilogramo (kg)' | 'Gramo (g)' | 'Litro (L)' | 'Mililitro (ml)';
export type TipoMovimiento = 'ENTRADA' | 'CONSUMO' | 'AJUSTE';

export const UNIDADES_INSUMO: readonly UnidadInsumo[] = [
  'Unidad',
  'Kilogramo (kg)',
  'Gramo (g)',
  'Litro (L)',
  'Mililitro (ml)',
];

export const ROLES_TEMPORALES: readonly RolTemporal[] = ['MESERO', 'COCINA', 'CAJA'];
export const FUNCIONES_ADMINISTRADOR: readonly RolTemporal[] = ['SUPERVISOR', 'MESERO', 'COCINA', 'CAJA'];

export interface HorarioBase {
  unidad: UnidadNegocio;
  horaInicio: string;
  horaFin: string;
  configurado: boolean;
}

export interface TurnoOperativo {
  id: number | null;
  unidad: UnidadNegocio;
  horaInicio: string;
  horaFin: string;
  estado: EstadoTurno;
  caja: EstadoCaja;
  alerta: string | null;
  responsable: string;
  cerradoPor: string;
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
  mesas: number;
  cajas: number;
  cocinas: number;
  uso?: 'sin-uso' | 'historial' | 'operacion-activa';
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
  categoria?: CategoriaProducto;
  ambito?: AmbitoProducto;
  precio?: number;
  insumos: RecetaInsumo[];
}

// ---------- Productos y carta del día ----------
// Espejo de dbo.productos, dbo.cartas y dbo.carta_detalle (V1). Lo marcado
// como "propuesta" todavía no existe en la base y requiere una migración nueva.

/** dbo.productos.ambito */
export type AmbitoProducto = 'RESTAURANTE' | 'POLLERIA' | 'AMBOS';

/** dbo.productos.categoria */
export type CategoriaProducto =
  | 'Menú'
  | 'Entrada'
  | 'Segundo'
  | 'Plato ejecutivo'
  | 'Plato marino'
  | 'Plato chifa'
  | 'Plato a la brasa'
  | 'Bebida'
  | 'Ensalada'
  | 'Guarnición'
  | 'Combo'
  | 'Postre'
  | 'Extra'
  | 'Otro';

export const CATEGORIAS_PRODUCTO: readonly CategoriaProducto[] = [
  'Menú',
  'Entrada',
  'Segundo',
  'Plato ejecutivo',
  'Plato marino',
  'Plato chifa',
  'Plato a la brasa',
  'Bebida',
  'Ensalada',
  'Guarnición',
  'Combo',
  'Postre',
  'Extra',
  'Otro',
];

/** Categorías que el supervisor elige al armar una receta. */
export const CATEGORIAS_RECETA: readonly CategoriaProducto[] = [
  'Entrada',
  'Segundo',
  'Plato ejecutivo',
  'Plato marino',
  'Plato chifa',
  'Plato a la brasa',
  'Bebida',
  'Ensalada',
  'Extra',
];

/** Propuesta dbo.producto_componente: un producto armado con otros productos (1/4 pollo + papas + ensalada). */
export interface ComponenteProducto {
  productoId: number;
  nombre: string;
  cantidad: number;
}

/**
 * dbo.productos. El `id` es el código que viaja en el pedido; el mesero solo
 * ve el `nombre`. `precio` es el precio base; la carta del día puede
 * sobreescribirlo con `DetalleCarta.precioDia`.
 */
export interface Producto {
  id: number;
  nombre: string;
  categoria: CategoriaProducto;
  ambito: AmbitoProducto;
  precio: number;
  activo: boolean;
  componentes?: ComponenteProducto[];
}

/** Propuesta dbo.carta_detalle.seccion: bloque de la carta en el que aparece el producto. */
export type SeccionCarta =
  | 'MENU_CRIOLLO'
  | 'MENU_CRIOLLO_ENTRADA'
  | 'MENU_CRIOLLO_SEGUNDO'
  | 'MENU_EJECUTIVO'
  | 'A_LA_CARTA'
  | 'BEBIDA'
  | 'POSTRE'
  | 'OTRO'
  | 'BRASA'
  | 'CHIFA'
  | 'EXTRA'
  | 'COMBO';

/** dbo.carta_detalle.estado_producto */
export type EstadoProductoCarta = 'DISPONIBLE' | 'AGOTADO';

/** dbo.carta_detalle */
export interface DetalleCarta {
  id: number;
  productoId: number;
  nombre: string;
  seccion: SeccionCarta;
  precioDia: number | null;
  precioEfectivo: number;
  estado: EstadoProductoCarta;
  componentes: ComponenteProducto[];
}

/** dbo.cartas: carta nombrada por unidad, con una vigente. */
export interface CartaDia {
  id: number;
  nombre: string;
  unidad: UnidadNegocio;
  fecha: string;
  estado: 'ACTIVA' | 'CERRADA';
  vigente: boolean;
  detalles: DetalleCarta[];
}

/** Línea resuelta para pantalla. */
export interface LineaCartaVista {
  detalleId: number;
  productoId: number;
  nombre: string;
  precio: number;
  estado: EstadoProductoCarta;
  componentes: readonly ComponenteProducto[];
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

export function etiquetaEstadoTurno(estado: EstadoTurno): string {
  if (estado === 'ACTIVO') {
    return 'Activo';
  }
  if (estado === 'EN CIERRE') {
    return 'En cierre';
  }
  return 'Cerrado';
}

export function minutosHastaFin(horaFin: string, ahora: Date): number {
  const partes = horaFin.split(':').map((p) => Number(p));
  const hora = partes[0] ?? 0;
  const minuto = partes[1] ?? 0;
  const fin = new Date(ahora);
  fin.setHours(hora, minuto, 0, 0);
  if (fin.getTime() <= ahora.getTime()) {
    fin.setDate(fin.getDate() + 1);
  }
  return Math.max(0, Math.floor((fin.getTime() - ahora.getTime()) / 60_000));
}

export function textoTiempoRestante(minutos: number): string {
  if (minutos <= 0) {
    return 'Tiempo cumplido';
  }
  if (minutos < 60) {
    return `Falta ${minutos} ${minutos === 1 ? 'minuto' : 'minutos'}`;
  }
  const horas = Math.floor(minutos / 60);
  const resto = minutos % 60;
  return `Falta ${horas} h ${String(resto).padStart(2, '0')} min`;
}

export function marcadorTurno(
  estado: EstadoTurno,
  minutos: number,
): { texto: string; clase: string } {
  if (estado === 'CERRADO') {
    return { texto: 'Cerrado', clase: 'lr-estado--inactivo' };
  }
  const resto = textoTiempoRestante(minutos);
  if (estado === 'EN CIERRE') {
    return { texto: `En cierre · ${resto}`, clase: 'lr-estado--alerta' };
  }
  if (minutos <= 30) {
    return { texto: `Activo · ${resto}`, clase: 'lr-estado--abierta' };
  }
  return { texto: `Activo · ${resto}`, clase: 'lr-estado--activo' };
}

export function funcionOperativaDe(persona: Pick<PersonalTurno, 'funcionActual' | 'rolPermanente'>): RolTemporal {
  const texto = persona.funcionActual.toUpperCase();
  if (texto.includes('SUPERVISOR')) {
    return 'SUPERVISOR';
  }
  if (texto.includes('CAJA')) {
    return 'CAJA';
  }
  if (texto.includes('COCINA')) {
    return 'COCINA';
  }
  if (texto.includes('MESERO')) {
    return 'MESERO';
  }
  if (persona.rolPermanente === 'SUPERVISOR' || persona.rolPermanente === 'COCINA' || persona.rolPermanente === 'CAJA') {
    return persona.rolPermanente;
  }
  return 'MESERO';
}

export function esAsignacionTemporal(persona: Pick<PersonalTurno, 'funcionActual' | 'estado'>): boolean {
  return persona.estado === 'Asignación temporal' || /temporal/i.test(persona.funcionActual);
}

export function etiquetaFuncionActual(persona: Pick<PersonalTurno, 'funcionActual' | 'rolPermanente' | 'estado'>): string {
  const clave = persona.funcionActual.toUpperCase();
  const etiqueta =
    clave in ROL_ETIQUETA ? ROL_ETIQUETA[clave as Rol] : ROL_ETIQUETA[funcionOperativaDe(persona)];
  return esAsignacionTemporal(persona) ? `${etiqueta} temporal` : etiqueta;
}

export function siguienteUnidad(unidad: UnidadNegocio): UnidadNegocio {
  return unidad === 'Pollería' ? 'Restaurante' : 'Pollería';
}

export function etiquetaRolTemporal(persona: Pick<PersonalTurno, 'funcionActual' | 'rolPermanente'>): string {
  return ROL_ETIQUETA[funcionOperativaDe(persona)];
}

export function claseRolTemporal(persona: Pick<PersonalTurno, 'funcionActual' | 'rolPermanente'>): string {
  return persona.funcionActual.toUpperCase() === persona.rolPermanente ||
    funcionOperativaDe(persona) === persona.rolPermanente
    ? 'lr-estado--activo'
    : 'lr-estado--abierta';
}

export function coincidePersonalTurno(
  persona: PersonalTurno,
  busqueda: string,
  funcion: RolTemporal | '',
): boolean {
  const texto = busqueda.trim().toLowerCase();
  const coincideTexto =
    !texto ||
    persona.empleado.toLowerCase().includes(texto) ||
    persona.username.toLowerCase().includes(texto);
  const rol = persona.funcionActual.toUpperCase();
  return coincideTexto && (!funcion || rol === funcion);
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
