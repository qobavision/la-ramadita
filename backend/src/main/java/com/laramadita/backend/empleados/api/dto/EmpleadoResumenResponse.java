package com.laramadita.backend.empleados.api.dto;

import com.laramadita.backend.empleados.model.Empleado;

/** Recorte del trabajador, para listar usuarios. */
public record EmpleadoResumenResponse(
		Integer id,
		String nombres,
		String apellidos,
		String dni,
		String correoPersonal,
		String cargo,
		boolean activo) {

	public static EmpleadoResumenResponse de(Empleado empleado) {
		return new EmpleadoResumenResponse(
				empleado.getId(),
				empleado.getNombres(),
				empleado.getApellidos(),
				empleado.getDni(),
				empleado.getCorreoPersonal(),
				empleado.getCargo().getNombre(),
				empleado.isActivo());
	}
}
