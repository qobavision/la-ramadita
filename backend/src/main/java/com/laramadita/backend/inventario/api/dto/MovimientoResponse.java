package com.laramadita.backend.inventario.api.dto;

import com.laramadita.backend.inventario.model.InventarioMovimiento;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MovimientoResponse(
		Integer id,
		LocalDateTime fechaHora,
		Integer insumoId,
		String insumo,
		String unidadMedida,
		String tipo,
		BigDecimal cantidad,
		BigDecimal stockAnterior,
		BigDecimal stockResultante,
		String origen,
		String usuario) {

	public static MovimientoResponse de(InventarioMovimiento movimiento) {
		var insumo = movimiento.getInsumo();
		var usuario = movimiento.getUsuario();
		String nombreUsuario = usuario.getUsername();
		if (usuario.getNombre() != null && !usuario.getNombre().isBlank()) {
			nombreUsuario = usuario.getApellido() == null || usuario.getApellido().isBlank()
					? usuario.getNombre()
					: usuario.getNombre() + " " + usuario.getApellido();
		}
		return new MovimientoResponse(
				movimiento.getId(),
				movimiento.getFechaMovimiento(),
				insumo.getId(),
				insumo.getNombre(),
				insumo.getUnidadMedida().name(),
				movimiento.getTipoMovimiento().tipoApi(),
				movimiento.getCantidad(),
				movimiento.getStockAnterior(),
				movimiento.getStockNuevo(),
				movimiento.getMotivo(),
				nombreUsuario);
	}
}
