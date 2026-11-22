package com.laramadita.backend.pedidos.model;

import java.util.EnumSet;
import java.util.Set;

/** Estados de {@code dbo.atenciones}. */
public enum EstadoAtencion {
	ABIERTA,
	PENDIENTE_PAGO,
	CERRADA;

	/** Atenciones que todavía ocupan una mesa o esperan cobro. */
	public static final Set<EstadoAtencion> ACTIVAS = EnumSet.of(ABIERTA, PENDIENTE_PAGO);
}
