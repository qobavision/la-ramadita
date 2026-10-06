import { Rol } from './auth.models';

/** Ruta raíz del panel que corresponde a cada rol oficial. */
export const PANEL_POR_ROL: Record<Rol, string> = {
  ADMINISTRADOR: '/admin',
  SUPERVISOR: '/supervision',
  MESERO: '/mesero',
  COCINA: '/cocina',
  CAJA: '/caja',
};

/** Devuelve la ruta del panel del rol indicado; si el rol es desconocido, la raíz. */
export function rutaPanelDe(rol: Rol | string | null | undefined): string {
  return (rol && PANEL_POR_ROL[rol as Rol]) || '/';
}
