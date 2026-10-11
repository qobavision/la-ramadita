package com.laramadita.backend.inventario.model;

/**
 * Tipos persistidos en {@code dbo.inventario_movimientos} (V1).
 *
 * ENTRADA_COMPRA = recepción. SALIDA_RECETA y ajustes quedan listos
 * para cocina y correcciones futuras, sin implementarse ahora.
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
