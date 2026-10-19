package com.laramadita.backend.turnos.api.dto;

import jakarta.validation.constraints.NotBlank;

public record TurnoRequest(@NotBlank String ambito) {
}
