package com.laramadita.backend.zonas.api.dto;

import com.laramadita.backend.zonas.model.Mesa;

public record MesaResponse(
		Integer id,
		Integer zonaId,
		String zona,
		String numero,
		String nombre,
		String estado,
		boolean activa) {

	public static MesaResponse de(Mesa mesa) {
		return new MesaResponse(
				mesa.getId(),
				mesa.getZona().getId(),
				mesa.getZona().getNombre(),
				mesa.getNumero(),
				"Mesa " + mesa.getNumero(),
				mesa.getEstado().name(),
				mesa.isActivo());
	}
}
