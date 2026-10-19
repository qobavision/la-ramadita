package com.laramadita.backend.productos.api.dto;

import com.laramadita.backend.productos.model.Producto;
import java.math.BigDecimal;
import java.util.List;

public record RecetaResponse(
		Integer productoId,
		String nombre,
		String categoria,
		String ambito,
		BigDecimal precio,
		boolean activo,
		List<RecetaInsumoResponse> insumos) {

	public static RecetaResponse de(Producto producto) {
		return new RecetaResponse(
				producto.getId(),
				producto.getNombre(),
				producto.getCategoria().name(),
				producto.getAmbito().name(),
				producto.getPrecio(),
				producto.isActivo(),
				producto.getReceta().stream().map(RecetaInsumoResponse::de).toList());
	}
}
