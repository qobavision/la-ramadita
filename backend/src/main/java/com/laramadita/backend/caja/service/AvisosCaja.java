package com.laramadita.backend.caja.service;

import com.laramadita.backend.pedidos.model.CuentaReabierta;
import com.laramadita.backend.pedidos.model.CuentaSolicitada;
import com.laramadita.backend.shared.tiemporeal.EventoTiempoReal;
import java.util.Map;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Traduce los eventos de pedidos a avisos en tiempo real para Caja: cuando el
 * mesero pide la cuenta, aparece al instante en Cobros (sin recargar la página).
 */
@Component
class AvisosCaja {

	private final ApplicationEventPublisher eventos;

	AvisosCaja(ApplicationEventPublisher eventos) {
		this.eventos = eventos;
	}

	@EventListener
	void cuentaReabierta(CuentaReabierta cuenta) {
		eventos.publishEvent(EventoTiempoReal.paraCanal(AccesoCaja.CANAL, "CUENTA_REABIERTA", Map.of(
				"atencionId", cuenta.atencionId(),
				"identificacion", cuenta.identificacion())));
	}

	@EventListener
	void cuentaSolicitada(CuentaSolicitada cuenta) {
		eventos.publishEvent(EventoTiempoReal.paraCanal(AccesoCaja.CANAL, "CUENTA_SOLICITADA", Map.of(
				"atencionId", cuenta.atencionId(),
				"identificacion", cuenta.identificacion(),
				"mesero", cuenta.mesero(),
				"total", cuenta.total())));
	}
}
