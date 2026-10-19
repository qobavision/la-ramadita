package com.laramadita.backend.productos.api.dto;

import com.laramadita.backend.productos.model.ProductoComponente;
import java.math.BigDecimal;

public record ComponenteResponse(Integer productoId, String nombre, BigDecimal cantidad) {

	public static ComponenteResponse de(ProductoComponente pieza) {
		return new ComponenteResponse(
				pieza.getComponente().getId(),
				pieza.getComponente().getNombre(),
				pieza.getCantidad());
	}
}
