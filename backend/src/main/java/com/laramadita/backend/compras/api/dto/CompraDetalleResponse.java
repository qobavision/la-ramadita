package com.laramadita.backend.compras.api.dto;

import com.laramadita.backend.compras.model.CompraDetalle;
import java.math.BigDecimal;

public record CompraDetalleResponse(
		Integer id,
		Integer insumoId,
		String insumo,
		String unidadMedida,
		BigDecimal cantidad,
		BigDecimal precioUnitario,
		BigDecimal subtotal,
		BigDecimal cantidadRecibida,
		BigDecimal cantidadPendiente,
		String estado) {

	public static CompraDetalleResponse de(CompraDetalle detalle) {
		return new CompraDetalleResponse(
				detalle.getId(),
				detalle.getInsumo().getId(),
				detalle.getInsumo().getNombre(),
				detalle.getInsumo().getUnidadMedida().name(),
				detalle.getCantidadComprada(),
				detalle.getCostoUnitario(),
				detalle.subtotal(),
				detalle.getCantidadRecibida(),
				detalle.pendiente(),
				detalle.estadoItem());
	}
}
