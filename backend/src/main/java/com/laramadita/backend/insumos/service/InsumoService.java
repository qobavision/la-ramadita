package com.laramadita.backend.insumos.service;

import com.laramadita.backend.insumos.api.dto.InsumoActualizarRequest;
import com.laramadita.backend.insumos.api.dto.InsumoRequest;
import com.laramadita.backend.insumos.api.dto.InsumoResponse;
import com.laramadita.backend.insumos.model.EstadoStock;
import com.laramadita.backend.insumos.model.FiltroEstadoInsumo;
import com.laramadita.backend.insumos.model.Insumo;
import com.laramadita.backend.insumos.model.UnidadMedida;
import com.laramadita.backend.insumos.repository.InsumoRepository;
import com.laramadita.backend.shared.error.ConflictoException;
import com.laramadita.backend.shared.error.RecursoNoEncontradoException;
import com.laramadita.backend.shared.error.ReglaNegocioException;
import com.laramadita.backend.shared.texto.TextoDescriptivo;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InsumoService {

	private final InsumoRepository insumos;

	public InsumoService(InsumoRepository insumos) {
		this.insumos = insumos;
	}

	@Transactional(readOnly = true)
	public List<InsumoResponse> listar(String busqueda, String estado) {
		FiltroEstadoInsumo filtro = FiltroEstadoInsumo.desde(estado);
		String texto = busqueda == null ? "" : busqueda.trim();
		boolean conBusqueda = !texto.isBlank();
		List<Insumo> lista = switch (filtro) {
			case ACTIVOS -> conBusqueda
					? insumos.findByActivoTrueAndNombreContainingIgnoreCaseOrderByNombreAsc(texto)
					: insumos.findByActivoTrueOrderByNombreAsc();
			case INACTIVOS -> conBusqueda
					? insumos.findByActivoFalseAndNombreContainingIgnoreCaseOrderByNombreAsc(texto)
					: insumos.findByActivoFalseOrderByNombreAsc();
			case TODOS -> conBusqueda
					? insumos.findByNombreContainingIgnoreCaseOrderByNombreAsc(texto)
					: insumos.findAllByOrderByNombreAsc();
		};
		return lista.stream().map(InsumoResponse::de).toList();
	}

	@Transactional(readOnly = true)
	public InsumoResponse obtener(Integer id) {
		return InsumoResponse.de(buscarActivo(id));
	}

	@Transactional(readOnly = true)
	public long contarStockBajoOAgotado() {
		return insumos.findByActivoTrueOrderByNombreAsc().stream()
				.filter(i -> i.estadoStock() != EstadoStock.NORMAL)
				.count();
	}

	public InsumoResponse crear(InsumoRequest request) {
		String nombre = exigirNombre(request.nombre());
		UnidadMedida unidad = exigirUnidad(request.unidadMedida());
		exigirNombreDisponible(nombre, null);
		Insumo creado = insumos.save(new Insumo(nombre, unidad, request.stockInicial(), request.stockMinimo()));
		return InsumoResponse.de(creado);
	}

	public InsumoResponse actualizar(Integer id, InsumoActualizarRequest request) {
		Insumo insumo = buscarActivo(id);
		String nombre = exigirNombre(request.nombre());
		UnidadMedida unidad = exigirUnidad(request.unidadMedida());
		exigirNombreDisponible(nombre, id);
		insumo.actualizar(nombre, unidad, request.stockMinimo());
		return InsumoResponse.de(insumo);
	}

	/**
	 * Eliminación híbrida: bloquea si hay dependencia activa, desactiva si hay
	 * historial y borra físicamente solo si el insumo nunca se usó.
	 *
	 * @return vacío cuando se eliminó de la base; el insumo desactivado si quedó
	 *         como historial.
	 */
	public Optional<InsumoResponse> eliminar(Integer id) {
		Insumo insumo = buscarActivo(id);
		if (marcado(insumos.existeEnRecetaDeProductoActivo(id))) {
			throw new ConflictoException(
					"Este insumo se utiliza actualmente en una receta activa y no puede eliminarse.");
		}
		if (marcado(insumos.existeEnCompraPendiente(id))) {
			throw new ConflictoException("Este insumo forma parte de una compra pendiente y no puede eliminarse.");
		}
		if (tieneHistorial(id)) {
			insumo.desactivar();
			return Optional.of(InsumoResponse.de(insumo));
		}
		insumos.delete(insumo);
		return Optional.empty();
	}

	public InsumoResponse reactivar(Integer id) {
		Insumo insumo = insumos.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el insumo"));
		if (insumo.isActivo()) {
			throw new ConflictoException("El insumo ya está activo");
		}
		insumo.activar();
		return InsumoResponse.de(insumo);
	}

	public Insumo buscarActivo(Integer id) {
		Insumo insumo = insumos.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el insumo"));
		if (!insumo.isActivo()) {
			throw new RecursoNoEncontradoException("No se encontró el insumo");
		}
		return insumo;
	}

	public Insumo bloquear(Integer id) {
		return insumos.bloquearPorId(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el insumo"));
	}

	private void exigirNombreDisponible(String nombre, Integer idActual) {
		Optional<Insumo> existente = insumos.findByNombreIgnoreCase(nombre);
		if (existente.isEmpty() || existente.get().getId().equals(idActual)) {
			return;
		}
		if (!existente.get().isActivo()) {
			throw new ConflictoException("Ya existe un insumo desactivado con el nombre " + nombre);
		}
		throw new ConflictoException("Ya existe un insumo con el nombre " + nombre);
	}

	private boolean tieneHistorial(Integer id) {
		return marcado(insumos.existeEnAlgunaCompra(id))
				|| marcado(insumos.existeEnMovimientos(id))
				|| marcado(insumos.existeEnAlgunaReceta(id));
	}

	private static boolean marcado(int flag) {
		return flag != 0;
	}

	private static String exigirNombre(String nombre) {
		String normalizado = TextoDescriptivo.normalizar(nombre);
		if (normalizado.isBlank()) {
			throw new ReglaNegocioException("Ingresa el nombre del insumo");
		}
		return normalizado;
	}

	private static UnidadMedida exigirUnidad(String unidad) {
		return UnidadMedida.desde(unidad)
				.orElseThrow(() -> new ReglaNegocioException(
						"La unidad debe ser UNIDAD, KILOGRAMO, GRAMO, LITRO o MILILITRO"));
	}

	public static BigDecimal cantidadPositiva(BigDecimal valor, String mensaje) {
		if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
			throw new ReglaNegocioException(mensaje);
		}
		return valor;
	}
}
