package com.laramadita.backend.insumos.model;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public enum UnidadMedida {

	UNIDAD,
	KILOGRAMO,
	GRAMO,
	LITRO,
	MILILITRO;

	private static final BigDecimal MIL = new BigDecimal("1000");
	private static final BigDecimal MILESIMA = new BigDecimal("0.001");

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

	public Optional<BigDecimal> factorHacia(UnidadMedida destino) {
		if (destino == null) {
			return Optional.empty();
		}
		if (this == destino) {
			return Optional.of(BigDecimal.ONE);
		}
		if (this == KILOGRAMO && destino == GRAMO) {
			return Optional.of(MIL);
		}
		if (this == GRAMO && destino == KILOGRAMO) {
			return Optional.of(MILESIMA);
		}
		if (this == LITRO && destino == MILILITRO) {
			return Optional.of(MIL);
		}
		if (this == MILILITRO && destino == LITRO) {
			return Optional.of(MILESIMA);
		}
		return Optional.empty();
	}

	public String etiqueta() {
		return switch (this) {
			case UNIDAD -> "unidad";
			case KILOGRAMO -> "kilogramo";
			case GRAMO -> "gramo";
			case LITRO -> "litro";
			case MILILITRO -> "mililitro";
		};
	}
}
