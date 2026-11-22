package com.laramadita.backend.zonas.api.dto;

import com.laramadita.backend.zonas.model.UsoZona;
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
		return de(zona, false);
	}

	/** Una zona sin mesas en operación pero con atenciones registradas queda en HISTORIAL. */
	public static ZonaResponse de(Zona zona, boolean conHistorial) {
		UsoZona uso = zona.uso() == UsoZona.SIN_USO && conHistorial ? UsoZona.HISTORIAL : zona.uso();
		return new ZonaResponse(
				zona.getId(),
				zona.getNombre(),
				zona.isActivo(),
				zona.getMesas().size(),
				zona.getCajas(),
				zona.getCocinas(),
				uso.name());
	}
}
