package com.laramadita.backend.shared.texto;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Normalización de textos humanos (nombres, razones sociales, contactos).
 *
 * No usar en username, correo, contraseña, DNI, RUC, celular, códigos,
 * roles, IDs ni valores técnicos.
 */
public final class TextoDescriptivo {

	private TextoDescriptivo() {
	}

	public static String normalizar(String valor) {
		if (valor == null) {
			return "";
		}
		return Arrays.stream(valor.trim().split("\\s+"))
				.filter(palabra -> !palabra.isBlank())
				.map(TextoDescriptivo::capitalizar)
				.collect(Collectors.joining(" "));
	}

	private static String capitalizar(String palabra) {
		String minusculas = palabra.toLowerCase(Locale.forLanguageTag("es"));
		return minusculas.substring(0, 1).toUpperCase(Locale.forLanguageTag("es")) + minusculas.substring(1);
	}
}
