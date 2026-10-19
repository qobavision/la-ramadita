package com.laramadita.backend.turnos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.laramadita.backend.shared.error.ConflictoException;
import com.laramadita.backend.turnos.api.dto.FuncionTurnoRequest;
import com.laramadita.backend.turnos.api.dto.HorarioRequest;
import com.laramadita.backend.turnos.api.dto.PersonalTurnoResponse;
import com.laramadita.backend.turnos.api.dto.TurnoResponse;
import com.laramadita.backend.turnos.model.EstadoTurno;
import com.laramadita.backend.turnos.model.HorarioOperacion;
import com.laramadita.backend.turnos.model.Turno;
import com.laramadita.backend.turnos.model.TurnoAsistencia;
import com.laramadita.backend.turnos.repository.HorarioOperacionRepository;
import com.laramadita.backend.turnos.repository.TurnoAsistenciaRepository;
import com.laramadita.backend.turnos.repository.TurnoRepository;
import com.laramadita.backend.unidadesnegocio.model.NombreUnidad;
import com.laramadita.backend.unidadesnegocio.model.UnidadNegocio;
import com.laramadita.backend.unidadesnegocio.repository.UnidadNegocioRepository;
import com.laramadita.backend.usuarios.model.RolNombre;
import com.laramadita.backend.usuarios.model.Usuario;
import com.laramadita.backend.usuarios.model.UsuariosFixture;
import com.laramadita.backend.usuarios.repository.UsuarioRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TurnoServiceTest {

	@Mock
	TurnoRepository turnos;
	@Mock
	TurnoAsistenciaRepository asistencias;
	@Mock
	HorarioOperacionRepository horarios;
	@Mock
	UnidadNegocioRepository unidades;
	@Mock
	UsuarioRepository usuarios;

	TurnoService service;
	UnidadNegocio restaurante;
	HorarioOperacion horario;
	Usuario supervisor;
	Clock reloj;

	@BeforeEach
	void preparar() {
		reloj = Clock.fixed(Instant.parse("2026-10-19T09:00:00Z"), ZoneOffset.UTC);
		service = new TurnoService(turnos, asistencias, horarios, unidades, usuarios, reloj);
		restaurante = nueva(UnidadNegocio.class);
		ReflectionTestUtils.setField(restaurante, "id", 1);
		ReflectionTestUtils.setField(restaurante, "nombre", NombreUnidad.RESTAURANTE);
		horario = nueva(HorarioOperacion.class);
		ReflectionTestUtils.setField(horario, "unidad", restaurante);
		ReflectionTestUtils.setField(horario, "horaInicio", LocalTime.of(7, 0));
		ReflectionTestUtils.setField(horario, "horaFin", LocalTime.of(16, 0));
		supervisor = mock(Usuario.class);
		when(supervisor.getNombre()).thenReturn("Rosa");
		when(supervisor.getApellido()).thenReturn("Huamán");
		when(unidades.findByNombre(NombreUnidad.RESTAURANTE)).thenReturn(Optional.of(restaurante));
		when(horarios.findByUnidad_Nombre(NombreUnidad.RESTAURANTE)).thenReturn(Optional.of(horario));
		when(usuarios.findByUsername("rosa.huaman")).thenReturn(Optional.of(supervisor));
		when(turnos.save(any(Turno.class))).thenAnswer(inv -> {
			Turno t = inv.getArgument(0);
			ReflectionTestUtils.setField(t, "id", 9);
			return t;
		});
		when(turnos.existsByUnidadAndEstadoIn(any(), any())).thenReturn(false);
		when(turnos.existsByEstadoIn(any())).thenReturn(false);
		when(horarios.findAllByOrderByUnidad_IdAsc()).thenReturn(List.of(horario));
		when(turnos.findFirstByEstadoInOrderByFechaAperturaDesc(any())).thenReturn(Optional.empty());
		when(turnos.findFirstByEstadoOrderByFechaCierreDesc(EstadoTurno.CERRADO)).thenReturn(Optional.empty());
		when(asistencias.existsByTurnoAndUsuario(any(), any())).thenReturn(false);
	}

	@Test
	void ventanaDeRestauranteNoAbreDeMadrugada() {
		assertThat(TurnoService.enVentana(LocalTime.of(4, 0), LocalTime.of(7, 0), LocalTime.of(16, 0))).isFalse();
		assertThat(TurnoService.enVentana(LocalTime.of(7, 0), LocalTime.of(7, 0), LocalTime.of(16, 0))).isTrue();
		assertThat(TurnoService.enVentana(LocalTime.of(17, 0), LocalTime.of(16, 0), LocalTime.of(1, 0))).isTrue();
		assertThat(TurnoService.enVentana(LocalTime.of(4, 0), LocalTime.of(16, 0), LocalTime.of(1, 0))).isFalse();
		assertThat(TurnoService.diaDeTurno(
						java.time.LocalDate.of(2026, 10, 20), LocalTime.of(0, 30), LocalTime.of(16, 0), LocalTime.of(1, 0)))
				.isEqualTo(java.time.LocalDate.of(2026, 10, 19));
	}

	@Test
	void actualSinTurnoQuedaCerradoEnRestaurante() {
		TurnoResponse r = service.actual();
		assertThat(r.estado()).isEqualTo("CERRADO");
		assertThat(r.unidad()).isEqualTo("Restaurante");
		assertThat(r.horaInicio()).isEqualTo("07:00");
	}

	@Test
	void iniciarAbreElTurno() {
		TurnoResponse r = service.iniciar("RESTAURANTE", "rosa.huaman");
		assertThat(r.id()).isEqualTo(9);
		assertThat(r.estado()).isEqualTo("ABIERTO");
		assertThat(r.responsable()).isEqualTo("Rosa Huamán");
	}

	@Test
	void noPermiteDosTurnosEnLaMismaUnidad() {
		when(turnos.existsByUnidadAndEstadoIn(restaurante, List.of(EstadoTurno.ABIERTO, EstadoTurno.EN_CIERRE)))
				.thenReturn(true);
		assertThatThrownBy(() -> service.iniciar("RESTAURANTE", "rosa.huaman"))
				.isInstanceOf(ConflictoException.class);
	}

	@Test
	void actualizaHorario() {
		assertThat(service.actualizarHorario("RESTAURANTE", new HorarioRequest("08:00", "15:30")).horaInicio())
				.isEqualTo("08:00");
	}

	@Test
	void aLasSieteAbreRestauranteSinResponsable() {
		TurnoService aLasSiete = new TurnoService(
				turnos,
				asistencias,
				horarios,
				unidades,
				usuarios,
				Clock.fixed(Instant.parse("2026-10-19T12:00:00Z"), ZoneOffset.UTC));
		aLasSiete.abrirPendientesPorHorario();
		verify(turnos).save(org.mockito.ArgumentMatchers.argThat(t -> t.getUsuarioApertura() == null));
	}

	@Test
	void polleriaDespuesDeMedianocheNoReabreSiYaHuboTurno() {
		HorarioOperacion polleria = nueva(HorarioOperacion.class);
		UnidadNegocio unidadPolleria = nueva(UnidadNegocio.class);
		ReflectionTestUtils.setField(unidadPolleria, "id", 2);
		ReflectionTestUtils.setField(unidadPolleria, "nombre", NombreUnidad.POLLERIA);
		ReflectionTestUtils.setField(polleria, "unidad", unidadPolleria);
		ReflectionTestUtils.setField(polleria, "horaInicio", LocalTime.of(16, 0));
		ReflectionTestUtils.setField(polleria, "horaFin", LocalTime.of(1, 0));
		when(horarios.findAllByOrderByUnidad_IdAsc()).thenReturn(List.of(polleria));
		when(turnos.existsByUnidadAndFechaAperturaGreaterThanEqualAndFechaAperturaLessThan(any(), any(), any()))
				.thenReturn(true);
		TurnoService deMadrugada = new TurnoService(
				turnos,
				asistencias,
				horarios,
				unidades,
				usuarios,
				Clock.fixed(Instant.parse("2026-10-20T05:30:00Z"), ZoneOffset.UTC));
		deMadrugada.abrirPendientesPorHorario();
		verify(turnos, never()).save(any());
	}

	@Test
	void alArrancarAbrePolleriaAunqueYaSeHubieraCerrado() {
		HorarioOperacion polleria = nueva(HorarioOperacion.class);
		UnidadNegocio unidadPolleria = nueva(UnidadNegocio.class);
		ReflectionTestUtils.setField(unidadPolleria, "id", 2);
		ReflectionTestUtils.setField(unidadPolleria, "nombre", NombreUnidad.POLLERIA);
		ReflectionTestUtils.setField(polleria, "unidad", unidadPolleria);
		ReflectionTestUtils.setField(polleria, "horaInicio", LocalTime.of(16, 0));
		ReflectionTestUtils.setField(polleria, "horaFin", LocalTime.of(1, 0));
		when(horarios.findAllByOrderByUnidad_IdAsc()).thenReturn(List.of(polleria));
		when(turnos.existsByUnidadAndFechaAperturaGreaterThanEqualAndFechaAperturaLessThan(any(), any(), any()))
				.thenReturn(true);
		TurnoService alArrancar = new TurnoService(
				turnos,
				asistencias,
				horarios,
				unidades,
				usuarios,
				Clock.fixed(Instant.parse("2026-10-19T22:37:00Z"), ZoneOffset.UTC));
		alArrancar.abrirPendientesAlArrancar();
		verify(turnos).save(org.mockito.ArgumentMatchers.argThat(t -> t.getUsuarioApertura() == null));
	}

	@Test
	void funcionActualSigueAlRolTemporalDelTurno() {
		Usuario mesero = UsuariosFixture.usuario(2, "mc1", RolNombre.MESERO, true);
		Turno abierto = new Turno(restaurante, supervisor);
		ReflectionTestUtils.setField(abierto, "id", 9);
		TurnoAsistencia fila = new TurnoAsistencia(abierto, mesero);
		fila.asignarFuncion(RolNombre.COCINA);
		when(turnos.findFirstByEstadoInOrderByFechaAperturaDesc(any())).thenReturn(Optional.of(abierto));
		when(asistencias.findByTurnoAndUsuario(abierto, mesero)).thenReturn(Optional.of(fila));
		assertThat(service.funcionActualDe(mesero)).isEqualTo("COCINA");
	}

	@Test
	void asignaFuncionTemporalSinCambiarElRolPermanente() {
		Usuario mesero = UsuariosFixture.usuario(2, "mc1", RolNombre.MESERO, true);
		Turno abierto = new Turno(restaurante, supervisor);
		ReflectionTestUtils.setField(abierto, "id", 9);
		TurnoAsistencia fila = new TurnoAsistencia(abierto, mesero);
		ReflectionTestUtils.setField(fila, "horaIngreso", LocalDateTime.of(2026, 10, 19, 20, 0));
		when(turnos.findFirstByEstadoInOrderByFechaAperturaDesc(any())).thenReturn(Optional.of(abierto));
		when(usuarios.findById(2)).thenReturn(Optional.of(mesero));
		when(asistencias.findByTurnoAndUsuario(abierto, mesero)).thenReturn(Optional.of(fila));
		PersonalTurnoResponse r = service.asignarFuncion(2, new FuncionTurnoRequest("CAJA"), "rosa.huaman");
		assertThat(r.rolPermanente()).isEqualTo("MESERO");
		assertThat(r.funcionActual()).isEqualTo("CAJA");
	}

	@Test
	void elAdministradorPuedeAsignarSupervisorEnElTurno() {
		Usuario admin = UsuariosFixture.usuario(1, "admin", RolNombre.ADMINISTRADOR, true);
		Usuario mesero = UsuariosFixture.usuario(2, "mc1", RolNombre.MESERO, true);
		Turno abierto = new Turno(restaurante, supervisor);
		ReflectionTestUtils.setField(abierto, "id", 9);
		TurnoAsistencia fila = new TurnoAsistencia(abierto, mesero);
		ReflectionTestUtils.setField(fila, "horaIngreso", LocalDateTime.of(2026, 10, 19, 20, 0));
		when(usuarios.findByUsername("admin")).thenReturn(Optional.of(admin));
		when(turnos.findFirstByEstadoInOrderByFechaAperturaDesc(any())).thenReturn(Optional.of(abierto));
		when(usuarios.findById(2)).thenReturn(Optional.of(mesero));
		when(asistencias.findByTurnoAndUsuario(abierto, mesero)).thenReturn(Optional.of(fila));
		PersonalTurnoResponse r = service.asignarFuncion(2, new FuncionTurnoRequest("SUPERVISOR"), "admin");
		assertThat(r.funcionActual()).isEqualTo("SUPERVISOR");
		assertThat(r.rolPermanente()).isEqualTo("MESERO");
	}

	@Test
	void elSupervisorNoPuedeAsignarLaFuncionDeSupervisor() {
		Usuario mesero = UsuariosFixture.usuario(2, "mc1", RolNombre.MESERO, true);
		Turno abierto = new Turno(restaurante, supervisor);
		TurnoAsistencia fila = new TurnoAsistencia(abierto, mesero);
		when(turnos.findFirstByEstadoInOrderByFechaAperturaDesc(any())).thenReturn(Optional.of(abierto));
		when(usuarios.findById(2)).thenReturn(Optional.of(mesero));
		when(asistencias.findByTurnoAndUsuario(abierto, mesero)).thenReturn(Optional.of(fila));
		assertThatThrownBy(() -> service.asignarFuncion(2, new FuncionTurnoRequest("SUPERVISOR"), "rosa.huaman"))
				.isInstanceOf(com.laramadita.backend.shared.error.ReglaNegocioException.class)
				.hasMessageContaining("administrador");
	}

	@Test
	void elSupervisorNoPuedeModificarAUnAdministrador() {
		Usuario admin = UsuariosFixture.usuario(1, "admin", RolNombre.ADMINISTRADOR, true);
		Turno abierto = new Turno(restaurante, supervisor);
		TurnoAsistencia fila = new TurnoAsistencia(abierto, admin);
		when(turnos.findFirstByEstadoInOrderByFechaAperturaDesc(any())).thenReturn(Optional.of(abierto));
		when(usuarios.findById(1)).thenReturn(Optional.of(admin));
		when(asistencias.findByTurnoAndUsuario(abierto, admin)).thenReturn(Optional.of(fila));
		assertThatThrownBy(() -> service.asignarFuncion(1, new FuncionTurnoRequest("MESERO"), "rosa.huaman"))
				.isInstanceOf(com.laramadita.backend.shared.error.ReglaNegocioException.class)
				.hasMessageContaining("administrador");
	}

	@Test
	void reingresarAlMismoTurnoNoDuplicaAsistencia() {
		Turno abierto = new Turno(restaurante, supervisor);
		ReflectionTestUtils.setField(abierto, "id", 9);
		when(turnos.findFirstByEstadoInOrderByFechaAperturaDesc(any())).thenReturn(Optional.of(abierto));
		when(asistencias.existsByTurnoAndUsuario(abierto, supervisor)).thenReturn(true);
		when(supervisor.puedeIniciarSesion()).thenReturn(true);
		service.registrarIngresoSiHayTurno(supervisor);
		verify(asistencias, never()).save(any());
	}

	private static <T> T nueva(Class<T> tipo) {
		try {
			var constructor = tipo.getDeclaredConstructor();
			constructor.setAccessible(true);
			return constructor.newInstance();
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException(e);
		}
	}
}
