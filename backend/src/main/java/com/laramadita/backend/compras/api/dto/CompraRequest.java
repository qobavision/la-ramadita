package com.laramadita.backend.compras.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CompraRequest(
		@NotNull Integer proveedorId,
		@NotEmpty @Valid List<CompraDetalleRequest> detalles) {
}
