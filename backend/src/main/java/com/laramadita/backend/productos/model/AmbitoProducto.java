package com.laramadita.backend.productos.model;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public enum AmbitoProducto {

	RESTAURANTE,
	POLLERIA,
	AMBOS;

	public static Optional<AmbitoProducto> desde(String texto) {
		return valor(texto);
	}

	public boolean cubre(AmbitoCarta carta) {
		return this == AMBOS || name().equals(carta.name());
	}

	static Optional<AmbitoProducto> valor(String texto) {
		if (texto == null || texto.isBlank()) {
			return Optional.empty();
		}
		String limpio = texto.trim().toUpperCase(Locale.ROOT)
				.replace('Í', 'I')
				.replace('á', 'A');
		if ("RESTAURANTE".equals(limpio) || "POLLERIA".equals(limpio) || "AMBOS".equals(limpio)) {
			return Optional.of(valueOf(limpio));
		}
		return Arrays.stream(values()).filter(v -> v.name().equals(limpio)).findFirst();
	}
}
