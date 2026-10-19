package com.laramadita.backend.turnos.api.dto;

import com.laramadita.backend.turnos.model.EstadoTurno;
import com.laramadita.backend.turnos.model.HorarioOperacion;
import com.laramadita.backend.turnos.model.Turno;
import com.laramadita.backend.unidadesnegocio.model.NombreUnidad;
import com.laramadita.backend.usuarios.model.Usuario;
import java.time.format.DateTimeFormatter;

public record TurnoResponse(
		Integer id,
		String ambito,
		String unidad,
		String estado,
		String horaInicio,
		String horaFin,
		String responsable,
		String cerradoPor) {

	private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

	public static TurnoResponse de(Turno turno, HorarioOperacion horario) {
		return new TurnoResponse(
				turno.getId(),
				turno.getUnidad().getNombre().name(),
				turno.getUnidad().getNombre().etiqueta(),
				turno.getEstado().name(),
				horario.getHoraInicio().format(HORA),
				horario.getHoraFin().format(HORA),
				nombreDe(turno.getUsuarioApertura(), "Apertura automática"),
				nombreDe(turno.getUsuarioCierre(), ""));
	}

	public static TurnoResponse cerrado(NombreUnidad unidad, HorarioOperacion horario) {
		return new TurnoResponse(
				null,
				unidad.name(),
				unidad.etiqueta(),
				EstadoTurno.CERRADO.name(),
				horario.getHoraInicio().format(HORA),
				horario.getHoraFin().format(HORA),
				"",
				"");
	}

	private static String nombreDe(Usuario usuario, String siVacio) {
		if (usuario == null) {
			return siVacio;
		}
		String apellido = usuario.getApellido() == null ? "" : usuario.getApellido().trim();
		return (usuario.getNombre() + " " + apellido).trim();
	}
}
