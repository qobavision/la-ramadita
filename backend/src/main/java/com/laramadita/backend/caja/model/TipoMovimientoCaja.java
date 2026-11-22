package com.laramadita.backend.caja.model;

import java.util.Locale;
import java.util.Optional;

/** Ingreso o egreso de caja que no es un pago de venta (M10). */
public enum TipoMovimientoCaja {
	INGRESO,
	EGRESO;

	public static Optional<TipoMovimientoCaja> desde(String texto) {
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
