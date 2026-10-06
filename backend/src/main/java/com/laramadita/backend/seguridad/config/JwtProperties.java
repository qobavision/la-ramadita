package com.laramadita.backend.seguridad.config;

import java.nio.charset.StandardCharsets;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Parámetros de JWT leídos desde {@code app.security.jwt.*}.
 *
 * El secreto llega únicamente por la variable de entorno JWT_SECRET. Si falta
 * o es demasiado corto, la aplicación no arranca: es preferible fallar a
 * firmar tokens con una clave débil o versionada.
 */
@ConfigurationProperties(prefix = "app.security.jwt")
public record JwtProperties(String secret, long expirationMinutes) {

	/** HS256 exige una clave de al menos 256 bits. */
	public static final int LONGITUD_MINIMA_BYTES = 32;

	public JwtProperties {
		if (secret == null || secret.isBlank()) {
			throw new IllegalStateException(
					"Falta la variable de entorno JWT_SECRET. Defina una clave de al menos "
							+ LONGITUD_MINIMA_BYTES + " caracteres antes de iniciar el backend.");
		}
		if (secret.getBytes(StandardCharsets.UTF_8).length < LONGITUD_MINIMA_BYTES) {
			throw new IllegalStateException(
					"JWT_SECRET es demasiado corta. Use al menos " + LONGITUD_MINIMA_BYTES + " caracteres.");
		}
		if (expirationMinutes <= 0) {
			throw new IllegalStateException("JWT_EXPIRATION_MINUTES debe ser mayor que cero.");
		}
	}
}
