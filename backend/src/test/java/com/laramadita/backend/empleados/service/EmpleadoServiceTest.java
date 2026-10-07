package com.laramadita.backend.empleados.service;

import static com.laramadita.backend.usuarios.model.UsuariosFixture.cargo;
import static com.laramadita.backend.usuarios.model.UsuariosFixture.cargoOtro;
import static com.laramadita.backend.usuarios.model.UsuariosFixture.empleado;
import static com.laramadita.backend.usuarios.model.UsuariosFixture.usuario;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.laramadita.backend.empleados.api.dto.EmpleadoActualizarRequest;
import com.laramadita.backend.empleados.api.dto.EmpleadoRequest;
import com.laramadita.backend.empleados.api.dto.EmpleadoResponse;
import com.laramadita.backend.empleados.model.Cargo;
import com.laramadita.backend.empleados.model.Empleado;
import com.laramadita.backend.empleados.repository.CargoRepository;
import com.laramadita.backend.empleados.repository.EmpleadoRepository;
import com.laramadita.backend.shared.error.ConflictoException;
import com.laramadita.backend.shared.error.ReglaNegocioException;
import com.laramadita.backend.usuarios.model.RolNombre;
import com.laramadita.backend.usuarios.model.Usuario;
import com.laramadita.backend.usuarios.repository.UsuarioRepository;
import com.laramadita.backend.usuarios.service.UsuarioService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EmpleadoServiceTest {

	@Mock
	EmpleadoRepository empleados;
	@Mock
	CargoRepository cargos;
	@Mock
	UsuarioRepository usuarios;
	@Mock
	UsuarioService usuariosService;

	EmpleadoService service;
	Cargo mesero;
	Usuario admin;

	@BeforeEach
	void preparar() {
		service = new EmpleadoService(empleados, cargos, usuarios, usuariosService);
		mesero = cargo(3, "Mesero");
		admin = usuario(1, "admin", RolNombre.ADMINISTRADOR, true);
		when(cargos.findById(3)).thenReturn(Optional.of(mesero));
		when(usuarios.findByUsername("admin")).thenReturn(Optional.of(admin));
		when(empleados.existsByDni(any())).thenReturn(false);
		when(empleados.existsByDniAndIdNot(any(), any())).thenReturn(false);
	}

	@Test
	void creaUnEmpleadoConDniDeOchoDigitos() {
		EmpleadoResponse r = service.crear(new EmpleadoRequest("Ana", "Perez", "45678901", "987654321",
				"ana@gmail.com", 3, true));

		ArgumentCaptor<Empleado> guardado = ArgumentCaptor.forClass(Empleado.class);
		verify(empleados).save(guardado.capture());
		assertThat(guardado.getValue().getDni()).isEqualTo("45678901");
		assertThat(guardado.getValue().getCargo().getNombre()).isEqualTo("Mesero");
		assertThat(r.nombres()).isEqualTo("Ana");
		assertThat(r.usuario()).isNull();
	}

	@Test
	void rechazaDniInvalidoAlCrear() {
		assertThatThrownBy(() -> service.crear(
				new EmpleadoRequest("Ana", "Perez", "123", null, null, 3, true)))
				.isInstanceOf(ReglaNegocioException.class)
				.hasMessageContaining("8 dígitos");
		verify(empleados, never()).save(any());
	}

	@Test
	void rechazaDniDuplicadoAlCrear() {
		when(empleados.existsByDni("45678901")).thenReturn(true);
		assertThatThrownBy(() -> service.crear(
				new EmpleadoRequest("Ana", "Perez", "45678901", null, null, 3, true)))
				.isInstanceOf(ConflictoException.class)
				.hasMessageContaining("DNI");
		verify(empleados, never()).save(any());
	}

	@Test
	void inactivarEmpleadoInactivaSuUsuario() {
		Empleado trabajador = empleado(10, true);
		Usuario cuenta = usuario(10, "mesero1", RolNombre.MESERO, true, trabajador);
		ReflectionTestUtils.setField(trabajador, "usuario", cuenta);
		when(empleados.findByIdConUsuario(10)).thenReturn(Optional.of(trabajador));
		when(usuarios.findByEmpleado_Id(10)).thenReturn(Optional.of(cuenta));

		EmpleadoResponse r = service.desactivar(10, "admin");

		assertThat(trabajador.isActivo()).isFalse();
		assertThat(trabajador.isArchivado()).isFalse();
		verify(usuariosService).inactivarPorBajaDeEmpleado(cuenta);
		verify(usuariosService, never()).archivarPorBajaDeEmpleado(any());
		assertThat(r.activo()).isFalse();
		assertThat(r.archivado()).isFalse();
	}

	@Test
	void archivarEmpleadoArchivaSuUsuarioYNoBorra() {
		Empleado trabajador = empleado(14, true);
		Usuario cuenta = usuario(14, "mesero3", RolNombre.MESERO, true, trabajador);
		ReflectionTestUtils.setField(trabajador, "usuario", cuenta);
		when(empleados.findByIdConUsuario(14)).thenReturn(Optional.of(trabajador));
		when(usuarios.findByEmpleado_Id(14)).thenReturn(Optional.of(cuenta));

		EmpleadoResponse r = service.archivar(14, "admin");

		assertThat(trabajador.isArchivado()).isTrue();
		assertThat(trabajador.isActivo()).isFalse();
		assertThat(trabajador.getFechaArchivado()).isNotNull();
		verify(usuariosService).archivarPorBajaDeEmpleado(cuenta);
		verify(empleados, never()).delete(any());
		assertThat(r.archivado()).isTrue();
		assertThat(r.fechaArchivado()).isNotNull();
	}

	@Test
	void unEmpleadoArchivadoNoSeReactivaDesdeEditar() {
		Empleado archivado = empleado(15, false);
		archivado.archivar();
		when(empleados.findByIdConUsuario(15)).thenReturn(Optional.of(archivado));
		when(usuarios.findByEmpleado_Id(15)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.actualizar(15, new EmpleadoActualizarRequest("Nombre", "Apellido", "12345678",
				null, null, 3, true), "admin"))
				.isInstanceOf(ReglaNegocioException.class)
				.hasMessageContaining("archivado");
	}

	@Test
	void reactivarEmpleadoNoReactivaUsuario() {
		Empleado trabajador = empleado(11, false);
		Usuario cuenta = usuario(11, "mesero2", RolNombre.MESERO, false, trabajador);
		ReflectionTestUtils.setField(trabajador, "usuario", cuenta);
		when(empleados.findByIdConUsuario(11)).thenReturn(Optional.of(trabajador));

		EmpleadoResponse r = service.activar(11);

		assertThat(trabajador.isActivo()).isTrue();
		assertThat(cuenta.isActivo()).isFalse();
		assertThat(r.activo()).isTrue();
		verify(usuariosService, never()).inactivarPorBajaDeEmpleado(any());
	}

	@Test
	void noPermiteInactivarAlEmpleadoDeLaSesionActual() {
		when(empleados.findByIdConUsuario(admin.getEmpleado().getId())).thenReturn(Optional.of(admin.getEmpleado()));
		when(usuarios.findByEmpleado_Id(admin.getEmpleado().getId())).thenReturn(Optional.of(admin));

		assertThatThrownBy(() -> service.desactivar(admin.getEmpleado().getId(), "admin"))
				.isInstanceOf(ReglaNegocioException.class)
				.hasMessageContaining("su propia cuenta");
	}

	@Test
	void unEmpleadoHeredadoPuedeQuedarseSinDniAlEditar() {
		Empleado heredado = empleado(12, "Luis", "Rojas", null, cargoOtro(), true);
		when(empleados.findByIdConUsuario(12)).thenReturn(Optional.of(heredado));
		when(usuarios.findByEmpleado_Id(12)).thenReturn(Optional.empty());
		when(cargos.findById(8)).thenReturn(Optional.of(cargoOtro()));

		EmpleadoResponse r = service.actualizar(12, new EmpleadoActualizarRequest("Luis", "Rojas", null, "999111222",
				null, 8, true), "admin");

		assertThat(heredado.getDni()).isNull();
		assertThat(r.telefono()).isEqualTo("999111222");
	}

	@Test
	void noQuitaElDniUnaVezRegistrado() {
		Empleado conDni = empleado(13, true);
		when(empleados.findByIdConUsuario(13)).thenReturn(Optional.of(conDni));

		assertThatThrownBy(() -> service.actualizar(13, new EmpleadoActualizarRequest("Nombre", "Apellido", null, null,
				null, 3, true), "admin"))
				.isInstanceOf(ReglaNegocioException.class)
				.hasMessageContaining("DNI");
	}
}
