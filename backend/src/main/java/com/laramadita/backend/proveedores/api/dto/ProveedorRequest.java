package com.laramadita.backend.proveedores.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProveedorRequest(
		@NotBlank @Size(max = 150) String nombreRazonSocial,
		@NotBlank String tipoDocumento,
		@NotBlank @Size(max = 20) String documento,
		@Size(max = 20) String celular,
		@Size(max = 120) String personaContacto,
		Boolean activo) {
}
