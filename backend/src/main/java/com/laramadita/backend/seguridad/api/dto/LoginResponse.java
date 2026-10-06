package com.laramadita.backend.seguridad.api.dto;

import com.laramadita.backend.usuarios.api.dto.UsuarioResponse;
import java.time.Instant;

public record LoginResponse(String token, String tipo, Instant expiraEn, UsuarioResponse usuario) {
}
