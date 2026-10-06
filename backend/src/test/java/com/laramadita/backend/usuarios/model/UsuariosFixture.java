package com.laramadita.backend.usuarios.model;

import org.springframework.test.util.ReflectionTestUtils;

/**
 * Construye entidades reales para pruebas unitarias. Vive en el paquete del
 * modelo para poder usar el constructor protegido de {@link Rol}; los campos
 * sin setter (id, nombre del rol) se asignan por reflexión.
 */
public final class UsuariosFixture {

	public static final String HASH_FICTICIO = "$2a$10$hash-ficticio-para-pruebas";

	private UsuariosFixture() {
	}

	public static Rol rol(RolNombre nombre) {
		Rol rol = new Rol();
		ReflectionTestUtils.setField(rol, "id", nombre.ordinal() + 1);
		ReflectionTestUtils.setField(rol, "nombre", nombre);
		ReflectionTestUtils.setField(rol, "descripcion", nombre.valorBaseDatos());
		ReflectionTestUtils.setField(rol, "activo", true);
		return rol;
	}

	public static Usuario usuario(Integer id, String username, RolNombre rolNombre, boolean activo) {
		Usuario u = new Usuario(username, HASH_FICTICIO, "Nombre", "Apellido", rol(rolNombre));
		ReflectionTestUtils.setField(u, "id", id);
		if (!activo) {
			u.desactivar();
		}
		return u;
	}
}
