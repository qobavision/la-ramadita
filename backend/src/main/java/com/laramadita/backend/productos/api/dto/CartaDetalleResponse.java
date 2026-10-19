package com.laramadita.backend.productos.api.dto;

import com.laramadita.backend.productos.model.CartaDetalle;
import java.math.BigDecimal;
import java.util.List;

public record CartaDetalleResponse(
		Integer id,
		Integer productoId,
		String nombre,
		String seccion,
		BigDecimal precioDia,
		BigDecimal precioBase,
		BigDecimal precioEfectivo,
		String estado,
		List<ComponenteResponse> componentes) {

	public static CartaDetalleResponse de(CartaDetalle detalle) {
		return new CartaDetalleResponse(
				detalle.getId(),
				detalle.getProducto().getId(),
				detalle.getProducto().getNombre(),
				detalle.getSeccion().name(),
				detalle.getPrecioDia(),
				detalle.getProducto().getPrecio(),
				detalle.precioEfectivo(),
				detalle.getEstado().name(),
				detalle.getProducto().getComponentes().stream().map(ComponenteResponse::de).toList());
	}
}
