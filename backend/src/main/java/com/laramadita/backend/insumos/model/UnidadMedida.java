package com.laramadita.backend.insumos.model;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public enum UnidadMedida {

	UNIDAD,
	KILOGRAMO,
	GRAMO,
	LITRO,
	MILILITRO;

	public static Optional<UnidadMedida> desde(String texto) {
		if (texto == null || texto.isBlank()) {
			return Optional.empty();
		}
		String limpio = texto.trim().toUpperCase(Locale.ROOT)
				.replace(' ', '_')
				.replace("(KG)", "")
				.replace("(G)", "")
				.replace("(L)", "")
				.replace("(ML)", "");
		return Arrays.stream(values()).filter(u -> u.name().equals(limpio)).findFirst();
	}
}
