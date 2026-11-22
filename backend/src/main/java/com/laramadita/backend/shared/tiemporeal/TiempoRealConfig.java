package com.laramadita.backend.shared.tiemporeal;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Punto de conexión WebSocket de la red local (AGENTS.md §10).
 *
 * Vive bajo {@code /api} para que el proxy de desarrollo y un reverse proxy
 * en producción lo reenvíen igual que la API. Acepta los mismos orígenes que
 * CORS ({@code CORS_ALLOWED_ORIGINS}).
 */
@Configuration
@EnableWebSocket
class TiempoRealConfig implements WebSocketConfigurer {

	static final String RUTA = "/api/v1/tiempo-real";

	private final TiempoRealHandler handler;
	private final TiempoRealHandshake handshake;
	private final String[] origenes;

	TiempoRealConfig(TiempoRealHandler handler, TiempoRealHandshake handshake,
			@Value("${app.cors.allowed-origins}") String origenes) {
		this.handler = handler;
		this.handshake = handshake;
		this.origenes = Arrays.stream(origenes.split(","))
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.toArray(String[]::new);
	}

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(handler, RUTA)
				.addInterceptors(handshake)
				.setAllowedOrigins(origenes);
	}
}
