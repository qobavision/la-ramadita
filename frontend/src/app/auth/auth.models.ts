/**
 * Contratos compartidos con el backend (módulo M01).
 * Deben mantenerse alineados con los DTOs de `com.laramadita.backend`.
 */

/** Constantes de rol tal como las expone la API (`UsuarioResponse.rol`). */
export type Rol = 'ADMINISTRADOR' | 'SUPERVISOR' | 'MESERO' | 'COCINA' | 'CAJA';

/** Etiqueta para mostrar en pantalla. */
export const ROL_ETIQUETA: Record<Rol, string> = {
  ADMINISTRADOR: 'Administrador',
  SUPERVISOR: 'Supervisor',
  MESERO: 'Mesero',
  COCINA: 'Cocina',
  CAJA: 'Caja',
};

export interface Usuario {
  id: number;
  username: string;
  nombre: string;
  apellido: string | null;
  /** Constante interna, p. ej. `ADMINISTRADOR`. Usar siempre esta en la lógica. */
  rol: Rol;
  /** Valor tal como está en base de datos. Solo para mostrar. */
  rolNombre: string;
  activo: boolean;
  fechaCreacion: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  tipo: string;
  expiraEn: string;
  usuario: Usuario;
}
