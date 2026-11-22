package com.laramadita.backend.ventas.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Detalle de una venta: lo necesario para reconstruirla aunque luego cambien
 * precios o productos (fecha, productos, cantidades, total, métodos de pago,
 * unidad de negocio y comprobantes).
 */
public record VentaDetalleResponse(
		VentaResumenResponse venta,
		List<Producto> productos,
		List<Pago> pagos,
		List<Comprobante> comprobantes) {

	public record Producto(String producto, int cantidad, BigDecimal precioUnitario, BigDecimal subtotal,
			String observacion) {
	}

	public record Pago(String metodo, BigDecimal monto, Instant fecha, String cajero, String estado) {
	}

	/** @param estado EMITIDO o ANULADO (al anular un pago la boleta queda anulada). */
	public record Comprobante(String tipo, String serie, int numero, BigDecimal total, Instant fecha,
			String cliente, String estado) {
	}
}
