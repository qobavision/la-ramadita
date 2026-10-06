package com.laramadita.backend.shared.config;

import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * CORS para el frontend Angular. El origen se toma de configuración
 * (variable CORS_ALLOWED_ORIGINS) porque en la red local el frontend no
 * siempre se sirve desde {@code localhost} (AGENTS.md §13).
 *
 * Se expone como bean {@link CorsConfigurationSource} para que Spring Security
 * lo aplique antes de la autenticación (preflight OPTIONS incluido).
 */
@Configuration
public class CorsConfig {

	@Bean
	CorsConfigurationSource corsConfigurationSource(
			@Value("${app.cors.allowed-origins}") String allowedOrigins) {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(separar(allowedOrigins));
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(List.of("*"));
		config.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/api/**", config);
		return source;
	}

	private static List<String> separar(String valor) {
		return Arrays.stream(valor.split(","))
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.toList();
	}
}
