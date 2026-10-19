package com.laramadita.backend.compras.service;

import com.laramadita.backend.compras.api.dto.CompraDetalleRequest;
import com.laramadita.backend.compras.api.dto.CompraRequest;
import com.laramadita.backend.compras.api.dto.CompraResponse;
import com.laramadita.backend.compras.api.dto.RecepcionDetalleRequest;
import com.laramadita.backend.compras.api.dto.RecepcionRequest;
import com.laramadita.backend.compras.model.Compra;
import com.laramadita.backend.compras.model.CompraDetalle;
import com.laramadita.backend.compras.model.EstadoCompra;
import com.laramadita.backend.compras.model.Recepcion;
import com.laramadita.backend.compras.model.RecepcionDetalle;
import com.laramadita.backend.compras.repository.CompraRepository;
import com.laramadita.backend.compras.repository.RecepcionRepository;
import com.laramadita.backend.insumos.model.Insumo;
import com.laramadita.backend.insumos.service.InsumoService;
import com.laramadita.backend.inventario.model.InventarioMovimiento;
import com.laramadita.backend.inventario.repository.InventarioMovimientoRepository;
import com.laramadita.backend.proveedores.model.Proveedor;
import com.laramadita.backend.proveedores.service.ProveedorService;
import com.laramadita.backend.shared.error.RecursoNoEncontradoException;
import com.laramadita.backend.shared.error.ReglaNegocioException;
import com.laramadita.backend.usuarios.model.Usuario;
import com.laramadita.backend.usuarios.repository.UsuarioRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CompraService {

	private final CompraRepository compras;
	private final RecepcionRepository recepciones;
	private final InventarioMovimientoRepository movimientos;
	private final ProveedorService proveedores;
	private final InsumoService insumos;
	private final UsuarioRepository usuarios;

	public CompraService(CompraRepository compras, RecepcionRepository recepciones,
			InventarioMovimientoRepository movimientos, ProveedorService proveedores, InsumoService insumos,
			UsuarioRepository usuarios) {
		this.compras = compras;
		this.recepciones = recepciones;
		this.movimientos = movimientos;
		this.proveedores = proveedores;
		this.insumos = insumos;
		this.usuarios = usuarios;
	}

	@Transactional(readOnly = true)
	public List<CompraResponse> listar() {
		return compras.findByEstadoNotOrderByFechaCompraDesc(EstadoCompra.CANCELADA).stream()
				.map(CompraResponse::de)
				.toList();
	}

	@Transactional(readOnly = true)
	public CompraResponse obtener(Integer id) {
		return CompraResponse.de(buscarCompleta(id));
	}

	@Transactional(readOnly = true)
	public long contarPorRecibir() {
		return compras.countByEstadoIn(List.of(EstadoCompra.PENDIENTE, EstadoCompra.PARCIAL));
	}

	public CompraResponse crear(CompraRequest request, String username) {
		Usuario usuario = usuarioDe(username);
		Proveedor proveedor = proveedores.exigirActivo(request.proveedorId());
		Compra compra = new Compra(siguienteCodigo(), proveedor, usuario);
		armarDetalles(request.detalles()).forEach(compra::agregarDetalle);
		return CompraResponse.de(compras.save(compra));
	}

	public CompraResponse actualizar(Integer id, CompraRequest request) {
		Compra compra = buscarCompleta(id);
		compra.exigirEditable();
		Proveedor proveedor = proveedorParaEdicion(compra, request.proveedorId());
		compra.reemplazarProveedor(proveedor);
		compra.reemplazarDetalles(armarDetalles(request.detalles()));
		return CompraResponse.de(compra);
	}

	public CompraResponse cancelar(Integer id) {
		Compra compra = buscarCompleta(id);
		compra.cancelar();
		return CompraResponse.de(compra);
	}

	public CompraResponse recibir(Integer id, RecepcionRequest request, String username) {
		Usuario usuario = usuarioDe(username);
		Compra compra = compras.bloquearPorId(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la compra"));
		compra.exigirRecepcionable();

		List<RecepcionDetalleRequest> lineas = request.detalles().stream()
				.filter(d -> d.cantidad() != null && d.cantidad().compareTo(BigDecimal.ZERO) > 0)
				.toList();
		if (lineas.isEmpty()) {
			throw new ReglaNegocioException("Ingresa al menos una cantidad recibida");
		}

		Recepcion recepcion = new Recepcion(compra, usuario);
		for (RecepcionDetalleRequest linea : lineas) {
			CompraDetalle detalle = compra.getDetalles().stream()
					.filter(d -> d.getId().equals(linea.compraDetalleId()))
					.findFirst()
					.orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el insumo de la compra"));
			BigDecimal cantidad = detalle.recibir(linea.cantidad());
			Insumo insumo = insumos.bloquear(detalle.getInsumo().getId());
			BigDecimal stockAnterior = insumo.incrementarStock(cantidad);
			recepcion.agregarDetalle(new RecepcionDetalle(detalle, cantidad));
			movimientos.save(InventarioMovimiento.entradaCompra(
					insumo,
					usuario,
					detalle,
					cantidad,
					stockAnterior,
					insumo.getStockActual(),
					"Recepción " + compra.getCodigo()));
		}
		recepciones.save(recepcion);
		compra.registrarRecepcion();
		return CompraResponse.de(compra);
	}

	private List<CompraDetalle> armarDetalles(List<CompraDetalleRequest> solicitudes) {
		Set<Integer> vistos = new HashSet<>();
		List<CompraDetalle> detalles = new ArrayList<>();
		for (CompraDetalleRequest linea : solicitudes) {
			if (!vistos.add(linea.insumoId())) {
				throw new ReglaNegocioException("No repitas el mismo insumo en la compra");
			}
			Insumo insumo = insumos.buscarActivo(linea.insumoId());
			detalles.add(new CompraDetalle(insumo, linea.cantidad(), linea.precioUnitario()));
		}
		return detalles;
	}

	private Proveedor proveedorParaEdicion(Compra compra, Integer proveedorId) {
		if (compra.getProveedor().getId().equals(proveedorId)) {
			return compra.getProveedor();
		}
		return proveedores.exigirActivo(proveedorId);
	}

	private Compra buscarCompleta(Integer id) {
		return compras.buscarCompleta(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la compra"));
	}

	private Usuario usuarioDe(String username) {
		return usuarios.findByUsername(username)
				.orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el usuario autenticado"));
	}

	private String siguienteCodigo() {
		Integer numero = compras.siguienteNumeroCodigo();
		if (numero == null || numero < 1) {
			throw new IllegalStateException("No se pudo generar el código de compra");
		}
		return "COMP-%03d".formatted(numero);
	}
}
