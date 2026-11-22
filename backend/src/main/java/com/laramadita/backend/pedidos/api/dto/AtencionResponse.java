package com.laramadita.backend.pedidos.api.dto;

import com.laramadita.backend.pedidos.model.Atencion;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** Atención completa: pedido en borrador, pedidos enviados y consumo acumulado. */
public record AtencionResponse(
		Integer id,
		String tipo,
		String identificacion,
		Integer mesaId,
		String zona,
		String mesero,
		String meseroUsername,
		String unidad,
		String estado,
		Instant inicio,
		BigDecimal totalEnviado,
		BigDecimal totalBorrador,
		PedidoResponse borrador,
		List<PedidoResponse> enviados) {

	public static AtencionResponse de(Atencion a) {
		return new AtencionResponse(
				a.getId(),
				a.getTipo().name(),
				a.identificacion(),
				a.getMesa() != null ? a.getMesa().getId() : null,
				a.getMesa() != null ? a.getMesa().getZona().getNombre() : null,
				AtencionResumenResponse.nombreDe(a),
				a.getMesero().getUsername(),
				a.getTurno().getUnidad().getNombre().etiqueta(),
				a.getEstado().name(),
				AtencionResumenResponse.utc(a.getFechaInicio()),
				a.totalEnviado(),
				a.totalBorrador(),
				a.borrador().map(PedidoResponse::de).orElse(null),
				a.enviados().stream().map(PedidoResponse::de).toList());
	}
}
