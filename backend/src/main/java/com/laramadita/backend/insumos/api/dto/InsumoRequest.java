package com.laramadita.backend.insumos.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record InsumoRequest(
		@NotBlank @Size(max = 100) String nombre,
		@NotBlank String unidadMedida,
		@DecimalMin(value = "0.0", inclusive = true, message = "El stock inicial no puede ser negativo") BigDecimal stockInicial,
		@NotNull @DecimalMin(value = "0.0", inclusive = true, message = "El stock mínimo no puede ser negativo") BigDecimal stockMinimo) {
}
