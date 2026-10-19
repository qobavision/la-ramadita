package com.laramadita.backend.zonas.api.dto;

import com.laramadita.backend.zonas.model.Zona;

public record ZonaResponse(
		Integer id,
		String nombre,
		boolean activa,
		int cantidadMesas,
		int cajas,
		int cocinas,
		String uso) {

	public static ZonaResponse de(Zona zona) {
		return new ZonaResponse(
				zona.getId(),
				zona.getNombre(),
				zona.isActivo(),
				zona.getMesas().size(),
				zona.getCajas(),
				zona.getCocinas(),
				zona.uso().name());
	}
}
