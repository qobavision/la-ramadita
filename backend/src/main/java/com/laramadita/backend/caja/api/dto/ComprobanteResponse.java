package com.laramadita.backend.caja.api.dto;

import com.laramadita.backend.caja.model.Comprobante;
import com.laramadita.backend.caja.model.Pago;
import com.laramadita.backend.pedidos.model.Atencion;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** Boleta simple (M10, sin SUNAT): datos de la venta, productos y pagos. */
public record ComprobanteResponse(
		Integer id,
		String tipo,
		String codigo,
		String estado,
		Instant fechaEmision,
		String unidad,
		String identificacion,
		String mesero,
		String emitidoPor,
		BigDecimal total,
		List<Linea> lineas,
		List<PagoLinea> pagos) {

	public record Linea(String producto, int cantidad, BigDecimal precioUnitario, BigDecimal subtotal) {
	}

	public record PagoLinea(String metodo, BigDecimal monto) {
	}

	/** @param pagos pagos CONFIRMADOS de la atención */
	public static ComprobanteResponse de(Comprobante c, List<Pago> pagos) {
		Atencion a = c.getAtencion();
		return new ComprobanteResponse(
				c.getId(),
				c.getTipo().name(),
				c.codigo(),
				c.getEstado().name(),
				PagoResponse.utc(c.getFechaEmision()),
				a.getTurno().getUnidad().getNombre().etiqueta(),
				a.identificacion(),
				PagoResponse.nombreDe(a.getMesero()),
				PagoResponse.nombreDe(c.getEmitidoPor()),
				c.getTotal(),
				CuentaResponse.lineasActivas(a).stream()
						.map(d -> new Linea(d.nombre(), d.getCantidad(), d.getPrecioUnitario(), d.subtotal()))
						.toList(),
				pagos.stream().map(p -> new PagoLinea(p.getMetodo().getNombre(), p.getMonto())).toList());
	}
}
