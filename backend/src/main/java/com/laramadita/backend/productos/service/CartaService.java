package com.laramadita.backend.productos.service;

import com.laramadita.backend.productos.api.dto.CartaDetalleRequest;
import com.laramadita.backend.productos.api.dto.CartaRequest;
import com.laramadita.backend.productos.api.dto.CartaResponse;
import com.laramadita.backend.productos.model.AmbitoCarta;
import com.laramadita.backend.productos.model.Carta;
import com.laramadita.backend.productos.model.CartaDetalle;
import com.laramadita.backend.productos.model.EstadoProductoCarta;
import com.laramadita.backend.productos.model.Producto;
import com.laramadita.backend.productos.model.SeccionCarta;
import com.laramadita.backend.productos.repository.CartaRepository;
import com.laramadita.backend.shared.error.ConflictoException;
import com.laramadita.backend.shared.error.RecursoNoEncontradoException;
import com.laramadita.backend.shared.error.ReglaNegocioException;
import com.laramadita.backend.shared.texto.TextoDescriptivo;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CartaService {

	private final CartaRepository cartas;
	private final ProductoService productos;

	public CartaService(CartaRepository cartas, ProductoService productos) {
		this.cartas = cartas;
		this.productos = productos;
	}

	public List<CartaResponse> listar(String ambito) {
		AmbitoCarta unidad = exigirAmbito(ambito);
		List<Carta> lista = unicosPorId(cartas.findByAmbitoOrderByNombreAsc(unidad));
		lista.forEach(Carta::retirarDetallesIncompatibles);
		cartas.flush();
		return lista.stream().map(CartaResponse::de).toList();
	}

	public CartaResponse obtener(Integer id) {
		Carta carta = buscarConDetalles(id);
		carta.retirarDetallesIncompatibles();
		cartas.flush();
		return CartaResponse.de(carta);
	}

	public CartaResponse vigente(String ambito) {
		AmbitoCarta unidad = exigirAmbito(ambito);
		Carta carta = cartas.findByAmbitoAndVigenteTrue(unidad)
				.orElseThrow(() -> new RecursoNoEncontradoException(
						"No hay una carta vigente para " + unidad.name()));
		carta.retirarDetallesIncompatibles();
		cartas.flush();
		return CartaResponse.de(carta);
	}

	public CartaResponse crear(CartaRequest request) {
		String nombre = exigirNombre(request.nombre());
		AmbitoCarta ambito = exigirAmbito(request.ambito());
		exigirNombreDisponible(ambito, nombre, null);
		Carta carta = new Carta(nombre, ambito);
		carta.reemplazarDetalles(armarDetalles(ambito, request.detalles()));
		if (!cartas.existsByAmbitoAndVigenteTrue(ambito)) {
			carta.marcarVigente();
		}
		return CartaResponse.de(cartas.save(carta));
	}

	public CartaResponse actualizar(Integer id, CartaRequest request) {
		Carta carta = buscarConDetalles(id);
		String nombre = exigirNombre(request.nombre());
		if (request.ambito() != null && !request.ambito().isBlank()) {
			AmbitoCarta ambito = exigirAmbito(request.ambito());
			if (ambito != carta.getAmbito()) {
				throw new ReglaNegocioException("No se puede cambiar el ámbito de una carta");
			}
		}
		exigirNombreDisponible(carta.getAmbito(), nombre, id);
		carta.renombrar(nombre);
		List<CartaDetalle> lineas = armarDetalles(carta.getAmbito(), request.detalles());
		Set<String> pedidos = new LinkedHashSet<>();
		for (CartaDetalle linea : lineas) {
			pedidos.add(Carta.claveDe(linea));
		}
		carta.retirarDetallesAusentes(pedidos);
		cartas.flush();
		carta.actualizarOAgregarDetalles(lineas);
		return CartaResponse.de(carta);
	}

	public CartaResponse marcarVigente(Integer id) {
		Carta carta = buscarConDetalles(id);
		cartas.quitarVigenteDe(carta.getAmbito());
		cartas.flush();
		carta.marcarVigente();
		return CartaResponse.de(carta);
	}

	public CartaResponse quitarDetalle(Integer cartaId, Integer detalleId) {
		Carta carta = buscarConDetalles(cartaId);
		CartaDetalle detalle = carta.getDetalles().stream()
				.filter(d -> Objects.equals(d.getId(), detalleId))
				.findFirst()
				.orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el producto en esta carta"));
		carta.quitarDetalle(detalle);
		return CartaResponse.de(carta);
	}

	public CartaResponse cambiarEstadoDetalle(Integer cartaId, Integer detalleId, String estadoTexto) {
		Carta carta = buscarConDetalles(cartaId);
		EstadoProductoCarta estado = EstadoProductoCarta.desde(estadoTexto)
				.orElseThrow(() -> new ReglaNegocioException("El estado debe ser DISPONIBLE o AGOTADO"));
		CartaDetalle detalle = carta.getDetalles().stream()
				.filter(d -> Objects.equals(d.getId(), detalleId))
				.findFirst()
				.orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el producto en esta carta"));
		detalle.cambiarEstado(estado);
		return CartaResponse.de(carta);
	}

	private Carta buscarConDetalles(Integer id) {
		return cartas.findWithDetallesById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la carta"));
	}

	private List<CartaDetalle> armarDetalles(AmbitoCarta ambito, List<CartaDetalleRequest> pedidos) {
		if (pedidos == null || pedidos.isEmpty()) {
			return List.of();
		}
		Map<String, CartaDetalle> unicos = new LinkedHashMap<>();
		for (CartaDetalleRequest pedido : pedidos) {
			SeccionCarta seccion = SeccionCarta.desde(pedido.seccion())
					.orElseThrow(() -> new ReglaNegocioException("La sección de la carta no es válida"));
			if (!seccion.perteneceA(ambito)) {
				throw new ReglaNegocioException("La sección " + seccion.name() + " no corresponde a " + ambito.name());
			}
			if (pedido.productoId() == null) {
				throw new ReglaNegocioException("Selecciona un producto");
			}
			Producto producto = productos.buscarActivo(pedido.productoId());
			if (!seccion.acepta(producto.getCategoria())) {
				continue;
			}
			if (!producto.getAmbito().cubre(ambito)) {
				throw new ReglaNegocioException(
						"El producto " + producto.getNombre() + " no pertenece a " + ambito.name());
			}
			EstadoProductoCarta estado = pedido.estado() == null || pedido.estado().isBlank()
					? EstadoProductoCarta.DISPONIBLE
					: EstadoProductoCarta.desde(pedido.estado())
							.orElseThrow(() -> new ReglaNegocioException("El estado debe ser DISPONIBLE o AGOTADO"));
			unicos.put(pedido.productoId() + ":" + seccion.name(),
					new CartaDetalle(producto, seccion, pedido.precioDia(), estado));
		}
		return List.copyOf(unicos.values());
	}

	private void exigirNombreDisponible(AmbitoCarta ambito, String nombre, Integer idActual) {
		boolean existe = idActual == null
				? cartas.existsByAmbitoAndNombreIgnoreCase(ambito, nombre)
				: cartas.existsByAmbitoAndNombreIgnoreCaseAndIdNot(ambito, nombre, idActual);
		if (existe) {
			throw new ConflictoException("Ya existe una carta con el nombre " + nombre);
		}
	}

	private static String exigirNombre(String nombre) {
		String normalizado = TextoDescriptivo.normalizar(nombre);
		if (normalizado.isBlank()) {
			throw new ReglaNegocioException("Ingresa el nombre de la carta");
		}
		return normalizado;
	}

	private static AmbitoCarta exigirAmbito(String texto) {
		return AmbitoCarta.desde(texto)
				.orElseThrow(() -> new ReglaNegocioException("El ámbito debe ser RESTAURANTE o POLLERIA"));
	}

	private static List<Carta> unicosPorId(List<Carta> lista) {
		Map<Integer, Carta> unicos = new LinkedHashMap<>();
		for (Carta carta : lista) {
			unicos.putIfAbsent(carta.getId(), carta);
		}
		return List.copyOf(unicos.values());
	}
}
