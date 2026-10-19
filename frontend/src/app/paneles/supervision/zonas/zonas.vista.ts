import { MesaDemo, ZonaDemo } from '../datos/supervision.models';

export type UsoZona = 'sin-uso' | 'historial' | 'operacion-activa';

export function enteroNoNegativo(valor: number | string): number {
  const n = Math.floor(Number(valor));
  if (!Number.isFinite(n) || n < 0) {
    return 0;
  }
  return n;
}

export function detalleCapacidadZona(zona: Pick<ZonaDemo, 'mesas' | 'cajas' | 'cocinas'>): string {
  const mesas = zona.mesas === 1 ? '1 mesa' : `${zona.mesas} mesas`;
  const cajas = zona.cajas === 1 ? '1 caja' : `${zona.cajas} cajas`;
  const cocinas = zona.cocinas === 1 ? '1 cocina' : `${zona.cocinas} cocinas`;
  return `${mesas} · ${cajas} · ${cocinas}`;
}

export function usoZona(zona: Pick<ZonaDemo, 'id'>, mesas: readonly MesaDemo[]): UsoZona {
  const deZona = mesas.filter((m) => m.zonaId === zona.id);
  if (deZona.some((m) => m.activa && m.estadoOp === 'Ocupada')) {
    return 'operacion-activa';
  }
  if (deZona.length > 0) {
    return 'historial';
  }
  return 'sin-uso';
}

export function puedeEliminarZona(uso: UsoZona): boolean {
  return uso === 'sin-uso';
}

export function puedeInactivarZona(zona: Pick<ZonaDemo, 'activa'>, uso: UsoZona): boolean {
  if (!zona.activa) {
    return true;
  }
  return uso !== 'operacion-activa';
}

export function tituloEliminarZona(uso: UsoZona): string {
  if (uso === 'operacion-activa') {
    return 'No se puede eliminar: hay operación pendiente';
  }
  if (uso === 'historial') {
    return 'No se puede eliminar: tiene historial';
  }
  return 'Eliminar zona';
}

export function tituloActividadZona(zona: Pick<ZonaDemo, 'activa'>, uso: UsoZona): string {
  if (zona.activa && uso === 'operacion-activa') {
    return 'No se puede inactivar: hay operación pendiente';
  }
  return zona.activa ? 'Inactivar' : 'Activar';
}

export const MESAS_EN_CARD = 12;

export function etiquetasMesas(cantidad: number): string[] {
  const n = enteroNoNegativo(cantidad);
  return Array.from({ length: n }, (_, i) => `M${i + 1}`);
}

export function mesasParaCard(cantidad: number, limite = MESAS_EN_CARD): { visibles: string[]; extra: number } {
  const todas = etiquetasMesas(cantidad);
  if (todas.length <= limite) {
    return { visibles: todas, extra: 0 };
  }
  return { visibles: todas.slice(0, limite), extra: todas.length - limite };
}
