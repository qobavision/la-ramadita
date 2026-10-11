package com.laramadita.backend.compras.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CompraDetalleRequest(
		@NotNull Integer insumoId,
		@NotNull @DecimalMin(value = "0.0001", message = "La cantidad debe ser mayor a 0") BigDecimal cantidad,
		@NotNull @DecimalMin(value = "0.0", inclusive = true, message = "El precio no puede ser negativo") BigDecimal precioUnitario) {
}
