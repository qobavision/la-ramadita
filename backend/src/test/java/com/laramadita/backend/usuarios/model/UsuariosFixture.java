package com.laramadita.backend.usuarios.model;

import com.laramadita.backend.empleados.model.Cargo;
import com.laramadita.backend.empleados.model.Empleado;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Construye entidades reales para pruebas unitarias. Vive en el paquete del
 * modelo para poder usar el constructor protegido de {@link Rol}; los campos
 * sin setter se asignan por reflexión.
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

	public static Cargo cargo(Integer id, String nombre) {
		Cargo cargo = new Cargo();
		ReflectionTestUtils.setField(cargo, "id", id);
		ReflectionTestUtils.setField(cargo, "nombre", nombre);
		ReflectionTestUtils.setField(cargo, "activo", true);
		return cargo;
	}

	public static Cargo cargoOtro() {
		return cargo(8, "Otro");
	}

	public static Empleado empleado(Integer id, boolean activo) {
		return empleado(id, "Nombre", "Apellido", "1234567" + id, cargoOtro(), activo);
	}

	public static Empleado empleado(Integer id, String nombres, String apellidos, String dni, Cargo cargo,
			boolean activo) {
		Empleado e = new Empleado(nombres, apellidos, dni, null, null, cargo);
		ReflectionTestUtils.setField(e, "id", id);
		if (!activo) {
			e.desactivar();
		}
		return e;
	}

	public static Usuario usuario(Integer id, String username, RolNombre rolNombre, boolean activo) {
		return usuario(id, username, rolNombre, activo, empleado(id, true));
	}

	public static Usuario usuario(Integer id, String username, RolNombre rolNombre, boolean activo, Empleado empleado) {
		Usuario u = new Usuario(username, HASH_FICTICIO, empleado, rol(rolNombre));
		ReflectionTestUtils.setField(u, "id", id);
		if (!activo) {
			u.desactivar();
		}
		return u;
	}
}
