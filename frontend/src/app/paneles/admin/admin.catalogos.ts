/**
 * Catálogos visuales del panel administrador. Son listas de opciones de la
 * interfaz (no datos del negocio) y se conectarán al backend cuando existan
 * los módulos correspondientes.
 */

/** Cargos laborales previstos. Un cargo NO es un rol del sistema. */
export const CARGOS = [
  'Supervisor',
  'Cajero',
  'Ayudante de cajero',
  'Cocinero',
  'Ayudante de cocina',
  'Mesero',
  'Motorizado',
  'Almacenero',
  'Seguridad',
  'Otro',
] as const;

export type Cargo = (typeof CARGOS)[number];

export function ordenarCargos<T extends { nombre: string }>(cargos: T[]): T[] {
  return [...cargos].sort((a, b) => {
    const ia = (CARGOS as readonly string[]).indexOf(a.nombre);
    const ib = (CARGOS as readonly string[]).indexOf(b.nombre);
    const na = ia < 0 ? CARGOS.length : ia;
    const nb = ib < 0 ? CARGOS.length : ib;
    return na - nb || a.nombre.localeCompare(b.nombre, 'es');
  });
}

/** Métodos de pago contemplados por el negocio (M09). */
export const METODOS_PAGO = ['Efectivo', 'Yape', 'Plin', 'Tarjeta', 'Transferencia'] as const;
