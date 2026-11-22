package com.laramadita.backend.shared.tiemporeal;

import java.util.Map;

/**
 * Aviso que se envía por WebSocket a las pantallas conectadas (AGENTS.md §10).
 *
 * Los módulos lo publican con {@code ApplicationEventPublisher} dentro de su
 * transacción; {@link NotificadorTiempoReal} lo entrega solo después del
 * commit. El aviso es una señal: el frontend vuelve a consultar la API REST,
 * que sigue siendo la fuente de verdad.
 *
 * Se entrega a quienes estén suscritos a {@code canal} (ver
 * {@link CanalTiempoReal}) o, si se indica {@code username}, solo a ese usuario.
 *
 * @param tipo     nombre del evento, p. ej. {@code PEDIDO_NUEVO}
 * @param canal    canal destino (p. ej. {@code cocina}) o {@code null}
 * @param username usuario destino (p. ej. el mesero de la atención) o {@code null}
 * @param datos    contenido mínimo para mostrar el aviso
 */
public record EventoTiempoReal(String tipo, String canal, String username, Map<String, Object> datos) {

	public static EventoTiempoReal paraCanal(String canal, String tipo, Map<String, Object> datos) {
		return new EventoTiempoReal(tipo, canal, null, datos);
	}

	public static EventoTiempoReal paraUsuario(String username, String tipo, Map<String, Object> datos) {
		return new EventoTiempoReal(tipo, null, username, datos);
	}
}
