package com.laramadita.backend.seguridad.api;

import com.laramadita.backend.seguridad.api.dto.LoginRequest;
import com.laramadita.backend.seguridad.api.dto.LoginResponse;
import com.laramadita.backend.seguridad.service.AuthService;
import com.laramadita.backend.usuarios.api.dto.UsuarioResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

	private final AuthService service;

	public AuthController(AuthService service) {
		this.service = service;
	}

	/** Público. Devuelve el token y los datos del usuario autenticado. */
	@PostMapping("/login")
	public LoginResponse login(@Valid @RequestBody LoginRequest request) {
		return service.login(request);
	}

	/** Requiere token. Útil para que el frontend recupere la sesión actual. */
	@GetMapping("/me")
	public UsuarioResponse me(Authentication auth) {
		return service.usuarioActual(auth.getName());
	}
}
