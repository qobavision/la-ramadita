package com.laramadita.backend.usuarios.api.dto;

import com.laramadita.backend.usuarios.model.Usuario;
import java.time.LocalDateTime;

/**
 * Representación pública de un usuario. Nunca incluye el hash de contraseña.
 *
 * {@code rol} es la constante interna (p. ej. ADMINISTRADOR) y {@code rolNombre}
 * el valor tal como está en base de datos. Hoy coinciden; se mantienen ambos
 * para que el frontend use siempre {@code rol} en lógica y {@code rolNombre}
 * solo para mostrar.
 */
public record UsuarioResponse(
		Integer id,
		String username,
		String nombre,
		String apellido,
		String rol,
		String rolNombre,
		boolean activo,
		LocalDateTime fechaCreacion) {

	public static UsuarioResponse de(Usuario u) {
		return new UsuarioResponse(
				u.getId(),
				u.getUsername(),
				u.getNombre(),
				u.getApellido(),
				u.getRol().getNombre().name(),
				u.getRol().getNombre().valorBaseDatos(),
				u.isActivo(),
				u.getFechaCreacion());
	}
}
