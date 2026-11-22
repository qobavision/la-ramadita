package com.laramadita.backend.inventario.model;

/**
 * Tipos persistidos en {@code dbo.inventario_movimientos} (V1).
 *
 * ENTRADA_COMPRA = recepción. SALIDA_RECETA = consumo por receta al preparar
 * en Cocina (V15). Los ajustes quedan listos para correcciones futuras.
 */
public enum TipoMovimientoInventario {

	ENTRADA_COMPRA,
	SALIDA_RECETA,
	AJUSTE_ENTRADA,
	AJUSTE_SALIDA;

	public String tipoApi() {
		return switch (this) {
			case ENTRADA_COMPRA -> "ENTRADA";
			case SALIDA_RECETA -> "CONSUMO";
			case AJUSTE_ENTRADA, AJUSTE_SALIDA -> "AJUSTE";
		};
	}
}
