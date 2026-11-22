package com.laramadita.backend.caja.api.dto;

import com.laramadita.backend.caja.model.MetodoPago;

public record MetodoPagoResponse(Integer id, String nombre) {

	public static MetodoPagoResponse de(MetodoPago m) {
		return new MetodoPagoResponse(m.getId(), m.getNombre());
	}
}
