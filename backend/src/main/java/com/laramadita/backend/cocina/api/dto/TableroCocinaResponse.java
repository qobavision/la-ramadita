package com.laramadita.backend.cocina.api.dto;

import java.util.List;

/**
 * Monitor de Cocina: comandas pendientes (la más antigua primero) y las
 * terminadas recientemente que esperan ser servidas o cobradas.
 */
public record TableroCocinaResponse(List<ComandaResponse> pendientes, List<ComandaResponse> preparados) {
}
