package com.laramadita.backend.productos.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ComponenteRequest(
		@NotNull Integer productoId,
		@NotNull @DecimalMin(value = "0.0", inclusive = false, message = "La cantidad del componente debe ser mayor a 0") BigDecimal cantidad) {
}
