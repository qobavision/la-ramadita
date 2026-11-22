package com.laramadita.backend.cocina.service;

import com.laramadita.backend.cocina.api.dto.ComandaResponse;
import com.laramadita.backend.cocina.api.dto.HistorialCocinaResponse;
import com.laramadita.backend.cocina.api.dto.TableroCocinaResponse;
import com.laramadita.backend.cocina.repository.ComandaRepository;
import com.laramadita.backend.cocina.repository.LineaComandaRepository;
import com.laramadita.backend.pedidos.model.EstadoAtencion;
import com.laramadita.backend.pedidos.model.EstadoCocina;
import com.laramadita.backend.pedidos.model.EstadoItem;
import com.laramadita.backend.pedidos.model.EstadoPedido;
import com.laramadita.backend.pedidos.model.Pedido;
import com.laramadita.backend.pedidos.model.PedidoDetalle;
import com.laramadita.backend.pedidos.model.ProductosPreparados;
import com.laramadita.backend.shared.error.RecursoNoEncontradoException;
import com.laramadita.backend.shared.error.ReglaNegocioException;
import com.laramadita.backend.shared.tiemporeal.EventoTiempoReal;
import com.laramadita.backend.turnos.model.EstadoTurno;
import com.laramadita.backend.turnos.model.Turno;
import com.laramadita.backend.turnos.repository.TurnoRepository;
import com.laramadita.backend.usuarios.model.Usuario;
import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * M08 Cocina: monitor de comandas, marcado de productos preparados y aviso
 * al mesero (RN-11, RN-12, RN-13). Las reglas se validan aquí y en el modelo.
 */
@Service
@Transactional
public class CocinaService {

	/** Comandas terminadas que se siguen mostrando en el monitor. */
	static final int PREPARADOS_VISIBLES = 15;

	/** CERRADO: el pedido se cobró (M09) pero puede seguir teniendo productos por preparar. */
	private static final List<EstadoPedido> EN_COCINA = List.of(EstadoPedido.ENVIADO_COCINA, EstadoPedido.CERRADO,
			EstadoPedido.CANCELADO);
	private static final List<EstadoTurno> TURNO_EN_CURSO = List.of(EstadoTurno.ABIERTO, EstadoTurno.EN_CIERRE);

	private final ComandaRepository comandas;
	private final LineaComandaRepository lineas;
	private final TurnoRepository turnos;
	private final AccesoCocina acceso;
	private final ApplicationEventPublisher eventos;

	public CocinaService(
			ComandaRepository comandas,
			LineaComandaRepository lineas,
			TurnoRepository turnos,
			AccesoCocina acceso,
			ApplicationEventPublisher eventos) {
		this.comandas = comandas;
		this.lineas = lineas;
		this.turnos = turnos;
		this.acceso = acceso;
		this.eventos = eventos;
	}

	/**
	 * Comandas de atenciones en curso. Pendientes: la más antigua primero (orden
	 * de llegada). Preparadas o canceladas: las más recientes, como referencia
	 * mientras el mesero las recoge.
	 */
	@Transactional(readOnly = true)
	public TableroCocinaResponse tablero(String username) {
		acceso.exigirConsultar(username);
		List<ComandaResponse> todas = comandas
				.enMonitor(EN_COCINA, EstadoAtencion.ACTIVAS, EstadoItem.ACTIVO, EstadoCocina.PENDIENTE).stream()
				.map(ComandaResponse::de)
				.toList();
		Map<Boolean, List<ComandaResponse>> partes = todas.stream()
				.collect(Collectors.partitioningBy(c -> "PENDIENTE".equals(c.estado())));
		List<ComandaResponse> terminadas = partes.get(false).stream()
				.sorted(Comparator.comparing(CocinaService::ultimaNovedad, Comparator.nullsLast(Comparator.reverseOrder())))
				.limit(PREPARADOS_VISIBLES)
				.toList();
		return new TableroCocinaResponse(partes.get(true), terminadas);
	}

	@Transactional(readOnly = true)
	public HistorialCocinaResponse historial(String username) {
		acceso.exigirConsultar(username);
		Optional<Turno> turno = turnos.findFirstByEstadoInOrderByFechaAperturaDesc(TURNO_EN_CURSO)
				.or(() -> turnos.findFirstByEstadoOrderByFechaCierreDesc(EstadoTurno.CERRADO));
		if (turno.isEmpty()) {
			return new HistorialCocinaResponse(null, List.of());
		}
		Turno t = turno.get();
		String rotulo = t.getUnidad().getNombre().etiqueta()
				+ (TURNO_EN_CURSO.contains(t.getEstado()) ? " · turno en curso" : " · último turno cerrado");
		List<ComandaResponse> lista = comandas.findByAtencion_Turno_IdAndFechaEnvioIsNotNullOrderByFechaEnvioDesc(t.getId())
				.stream()
				.map(ComandaResponse::de)
				.toList();
		return new HistorialCocinaResponse(rotulo, lista);
	}

	/** RN-12 / RN-13: un producto queda Preparado y se avisa al mesero de la atención. */
	public ComandaResponse prepararLinea(Integer detalleId, String username) {
		Usuario cocinero = acceso.exigirPreparar(username);
		// Primero se bloquea la atención y recién después se lee la línea: si el
		// mesero la estaba cambiando o cancelando, aquí se ve su versión final.
		bloquearAtencion(lineas.atencionDeLinea(detalleId));
		PedidoDetalle linea = lineas.findLineaById(detalleId)
				.orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el producto en Cocina"));
		Pedido pedido = buscar(linea.getPedido().getId());
		linea.marcarPreparado(cocinero);
		avisarPreparado(pedido, List.of(linea), cocinero);
		return ComandaResponse.de(pedido);
	}

	/** Marca de una vez todos los productos pendientes de la comanda. */
	public ComandaResponse prepararComanda(Integer pedidoId, String username) {
		Usuario cocinero = acceso.exigirPreparar(username);
		bloquearAtencion(comandas.atencionDeComanda(pedidoId));
		Pedido pedido = buscar(pedidoId);
		List<PedidoDetalle> pendientes = pedido.getDetalles().stream().filter(PedidoDetalle::pendienteEnCocina).toList();
		if (pendientes.isEmpty()) {
			throw new ReglaNegocioException("La comanda no tiene productos pendientes");
		}
		pendientes.forEach(l -> l.marcarPreparado(cocinero));
		avisarPreparado(pedido, pendientes, cocinero);
		return ComandaResponse.de(pedido);
	}

	/** Mismo orden de bloqueo que el mesero y Caja: la atención primero (evita "lost update" y bloqueos cruzados). */
	private void bloquearAtencion(Optional<Integer> atencionId) {
		comandas.bloquearAtencion(atencionId
				.orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el producto en Cocina")));
	}

	private Pedido buscar(Integer pedidoId) {
		return comandas.findComandaById(pedidoId)
				.orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la comanda"));
	}

	/**
	 * Descuento de insumos (Inventario escucha {@link ProductosPreparados} en esta
	 * misma transacción), aviso al mesero (RN-13) y refresco de las demás pantallas de Cocina.
	 */
	private void avisarPreparado(Pedido pedido, List<PedidoDetalle> preparadas, Usuario cocinero) {
		eventos.publishEvent(new ProductosPreparados(preparadas.stream().map(PedidoDetalle::getId).toList(),
				cocinero.getId()));

		String productos = preparadas.stream()
				.map(l -> l.getCantidad() + " × " + l.nombre())
				.collect(Collectors.joining(", "));
		boolean completo = pedido.getDetalles().stream().noneMatch(PedidoDetalle::pendienteEnCocina);

		Map<String, Object> datos = new LinkedHashMap<>();
		datos.put("atencionId", pedido.getAtencion().getId());
		datos.put("pedidoId", pedido.getId());
		datos.put("identificacion", pedido.getAtencion().identificacion());
		datos.put("producto", productos);
		datos.put("cantidad", preparadas.stream().mapToInt(PedidoDetalle::getCantidad).sum());
		datos.put("pedidoCompleto", completo);

		eventos.publishEvent(EventoTiempoReal.paraUsuario(
				pedido.getAtencion().getMesero().getUsername(), "PRODUCTO_PREPARADO", datos));
		eventos.publishEvent(EventoTiempoReal.paraCanal(AccesoCocina.CANAL, "COCINA_ACTUALIZADA", datos));
	}

	private static Instant ultimaNovedad(ComandaResponse c) {
		return c.listo() != null ? c.listo() : c.enviado();
	}
}
