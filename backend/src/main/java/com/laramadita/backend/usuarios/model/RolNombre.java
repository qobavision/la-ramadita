package com.laramadita.backend.usuarios.model;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Roles oficiales del sistema (AGENTS.md, sección 7).
 *
 * La constante Java es ASCII (DUENO) y se mapea al valor almacenado en la
 * tabla {@code roles} sembrada por V1 (DUEÑO). El resto coincide literalmente.
 */
public enum RolNombre {

	DUENO("DUE\u00D1O"),
	SUPERVISOR("SUPERVISOR"),
	MESERO("MESERO"),
	COCINA("COCINA"),
	CAJA("CAJA");

	private static final String PREFIJO_AUTHORITY = "ROLE_";

	private final String valorBaseDatos;

	RolNombre(String valorBaseDatos) {
		this.valorBaseDatos = valorBaseDatos;
	}

	/** Valor exacto guardado en {@code dbo.roles.nombre}. */
	public String valorBaseDatos() {
		return valorBaseDatos;
	}

	/** Autoridad usada por Spring Security, por ejemplo {@code ROLE_DUENO}. */
	public String authority() {
		return PREFIJO_AUTHORITY + name();
	}

	/** Convierte el valor almacenado en base de datos a la constante Java. */
	public static RolNombre desdeBaseDatos(String valor) {
		return Arrays.stream(values())
				.filter(r -> r.valorBaseDatos.equals(valor))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Rol no reconocido en base de datos: " + valor));
	}

	/**
	 * Interpreta texto proveniente de la API. Acepta tanto la constante
	 * (DUENO) como el valor de base de datos (DUEÑO), sin distinguir mayúsculas.
	 */
	public static Optional<RolNombre> desde(String texto) {
		if (texto == null) {
			return Optional.empty();
		}
		String limpio = texto.trim().toUpperCase(Locale.ROOT);
		return Arrays.stream(values())
				.filter(r -> r.name().equals(limpio) || r.valorBaseDatos.equals(limpio))
				.findFirst();
	}

	/** Lista legible de valores aceptados, para mensajes de error. */
	public static String valoresAceptados() {
		return Arrays.stream(values()).map(Enum::name).collect(Collectors.joining(", "));
	}
}
