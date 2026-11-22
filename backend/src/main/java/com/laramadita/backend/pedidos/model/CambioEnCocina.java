package com.laramadita.backend.pedidos.model;

/**
 * Evento de dominio: un pedido llegó a Cocina o cambió después de llegar
 * (RN-05 / RN-06). Lo publica {@code AtencionService} y lo escucha el módulo
 * de Cocina para avisar en tiempo real; así pedidos no depende de Cocina.
 *
 * @param detalle texto corto del cambio, p. ej. "Lomo saltado: 2 → 3"
 */
public record CambioEnCocina(Tipo tipo, Integer pedidoId, Integer atencionId, String identificacion, String zona,
		String detalle) {

	public enum Tipo {
		/** Pedido enviado por el mesero. */
		NUEVO,
		/** Cambió la cantidad u observación, o se canceló un producto. */
		MODIFICADO,
		/** Se cancelaron todos los productos del pedido. */
		CANCELADO
	}

	public static CambioEnCocina de(Tipo tipo, Pedido pedido, String detalle) {
		Atencion a = pedido.getAtencion();
		return new CambioEnCocina(tipo, pedido.getId(), a.getId(), a.identificacion(),
				a.getMesa() != null ? a.getMesa().getZona().getNombre() : null, detalle);
	}
}
