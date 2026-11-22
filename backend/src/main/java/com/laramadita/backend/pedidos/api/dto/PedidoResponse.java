package com.laramadita.backend.pedidos.api.dto;

import com.laramadita.backend.pedidos.model.Pedido;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record PedidoResponse(
		Integer id,
		String estado,
		Instant fechaEnvio,
		BigDecimal total,
		List<DetallePedidoResponse> detalles) {

	public static PedidoResponse de(Pedido p) {
		return new PedidoResponse(
				p.getId(),
				p.getEstado().name(),
				AtencionResumenResponse.utc(p.getFechaEnvio()),
				p.total(),
				p.getDetalles().stream().map(DetallePedidoResponse::de).toList());
	}
}
