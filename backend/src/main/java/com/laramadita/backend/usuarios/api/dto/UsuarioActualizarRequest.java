package com.laramadita.backend.usuarios.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Datos para editar un usuario. La contraseña es opcional: si viene vacía
 * se conserva la actual.
 */
public record UsuarioActualizarRequest(
		@NotBlank @Size(min = 3, max = 60) String username,
		@Size(min = 8, max = 72) String password,
		@NotBlank @Size(min = 2, max = 80) String nombre,
		@Size(max = 80) String apellido,
		@NotBlank String rol,
		@NotNull Boolean activo) {
}
