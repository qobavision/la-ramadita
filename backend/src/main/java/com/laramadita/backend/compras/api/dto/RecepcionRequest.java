package com.laramadita.backend.compras.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record RecepcionRequest(
		@NotEmpty @Valid List<RecepcionDetalleRequest> detalles) {
}
