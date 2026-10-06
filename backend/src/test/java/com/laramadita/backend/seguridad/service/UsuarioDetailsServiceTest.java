package com.laramadita.backend.seguridad.service;

import static com.laramadita.backend.usuarios.model.UsuariosFixture.usuario;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.laramadita.backend.usuarios.model.RolNombre;
import com.laramadita.backend.usuarios.model.UsuariosFixture;
import com.laramadita.backend.usuarios.repository.UsuarioRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

class UsuarioDetailsServiceTest {

	UsuarioRepository usuarios = mock(UsuarioRepository.class);
	UsuarioDetailsService service = new UsuarioDetailsService(usuarios);

	@Test
	void construyeLaAutoridadDelRolOficialEnFormatoAscii() {
		when(usuarios.findByUsername("admin")).thenReturn(Optional.of(usuario(1, "admin", RolNombre.ADMINISTRADOR, true)));

		UserDetails d = service.loadUserByUsername("  ADMIN ");

		assertThat(d.getUsername()).isEqualTo("admin");
		assertThat(d.getPassword()).isEqualTo(UsuariosFixture.HASH_FICTICIO);
		assertThat(d.getAuthorities()).extracting(GrantedAuthority::getAuthority).containsExactly("ROLE_ADMINISTRADOR");
		assertThat(d.isEnabled()).isTrue();
	}

	@Test
	void unUsuarioInactivoQuedaDeshabilitado() {
		when(usuarios.findByUsername("caja1")).thenReturn(Optional.of(usuario(5, "caja1", RolNombre.CAJA, false)));

		assertThat(service.loadUserByUsername("caja1").isEnabled()).isFalse();
	}

	@Test
	void unUsuarioInexistenteLanzaUsernameNotFound() {
		when(usuarios.findByUsername("nadie")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.loadUserByUsername("nadie"))
				.isInstanceOf(UsernameNotFoundException.class);
	}
}
