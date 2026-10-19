package com.laramadita.backend.productos.service;

import com.laramadita.backend.insumos.model.Insumo;
import com.laramadita.backend.insumos.service.InsumoService;
import com.laramadita.backend.productos.api.dto.RecetaInsumoRequest;
import com.laramadita.backend.productos.api.dto.RecetaRequest;
import com.laramadita.backend.productos.api.dto.RecetaResponse;
import com.laramadita.backend.productos.model.Producto;
import com.laramadita.backend.productos.model.RecetaDetalle;
import com.laramadita.backend.productos.repository.ProductoRepository;
import com.laramadita.backend.shared.error.RecursoNoEncontradoException;
import com.laramadita.backend.shared.error.ReglaNegocioException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RecetaService {

	private final ProductoRepository productos;
	private final ProductoService productoService;
	private final InsumoService insumos;
	private final CoherenciaCartaService coherenciaCarta;

	public RecetaService(ProductoRepository productos, ProductoService productoService, InsumoService insumos,
			CoherenciaCartaService coherenciaCarta) {
		this.productos = productos;
		this.productoService = productoService;
		this.insumos = insumos;
		this.coherenciaCarta = coherenciaCarta;
	}

	@Transactional(readOnly = true)
	public List<RecetaResponse> listar() {
		return productos.findConReceta().stream().map(RecetaResponse::de).toList();
	}

	@Transactional(readOnly = true)
	public RecetaResponse obtener(Integer productoId) {
		Producto producto = productos.findConRecetaById(productoId)
				.orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la receta"));
		if (!producto.tieneReceta()) {
			throw new RecursoNoEncontradoException("No se encontró la receta");
		}
		return RecetaResponse.de(producto);
	}

	public RecetaResponse crear(RecetaRequest request) {
		String nombre = ProductoService.exigirNombre(request.nombre());
		productoService.exigirNombreDisponible(nombre, null);
		Producto producto = new Producto(nombre, ProductoService.exigirCategoriaReceta(request.categoria()),
				ProductoService.exigirAmbito(request.ambito()), request.precio());
		producto.reemplazarReceta(armarLineas(request.insumos()));
		return RecetaResponse.de(productos.save(producto));
	}

	public RecetaResponse actualizar(Integer productoId, RecetaRequest request) {
		Producto producto = productos.findConRecetaById(productoId)
				.orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la receta"));
		String nombre = ProductoService.exigirNombre(request.nombre());
		productoService.exigirNombreDisponible(nombre, productoId);
		producto.actualizar(nombre, ProductoService.exigirCategoriaReceta(request.categoria()),
				ProductoService.exigirAmbito(request.ambito()), request.precio());
		coherenciaCarta.retirarDeSeccionesIncompatibles(productoId, producto.getCategoria());
		List<RecetaDetalle> lineas = armarLineas(request.insumos());
		Set<Integer> pedidos = new LinkedHashSet<>();
		for (RecetaDetalle linea : lineas) {
			pedidos.add(linea.getInsumo().getId());
		}
		producto.retirarInsumosAusentes(pedidos);
		productos.flush();
		producto.actualizarOAgregarReceta(lineas);
		return RecetaResponse.de(producto);
	}

	public void eliminar(Integer productoId) {
		Producto producto = productos.findConRecetaById(productoId)
				.orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la receta"));
		if (!producto.tieneReceta()) {
			throw new RecursoNoEncontradoException("No se encontró la receta");
		}
		producto.reemplazarReceta(List.of());
	}

	private List<RecetaDetalle> armarLineas(List<RecetaInsumoRequest> pedidos) {
		if (pedidos == null || pedidos.isEmpty()) {
			throw new ReglaNegocioException("Agrega al menos un insumo");
		}
		Set<Integer> vistos = new HashSet<>();
		List<RecetaDetalle> lineas = new ArrayList<>();
		for (RecetaInsumoRequest pedido : pedidos) {
			if (pedido.insumoId() == null) {
				throw new ReglaNegocioException("Selecciona un insumo registrado");
			}
			if (!vistos.add(pedido.insumoId())) {
				throw new ReglaNegocioException("No repitas el mismo insumo en la receta");
			}
			Insumo insumo = insumos.buscarActivo(pedido.insumoId());
			lineas.add(new RecetaDetalle(insumo, pedido.cantidad()));
		}
		return lineas;
	}
}
