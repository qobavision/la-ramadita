package com.laramadita.backend.productos.model;

import java.util.Locale;
import java.util.Optional;

public enum EstadoProductoCarta {

	DISPONIBLE,
	AGOTADO;

	public static Optional<EstadoProductoCarta> desde(String texto) {
		if (texto == null || texto.isBlank()) {
			return Optional.empty();
		}
		try {
			return Optional.of(valueOf(texto.trim().toUpperCase(Locale.ROOT)));
		} catch (IllegalArgumentException e) {
			return Optional.empty();
		}
	}
}
