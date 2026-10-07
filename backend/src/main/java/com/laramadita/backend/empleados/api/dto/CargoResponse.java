package com.laramadita.backend.empleados.api.dto;

import com.laramadita.backend.empleados.model.Cargo;

public record CargoResponse(Integer id, String nombre) {

	public static CargoResponse de(Cargo cargo) {
		return new CargoResponse(cargo.getId(), cargo.getNombre());
	}
}
