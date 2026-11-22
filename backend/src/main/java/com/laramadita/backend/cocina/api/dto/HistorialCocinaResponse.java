package com.laramadita.backend.cocina.api.dto;

import java.util.List;

/**
 * Comandas del turno en curso o, si no hay, del último turno cerrado.
 *
 * @param turno   rótulo del turno, p. ej. "Restaurante · en curso"; {@code null} si no hubo turnos
 */
public record HistorialCocinaResponse(String turno, List<ComandaResponse> comandas) {
}
