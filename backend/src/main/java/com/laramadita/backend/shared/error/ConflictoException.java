package com.laramadita.backend.shared.error;

/** Se traduce a HTTP 409. Por ejemplo, un username ya registrado. */
public class ConflictoException extends RuntimeException {

	public ConflictoException(String mensaje) {
		super(mensaje);
	}
}
