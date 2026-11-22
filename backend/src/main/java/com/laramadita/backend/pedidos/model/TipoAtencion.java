package com.laramadita.backend.pedidos.model;

import java.util.Locale;
import java.util.Optional;

/** Modalidad de atención (M06 / RN-03). */
public enum TipoAtencion {
	MESA,
	PARA_LLEVAR;

	public static Optional<TipoAtencion> desde(String texto) {
		if (texto == null || texto.isBlank()) {
			return Optional.empty();
		}
		try {
			return Optional.of(valueOf(texto.trim().toUpperCase(Locale.ROOT).replace(' ', '_')));
		} catch (IllegalArgumentException e) {
			return Optional.empty();
		}
	}
}
