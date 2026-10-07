package com.laramadita.backend.usuarios.service;

import static com.laramadita.backend.usuarios.model.UsuariosFixture.empleado;
import static com.laramadita.backend.usuarios.model.UsuariosFixture.rol;
import static com.laramadita.backend.usuarios.model.UsuariosFixture.usuario;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.laramadita.backend.empleados.model.Empleado;
import com.laramadita.backend.empleados.repository.EmpleadoRepository;
import com.laramadita.backend.shared.error.ConflictoException;
import com.laramadita.backend.shared.error.OperacionNoAutorizadaException;
import com.laramadita.backend.shared.error.RecursoNoEncontradoException;
import com.laramadita.backend.shared.error.ReglaNegocioException;
import com.laramadita.backend.usuarios.api.dto.PasswordRequest;
import com.laramadita.backend.usuarios.api.dto.UsuarioActualizarRequest;
import com.laramadita.backend.usuarios.api.dto.UsuarioRequest;
import com.laramadita.backend.usuarios.api.dto.UsuarioResponse;
import com.laramadita.backend.usuarios.model.RolNombre;
import com.laramadita.backend.seguridad.service.UsuarioDetailsService;
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
	EmpleadoRepository empleados;
	@Mock
	PasswordEncoder encoder;

	UsuarioService service;

	Usuario admin;
	Usuario otroAdmin;
	Usuario supervisor;
	Usuario mesero;
	Empleado empleadoLibre;
	Empleado empleadoInactivo;

	@BeforeEach
	void preparar() {
		service = new UsuarioService(usuarios, roles, empleados, encoder);

		admin = usuario(1, "admin", RolNombre.ADMINISTRADOR, true);
		otroAdmin = usuario(2, "admin2", RolNombre.ADMINISTRADOR, true);
		supervisor = usuario(3, "supervisora", RolNombre.SUPERVISOR, true);
		mesero = usuario(4, "mesero1", RolNombre.MESERO, true);
		empleadoLibre = empleado(20, true);
		empleadoInactivo = empleado(21, false);

		for (Usuario u : new Usuario[] { admin, otroAdmin, supervisor, mesero }) {
			when(usuarios.findByUsername(u.getUsername())).thenReturn(Optional.of(u));
			when(usuarios.findById(u.getId())).thenReturn(Optional.of(u));
		}
		when(empleados.findByIdConUsuario(empleadoLibre.getId())).thenReturn(Optional.of(empleadoLibre));
		when(empleados.findByIdConUsuario(empleadoInactivo.getId())).thenReturn(Optional.of(empleadoInactivo));
		for (RolNombre r : RolNombre.values()) {
			when(roles.findByNombre(r)).thenReturn(Optional.of(rol(r)));
		}
		when(encoder.encode(anyString())).thenReturn(HASH);
		when(usuarios.existsByUsername(anyString())).thenReturn(false);
		when(usuarios.existsByUsernameAndIdNot(anyString(), any())).thenReturn(false);
		when(usuarios.existsByEmpleado_Id(any())).thenReturn(false);
	}

	private static UsuarioRequest alta(Integer empleadoId, String username, String rol) {
		return new UsuarioRequest(empleadoId, username, "Clave2026!", rol, true);
	}

	private static UsuarioActualizarRequest edicion(Usuario base, String rol, boolean activo) {
		return new UsuarioActualizarRequest(base.getUsername(), null, rol, activo);
	}

	@Nested
	class Crear {

		@Test
		void hasheaLaPasswordYNormalizaElUsername() {
			UsuarioResponse r = service.crear(alta(empleadoLibre.getId(), "  Nuevo.Mesero ", "MESERO"), "admin");

			ArgumentCaptor<Usuario> guardado = ArgumentCaptor.forClass(Usuario.class);
			verify(usuarios).save(guardado.capture());
			verify(encoder).encode("Clave2026!");
			assertThat(guardado.getValue().getPasswordHash()).isEqualTo(HASH);
			assertThat(guardado.getValue().getUsername()).isEqualTo("nuevo.mesero");
			assertThat(guardado.getValue().getEmpleado().getId()).isEqualTo(empleadoLibre.getId());
			assertThat(r.username()).isEqualTo("nuevo.mesero");
			assertThat(r.rol()).isEqualTo("MESERO");
			assertThat(r.empleadoId()).isEqualTo(empleadoLibre.getId());
			assertThat(r.activo()).isTrue();
		}

		@Test
		void tomaNombreYApellidoDelEmpleado() {
			UsuarioResponse r = service.crear(alta(empleadoLibre.getId(), "nuevo.mesero", "MESERO"), "admin");
			assertThat(r.nombre()).isEqualTo(empleadoLibre.getNombres());
			assertThat(r.apellido()).isEqualTo(empleadoLibre.getApellidos());
		}

		@Test
		void rechazaUnRolInexistenteComoCocinero() {
			assertThatThrownBy(() -> service.crear(alta(empleadoLibre.getId(), "x.y", "COCINERO"), "admin"))
					.isInstanceOf(ReglaNegocioException.class)
					.hasMessageContaining("ADMINISTRADOR, SUPERVISOR, MESERO, COCINA, CAJA");
		}

		@Test
		void rechazaUsernameDuplicado() {
			when(usuarios.existsByUsername("mesero1")).thenReturn(true);
			assertThatThrownBy(() -> service.crear(alta(empleadoLibre.getId(), "MESERO1", "MESERO"), "admin"))
					.isInstanceOf(ConflictoException.class);
			verify(usuarios, never()).save(any());
		}

		@Test
		void rechazaSegundoUsuarioParaElMismoEmpleado() {
			when(usuarios.existsByEmpleado_Id(empleadoLibre.getId())).thenReturn(true);
			assertThatThrownBy(() -> service.crear(alta(empleadoLibre.getId(), "otro", "MESERO"), "admin"))
					.isInstanceOf(ConflictoException.class)
					.hasMessageContaining("ya tiene una cuenta");
		}

		@Test
		void rechazaUsuarioParaEmpleadoInactivo() {
			assertThatThrownBy(() -> service.crear(alta(empleadoInactivo.getId(), "nuevo", "MESERO"), "admin"))
					.isInstanceOf(ReglaNegocioException.class)
					.hasMessageContaining("empleado inactivo");
		}

		@Test
		void elSupervisorNoPuedeCrearUsuarios() {
			assertThatThrownBy(() -> service.crear(alta(empleadoLibre.getId(), "mesero.nuevo", "MESERO"), "supervisora"))
					.isInstanceOf(OperacionNoAutorizadaException.class);
			verify(usuarios, never()).save(any());
		}

		@Test
		void elAdministradorPuedeCrearOtroAdministrador() {
			UsuarioResponse r = service.crear(alta(empleadoLibre.getId(), "admin.nuevo", "ADMINISTRADOR"), "admin");
			assertThat(r.rol()).isEqualTo("ADMINISTRADOR");
		}

		@Test
		void unUsuarioCreadoActivoQuedaHabilitadoParaIniciarSesion() {
			when(usuarios.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));
			service.crear(alta(empleadoLibre.getId(), "mesero.nuevo", "MESERO"), "admin");

			ArgumentCaptor<Usuario> guardado = ArgumentCaptor.forClass(Usuario.class);
			verify(usuarios).save(guardado.capture());
			Usuario cuenta = guardado.getValue();
			when(usuarios.findByUsername("mesero.nuevo")).thenReturn(Optional.of(cuenta));

			assertThat(new UsuarioDetailsService(usuarios).loadUserByUsername("mesero.nuevo").isEnabled()).isTrue();
			assertThat(cuenta.getPasswordHash()).isEqualTo(HASH);
		}

		@Test
		void puedeCrearseInactivoDesdeElInicio() {
			UsuarioResponse r = service.crear(
					new UsuarioRequest(empleadoLibre.getId(), "inactivo", "Clave2026!", "CAJA", false), "admin");
			assertThat(r.activo()).isFalse();
		}
	}

	@Nested
	class Actualizar {

		@Test
		void elSupervisorNoPuedeModificarUsuarios() {
			assertThatThrownBy(() -> service.actualizar(mesero.getId(), edicion(mesero, "CAJA", true), "supervisora"))
					.isInstanceOf(OperacionNoAutorizadaException.class);
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
					"MESERO", true), "admin");
			verify(encoder).encode("NuevaClave1!");
			assertThat(mesero.getPasswordHash()).isEqualTo(HASH);
		}

		@Test
		void noActivaUnUsuarioSiElEmpleadoEstaInactivo() {
			Usuario cuenta = usuario(30, "inactivo.emp", RolNombre.CAJA, false, empleadoInactivo);
			when(usuarios.findById(30)).thenReturn(Optional.of(cuenta));
			assertThatThrownBy(() -> service.actualizar(30, new UsuarioActualizarRequest("inactivo.emp", null, "CAJA", true),
					"admin"))
					.isInstanceOf(ReglaNegocioException.class)
					.hasMessageContaining("inactivo o archivado");
		}

		@Test
		void rechazaUsernameYaUsadoPorOtroUsuario() {
			when(usuarios.existsByUsernameAndIdNot("admin", mesero.getId())).thenReturn(true);
			assertThatThrownBy(() -> service.actualizar(mesero.getId(),
					new UsuarioActualizarRequest("admin", null, "MESERO", true), "admin"))
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
		void elSupervisorNoPuedeDesactivarNiActivar() {
			assertThatThrownBy(() -> service.desactivar(mesero.getId(), "supervisora"))
					.isInstanceOf(OperacionNoAutorizadaException.class);
			assertThatThrownBy(() -> service.activar(mesero.getId(), "supervisora"))
					.isInstanceOf(OperacionNoAutorizadaException.class);
		}

		@Test
		void noActivaSiElEmpleadoEstaInactivo() {
			Usuario cuenta = usuario(31, "caja.inact", RolNombre.CAJA, false, empleadoInactivo);
			when(usuarios.findById(31)).thenReturn(Optional.of(cuenta));
			assertThatThrownBy(() -> service.activar(31, "admin"))
					.isInstanceOf(ReglaNegocioException.class);
		}

		@Test
		void laDesactivacionEsLogicaNuncaBorra() {
			service.desactivar(mesero.getId(), "admin");
			verify(usuarios, never()).delete(any());
			verify(usuarios, never()).deleteById(any());
		}

		@Test
		void restableceLaPasswordConBcrypt() {
			service.restablecerPassword(mesero.getId(), new PasswordRequest("OtraClave9!"), "admin");
			verify(encoder).encode("OtraClave9!");
			assertThat(mesero.getPasswordHash()).isEqualTo(HASH);
		}

		@Test
		void archivarEsBajaLogicaYNoBorra() {
			UsuarioResponse r = service.archivar(mesero.getId(), "admin");
			assertThat(mesero.isArchivado()).isTrue();
			assertThat(mesero.isActivo()).isFalse();
			assertThat(mesero.getFechaArchivado()).isNotNull();
			assertThat(r.archivado()).isTrue();
			assertThat(r.fechaArchivado()).isNotNull();
			verify(usuarios, never()).delete(any());
			verify(usuarios, never()).deleteById(any());
		}

		@Test
		void unUsuarioArchivadoNoSeReactivaDesdeEditar() {
			mesero.archivar();
			assertThatThrownBy(() -> service.actualizar(mesero.getId(), edicion(mesero, "MESERO", true), "admin"))
					.isInstanceOf(ReglaNegocioException.class)
					.hasMessageContaining("archivado");
		}

		@Test
		void nadiePuedeArchivarSuPropiaCuenta() {
			assertThatThrownBy(() -> service.archivar(admin.getId(), "admin"))
					.isInstanceOf(ReglaNegocioException.class)
					.hasMessageContaining("su propia cuenta");
		}
	}

	@Test
	void laRespuestaNuncaExponeElHash() {
		UsuarioResponse r = service.obtener(mesero.getId());
		assertThat(r.toString()).doesNotContain(mesero.getPasswordHash());
	}
}
