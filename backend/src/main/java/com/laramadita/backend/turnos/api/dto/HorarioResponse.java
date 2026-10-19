package com.laramadita.backend.turnos.api.dto;

import com.laramadita.backend.turnos.model.HorarioOperacion;
import java.time.format.DateTimeFormatter;

public record HorarioResponse(String ambito, String unidad, String horaInicio, String horaFin) {

	private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

	public static HorarioResponse de(HorarioOperacion horario) {
		return new HorarioResponse(
				horario.getUnidad().getNombre().name(),
				horario.getUnidad().getNombre().etiqueta(),
				horario.getHoraInicio().format(HORA),
				horario.getHoraFin().format(HORA));
	}
}
