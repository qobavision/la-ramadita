package com.laramadita.backend.usuarios.service;

import static com.laramadita.backend.usuarios.model.UsuariosFixture.rol;
import static com.laramadita.backend.usuarios.model.UsuariosFixture.usuario;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.laramadita.backend.shared.error.ConflictoException;
import com.laramadita.backend.shared.error.OperacionNoAutorizadaException;
import com.laramadita.backend.shared.error.RecursoNoEncontradoException;
import com.laramadita.backend.shared.error.ReglaNegocioException;
import com.laramadita.backend.usuarios.api.dto.UsuarioActualizarRequest;
import com.laramadita.backend.usuarios.api.dto.UsuarioRequest;
import com.laramadita.backend.usuarios.api.dto.UsuarioResponse;
import com.laramadita.backend.usuarios.model.RolNombre;
import com.laramadita.backend.usuarios.model.Usuario;
import com.laramadita.backend.usuarios.repository.RolRepository;
import com.laramadita.backend.usuarios.repository.UsuarioRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UsuarioServiceTest {

	private static final String HASH = "$2a$10$hash-generado";

	@Mock
	UsuarioRepository usuarios;
	@Mock
	RolRepository roles;
	@Mock
	PasswordEncoder encoder;

	UsuarioService service;

	Usuario admin;
	Usuario otroAdmin;
	Usuario supervisor;
	Usuario mesero;

	@BeforeEach
	void preparar() {
		service = new UsuarioService(usuarios, roles, encoder);

		admin = usuario(1, "admin", RolNombre.ADMINISTRADOR, true);
		otroAdmin = usuario(2, "admin2", RolNombre.ADMINISTRADOR, true);
		supervisor = usuario(3, "supervisora", RolNombre.SUPERVISOR, true);
		mesero = usuario(4, "mesero1", RolNombre.MESERO, true);

		for (Usuario u : new Usuario[] { admin, otroAdmin, supervisor, mesero }) {
			when(usuarios.findByUsername(u.getUsername())).thenReturn(Optional.of(u));
			when(usuarios.findById(u.getId())).thenReturn(Optional.of(u));
		}
		for (RolNombre r : RolNombre.values()) {
			when(roles.findByNombre(r)).thenReturn(Optional.of(rol(r)));
		}
		when(encoder.encode(anyString())).thenReturn(HASH);
		when(usuarios.existsByUsername(anyString())).thenReturn(false);
		when(usuarios.existsByUsernameAndIdNot(anyString(), any())).thenReturn(false);
	}

	private static UsuarioRequest alta(String username, String rol) {
		return new UsuarioRequest(username, "Clave2026!", "Nombre", "Apellido", rol, true);
	}

	private static UsuarioActualizarRequest edicion(Usuario base, String rol, boolean activo) {
		return new UsuarioActualizarRequest(base.getUsername(), null, base.getNombre(), base.getApellido(), rol,
				activo);
	}

	@Nested
	class Crear {

		@Test
		void hasheaLaPasswordYNormalizaElUsername() {
			UsuarioResponse r = service.crear(alta("  Nuevo.Mesero ", "MESERO"), "admin");

			ArgumentCaptor<Usuario> guardado = ArgumentCaptor.forClass(Usuario.class);
			verify(usuarios).save(guardado.capture());
			verify(encoder).encode("Clave2026!");
			assertThat(guardado.getValue().getPasswordHash()).isEqualTo(HASH);
			assertThat(guardado.getValue().getUsername()).isEqualTo("nuevo.mesero");
			assertThat(r.username()).isEqualTo("nuevo.mesero");
			assertThat(r.rol()).isEqualTo("MESERO");
			assertThat(r.activo()).isTrue();
		}

		@Test
		void aceptaElRolEnMinusculasYDevuelveLaConstanteYElNombreOficial() {
			UsuarioResponse r = service.crear(alta("nuevo.admin", "administrador"), "admin");
			assertThat(r.rol()).isEqualTo("ADMINISTRADOR");
			assertThat(r.rolNombre()).isEqualTo("ADMINISTRADOR");
		}

		@Test
		void rechazaUnRolInexistenteComoCocinero() {
			assertThatThrownBy(() -> service.crear(alta("x.y", "COCINERO"), "admin"))
					.isInstanceOf(ReglaNegocioException.class)
					.hasMessageContaining("ADMINISTRADOR, SUPERVISOR, MESERO, COCINA, CAJA");
		}

		@Test
		void rechazaUsernameDuplicado() {
			when(usuarios.existsByUsername("mesero1")).thenReturn(true);
			assertThatThrownBy(() -> service.crear(alta("MESERO1", "MESERO"), "admin"))
					.isInstanceOf(ConflictoException.class);
			verify(usuarios, never()).save(any());
		}

		@Test
		void elSupervisorPuedeCrearMeseroPeroNoAdministrador() {
			service.crear(alta("mesero.nuevo", "MESERO"), "supervisora");
			verify(usuarios).save(any());

			assertThatThrownBy(() -> service.crear(alta("admin.nuevo", "ADMINISTRADOR"), "supervisora"))
					.isInstanceOf(OperacionNoAutorizadaException.class);
		}

		@Test
		void elAdministradorPuedeCrearOtroAdministrador() {
			UsuarioResponse r = service.crear(alta("admin.nuevo", "ADMINISTRADOR"), "admin");
			assertThat(r.rol()).isEqualTo("ADMINISTRADOR");
		}

		@Test
		void puedeCrearseInactivoDesdeElInicio() {
			UsuarioResponse r = service.crear(
					new UsuarioRequest("inactivo", "Clave2026!", "Nombre", null, "CAJA", false), "admin");
			assertThat(r.activo()).isFalse();
		}
	}

	@Nested
	class Actualizar {

		@Test
		void elSupervisorNoPuedeModificarAUnAdministrador() {
			assertThatThrownBy(() -> service.actualizar(admin.getId(), edicion(admin, "ADMINISTRADOR", true), "supervisora"))
					.isInstanceOf(OperacionNoAutorizadaException.class);
		}

		@Test
		void elSupervisorNoPuedeConvertirAUnMeseroEnAdministrador() {
			assertThatThrownBy(() -> service.actualizar(mesero.getId(), edicion(mesero, "ADMINISTRADOR", true), "supervisora"))
					.isInstanceOf(OperacionNoAutorizadaException.class);
		}

		@Test
		void elSupervisorPuedeCambiarElRolDeUnMeseroACaja() {
			UsuarioResponse r = service.actualizar(mesero.getId(), edicion(mesero, "CAJA", true), "supervisora");
			assertThat(r.rol()).isEqualTo("CAJA");
		}

		@Test
		void nadiePuedeDesactivarseASiMismoAlEditar() {
			assertThatThrownBy(() -> service.actualizar(admin.getId(), edicion(admin, "ADMINISTRADOR", false), "admin"))
					.isInstanceOf(ReglaNegocioException.class)
					.hasMessageContaining("a sí mismo");
		}

		@Test
		void noPuedeQuitarseElRolAdministradorAlUltimoAdministradorActivo() {
			when(usuarios.contarActivosPorRolExcluyendo(RolNombre.ADMINISTRADOR, admin.getId())).thenReturn(0L);
			assertThatThrownBy(() -> service.actualizar(admin.getId(), edicion(admin, "SUPERVISOR", true), "admin"))
					.isInstanceOf(ReglaNegocioException.class)
					.hasMessageContaining("al menos un usuario ADMINISTRADOR activo");
		}

		@Test
		void siQuedaOtroAdministradorActivoPuedeCambiarDeRol() {
			when(usuarios.contarActivosPorRolExcluyendo(RolNombre.ADMINISTRADOR, admin.getId())).thenReturn(1L);
			UsuarioResponse r = service.actualizar(admin.getId(), edicion(admin, "SUPERVISOR", true), "admin");
			assertThat(r.rol()).isEqualTo("SUPERVISOR");
		}

		@Test
		void soloCambiaLaPasswordSiSeEnviaUnaNueva() {
			service.actualizar(mesero.getId(), edicion(mesero, "MESERO", true), "admin");
			verify(encoder, never()).encode(anyString());

			service.actualizar(mesero.getId(), new UsuarioActualizarRequest(mesero.getUsername(), "NuevaClave1!",
					"Nombre", null, "MESERO", true), "admin");
			verify(encoder).encode("NuevaClave1!");
			assertThat(mesero.getPasswordHash()).isEqualTo(HASH);
		}

		@Test
		void rechazaUsernameYaUsadoPorOtroUsuario() {
			when(usuarios.existsByUsernameAndIdNot("admin", mesero.getId())).thenReturn(true);
			assertThatThrownBy(() -> service.actualizar(mesero.getId(),
					new UsuarioActualizarRequest("admin", null, "Nombre", null, "MESERO", true), "admin"))
					.isInstanceOf(ConflictoException.class);
		}

		@Test
		void usuarioInexistenteDevuelveNoEncontrado() {
			when(usuarios.findById(99)).thenReturn(Optional.empty());
			assertThatThrownBy(() -> service.actualizar(99, edicion(mesero, "MESERO", true), "admin"))
					.isInstanceOf(RecursoNoEncontradoException.class);
		}
	}

	@Nested
	class DesactivarYActivar {

		@Test
		void nadiePuedeDesactivarseASiMismo() {
			assertThatThrownBy(() -> service.desactivar(admin.getId(), "admin"))
					.isInstanceOf(ReglaNegocioException.class);
			assertThat(admin.isActivo()).isTrue();
		}

		@Test
		void noSePuedeDesactivarAlUltimoAdministradorActivo() {
			when(usuarios.contarActivosPorRolExcluyendo(RolNombre.ADMINISTRADOR, otroAdmin.getId())).thenReturn(0L);
			assertThatThrownBy(() -> service.desactivar(otroAdmin.getId(), "admin"))
					.isInstanceOf(ReglaNegocioException.class);
			assertThat(otroAdmin.isActivo()).isTrue();
		}

		@Test
		void unAdministradorPuedeDesactivarAOtroAdministradorSiQuedaAlgunoActivo() {
			when(usuarios.contarActivosPorRolExcluyendo(RolNombre.ADMINISTRADOR, otroAdmin.getId())).thenReturn(1L);
			UsuarioResponse r = service.desactivar(otroAdmin.getId(), "admin");
			assertThat(r.activo()).isFalse();
			assertThat(otroAdmin.isActivo()).isFalse();
		}

		@Test
		void elSupervisorNoPuedeDesactivarNiActivarAUnAdministrador() {
			assertThatThrownBy(() -> service.desactivar(admin.getId(), "supervisora"))
					.isInstanceOf(OperacionNoAutorizadaException.class);
			assertThatThrownBy(() -> service.activar(admin.getId(), "supervisora"))
					.isInstanceOf(OperacionNoAutorizadaException.class);
		}

		@Test
		void elSupervisorPuedeDesactivarYReactivarAUnMesero() {
			assertThat(service.desactivar(mesero.getId(), "supervisora").activo()).isFalse();
			assertThat(service.activar(mesero.getId(), "supervisora").activo()).isTrue();
		}

		@Test
		void laDesactivacionEsLogicaNuncaBorra() {
			service.desactivar(mesero.getId(), "admin");
			verify(usuarios, never()).delete(any());
			verify(usuarios, never()).deleteById(any());
		}
	}

	@Test
	void laRespuestaNuncaExponeElHash() {
		UsuarioResponse r = service.obtener(mesero.getId());
		assertThat(r.toString()).doesNotContain(mesero.getPasswordHash());
	}
}
