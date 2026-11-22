package com.laramadita.backend.caja.api.dto;

import com.laramadita.backend.caja.model.Pago;
import com.laramadita.backend.caja.model.PagoDetalle;
import com.laramadita.backend.pedidos.model.Atencion;
import com.laramadita.backend.pedidos.model.PedidoDetalle;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cuenta de una atención para Caja (M09): consumo, pagado y saldo. Estado
 * {@code PENDIENTE} mientras el saldo sea mayor a cero y {@code PAGADO} cuando llega a cero (RN-14, RN-16).
 */
public record CuentaResponse(
		Integer atencionId,
		String tipo,
		String identificacion,
		String zona,
		String mesero,
		String unidad,
		String estado,
		Instant inicio,
		BigDecimal total,
		BigDecimal pagado,
		BigDecimal saldo,
		List<Linea> lineas,
		List<PagoResponse> pagos) {

	/** Producto de la cuenta con lo ya pagado por productos (división de cuenta). */
	public record Linea(
			Integer pedidoDetalleId,
			String producto,
			String observacion,
			int cantidad,
			BigDecimal precioUnitario,
			BigDecimal subtotal,
			int cantidadPagada,
			int cantidadPendiente) {
	}

	/** Resumen para la lista de cobros pendientes. */
	public record Resumen(
			Integer atencionId,
			String tipo,
			String identificacion,
			String zona,
			String mesero,
			Instant inicio,
			BigDecimal total,
			BigDecimal pagado,
			BigDecimal saldo) {

		public static Resumen de(Atencion a, List<Pago> pagos) {
			BigDecimal total = a.totalEnviado();
			BigDecimal pagado = sumar(pagos);
			return new Resumen(a.getId(), a.getTipo().name(), a.identificacion(), zonaDe(a),
					PagoResponse.nombreDe(a.getMesero()), PagoResponse.utc(a.getFechaInicio()), total, pagado,
					total.subtract(pagado).max(BigDecimal.ZERO));
		}
	}

	/** @param pagos pagos CONFIRMADOS de la atención */
	public static CuentaResponse de(Atencion a, List<Pago> pagos) {
		Map<Integer, Integer> pagadas = cantidadesPagadas(pagos);
		List<Linea> lineas = lineasActivas(a).stream()
				.map(d -> {
					int pagada = Math.min(d.getCantidad(), pagadas.getOrDefault(d.getId(), 0));
					return new Linea(d.getId(), d.nombre(), d.getObservacion(), d.getCantidad(),
							d.getPrecioUnitario(), d.subtotal(), pagada, d.getCantidad() - pagada);
				})
				.toList();
		BigDecimal total = a.totalEnviado();
		BigDecimal pagado = sumar(pagos);
		BigDecimal saldo = total.subtract(pagado).max(BigDecimal.ZERO);
		return new CuentaResponse(
				a.getId(),
				a.getTipo().name(),
				a.identificacion(),
				zonaDe(a),
				PagoResponse.nombreDe(a.getMesero()),
				a.getTurno().getUnidad().getNombre().etiqueta(),
				saldo.signum() == 0 ? "PAGADO" : "PENDIENTE",
				PagoResponse.utc(a.getFechaInicio()),
				total,
				pagado,
				saldo,
				lineas,
				pagos.stream().map(PagoResponse::de).toList());
	}

	/** Productos cobrables: líneas activas de los pedidos enviados a Cocina. */
	public static List<PedidoDetalle> lineasActivas(Atencion a) {
		return a.enviados().stream()
				.flatMap(p -> p.getDetalles().stream())
				.filter(PedidoDetalle::activo)
				.toList();
	}

	public static Map<Integer, Integer> cantidadesPagadas(List<Pago> pagos) {
		Map<Integer, Integer> pagadas = new HashMap<>();
		for (Pago p : pagos) {
			for (PagoDetalle d : p.getDetalles()) {
				pagadas.merge(d.getLinea().getId(), d.getCantidadPagada(), Integer::sum);
			}
		}
		return pagadas;
	}

	public static BigDecimal sumar(List<Pago> pagos) {
		return pagos.stream().map(Pago::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	private static String zonaDe(Atencion a) {
		return a.getMesa() != null ? a.getMesa().getZona().getNombre() : null;
	}
}
