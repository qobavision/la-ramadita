package com.laramadita.backend.insumos.model;

import java.math.BigDecimal;

public enum EstadoStock {

	NORMAL,
	STOCK_BAJO,
	AGOTADO;

	public static EstadoStock de(BigDecimal stockActual, BigDecimal stockMinimo) {
		BigDecimal actual = stockActual == null ? BigDecimal.ZERO : stockActual;
		BigDecimal minimo = stockMinimo == null ? BigDecimal.ZERO : stockMinimo;
		if (actual.compareTo(BigDecimal.ZERO) == 0) {
			return AGOTADO;
		}
		if (actual.compareTo(minimo) <= 0) {
			return STOCK_BAJO;
		}
		return NORMAL;
	}
}
