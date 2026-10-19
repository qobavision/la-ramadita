package com.laramadita.backend.productos.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CartaDetalleRequest(
		@NotNull Integer productoId,
		@NotBlank String seccion,
		@DecimalMin(value = "0.0", inclusive = true, message = "El precio del día no puede ser negativo") BigDecimal precioDia,
		String estado) {
}
