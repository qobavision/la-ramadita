package com.laramadita.backend.zonas.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ZonaRequest(
		@NotBlank @Size(max = 60) String nombre,
		@NotNull @Min(0) Integer cantidadMesas,
		@NotNull @Min(0) Integer cajas,
		@NotNull @Min(0) Integer cocinas) {
}
