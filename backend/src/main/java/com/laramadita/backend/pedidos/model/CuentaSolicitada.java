package com.laramadita.backend.pedidos.model;

import java.math.BigDecimal;

/**
 * Evento de dominio: el mesero pidió la cuenta y la atención quedó pendiente
 * de pago. Caja lo traduce a un aviso en tiempo real sin que pedidos dependa de caja.
 */
public record CuentaSolicitada(Integer atencionId, String identificacion, String mesero, BigDecimal total) {

	public static CuentaSolicitada de(Atencion atencion) {
		return new CuentaSolicitada(
				atencion.getId(),
				atencion.identificacion(),
				atencion.getMesero().getNombre(),
				atencion.totalEnviado());
	}
}
