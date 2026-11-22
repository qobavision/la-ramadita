package com.laramadita.backend.shared.tiemporeal;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.json.JsonMapper;

/**
 * Sesiones WebSocket abiertas en la red local. Solo envía (servidor → pantalla);
 * los mensajes entrantes se ignoran porque toda acción pasa por la API REST,
 * donde se validan permisos y reglas de negocio.
 */
@Component
public class TiempoRealHandler extends TextWebSocketHandler {

	private static final Logger log = LoggerFactory.getLogger(TiempoRealHandler.class);
	private static final int LIMITE_ENVIO_MS = 5_000;
	private static final int LIMITE_BUFFER = 256 * 1024;

	private final Map<String, WebSocketSession> sesiones = new ConcurrentHashMap<>();
	private final JsonMapper json;

	public TiempoRealHandler(JsonMapper json) {
		this.json = json;
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) {
		sesiones.put(session.getId(), new ConcurrentWebSocketSessionDecorator(session, LIMITE_ENVIO_MS, LIMITE_BUFFER));
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
		sesiones.remove(session.getId());
	}

	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) {
		sesiones.remove(session.getId());
	}

	void difundir(EventoTiempoReal evento) {
		Map<String, Object> cuerpo = new LinkedHashMap<>();
		cuerpo.put("tipo", evento.tipo());
		if (evento.datos() != null) {
			cuerpo.putAll(evento.datos());
		}
		TextMessage mensaje = new TextMessage(json.writeValueAsString(cuerpo));
		sesiones.values().stream()
				.filter(s -> s.isOpen() && corresponde(s, evento))
				.forEach(s -> enviar(s, mensaje));
	}

	/**
	 * Cierra las sesiones que ya no deben recibir avisos. El navegador se
	 * reconecta solo y el handshake vuelve a decidir con el estado actual.
	 */
	void cerrarSi(Predicate<WebSocketSession> debeCerrarse) {
		for (WebSocketSession sesion : sesiones.values()) {
			if (!sesion.isOpen() || debeCerrarse.test(sesion)) {
				sesiones.remove(sesion.getId());
				try {
					sesion.close(CloseStatus.POLICY_VIOLATION.withReason("Sesión no vigente"));
				} catch (IOException | IllegalStateException e) {
					log.debug("No se pudo cerrar la sesión {}: {}", sesion.getId(), e.getMessage());
				}
			}
		}
	}

	private static boolean corresponde(WebSocketSession sesion, EventoTiempoReal evento) {
		Map<String, Object> atributos = sesion.getAttributes();
		if (evento.username() != null) {
			return evento.username().equals(atributos.get(TiempoRealHandshake.ATRIBUTO_USUARIO));
		}
		return atributos.get(TiempoRealHandshake.ATRIBUTO_CANALES) instanceof Set<?> canales
				&& canales.contains(evento.canal());
	}

	private void enviar(WebSocketSession sesion, TextMessage mensaje) {
		try {
			sesion.sendMessage(mensaje);
		} catch (IOException | IllegalStateException e) {
			log.debug("No se pudo enviar el aviso a la sesión {}: {}", sesion.getId(), e.getMessage());
			sesiones.remove(sesion.getId());
		}
	}
}
