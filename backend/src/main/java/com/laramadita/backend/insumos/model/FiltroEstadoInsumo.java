package com.laramadita.backend.insumos.model;

import com.laramadita.backend.shared.error.ReglaNegocioException;
import java.util.Locale;

/** Filtro operativo del listado de insumos. No se confunde con el estado de stock. */
public enum FiltroEstadoInsumo {

	ACTIVOS,
	INACTIVOS,
	TODOS;

	public static FiltroEstadoInsumo desde(String valor) {
		if (valor == null || valor.isBlank()) {
			return ACTIVOS;
		}
		try {
			return valueOf(valor.trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException e) {
			throw new ReglaNegocioException("El estado debe ser ACTIVOS, INACTIVOS o TODOS");
		}
	}
}
