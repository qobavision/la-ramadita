package com.laramadita.backend.caja.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

/** Solicitudes del módulo de caja (M09 / M10). */
public final class CajaRequests {

	private CajaRequests() {
	}

	/** Apertura de caja con el efectivo inicial (sencillo). */
	public record Apertura(@NotNull @DecimalMin("0.00") BigDecimal montoInicial) {
	}

	/** Ingreso o egreso de caja que no es un pago de venta. */
	public record Movimiento(
			@NotBlank String tipo,
			@NotNull @DecimalMin("0.01") BigDecimal monto,
			@NotBlank @Size(max = 300) String motivo) {
	}

	/** Cierre con arqueo: efectivo contado y observación si hay diferencia. */
	public record Cierre(@NotNull @DecimalMin("0.00") BigDecimal efectivoContado, @Size(max = 500) String observacion) {
	}

	/**
	 * Pago de una cuenta. Por monto parcial ({@code monto}) o por productos
	 * ({@code lineas}); en ese caso el monto lo calcula el backend (RN-17).
	 */
	public record Pago(
			@NotBlank String metodo,
			BigDecimal monto,
			@Size(max = 300) String observacion,
			@Valid List<LineaPago> lineas) {
	}

	public record LineaPago(@NotNull Integer pedidoDetalleId, @NotNull @Min(1) Integer cantidad) {
	}

	/** Anulación de un pago por el ADMINISTRADOR: el motivo queda como evidencia. */
	public record Anulacion(@NotBlank @Size(max = 300) String motivo) {
	}
}
