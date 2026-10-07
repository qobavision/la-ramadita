/**
 * Catálogos visuales del panel administrador. Son listas de opciones de la
 * interfaz (no datos del negocio) y se conectarán al backend cuando existan
 * los módulos correspondientes.
 */

/** Cargos laborales previstos. Un cargo NO es un rol del sistema. */
export const CARGOS = [
  'Cocinero',
  'Ayudante de cocina',
  'Mesero',
  'Cajero',
  'Supervisor',
  'Seguridad',
  'Almacenero',
  'Otro',
] as const;

export type Cargo = (typeof CARGOS)[number];

/** Métodos de pago contemplados por el negocio (M09). */
export const METODOS_PAGO = ['Efectivo', 'Yape', 'Plin', 'Tarjeta', 'Transferencia'] as const;
