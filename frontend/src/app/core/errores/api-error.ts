import { HttpErrorResponse } from '@angular/common/http';

/**
 * Cuerpo de error que devuelve el backend (RFC 9457 Problem Details,
 * generado por `ApiExceptionHandler` y `ProblemaHttp`).
 */
export interface ProblemDetail {
  type?: string;
  title?: string;
  status?: number;
  detail?: string;
  /** Presente en errores 400 de validación: campo → mensaje. */
  errores?: Record<string, string>;
}

/**
 * Convierte cualquier error HTTP en un mensaje legible para el usuario,
 * priorizando el `detail` enviado por el backend.
 */
export function mensajeDeError(error: unknown, porDefecto = 'Ocurrió un error inesperado.'): string {
  if (!(error instanceof HttpErrorResponse)) {
    return porDefecto;
  }

  if (error.status === 0) {
    return 'No se pudo conectar con el servidor. Verifica que el backend esté en ejecución y la red local disponible.';
  }

  const cuerpo = error.error as ProblemDetail | null | undefined;
  if (cuerpo && typeof cuerpo === 'object') {
    if (cuerpo.errores && Object.keys(cuerpo.errores).length > 0) {
      return Object.values(cuerpo.errores).join(' ');
    }
    if (cuerpo.detail) {
      return cuerpo.detail;
    }
    if (cuerpo.title) {
      return cuerpo.title;
    }
  }

  return porDefecto;
}
