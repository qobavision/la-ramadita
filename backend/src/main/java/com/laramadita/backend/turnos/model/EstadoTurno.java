package com.laramadita.backend.turnos.model;

public enum EstadoTurno {
	ABIERTO,
	EN_CIERRE,
	CERRADO;

	public boolean enCurso() {
		return this == ABIERTO || this == EN_CIERRE;
	}
}
