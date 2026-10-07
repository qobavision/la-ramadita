import { Rol } from './auth.models';

/** Ruta raíz del panel que corresponde a cada rol oficial. */
export const PANEL_POR_ROL: Record<Rol, string> = {
  ADMINISTRADOR: '/admin',
  SUPERVISOR: '/supervision',
  MESERO: '/mesero',
  COCINA: '/cocina',
  CAJA: '/caja',
};

/**
 * Rol con control general del sistema. Decisión del equipo: además de su
 * propio panel, puede entrar a los paneles operativos (supervisión, mesero,
 * cocina y caja) con su misma sesión, para revisar o intervenir.
 */
export const ROL_CONTROL_GENERAL: Rol = 'ADMINISTRADOR';

/** Devuelve la ruta del panel del rol indicado; si el rol es desconocido, la raíz. */
export function rutaPanelDe(rol: Rol | string | null | undefined): string {
  return (rol && PANEL_POR_ROL[rol as Rol]) || '/';
}

/**
 * Indica si un rol puede entrar a un panel pensado para `rolesPanel`.
 * El rol de control general entra a todos; los demás solo al suyo.
 */
export function puedeEntrarAlPanel(rol: Rol, rolesPanel: readonly Rol[]): boolean {
  return rol === ROL_CONTROL_GENERAL || rolesPanel.includes(rol);
}
