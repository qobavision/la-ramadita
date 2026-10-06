package com.laramadita.backend.shared.error;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

/**
 * Escribe respuestas con formato Problem Details (RFC 9457) desde puntos donde
 * no interviene Spring MVC, como los filtros de Spring Security.
 */
public final class ProblemaHttp {

	private ProblemaHttp() {
	}

	public static void escribir(HttpServletResponse response, HttpStatus status, String titulo, String detalle)
			throws IOException {
		response.setStatus(status.value());
		response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.getWriter().write(json(status, titulo, detalle));
	}

	static String json(HttpStatus status, String titulo, String detalle) {
		return "{\"type\":\"about:blank\",\"title\":\"" + escapar(titulo) + "\",\"status\":" + status.value()
				+ ",\"detail\":\"" + escapar(detalle) + "\"}";
	}

	private static String escapar(String texto) {
		if (texto == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder(texto.length() + 8);
		for (char c : texto.toCharArray()) {
			switch (c) {
				case '"' -> sb.append("\\\"");
				case '\\' -> sb.append("\\\\");
				case '\n' -> sb.append("\\n");
				case '\r' -> sb.append("\\r");
				case '\t' -> sb.append("\\t");
				default -> {
					if (c < 0x20) {
						sb.append(String.format("\\u%04x", (int) c));
					} else {
						sb.append(c);
					}
				}
			}
		}
		return sb.toString();
	}
}
