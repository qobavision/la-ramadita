package com.laramadita.backend.cocina.api.dto;

import com.laramadita.backend.pedidos.model.Atencion;
import com.laramadita.backend.pedidos.model.Pedido;
import com.laramadita.backend.pedidos.model.PedidoDetalle;
import java.time.Instant;
import java.util.List;

/**
 * Comanda: un pedido enviado a Cocina con lo mínimo para prepararlo (RN-11):
 * mesa o identificación, zona, productos, cantidades y observaciones.
 *
 * @param estado      {@code PENDIENTE} (queda algo por preparar), {@code PREPARADO}
 *                    o {@code CANCELADO} (todos sus productos se cancelaron)
 * @param modificado  si algún producto cambió o se canceló después del envío (RN-06)
 * @param pendientes  unidades que faltan preparar
 * @param unidades    unidades activas de la comanda
 * @param listo       cuándo quedó preparado el último producto
 */
public record ComandaResponse(
		Integer pedidoId,
		Integer atencionId,
		String tipo,
		String identificacion,
		String zona,
		String mesero,
		Instant enviado,
		String estado,
		boolean modificado,
		int pendientes,
		int unidades,
		Instant listo,
		List<LineaComandaResponse> lineas) {

	public static ComandaResponse de(Pedido p) {
		Atencion a = p.getAtencion();
		List<PedidoDetalle> detalles = p.getDetalles();
		List<LineaComandaResponse> lineas = detalles.stream().map(LineaComandaResponse::de).toList();
		int pendientes = detalles.stream().filter(PedidoDetalle::pendienteEnCocina).mapToInt(PedidoDetalle::getCantidad).sum();
		int unidades = detalles.stream().filter(PedidoDetalle::activo).mapToInt(PedidoDetalle::getCantidad).sum();
		String estado = unidades == 0 ? "CANCELADO" : pendientes > 0 ? "PENDIENTE" : "PREPARADO";
		Instant listo = "PREPARADO".equals(estado)
				? lineas.stream().map(LineaComandaResponse::preparadoEn).filter(i -> i != null).max(Instant::compareTo).orElse(null)
				: null;
		return new ComandaResponse(
				p.getId(),
				a.getId(),
				a.getTipo().name(),
				a.identificacion(),
				a.getMesa() != null ? a.getMesa().getZona().getNombre() : null,
				LineaComandaResponse.nombre(a.getMesero()),
				LineaComandaResponse.utc(p.getFechaEnvio()),
				estado,
				lineas.stream().anyMatch(LineaComandaResponse::modificado),
				pendientes,
				unidades,
				listo,
				lineas);
	}
}
