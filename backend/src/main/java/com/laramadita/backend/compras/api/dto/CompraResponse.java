package com.laramadita.backend.compras.api.dto;

import com.laramadita.backend.compras.model.Compra;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CompraResponse(
		Integer id,
		String codigo,
		Integer proveedorId,
		String proveedor,
		LocalDateTime fechaRegistro,
		String estado,
		BigDecimal total,
		String registradoPor,
		List<CompraDetalleResponse> detalles) {

	public static CompraResponse de(Compra compra) {
		var usuario = compra.getRegistradoPor();
		return new CompraResponse(
				compra.getId(),
				compra.getCodigo(),
				compra.getProveedor().getId(),
				compra.getProveedor().getNombreRazonSocial(),
				compra.getFechaCompra(),
				compra.getEstado().name(),
				compra.total(),
				usuario == null ? null : usuario.getUsername(),
				compra.getDetalles().stream().map(CompraDetalleResponse::de).toList());
	}
}
