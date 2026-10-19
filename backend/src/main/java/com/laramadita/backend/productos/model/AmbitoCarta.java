package com.laramadita.backend.productos.model;

import java.util.Locale;
import java.util.Optional;

/** Ámbito de una carta: una sola unidad, nunca AMBOS. */
public enum AmbitoCarta {

	RESTAURANTE,
	POLLERIA;

	public static Optional<AmbitoCarta> desde(String texto) {
		if (texto == null || texto.isBlank()) {
			return Optional.empty();
		}
		String limpio = texto.trim().toUpperCase(Locale.ROOT).replace('Í', 'I');
		try {
			return Optional.of(valueOf(limpio));
		} catch (IllegalArgumentException e) {
			return Optional.empty();
		}
	}
}
