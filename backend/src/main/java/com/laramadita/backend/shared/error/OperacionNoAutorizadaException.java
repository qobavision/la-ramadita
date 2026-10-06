package com.laramadita.backend.shared.error;

/**
 * Se traduce a HTTP 403. El usuario está autenticado pero su rol no le
 * permite realizar la operación sobre el recurso indicado.
 */
public class OperacionNoAutorizadaException extends RuntimeException {

	public OperacionNoAutorizadaException(String mensaje) {
		super(mensaje);
	}
}
