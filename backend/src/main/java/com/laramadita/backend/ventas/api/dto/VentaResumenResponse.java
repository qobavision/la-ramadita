package com.laramadita.backend.ventas.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Venta histórica (M11): atención cobrada con su total y métodos de pago.
 *
 * @param consumo suma de productos activos al precio del momento de la venta
 * @param cobrado suma de pagos confirmados
 */
public record VentaResumenResponse(
		Integer atencionId,
		Instant fecha,
		String unidad,
		String tipo,
		String identificacion,
		String zona,
		String mesero,
		BigDecimal consumo,
		BigDecimal cobrado,
		List<MontoMetodoResponse> metodos) {
}
