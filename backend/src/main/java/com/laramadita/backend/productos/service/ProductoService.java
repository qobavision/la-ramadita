package com.laramadita.backend.productos.service;

import com.laramadita.backend.productos.api.dto.ComponenteRequest;
import com.laramadita.backend.productos.api.dto.ProductoRequest;
import com.laramadita.backend.productos.api.dto.ProductoResponse;
import com.laramadita.backend.productos.model.AmbitoCarta;
import com.laramadita.backend.productos.model.AmbitoProducto;
import com.laramadita.backend.productos.model.CategoriaProducto;
import com.laramadita.backend.productos.model.Producto;
import com.laramadita.backend.productos.model.ProductoComponente;
import com.laramadita.backend.productos.repository.ProductoRepository;
import com.laramadita.backend.shared.error.ConflictoException;
import com.laramadita.backend.shared.error.RecursoNoEncontradoException;
import com.laramadita.backend.shared.error.ReglaNegocioException;
import com.laramadita.backend.shared.texto.TextoDescriptivo;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductoService {

	private final ProductoRepository productos;
	private final CoherenciaCartaService coherenciaCarta;

	public ProductoService(ProductoRepository productos, CoherenciaCartaService coherenciaCarta) {
		this.productos = productos;
		this.coherenciaCarta = coherenciaCarta;
	}

	@Transactional(readOnly = true)
	public List<ProductoResponse> listar(String ambito, boolean soloActivos) {
		List<Producto> lista = unicosPorId(soloActivos
				? productos.findActivosConRelaciones()
				: productos.findAllConRelaciones());
		if (ambito == null || ambito.isBlank()) {
			return lista.stream().map(ProductoResponse::de).toList();
		}
		AmbitoCarta unidad = AmbitoCarta.desde(ambito)
				.orElseThrow(() -> new ReglaNegocioException("El ámbito debe ser RESTAURANTE o POLLERIA"));
		return lista.stream()
				.filter(p -> p.getAmbito().cubre(unidad))
				.map(ProductoResponse::de)
				.toList();
	}

	@Transactional(readOnly = true)
	public ProductoResponse obtener(Integer id) {
		return ProductoResponse.de(buscarConRelaciones(id));
	}

	public ProductoResponse crear(ProductoRequest request) {
		String nombre = exigirNombre(request.nombre());
		exigirNombreDisponible(nombre, null);
		CategoriaProducto categoria = exigirCategoria(request.categoria());
		Producto producto = new Producto(nombre, categoria, exigirAmbito(request.ambito()), request.precio());
		producto.reemplazarComponentes(armarComponentes(request.componentes(), null, categoria));
		return ProductoResponse.de(productos.save(producto));
	}

	public ProductoResponse actualizar(Integer id, ProductoRequest request) {
		Producto producto = buscarConRelaciones(id);
		String nombre = exigirNombre(request.nombre());
		exigirNombreDisponible(nombre, id);
		CategoriaProducto categoria = exigirCategoria(request.categoria());
		producto.actualizar(nombre, categoria, exigirAmbito(request.ambito()), request.precio());
		coherenciaCarta.retirarDeSeccionesIncompatibles(id, producto.getCategoria());
		List<ProductoComponente> piezas = armarComponentes(request.componentes(), id, categoria);
		Set<Integer> pedidos = new LinkedHashSet<>();
		for (ProductoComponente pieza : piezas) {
			pedidos.add(pieza.getComponente().getId());
		}
		producto.retirarComponentesAusentes(pedidos);
		productos.flush();
		producto.actualizarOAgregarComponentes(piezas);
		return ProductoResponse.de(producto);
	}

	public ProductoResponse cambiarActivo(Integer id, boolean activo) {
		Producto producto = buscar(id);
		if (activo) {
			producto.activar();
		} else {
			producto.desactivar();
		}
		return ProductoResponse.de(producto);
	}

	public Producto buscar(Integer id) {
		return productos.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el producto"));
	}

	public Producto buscarActivo(Integer id) {
		Producto producto = buscar(id);
		if (!producto.isActivo()) {
			throw new ReglaNegocioException("El producto " + producto.getNombre() + " está inactivo");
		}
		return producto;
	}

	public Producto buscarConRelaciones(Integer id) {
		return productos.findWithRelacionesById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el producto"));
	}

	public void exigirNombreDisponible(String nombre, Integer idActual) {
		productos.findByNombreIgnoreCase(nombre).ifPresent(existente -> {
			if (idActual != null && existente.getId().equals(idActual)) {
				return;
			}
			throw new ConflictoException("Ya existe un producto con el nombre " + nombre);
		});
	}

	public static String exigirNombre(String nombre) {
		String normalizado = TextoDescriptivo.normalizar(nombre);
		if (normalizado.isBlank()) {
			throw new ReglaNegocioException("Ingresa el nombre del producto");
		}
		return normalizado;
	}

	public static CategoriaProducto exigirCategoria(String texto) {
		return CategoriaProducto.desde(texto)
				.map(CategoriaProducto::paraPersistir)
				.orElseThrow(() -> new ReglaNegocioException("La categoría del producto no es válida"));
	}

	public static CategoriaProducto exigirCategoriaReceta(String texto) {
		CategoriaProducto categoria = exigirCategoria(texto);
		if (!categoria.esDeReceta()) {
			throw new ReglaNegocioException(
					"La receta debe ser entrada, segundo, plato ejecutivo, plato marino, plato chifa, plato a la brasa, bebida, ensalada o extra");
		}
		return categoria;
	}

	public static AmbitoProducto exigirAmbito(String texto) {
		return AmbitoProducto.desde(texto)
				.orElseThrow(() -> new ReglaNegocioException("El ámbito debe ser RESTAURANTE, POLLERIA o AMBOS"));
	}

	public static BigDecimal exigirPrecio(BigDecimal precio) {
		if (precio == null || precio.compareTo(BigDecimal.ZERO) < 0) {
			throw new ReglaNegocioException("El precio no puede ser negativo");
		}
		return precio;
	}

	private static List<Producto> unicosPorId(List<Producto> lista) {
		Map<Integer, Producto> unicos = new LinkedHashMap<>();
		for (Producto producto : lista) {
			unicos.putIfAbsent(producto.getId(), producto);
		}
		return List.copyOf(unicos.values());
	}

	private List<ProductoComponente> armarComponentes(List<ComponenteRequest> pedidos, Integer productoId,
			CategoriaProducto categoria) {
		if (categoria != CategoriaProducto.COMBO) {
			return List.of();
		}
		if (pedidos == null || pedidos.isEmpty()) {
			throw new ReglaNegocioException("Un combo se arma con al menos dos recetas");
		}
		Set<Integer> vistos = new HashSet<>();
		List<ProductoComponente> piezas = new ArrayList<>();
		for (ComponenteRequest pedido : pedidos) {
			if (pedido.productoId() == null) {
				throw new ReglaNegocioException("Selecciona una receta para el combo");
			}
			if (productoId != null && pedido.productoId().equals(productoId)) {
				throw new ReglaNegocioException("Un producto no puede componerse a sí mismo");
			}
			if (!vistos.add(pedido.productoId())) {
				throw new ReglaNegocioException("No repitas la misma receta en el combo");
			}
			Producto hijo = buscarActivo(pedido.productoId());
			if (hijo.esCompuesto()) {
				throw new ReglaNegocioException(
						"No se puede anidar combos: " + hijo.getNombre() + " ya es un producto compuesto");
			}
			if (!hijo.tieneReceta()) {
				throw new ReglaNegocioException(
						"El combo solo se arma con recetas registradas: " + hijo.getNombre());
			}
			piezas.add(new ProductoComponente(hijo, pedido.cantidad()));
		}
		if (piezas.size() < 2) {
			throw new ReglaNegocioException("Un combo se arma con al menos dos recetas");
		}
		return piezas;
	}
}
