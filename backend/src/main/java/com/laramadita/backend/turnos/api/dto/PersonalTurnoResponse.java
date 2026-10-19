package com.laramadita.backend.turnos.api.dto;

import com.laramadita.backend.empleados.model.Empleado;
import com.laramadita.backend.turnos.model.TurnoAsistencia;
import com.laramadita.backend.usuarios.model.Usuario;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public record PersonalTurnoResponse(
		Integer id,
		String empleado,
		String username,
		String rolPermanente,
		String funcionActual,
		String horaIngreso) {

	private static final ZoneId LIMA = ZoneId.of("America/Lima");
	private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

	public static PersonalTurnoResponse de(TurnoAsistencia asistencia) {
		Usuario usuario = asistencia.getUsuario();
		Empleado empleado = usuario.getEmpleado();
		String nombre = empleado != null
				? (empleado.getNombres() + " " + (empleado.getApellidos() == null ? "" : empleado.getApellidos())).trim()
				: (usuario.getNombre() + " " + (usuario.getApellido() == null ? "" : usuario.getApellido())).trim();
		String rol = usuario.getRol().getNombre().name();
		return new PersonalTurnoResponse(
				usuario.getId(),
				nombre,
				usuario.getUsername(),
				rol,
				asistencia.funcionActual().name(),
				asistencia.getHoraIngreso().atZone(ZoneId.of("UTC")).withZoneSameInstant(LIMA).format(HORA));
	}
}
