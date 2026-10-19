import { Rol, Usuario } from './auth.models';

/** Ruta raíz del panel que corresponde a cada rol oficial. */
export const PANEL_POR_ROL: Record<Rol, string> = {
  ADMINISTRADOR: '/admin',
  SUPERVISOR: '/supervision',
  MESERO: '/mesero',
  COCINA: '/cocina',
  CAJA: '/caja',
};

const ROLES_OPERATIVOS: readonly Rol[] = ['SUPERVISOR', 'MESERO', 'COCINA', 'CAJA'];

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

/** Panel al que entra tras el login: la función del turno, si es operativa. */
export function rutaInicioDe(usuario: Pick<Usuario, 'rol' | 'funcionActual'>): string {
  const funcion = usuario.funcionActual;
  if (funcion && ROLES_OPERATIVOS.includes(funcion)) {
    return PANEL_POR_ROL[funcion];
  }
  return rutaPanelDe(usuario.rol);
}

/**
 * Indica si el usuario puede entrar a un panel pensado para `rolesPanel`.
 * El administrador entra a todos. Los demás entran con su rol permanente
 * y, si el turno les asignó otra función, también a ese panel.
 */
export function puedeEntrarAlPanel(
  usuario: Pick<Usuario, 'rol' | 'funcionActual'>,
  rolesPanel: readonly Rol[],
): boolean {
  if (usuario.rol === ROL_CONTROL_GENERAL) {
    return true;
  }
  if (rolesPanel.includes(usuario.rol)) {
    return true;
  }
  return !!usuario.funcionActual && rolesPanel.includes(usuario.funcionActual);
}
