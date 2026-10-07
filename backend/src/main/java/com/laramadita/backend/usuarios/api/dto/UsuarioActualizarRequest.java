package com.laramadita.backend.usuarios.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Edición de cuenta. La contraseña es opcional: si viene vacía se conserva.
 * Los nombres se toman del empleado, no se editan aquí.
 */
public record UsuarioActualizarRequest(
		@NotBlank @Size(min = 3, max = 60) String username,
		@Size(min = 8, max = 72) String password,
		@NotBlank String rol,
		@NotNull Boolean activo) {
}
