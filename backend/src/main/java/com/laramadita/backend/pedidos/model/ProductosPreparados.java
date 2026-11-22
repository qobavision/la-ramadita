package com.laramadita.backend.pedidos.model;

import java.util.List;

/**
 * Evento de dominio: Cocina marcó productos como preparados (RN-12). Se publica
 * dentro de la misma transacción para que Inventario descuente la receta de
 * cada línea; así Cocina no depende de Inventario.
 *
 * @param detalleIds líneas de pedido que quedaron preparadas
 * @param usuarioId  usuario de Cocina que las preparó
 */
public record ProductosPreparados(List<Integer> detalleIds, Integer usuarioId) {

	public ProductosPreparados {
		detalleIds = List.copyOf(detalleIds);
	}
}
