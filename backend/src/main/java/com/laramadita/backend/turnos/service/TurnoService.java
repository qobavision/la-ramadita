package com.laramadita.backend.turnos.service;

import com.laramadita.backend.shared.error.ConflictoException;
import com.laramadita.backend.shared.error.RecursoNoEncontradoException;
import com.laramadita.backend.shared.error.ReglaNegocioException;
import com.laramadita.backend.turnos.api.dto.FuncionTurnoRequest;
import com.laramadita.backend.turnos.api.dto.HorarioRequest;
import com.laramadita.backend.turnos.api.dto.HorarioResponse;
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
import com.laramadita.backend.usuarios.repository.UsuarioRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TurnoService {

	private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");
	private static final ZoneId LIMA = ZoneId.of("America/Lima");
	private static final List<EstadoTurno> EN_CURSO = List.of(EstadoTurno.ABIERTO, EstadoTurno.EN_CIERRE);

	private final TurnoRepository turnos;
	private final TurnoAsistenciaRepository asistencias;
	private final HorarioOperacionRepository horarios;
	private final UnidadNegocioRepository unidades;
	private final UsuarioRepository usuarios;
	private final Clock clock;

	public TurnoService(
			TurnoRepository turnos,
			TurnoAsistenciaRepository asistencias,
			HorarioOperacionRepository horarios,
			UnidadNegocioRepository unidades,
			UsuarioRepository usuarios,
			Clock clock) {
		this.turnos = turnos;
		this.asistencias = asistencias;
		this.horarios = horarios;
		this.unidades = unidades;
		this.usuarios = usuarios;
		this.clock = clock;
	}

	@Transactional(readOnly = true)
	public List<HorarioResponse> listarHorarios() {
		return horarios.findAllByOrderByUnidad_IdAsc().stream().map(HorarioResponse::de).toList();
	}

	public HorarioResponse actualizarHorario(String ambito, HorarioRequest request) {
		NombreUnidad unidad = exigirUnidad(ambito);
		HorarioOperacion horario = horarioDe(unidad);
		horario.actualizar(exigirHora(request.horaInicio()), exigirHora(request.horaFin()));
		return HorarioResponse.de(horario);
	}

	public TurnoResponse actual() {
		abrirPendientesPorHorario();
		return turnos.findFirstByEstadoInOrderByFechaAperturaDesc(EN_CURSO)
				.map(turno -> TurnoResponse.de(turno, horarioDe(turno.getUnidad().getNombre())))
				.orElseGet(() -> {
					NombreUnidad visible = unidadEnVentanaActual().orElse(NombreUnidad.RESTAURANTE);
					return TurnoResponse.cerrado(visible, horarioDe(visible));
				});
	}

	public List<PersonalTurnoResponse> personalActual() {
		abrirPendientesPorHorario();
		return turnos.findFirstByEstadoInOrderByFechaAperturaDesc(EN_CURSO)
				.map(turno -> asistencias.findByTurnoOrderByHoraIngresoAsc(turno).stream()
						.map(PersonalTurnoResponse::de)
						.toList())
				.orElseGet(List::of);
	}

	public TurnoResponse iniciar(String ambito, String username) {
		NombreUnidad nombre = exigirUnidad(ambito);
		UnidadNegocio unidad = unidades.findByNombre(nombre)
				.orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la unidad"));
		if (turnos.existsByUnidadAndEstadoIn(unidad, EN_CURSO)) {
			throw new ConflictoException("Ya hay un turno en curso para " + nombre.etiqueta());
		}
		Usuario responsable = usuarioDe(username);
		Turno turno = turnos.save(new Turno(unidad, responsable));
		registrarIngreso(turno, responsable);
		return TurnoResponse.de(turno, horarioDe(nombre));
	}

	public TurnoResponse prepararCierre(Integer id) {
		Turno turno = buscar(id);
		turno.prepararCierre();
		return TurnoResponse.de(turno, horarioDe(turno.getUnidad().getNombre()));
	}

	public TurnoResponse cerrar(Integer id, String username) {
		Turno turno = buscar(id);
		turno.cerrar(usuarioDe(username));
		return TurnoResponse.de(turno, horarioDe(turno.getUnidad().getNombre()));
	}

	public PersonalTurnoResponse asignarFuncion(Integer usuarioId, FuncionTurnoRequest request, String actorUsername) {
		Usuario actor = usuarioDe(actorUsername);
		TurnoAsistencia fila = asistenciaDelTurno(usuarioId);
		exigirPuedeGestionar(actor, fila.getUsuario());
		RolNombre funcion = request == null || request.funcionActual() == null || request.funcionActual().isBlank()
				? null
				: RolNombre.desde(request.funcionActual())
						.orElseThrow(() -> new ReglaNegocioException(
								"La función temporal debe ser SUPERVISOR, MESERO, COCINA o CAJA"));
		exigirFuncionPermitida(actor, funcion);
		fila.asignarFuncion(funcion);
		return PersonalTurnoResponse.de(fila);
	}

	public void expulsar(Integer usuarioId, String actorUsername) {
		Usuario actor = usuarioDe(actorUsername);
		TurnoAsistencia fila = asistenciaDelTurno(usuarioId);
		exigirPuedeGestionar(actor, fila.getUsuario());
		asistencias.delete(fila);
	}

	@Transactional(readOnly = true)
	public String funcionActualDe(Usuario usuario) {
		if (usuario == null || usuario.getRol() == null) {
			return null;
		}
		String permanente = usuario.getRol().getNombre().name();
		return turnos.findFirstByEstadoInOrderByFechaAperturaDesc(EN_CURSO)
				.flatMap(turno -> asistencias.findByTurnoAndUsuario(turno, usuario))
				.map(asistencia -> asistencia.funcionActual().name())
				.orElse(permanente);
	}

	public void registrarIngresoSiHayTurno(Usuario usuario) {
		if (usuario == null || !usuario.puedeIniciarSesion()) {
			return;
		}
		abrirPendientesPorHorario();
		turnos.findFirstByEstadoInOrderByFechaAperturaDesc(EN_CURSO)
				.ifPresent(turno -> registrarIngreso(turno, usuario));
	}

	public void abrirPendientesPorHorario() {
		abrirSiCorresponde(false);
	}

	public void abrirPendientesAlArrancar() {
		abrirSiCorresponde(true);
	}

	private void abrirSiCorresponde(boolean alArrancar) {
		if (turnos.existsByEstadoIn(EN_CURSO)) {
			return;
		}
		LocalDateTime ahoraLima = ahoraLima();
		LocalTime hora = ahoraLima.toLocalTime();
		LocalDate dia = ahoraLima.toLocalDate();
		for (HorarioOperacion horario : horarios.findAllByOrderByUnidad_IdAsc()) {
			if (!enVentana(hora, horario.getHoraInicio(), horario.getHoraFin())) {
				continue;
			}
			UnidadNegocio unidad = horario.getUnidad();
			LocalDate diaTurno = diaDeTurno(dia, hora, horario.getHoraInicio(), horario.getHoraFin());
			if (!alArrancar && yaHuboTurnoElDia(unidad, diaTurno)) {
				continue;
			}
			try {
				turnos.save(Turno.automatico(unidad));
			} catch (DataIntegrityViolationException omitido) {
				return;
			}
			return;
		}
	}

	private static void exigirPuedeGestionar(Usuario actor, Usuario objetivo) {
		if (objetivo.esAdministrador() && !actor.esAdministrador()) {
			throw new ReglaNegocioException("El supervisor no puede modificar a un administrador");
		}
	}

	private static void exigirFuncionPermitida(Usuario actor, RolNombre funcion) {
		if (funcion == null) {
			return;
		}
		if (funcion == RolNombre.ADMINISTRADOR) {
			throw new ReglaNegocioException("Administrador no es una función de turno");
		}
		if (funcion == RolNombre.SUPERVISOR && !actor.esAdministrador()) {
			throw new ReglaNegocioException("Solo el administrador puede asignar la función de supervisor");
		}
	}

	private TurnoAsistencia asistenciaDelTurno(Integer usuarioId) {
		Turno turno = turnos.findFirstByEstadoInOrderByFechaAperturaDesc(EN_CURSO)
				.orElseThrow(() -> new ReglaNegocioException("No hay un turno en curso"));
		Usuario usuario = usuarios.findById(usuarioId)
				.orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el usuario"));
		return asistencias.findByTurnoAndUsuario(turno, usuario)
				.orElseThrow(() -> new RecursoNoEncontradoException("Esa persona no está en el turno"));
	}

	private java.util.Optional<NombreUnidad> unidadEnVentanaActual() {
		LocalTime hora = ahoraLima().toLocalTime();
		return horarios.findAllByOrderByUnidad_IdAsc().stream()
				.filter(horario -> enVentana(hora, horario.getHoraInicio(), horario.getHoraFin()))
				.map(horario -> horario.getUnidad().getNombre())
				.findFirst();
	}

	private LocalDateTime ahoraLima() {
		return LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC)
				.atZone(ZoneOffset.UTC)
				.withZoneSameInstant(LIMA)
				.toLocalDateTime();
	}

	private void registrarIngreso(Turno turno, Usuario usuario) {
		if (asistencias.existsByTurnoAndUsuario(turno, usuario)) {
			return;
		}
		try {
			asistencias.save(new TurnoAsistencia(turno, usuario));
		} catch (DataIntegrityViolationException omitido) {
			// El mismo usuario reingresó al mismo turno.
		}
	}

	private boolean yaHuboTurnoElDia(UnidadNegocio unidad, LocalDate diaLima) {
		LocalDateTime desde = diaLima.atStartOfDay(LIMA).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
		LocalDateTime hasta = diaLima.plusDays(1).atStartOfDay(LIMA).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
		return turnos.existsByUnidadAndFechaAperturaGreaterThanEqualAndFechaAperturaLessThan(unidad, desde, hasta);
	}

	static LocalDate diaDeTurno(LocalDate diaLima, LocalTime ahora, LocalTime inicio, LocalTime fin) {
		if (fin.isBefore(inicio) && ahora.isBefore(fin)) {
			return diaLima.minusDays(1);
		}
		return diaLima;
	}

	static boolean enVentana(LocalTime ahora, LocalTime inicio, LocalTime fin) {
		if (inicio.equals(fin)) {
			return true;
		}
		if (fin.isAfter(inicio)) {
			return !ahora.isBefore(inicio) && ahora.isBefore(fin);
		}
		return !ahora.isBefore(inicio) || ahora.isBefore(fin);
	}

	private Turno buscar(Integer id) {
		return turnos.findWithRelacionesById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el turno"));
	}

	private HorarioOperacion horarioDe(NombreUnidad unidad) {
		return horarios.findByUnidad_Nombre(unidad)
				.orElseThrow(() -> new RecursoNoEncontradoException("No hay horario para " + unidad.etiqueta()));
	}

	private Usuario usuarioDe(String username) {
		return usuarios.findByUsername(Usuario.normalizarUsername(username))
				.orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el usuario"));
	}

	private static NombreUnidad exigirUnidad(String texto) {
		return NombreUnidad.desde(texto)
				.orElseThrow(() -> new ReglaNegocioException("El ámbito debe ser RESTAURANTE o POLLERIA"));
	}

	private static LocalTime exigirHora(String texto) {
		try {
			return LocalTime.parse(texto.trim(), HORA);
		} catch (DateTimeParseException | NullPointerException ex) {
			throw new ReglaNegocioException("La hora debe tener el formato HH:mm");
		}
	}
}
