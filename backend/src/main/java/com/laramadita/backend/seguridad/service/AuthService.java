package com.laramadita.backend.seguridad.service;

import com.laramadita.backend.seguridad.api.dto.LoginRequest;
import com.laramadita.backend.seguridad.api.dto.LoginResponse;
import com.laramadita.backend.usuarios.api.dto.UsuarioResponse;
import com.laramadita.backend.usuarios.model.Usuario;
import com.laramadita.backend.usuarios.repository.UsuarioRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Inicio de sesión con usuario y contraseña (M01). */
@Service
public class AuthService {

	private final AuthenticationManager authenticationManager;
	private final UsuarioRepository usuarios;
	private final JwtService jwt;

	public AuthService(AuthenticationManager authenticationManager, UsuarioRepository usuarios, JwtService jwt) {
		this.authenticationManager = authenticationManager;
		this.usuarios = usuarios;
		this.jwt = jwt;
	}

	/**
	 * Autentica y emite el token. Si las credenciales no coinciden o el usuario
	 * está inactivo, Spring Security lanza una {@code AuthenticationException}
	 * que la API traduce a 401 sin revelar cuál fue el motivo.
	 */
	@Transactional
	public LoginResponse login(LoginRequest request) {
		String username = Usuario.normalizarUsername(request.username());
		authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, request.password()));

		Usuario usuario = usuarios.findByUsername(username)
				.orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));
		usuario.registrarUltimoAcceso(LocalDateTime.now(ZoneOffset.UTC));
		usuarios.save(usuario);
		JwtService.Token token = jwt.emitir(usuario);
		return new LoginResponse(token.valor(), "Bearer", token.expiraEn(), UsuarioResponse.de(usuario));
	}

	@Transactional(readOnly = true)
	public UsuarioResponse usuarioActual(String username) {
		Usuario usuario = usuarios.findByUsername(Usuario.normalizarUsername(username))
				.orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));
		return UsuarioResponse.de(usuario);
	}
}
