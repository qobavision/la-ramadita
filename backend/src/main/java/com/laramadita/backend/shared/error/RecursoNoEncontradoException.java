package com.laramadita.backend.shared.error;

/** Se traduce a HTTP 404. */
public class RecursoNoEncontradoException extends RuntimeException {

	public RecursoNoEncontradoException(String mensaje) {
		super(mensaje);
	}
}
