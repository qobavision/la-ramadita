package com.laramadita.backend.pedidos.api.dto;

import com.laramadita.backend.zonas.model.Mesa;

/** Mesa del salón con su estado y, si está en uso, la atención en curso. */
public record MesaSalonResponse(
		Integer id,
		String nombre,
		Integer zonaId,
		String zona,
		String estado,
		AtencionResumenResponse atencion) {

	public static MesaSalonResponse de(Mesa mesa, AtencionResumenResponse atencion) {
		return new MesaSalonResponse(
				mesa.getId(),
				mesa.nombre(),
				mesa.getZona().getId(),
				mesa.getZona().getNombre(),
				mesa.getEstado().name(),
				atencion);
	}
}
