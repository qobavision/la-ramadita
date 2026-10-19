package com.laramadita.backend.compras.api.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record RecepcionDetalleRequest(
		@NotNull Integer compraDetalleId,
		@NotNull BigDecimal cantidad) {
}
