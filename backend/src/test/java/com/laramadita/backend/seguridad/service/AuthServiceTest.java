package com.laramadita.backend.seguridad.service;

import static com.laramadita.backend.usuarios.model.UsuariosFixture.usuario;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.laramadita.backend.seguridad.api.dto.LoginRequest;
import com.laramadita.backend.usuarios.model.RolNombre;
import com.laramadita.backend.usuarios.model.Usuario;
import com.laramadita.backend.usuarios.repository.UsuarioRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	AuthenticationManager authenticationManager;
	@Mock
	UsuarioRepository usuarios;
	@Mock
	JwtService jwt;

	AuthService service;

	@BeforeEach
	void preparar() {
		service = new AuthService(authenticationManager, usuarios, jwt);
	}

	@Test
	void unLoginExitosoRegistraLaFechaDeUltimoAcceso() {
		Usuario cuenta = usuario(1, "admin", RolNombre.ADMINISTRADOR, true);
		when(authenticationManager.authenticate(any())).thenReturn(mock(Authentication.class));
		when(usuarios.findByUsername("admin")).thenReturn(Optional.of(cuenta));
		when(jwt.emitir(cuenta)).thenReturn(new JwtService.Token("tok", Instant.now().plusSeconds(60)));

		service.login(new LoginRequest("admin", "clave"));

		assertThat(cuenta.getFechaUltimoAcceso()).isNotNull();
		verify(usuarios).save(cuenta);
	}

	@Test
	void unLoginFallidoNoRegistraUltimoAcceso() {
		when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("x"));

		assertThatThrownBy(() -> service.login(new LoginRequest("admin", "mala")))
				.isInstanceOf(BadCredentialsException.class);

		verify(usuarios, never()).save(any());
	}

	@Test
	void consultarElUsuarioActualNoRegistraUltimoAcceso() {
		Usuario cuenta = usuario(1, "admin", RolNombre.ADMINISTRADOR, true);
		when(usuarios.findByUsername("admin")).thenReturn(Optional.of(cuenta));

		service.usuarioActual("admin");

		assertThat(cuenta.getFechaUltimoAcceso()).isNull();
		verify(usuarios, never()).save(any());
	}
}
