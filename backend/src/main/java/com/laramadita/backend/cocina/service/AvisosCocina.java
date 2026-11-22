package com.laramadita.backend.cocina.service;

import com.laramadita.backend.pedidos.model.CambioEnCocina;
import com.laramadita.backend.shared.tiemporeal.EventoTiempoReal;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Traduce los cambios de pedidos (M07) en avisos para las pantallas de Cocina:
 * {@code PEDIDO_NUEVO}, {@code PEDIDO_MODIFICADO} y {@code PEDIDO_CANCELADO}.
 * Corre dentro de la transacción del mesero; el envío real ocurre tras el commit.
 */
@Component
class AvisosCocina {

	private final ApplicationEventPublisher eventos;

	AvisosCocina(ApplicationEventPublisher eventos) {
		this.eventos = eventos;
	}

	@EventListener
	void alCambiarPedido(CambioEnCocina cambio) {
		Map<String, Object> datos = new LinkedHashMap<>();
		datos.put("pedidoId", cambio.pedidoId());
		datos.put("atencionId", cambio.atencionId());
		datos.put("identificacion", cambio.identificacion());
		datos.put("zona", cambio.zona());
		datos.put("detalle", cambio.detalle());
		eventos.publishEvent(EventoTiempoReal.paraCanal(AccesoCocina.CANAL, "PEDIDO_" + cambio.tipo().name(), datos));
	}
}
