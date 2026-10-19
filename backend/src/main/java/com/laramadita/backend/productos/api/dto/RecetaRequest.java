package com.laramadita.backend.productos.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

public record RecetaRequest(
		@NotBlank @Size(max = 120) String nombre,
		@NotBlank String categoria,
		@NotBlank String ambito,
		@NotNull @DecimalMin(value = "0.0", inclusive = true, message = "El precio no puede ser negativo") BigDecimal precio,
		@NotEmpty @Valid List<RecetaInsumoRequest> insumos) {
}
