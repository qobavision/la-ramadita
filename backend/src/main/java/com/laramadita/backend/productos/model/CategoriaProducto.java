package com.laramadita.backend.productos.model;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public enum CategoriaProducto {

	MENU,
	ENTRADA,
	SEGUNDO,
	PLATO,
	PLATO_EJECUTIVO,
	PLATO_MARINO,
	BEBIDA,
	ENSALADA,
	POLLO_A_LA_BRASA,
	CHIFA,
	GUARNICION,
	COMBO,
	POSTRE,
	EXTRA,
	OTRO;

	private static final Set<CategoriaProducto> DE_RECETA = Set.of(
			ENTRADA, SEGUNDO, PLATO, PLATO_EJECUTIVO, PLATO_MARINO, CHIFA, POLLO_A_LA_BRASA, BEBIDA, ENSALADA, EXTRA);

	public static Optional<CategoriaProducto> desde(String texto) {
		if (texto == null || texto.isBlank()) {
			return Optional.empty();
		}
		String original = texto.trim().toUpperCase(Locale.ROOT);
		if ("MENU".equals(original) || "MENÚ".equals(original)) {
			return Optional.of(MENU);
		}
		String limpio = original
				.replace('Ú', 'U')
				.replace('Í', 'I')
				.replace('Ó', 'O')
				.replace(' ', '_')
				.replace('-', '_');
		return Optional.ofNullable(alias(limpio))
				.or(() -> Arrays.stream(values()).filter(v -> v.name().equals(limpio)).findFirst());
	}

	public boolean esDeReceta() {
		return DE_RECETA.contains(this);
	}

	public CategoriaProducto paraPersistir() {
		return this == PLATO ? PLATO_EJECUTIVO : this;
	}

	private static CategoriaProducto alias(String limpio) {
		return switch (limpio) {
			case "PLATO", "PLATO_EJECUTIVO", "PLATOS_EJECUTIVOS" -> PLATO_EJECUTIVO;
			case "PLATO_MARINO", "PLATOS_MARINOS", "MARINO" -> PLATO_MARINO;
			case "PLATO_CHIFA", "PLATOS_CHIFA", "CHIFA" -> CHIFA;
			case "PLATO_A_LA_BRASA", "PLATOS_A_LA_BRASA", "POLLO_A_LA_BRASA", "BRASA" -> POLLO_A_LA_BRASA;
			default -> null;
		};
	}
}
