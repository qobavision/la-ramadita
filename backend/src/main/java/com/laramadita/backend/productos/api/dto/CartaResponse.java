package com.laramadita.backend.productos.api.dto;

import com.laramadita.backend.productos.model.Carta;
import com.laramadita.backend.productos.model.CartaDetalle;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record CartaResponse(
		Integer id,
		String nombre,
		String ambito,
		LocalDate fecha,
		String estado,
		boolean vigente,
		List<CartaDetalleResponse> detalles) {

	public static CartaResponse de(Carta carta) {
		List<CartaDetalleResponse> lineas = unicos(carta.getDetalles()).stream()
				.sorted(Comparator.comparing((CartaDetalle d) -> d.getSeccion().name())
						.thenComparing(d -> d.getProducto().getNombre()))
				.map(CartaDetalleResponse::de)
				.toList();
		return new CartaResponse(
				carta.getId(),
				carta.getNombre(),
				carta.getAmbito().name(),
				carta.getFecha(),
				carta.getEstado().name(),
				carta.isVigente(),
				lineas);
	}

	private static List<CartaDetalle> unicos(List<CartaDetalle> detalles) {
		Map<String, CartaDetalle> unicos = new LinkedHashMap<>();
		for (CartaDetalle detalle : detalles) {
			Integer id = detalle.getId();
			String clave = id != null
					? "id:" + id
					: detalle.getProducto().getId() + ":" + detalle.getSeccion().name();
			unicos.putIfAbsent(clave, detalle);
		}
		return List.copyOf(unicos.values());
	}
}
