package com.laramadita.backend.pedidos.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Cambio de cantidad u observación de una línea en borrador (RN-04). */
public record DetalleActualizarRequest(
		@NotNull @Min(1) @Max(99) Integer cantidad,
		@Size(max = 500) String observacion) {
}
