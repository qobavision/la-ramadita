package com.laramadita.backend.shared.tiemporeal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Entrega los {@link EventoTiempoReal} cuando la transacción que los publicó
 * se confirma: así la pantalla que recibe el aviso ya encuentra los datos
 * guardados al volver a consultar la API.
 */
@Component
class NotificadorTiempoReal {

	private static final Logger log = LoggerFactory.getLogger(NotificadorTiempoReal.class);

	private final TiempoRealHandler handler;

	NotificadorTiempoReal(TiempoRealHandler handler) {
		this.handler = handler;
	}

	/**
	 * Un aviso que no se pudo enviar nunca debe convertir en error una operación
	 * que ya se guardó: se registra y la pantalla se pondrá al día al reconectar.
	 */
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
	void alConfirmar(EventoTiempoReal evento) {
		try {
			handler.difundir(evento);
		} catch (RuntimeException e) {
			log.warn("No se pudo difundir el aviso {}: {}", evento.tipo(), e.getMessage());
		}
	}
}
