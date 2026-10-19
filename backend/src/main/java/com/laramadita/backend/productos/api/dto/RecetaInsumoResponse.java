package com.laramadita.backend.productos.api.dto;

import com.laramadita.backend.productos.model.RecetaDetalle;
import java.math.BigDecimal;

public record RecetaInsumoResponse(
		Integer insumoId,
		String insumo,
		String unidadMedida,
		BigDecimal cantidad) {

	public static RecetaInsumoResponse de(RecetaDetalle linea) {
		return new RecetaInsumoResponse(
				linea.getInsumo().getId(),
				linea.getInsumo().getNombre(),
				linea.getInsumo().getUnidadMedida().name(),
				linea.getCantidadRequerida());
	}
}
