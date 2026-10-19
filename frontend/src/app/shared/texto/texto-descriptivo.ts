/**
 * Normalización visual de textos humanos (nombres, razones sociales, recetas).
 *
 * NO usar en username, correo, contraseña, DNI, RUC, celular, códigos,
 * roles, IDs, tokens, URLs ni cualquier valor técnico que deba
 * conservarse exactamente como se escribió.
 */
export function normalizarTextoDescriptivo(valor: string): string {
  return valor
    .trim()
    .replace(/\s+/g, ' ')
    .split(' ')
    .filter((palabra) => palabra.length > 0)
    .map((palabra) => palabra.charAt(0).toLocaleUpperCase('es') + palabra.slice(1).toLocaleLowerCase('es'))
    .join(' ');
}
