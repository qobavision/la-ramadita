package com.laramadita.backend.usuarios.api.dto;

import com.laramadita.backend.empleados.api.dto.EmpleadoResumenResponse;
import com.laramadita.backend.empleados.model.Empleado;
import com.laramadita.backend.usuarios.model.Usuario;
import java.time.LocalDateTime;

/**
 * Representación pública de un usuario. Nunca incluye el hash de contraseña.
 *
 * {@code nombre} y {@code apellido} salen del empleado vinculado (fuente
 * funcional). Se conservan en la respuesta para no romper login y {@code /me}.
 */
public record UsuarioResponse(
		Integer id,
		Integer empleadoId,
		String username,
		String nombre,
		String apellido,
		String rol,
		String rolNombre,
		boolean activo,
		boolean archivado,
		LocalDateTime fechaCreacion,
		LocalDateTime fechaArchivado,
		LocalDateTime fechaUltimoAcceso,
		EmpleadoResumenResponse empleado) {

	public static UsuarioResponse de(Usuario u) {
		Empleado empleado = u.getEmpleado();
		return new UsuarioResponse(
				u.getId(),
				empleado == null ? null : empleado.getId(),
				u.getUsername(),
				empleado != null ? empleado.getNombres() : u.getNombre(),
				empleado != null ? vacioANulo(empleado.getApellidos()) : u.getApellido(),
				u.getRol().getNombre().name(),
				u.getRol().getNombre().valorBaseDatos(),
				u.isActivo(),
				u.isArchivado(),
				u.getFechaCreacion(),
				u.getFechaArchivado(),
				u.getFechaUltimoAcceso(),
				empleado == null ? null : EmpleadoResumenResponse.de(empleado));
	}

	private static String vacioANulo(String valor) {
		return valor == null || valor.isBlank() ? null : valor;
	}
}
