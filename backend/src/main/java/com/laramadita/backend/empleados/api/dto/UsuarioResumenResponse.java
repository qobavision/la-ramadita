package com.laramadita.backend.empleados.api.dto;

import com.laramadita.backend.usuarios.model.Usuario;

/** Recorte de la cuenta de acceso, para listar empleados. */
public record UsuarioResumenResponse(Integer id, String username, String rol, boolean activo) {

	public static UsuarioResumenResponse de(Usuario usuario) {
		return new UsuarioResumenResponse(
				usuario.getId(),
				usuario.getUsername(),
				usuario.getRol().getNombre().name(),
				usuario.isActivo());
	}
}
