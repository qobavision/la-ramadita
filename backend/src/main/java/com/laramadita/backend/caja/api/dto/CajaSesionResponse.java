package com.laramadita.backend.caja.api.dto;

import com.laramadita.backend.caja.model.CajaMovimiento;
import com.laramadita.backend.caja.model.CajaSesion;
import com.laramadita.backend.caja.model.EstadoCaja;
import com.laramadita.backend.caja.model.Pago;
import com.laramadita.backend.caja.model.TipoMovimientoCaja;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Caja de un cajero: totales por método, movimientos, efectivo esperado y arqueo (M10). */
public record CajaSesionResponse(
		Integer id,
		String estado,
		String responsable,
		String unidad,
		Integer turnoId,
		Instant apertura,
		Instant cierre,
		BigDecimal montoInicial,
		BigDecimal totalCobrado,
		List<TotalMetodo> porMetodo,
		BigDecimal ingresos,
		BigDecimal egresos,
		BigDecimal efectivoEsperado,
		BigDecimal efectivoContado,
		BigDecimal diferencia,
		String observacionCierre,
		List<Movimiento> movimientos,
		List<PagoResponse> pagos,
		List<PagoResponse> anulados) {

	public record TotalMetodo(String metodo, BigDecimal total) {
	}

	public record Movimiento(Integer id, String tipo, BigDecimal monto, String motivo, String usuario, Instant fecha) {

		static Movimiento de(CajaMovimiento m) {
			return new Movimiento(m.getId(), m.getTipo().name(), m.getMonto(), m.getMotivo(),
					PagoResponse.nombreDe(m.getUsuario()), PagoResponse.utc(m.getFecha()));
		}
	}

	/** @param pagos pagos CONFIRMADOS de esta caja */
	public static CajaSesionResponse de(CajaSesion s, List<Pago> pagos) {
		return de(s, pagos, List.of());
	}

	/** @param pagos pagos CONFIRMADOS de esta caja; @param anulados pagos ANULADOS (evidencia) */
	public static CajaSesionResponse de(CajaSesion s, List<Pago> pagos, List<Pago> anulados) {
		Map<String, BigDecimal> porMetodo = new LinkedHashMap<>();
		for (Pago p : pagos) {
			porMetodo.merge(p.getMetodo().getNombre(), p.getMonto(), BigDecimal::add);
		}
		boolean cerrada = s.getEstado() == EstadoCaja.CERRADA;
		BigDecimal esperado = cerrada ? s.getEfectivoEsperado() : efectivoEsperado(s, pagos);
		BigDecimal diferencia = cerrada && s.getEfectivoReal() != null ? s.getEfectivoReal().subtract(esperado) : null;
		return new CajaSesionResponse(
				s.getId(),
				s.getEstado().name(),
				PagoResponse.nombreDe(s.getUsuarioApertura()),
				s.getTurno().getUnidad().getNombre().etiqueta(),
				s.getTurno().getId(),
				PagoResponse.utc(s.getFechaApertura()),
				PagoResponse.utc(s.getFechaCierre()),
				s.getMontoInicial(),
				pagos.stream().map(Pago::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add),
				porMetodo.entrySet().stream().map(e -> new TotalMetodo(e.getKey(), e.getValue())).toList(),
				s.totalMovimientos(TipoMovimientoCaja.INGRESO),
				s.totalMovimientos(TipoMovimientoCaja.EGRESO),
				esperado,
				s.getEfectivoReal(),
				diferencia,
				s.getObservacionCierre(),
				s.getMovimientos().stream().map(Movimiento::de).toList(),
				pagos.stream().map(PagoResponse::de).toList(),
				anulados.stream().map(PagoResponse::de).toList());
	}

	/** Monto inicial + pagos en efectivo + ingresos − egresos. */
	public static BigDecimal efectivoEsperado(CajaSesion s, List<Pago> pagos) {
		BigDecimal efectivo = pagos.stream()
				.filter(p -> p.getMetodo().esEfectivo())
				.map(Pago::getMonto)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		return s.getMontoInicial()
				.add(efectivo)
				.add(s.totalMovimientos(TipoMovimientoCaja.INGRESO))
				.subtract(s.totalMovimientos(TipoMovimientoCaja.EGRESO));
	}
}
