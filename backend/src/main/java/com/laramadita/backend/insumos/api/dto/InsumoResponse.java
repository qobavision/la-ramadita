package com.laramadita.backend.insumos.api.dto;

import com.laramadita.backend.insumos.model.EstadoStock;
import com.laramadita.backend.insumos.model.Insumo;
import java.math.BigDecimal;

public record InsumoResponse(
		Integer id,
		String nombre,
		String unidadMedida,
		BigDecimal stockActual,
		BigDecimal stockMinimo,
		EstadoStock estado,
		boolean activo) {

	public static InsumoResponse de(Insumo insumo) {
		return new InsumoResponse(
				insumo.getId(),
				insumo.getNombre(),
				insumo.getUnidadMedida().name(),
				insumo.getStockActual(),
				insumo.getStockMinimo(),
				insumo.estadoStock(),
				insumo.isActivo());
	}
}
