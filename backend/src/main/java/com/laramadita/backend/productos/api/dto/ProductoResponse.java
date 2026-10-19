package com.laramadita.backend.productos.api.dto;

import com.laramadita.backend.productos.model.Producto;
import java.math.BigDecimal;
import java.util.List;

public record ProductoResponse(
		Integer id,
		String nombre,
		String categoria,
		String ambito,
		BigDecimal precio,
		boolean activo,
		boolean compuesto,
		List<ComponenteResponse> componentes) {

	public static ProductoResponse de(Producto producto) {
		List<ComponenteResponse> piezas = producto.getComponentes().stream()
				.map(ComponenteResponse::de)
				.toList();
		return new ProductoResponse(
				producto.getId(),
				producto.getNombre(),
				producto.getCategoria().name(),
				producto.getAmbito().name(),
				producto.getPrecio(),
				producto.isActivo(),
				!piezas.isEmpty(),
				piezas);
	}
}
