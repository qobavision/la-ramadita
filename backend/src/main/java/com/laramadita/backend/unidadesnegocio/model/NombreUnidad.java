package com.laramadita.backend.unidadesnegocio.model;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public enum NombreUnidad {
	RESTAURANTE,
	POLLERIA;

	public static Optional<NombreUnidad> desde(String texto) {
		if (texto == null || texto.isBlank()) {
			return Optional.empty();
		}
		String limpio = texto.trim().toUpperCase(Locale.ROOT)
				.replace('Í', 'I')
				.replace('É', 'E');
		if ("POLLERIA".equals(limpio) || "POLLERÍA".equals(texto.trim().toUpperCase(Locale.ROOT))) {
			return Optional.of(POLLERIA);
		}
		return Arrays.stream(values()).filter(v -> v.name().equals(limpio)).findFirst();
	}

	public String etiqueta() {
		return this == POLLERIA ? "Pollería" : "Restaurante";
	}
}
