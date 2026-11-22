package com.laramadita.backend.caja.api.dto;

import com.laramadita.backend.caja.model.Pago;
import com.laramadita.backend.usuarios.model.Usuario;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public record PagoResponse(
		Integer id,
		Integer atencionId,
		String identificacion,
		String metodo,
		BigDecimal monto,
		String cajero,
		Instant fecha,
		String observacion,
		boolean porProductos,
		String estado,
		String anuladoPor,
		Instant fechaAnulacion,
		String motivoAnulacion) {

	public static PagoResponse de(Pago p) {
		return new PagoResponse(
				p.getId(),
				p.getAtencion().getId(),
				p.getAtencion().identificacion(),
				p.getMetodo().getNombre(),
				p.getMonto(),
				nombreDe(p.getUsuario()),
				utc(p.getFecha()),
				p.getObservacion(),
				!p.getDetalles().isEmpty(),
				p.getEstado().name(),
				nombreDe(p.getAnuladoPor()),
				utc(p.getFechaAnulacion()),
				p.getMotivoAnulacion());
	}

	public static String nombreDe(Usuario u) {
		if (u == null) {
			return null;
		}
		String apellido = u.getApellido();
		return apellido == null || apellido.isBlank() ? u.getNombre() : u.getNombre() + " " + apellido;
	}

	public static Instant utc(LocalDateTime fecha) {
		return fecha == null ? null : fecha.toInstant(ZoneOffset.UTC);
	}
}
