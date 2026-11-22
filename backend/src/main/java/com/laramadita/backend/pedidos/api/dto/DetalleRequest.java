package com.laramadita.backend.pedidos.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Producto de la carta vigente que se agrega al pedido en borrador. */
public record DetalleRequest(
		@NotNull Integer cartaDetalleId,
		@NotNull @Min(1) @Max(99) Integer cantidad,
		@Size(max = 500) String observacion) {
}
