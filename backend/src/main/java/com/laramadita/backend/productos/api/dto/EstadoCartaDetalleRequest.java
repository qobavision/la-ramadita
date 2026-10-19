package com.laramadita.backend.productos.api.dto;

import jakarta.validation.constraints.NotBlank;

public record EstadoCartaDetalleRequest(@NotBlank String estado) {
}
