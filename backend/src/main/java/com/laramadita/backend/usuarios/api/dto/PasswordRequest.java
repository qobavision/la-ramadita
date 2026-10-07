package com.laramadita.backend.usuarios.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Restablecimiento de contraseña desde administración. */
public record PasswordRequest(@NotBlank @Size(min = 8, max = 72) String password) {
}
