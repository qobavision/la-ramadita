package com.laramadita.backend.turnos;

import com.laramadita.backend.turnos.service.TurnoService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AperturaTurnoProgramada {

	private final TurnoService turnos;

	public AperturaTurnoProgramada(TurnoService turnos) {
		this.turnos = turnos;
	}

	@EventListener(ApplicationReadyEvent.class)
	public void alArrancar() {
		turnos.abrirPendientesAlArrancar();
	}

	@Scheduled(cron = "0 * * * * *", zone = "America/Lima")
	public void revisarHorario() {
		turnos.abrirPendientesPorHorario();
	}
}
