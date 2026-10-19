package com.laramadita.backend.productos.service;

import com.laramadita.backend.productos.model.CartaDetalle;
import com.laramadita.backend.productos.model.CategoriaProducto;
import com.laramadita.backend.productos.repository.CartaDetalleRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CoherenciaCartaService {

	private final CartaDetalleRepository detalles;

	public CoherenciaCartaService(CartaDetalleRepository detalles) {
		this.detalles = detalles;
	}

	public void retirarDeSeccionesIncompatibles(Integer productoId, CategoriaProducto categoria) {
		List<CartaDetalle> fuera = detalles.findByProducto_Id(productoId).stream()
				.filter(detalle -> !detalle.getSeccion().acepta(categoria))
				.toList();
		if (!fuera.isEmpty()) {
			detalles.deleteAll(fuera);
		}
	}
}
