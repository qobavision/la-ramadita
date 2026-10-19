package com.laramadita.backend.zonas.model;

public enum EstadoMesa {
	DISPONIBLE,
	OCUPADA,
	PENDIENTE_PAGO;

	public boolean enOperacion() {
		return this != DISPONIBLE;
	}
}
