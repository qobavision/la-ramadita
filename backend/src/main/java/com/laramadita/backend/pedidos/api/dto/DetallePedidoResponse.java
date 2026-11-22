package com.laramadita.backend.pedidos.api.dto;

import com.laramadita.backend.pedidos.model.PedidoDetalle;
import java.math.BigDecimal;

public record DetallePedidoResponse(
		Integer id,
		Integer productoId,
		String producto,
		int cantidad,
		BigDecimal precioUnitario,
		BigDecimal subtotal,
		String observacion,
		String estadoCocina,
		String estadoItem) {

	public static DetallePedidoResponse de(PedidoDetalle d) {
		return new DetallePedidoResponse(
				d.getId(),
				d.getProducto().getId(),
				d.nombre(),
				d.getCantidad(),
				d.getPrecioUnitario(),
				d.subtotal(),
				d.getObservacion(),
				d.getEstadoCocina().name(),
				d.getEstadoItem().name());
	}
}
