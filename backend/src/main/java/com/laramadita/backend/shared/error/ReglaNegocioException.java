package com.laramadita.backend.shared.error;

/** Se traduce a HTTP 422. Una regla de negocio impide la operación. */
public class ReglaNegocioException extends RuntimeException {

	public ReglaNegocioException(String mensaje) {
		super(mensaje);
	}
}
