package com.laramadita.backend.empleados.api.dto;

import com.laramadita.backend.empleados.model.Empleado;
import java.time.LocalDateTime;

public record EmpleadoResponse(
		Integer id,
		String nombres,
		String apellidos,
		String dni,
		String telefono,
		String correoPersonal,
		CargoResponse cargo,
		boolean activo,
		boolean archivado,
		LocalDateTime fechaCreacion,
		LocalDateTime fechaArchivado,
		UsuarioResumenResponse usuario) {

	public static EmpleadoResponse de(Empleado empleado) {
		return new EmpleadoResponse(
				empleado.getId(),
				empleado.getNombres(),
				empleado.getApellidos(),
				empleado.getDni(),
				empleado.getTelefono(),
				empleado.getCorreoPersonal(),
				CargoResponse.de(empleado.getCargo()),
				empleado.isActivo(),
				empleado.isArchivado(),
				empleado.getFechaCreacion(),
				empleado.getFechaArchivado(),
				empleado.getUsuario() == null ? null : UsuarioResumenResponse.de(empleado.getUsuario()));
	}
}
