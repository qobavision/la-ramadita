package com.laramadita.backend.pedidos.api.dto;

import com.laramadita.backend.pedidos.model.Atencion;
import com.laramadita.backend.pedidos.model.EstadoCocina;
import com.laramadita.backend.pedidos.model.Pedido;
import com.laramadita.backend.pedidos.model.PedidoDetalle;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.function.Predicate;

/** Atención en curso, para el salón y la lista de pedidos del mesero. */
public record AtencionResumenResponse(
		Integer id,
		String tipo,
		String identificacion,
		Integer mesaId,
		String zona,
		String mesero,
		String meseroUsername,
		String estado,
		Instant inicio,
		BigDecimal total,
		int itemsBorrador,
		int itemsPendientes,
		int itemsPreparados) {

	public static AtencionResumenResponse de(Atencion a) {
		return new AtencionResumenResponse(
				a.getId(),
				a.getTipo().name(),
				a.identificacion(),
				a.getMesa() != null ? a.getMesa().getId() : null,
				a.getMesa() != null ? a.getMesa().getZona().getNombre() : null,
				nombreDe(a),
				a.getMesero().getUsername(),
				a.getEstado().name(),
				utc(a.getFechaInicio()),
				a.totalEnviado(),
				a.borrador().map(p -> unidades(p, d -> true)).orElse(0),
				a.enviados().stream().mapToInt(p -> unidades(p, d -> d.getEstadoCocina() == EstadoCocina.PENDIENTE)).sum(),
				a.enviados().stream().mapToInt(p -> unidades(p, d -> d.getEstadoCocina() == EstadoCocina.PREPARADO)).sum());
	}

	static String nombreDe(Atencion a) {
		String apellido = a.getMesero().getApellido();
		return apellido == null || apellido.isBlank()
				? a.getMesero().getNombre()
				: a.getMesero().getNombre() + " " + apellido;
	}

	static Instant utc(LocalDateTime fecha) {
		return fecha == null ? null : fecha.toInstant(ZoneOffset.UTC);
	}

	private static int unidades(Pedido p, Predicate<PedidoDetalle> filtro) {
		return p.getDetalles().stream().filter(PedidoDetalle::activo).filter(filtro).mapToInt(PedidoDetalle::getCantidad).sum();
	}
}
