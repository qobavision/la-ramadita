package com.laramadita.backend.cocina.api.dto;

import com.laramadita.backend.pedidos.model.EstadoCocina;
import com.laramadita.backend.pedidos.model.PedidoDetalle;
import com.laramadita.backend.pedidos.model.PedidoHistorial;
import com.laramadita.backend.usuarios.model.Usuario;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Producto de una comanda.
 *
 * @param estado           {@code PENDIENTE}, {@code PREPARADO} o {@code CANCELADO}
 * @param modificado       si cambió después del envío (RN-06)
 * @param cantidadAnterior cantidad antes del último cambio, para mostrar "2 → 3"
 * @param cambio           descripción del último cambio o de la cancelación
 */
public record LineaComandaResponse(
		Integer id,
		String producto,
		int cantidad,
		String observacion,
		String estado,
		boolean modificado,
		Integer cantidadAnterior,
		String cambio,
		String preparadoPor,
		Instant preparadoEn) {

	public static LineaComandaResponse de(PedidoDetalle d) {
		PedidoHistorial modificacion = d.ultimaModificacion().orElse(null);
		String cambio = !d.activo()
				? "Cancelado por " + nombre(d.cancelacion().map(PedidoHistorial::getUsuario).orElse(null))
				: modificacion != null ? modificacion.getDetalleCambio() : null;
		return new LineaComandaResponse(
				d.getId(),
				d.nombre(),
				d.getCantidad(),
				d.getObservacion(),
				estado(d),
				modificacion != null || !d.activo(),
				modificacion != null ? modificacion.getCantidadAnterior() : null,
				cambio,
				d.getPreparadoPor() != null ? nombre(d.getPreparadoPor()) : null,
				utc(d.getFechaPreparado()));
	}

	static String estado(PedidoDetalle d) {
		if (!d.activo()) {
			return "CANCELADO";
		}
		return d.getEstadoCocina() == EstadoCocina.PREPARADO ? "PREPARADO" : "PENDIENTE";
	}

	static String nombre(Usuario u) {
		if (u == null) {
			return "el mesero";
		}
		String apellido = u.getApellido();
		return apellido == null || apellido.isBlank() ? u.getNombre() : u.getNombre() + " " + apellido;
	}

	static Instant utc(LocalDateTime fecha) {
		return fecha == null ? null : fecha.toInstant(ZoneOffset.UTC);
	}
}
