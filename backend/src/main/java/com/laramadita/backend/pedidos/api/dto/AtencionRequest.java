package com.laramadita.backend.pedidos.api.dto;

import jakarta.validation.constraints.NotBlank;

/** Inicio de atención: {@code MESA} (con {@code mesaId}) o {@code PARA_LLEVAR}. */
public record AtencionRequest(@NotBlank String tipo, Integer mesaId) {
}
