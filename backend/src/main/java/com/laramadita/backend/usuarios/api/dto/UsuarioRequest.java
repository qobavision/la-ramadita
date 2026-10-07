package com.laramadita.backend.usuarios.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Datos para crear un usuario vinculado a un empleado existente. */
public record UsuarioRequest(
		@NotNull Integer empleadoId,
		@NotBlank @Size(min = 3, max = 60) String username,
		@NotBlank @Size(min = 8, max = 72) String password,
		@NotBlank String rol,
		Boolean activo) {
}
