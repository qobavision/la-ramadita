package com.laramadita.backend.usuarios.api.dto;

import com.laramadita.backend.usuarios.model.Usuario;
import java.time.LocalDateTime;

/**
 * Representación pública de un usuario. Nunca incluye el hash de contraseña.
 *
 * {@code rol} es la constante interna (DUENO) y {@code rolNombre} el valor
 * oficial tal como está en base de datos (DUEÑO).
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
