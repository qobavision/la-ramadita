package com.laramadita.backend.productos.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CartaRequest(
		@NotBlank @Size(max = 120) String nombre,
		@NotBlank String ambito,
		@Valid List<CartaDetalleRequest> detalles) {
}
