package com.laramadita.backend.turnos.api.dto;

import jakarta.validation.constraints.NotBlank;

public record HorarioRequest(@NotBlank String horaInicio, @NotBlank String horaFin) {
}
