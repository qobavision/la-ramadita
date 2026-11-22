package com.laramadita.backend.shared.tiemporeal;

import java.time.Clock;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * El WebSocket se autoriza al conectar; esta revisión periódica corta las
 * sesiones cuyo JWT venció, cuyo usuario fue inactivado o que ya no tienen
 * permiso para su canal (p. ej. expulsado del turno o sin la función). Sin
 * esto seguirían recibiendo avisos hasta reconectarse.
 */
@Component
class RevisionSesionesTiempoReal {

	private static final Logger log = LoggerFactory.getLogger(RevisionSesionesTiempoReal.class);

	private final TiempoRealHandler handler;
	private final TiempoRealHandshake handshake;
	private final Clock clock;

	RevisionSesionesTiempoReal(TiempoRealHandler handler, TiempoRealHandshake handshake, Clock clock) {
		this.handler = handler;
		this.handshake = handshake;
		this.clock = clock;
	}

	@Scheduled(fixedDelay = 60_000, initialDelay = 60_000)
	void revisar() {
		Instant ahora = clock.instant();
		try {
			handler.cerrarSi(sesion -> !handshake.sigueVigente(sesion.getAttributes(), ahora));
		} catch (RuntimeException e) {
			log.warn("No se pudo revisar las sesiones de tiempo real: {}", e.getMessage());
		}
	}
}
