package com.laramadita.backend.shared.texto;

import com.laramadita.backend.shared.error.ReglaNegocioException;

/** Validaciones básicas de documento y celular peruanos (formato, no SUNAT). */
public final class DocumentoPeru {

	public static final int LONGITUD_RUC = 11;
	public static final int LONGITUD_DNI = 8;
	public static final int LONGITUD_CELULAR = 9;

	private DocumentoPeru() {
	}

	public static String soloDigitos(String valor) {
		if (valor == null) {
			return "";
		}
		return valor.replaceAll("\\D", "");
	}

	public static String exigirDocumento(String tipo, String numero) {
		String tipoLimpio = tipo == null ? "" : tipo.trim().toUpperCase();
		String digitos = soloDigitos(numero);
		if ("RUC".equals(tipoLimpio)) {
			if (digitos.length() != LONGITUD_RUC) {
				throw new ReglaNegocioException("El RUC debe tener exactamente 11 dígitos");
			}
			return digitos;
		}
		if ("DNI".equals(tipoLimpio)) {
			if (digitos.length() != LONGITUD_DNI) {
				throw new ReglaNegocioException("El DNI debe tener exactamente 8 dígitos");
			}
			return digitos;
		}
		throw new ReglaNegocioException("El tipo de documento debe ser RUC o DNI");
	}

	public static String celularOpcional(String valor) {
		if (valor == null || valor.isBlank()) {
			return null;
		}
		String digitos = soloDigitos(valor);
		if (digitos.length() != LONGITUD_CELULAR) {
			throw new ReglaNegocioException("El celular debe tener exactamente 9 dígitos");
		}
		return digitos;
	}
}
