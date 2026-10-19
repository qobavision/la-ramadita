/** Longitudes de documentos y celular peruanos (formato básico, sin reglas SUNAT). */
export const LONGITUD_RUC = 11;
export const LONGITUD_DNI = 8;
export const LONGITUD_CELULAR = 9;

export function soloDigitos(valor: string): string {
  return valor.replace(/\D/g, '');
}

export function recortarDigitos(valor: string, maximo: number): string {
  return soloDigitos(valor).slice(0, maximo);
}

export function longitudDocumento(tipo: 'RUC' | 'DNI'): number {
  return tipo === 'RUC' ? LONGITUD_RUC : LONGITUD_DNI;
}
