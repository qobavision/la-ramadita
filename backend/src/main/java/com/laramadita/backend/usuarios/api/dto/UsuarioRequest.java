package com.laramadita.backend.usuarios.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Datos para crear un usuario. Tamaños alineados con las columnas de V1. */
public record UsuarioRequest(
		@NotBlank @Size(min = 3, max = 60) String username,
		@NotBlank @Size(min = 8, max = 72) String password,
		@NotBlank @Size(min = 2, max = 80) String nombre,
		@Size(max = 80) String apellido,
		@NotBlank String rol,
		Boolean activo) {
}
