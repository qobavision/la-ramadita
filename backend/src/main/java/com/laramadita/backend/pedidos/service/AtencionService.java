package com.laramadita.backend.pedidos.service;

import com.laramadita.backend.pedidos.api.dto.AtencionRequest;
import com.laramadita.backend.pedidos.api.dto.AtencionResponse;
import com.laramadita.backend.pedidos.api.dto.AtencionResumenResponse;
import com.laramadita.backend.pedidos.api.dto.DetalleActualizarRequest;
import com.laramadita.backend.pedidos.api.dto.DetalleRequest;
import com.laramadita.backend.pedidos.api.dto.MesaSalonResponse;
import com.laramadita.backend.pedidos.model.Atencion;
import com.laramadita.backend.pedidos.model.CuentaReabierta;
import com.laramadita.backend.pedidos.model.CuentaSolicitada;
import com.laramadita.backend.pedidos.model.CambioEnCocina;
import com.laramadita.backend.pedidos.model.EstadoAtencion;
import com.laramadita.backend.pedidos.model.Pedido;
import com.laramadita.backend.pedidos.model.PedidoDetalle;
import com.laramadita.backend.pedidos.model.TipoAtencion;
import com.laramadita.backend.pedidos.repository.AtencionRepository;
import com.laramadita.backend.productos.api.dto.CartaResponse;
import com.laramadita.backend.productos.model.CartaDetalle;
import com.laramadita.backend.productos.model.EstadoProductoCarta;
import com.laramadita.backend.productos.repository.CartaDetalleRepository;
import com.laramadita.backend.productos.service.CartaService;
import com.laramadita.backend.shared.error.ConflictoException;
import com.laramadita.backend.shared.error.OperacionNoAutorizadaException;
import com.laramadita.backend.shared.error.RecursoNoEncontradoException;
import com.laramadita.backend.shared.error.ReglaNegocioException;
import com.laramadita.backend.turnos.model.EstadoTurno;
import com.laramadita.backend.turnos.model.Turno;
import com.laramadita.backend.turnos.service.TurnoService;
import com.laramadita.backend.usuarios.model.RolNombre;
import com.laramadita.backend.usuarios.model.Usuario;
import com.laramadita.backend.usuarios.repository.UsuarioRepository;
import com.laramadita.backend.zonas.model.Mesa;
import com.laramadita.backend.zonas.repository.MesaRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * M06 Mesas y atención + M07 Pedidos (lado del mesero).
 * Todas las reglas de negocio se validan aquí (RN-01 a RN-05).
 */
@Service
@Transactional
public class AtencionService {

	private final AtencionRepository atenciones;
	private final MesaRepository mesas;
	private final CartaDetalleRepository lineasCarta;
	private final CartaService cartas;
	private final TurnoService turnos;
	private final UsuarioRepository usuarios;
	private final ApplicationEventPublisher eventos;

	public AtencionService(
			AtencionRepository atenciones,
			MesaRepository mesas,
			CartaDetalleRepository lineasCarta,
			CartaService cartas,
			TurnoService turnos,
			UsuarioRepository usuarios,
			ApplicationEventPublisher eventos) {
		this.atenciones = atenciones;
		this.mesas = mesas;
		this.lineasCarta = lineasCarta;
		this.cartas = cartas;
		this.turnos = turnos;
		this.usuarios = usuarios;
		this.eventos = eventos;
	}

	/** Mesas activas de zonas activas, con la atención en curso de cada una. */
	@Transactional(readOnly = true)
	public List<MesaSalonResponse> salon(String username) {
		exigirConsulta(username);
		Map<Integer, AtencionResumenResponse> porMesa = atenciones
				.findByEstadoInOrderByFechaInicioAsc(EstadoAtencion.ACTIVAS).stream()
				.filter(a -> a.getMesa() != null)
				.collect(Collectors.toMap(a -> a.getMesa().getId(), AtencionResumenResponse::de, (a, b) -> a));
		return mesas.findAllByOrderByZona_NombreAscIdAsc().stream()
				.filter(m -> m.isActivo() && m.getZona().isActivo())
				.map(m -> MesaSalonResponse.de(m, porMesa.get(m.getId())))
				.toList();
	}

	/** Atenciones en curso (mesa y para llevar), abiertas o pendientes de pago. */
	@Transactional(readOnly = true)
	public List<AtencionResumenResponse> activas(String username) {
		exigirConsulta(username);
		return atenciones.findByEstadoInOrderByFechaInicioAsc(EstadoAtencion.ACTIVAS).stream()
				.map(AtencionResumenResponse::de)
				.toList();
	}

	/**
	 * Carta vigente de la unidad de negocio de la atención (la del turno en que se abrió),
	 * así una atención de Restaurante no muestra la carta de Pollería. Los agotados se
	 * muestran pero no se pueden agregar.
	 */
	public CartaResponse carta(Integer id, String username) {
		exigirMesero(username);
		return cartas.vigente(unidadDe(buscar(id)));
	}

	public AtencionResponse abrir(AtencionRequest request, String username) {
		Usuario mesero = exigirMesero(username);
		TipoAtencion tipo = TipoAtencion.desde(request.tipo())
				.orElseThrow(() -> new ReglaNegocioException("El tipo de atención debe ser MESA o PARA_LLEVAR"));
		Turno turno = turnos.exigirTurnoEnCurso();
		// Decisión del equipo: en cierre solo se atienden y cobran las mesas que quedan.
		if (turno.getEstado() == EstadoTurno.EN_CIERRE) {
			throw new ReglaNegocioException(
					"El turno está en cierre: solo se atienden y cobran las mesas que quedan; no se aceptan nuevas atenciones.");
		}
		Atencion atencion;
		if (tipo == TipoAtencion.MESA) {
			if (request.mesaId() == null) {
				throw new ReglaNegocioException("Selecciona una mesa");
			}
			Mesa mesa = mesas.findById(request.mesaId())
					.orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la mesa"));
			if (!mesa.isActivo() || !mesa.getZona().isActivo()) {
				throw new ReglaNegocioException(mesa.nombre() + " no está habilitada");
			}
			if (atenciones.existsByMesa_IdAndEstadoIn(mesa.getId(), EstadoAtencion.ACTIVAS)) {
				throw new ConflictoException(mesa.nombre() + " ya tiene una atención en curso");
			}
			atencion = Atencion.enMesa(mesa, turno, mesero);
		} else {
			atencion = Atencion.paraLlevar(turno, mesero);
		}
		Atencion guardada = atenciones.save(atencion);
		eventos.publishEvent(CanalSalon.aviso("ABIERTA", guardada));
		return AtencionResponse.de(guardada);
	}

	@Transactional(readOnly = true)
	public AtencionResponse obtener(Integer id, String username) {
		exigirMesero(username);
		return AtencionResponse.de(buscar(id));
	}

	/** RN-04 / RN-05: solo productos Disponibles de la carta vigente de la unidad del turno. */
	public AtencionResponse agregar(Integer id, DetalleRequest request, String username) {
		Usuario usuario = exigirMesero(username);
		Atencion atencion = buscar(id);
		atencion.exigirAbierta();
		CartaDetalle linea = lineasCarta.findById(request.cartaDetalleId())
				.orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el producto en la carta"));
		if (!linea.getCarta().isVigente() || !linea.getCarta().getAmbito().name().equals(unidadDe(atencion))) {
			throw new ReglaNegocioException("El producto no pertenece a la carta vigente");
		}
		if (linea.getEstado() == EstadoProductoCarta.AGOTADO) {
			throw new ReglaNegocioException(linea.getProducto().getNombre() + " está agotado");
		}
		atencion.agregar(linea.getProducto(), request.cantidad(), linea.precioEfectivo(), request.observacion(), usuario);
		return guardar(atencion);
	}

	/**
	 * RN-04 en borrador; RN-06 si la línea ya está en Cocina (se registra y se avisa a Cocina).
	 * La atención se bloquea primero (mismo orden que Cocina y Caja) para que un
	 * "Listo" de Cocina simultáneo no pise el cambio, ni al revés.
	 */
	public AtencionResponse actualizarDetalle(Integer id, Integer detalleId, DetalleActualizarRequest request,
			String username) {
		Usuario usuario = exigirMesero(username);
		Atencion atencion = buscarBloqueada(id);
		exigirNoAgotadoAlAumentar(atencion, detalleId, request.cantidad());
		Optional<CambioEnCocina> cambio = atencion.actualizarDetalle(detalleId, request.cantidad(),
				request.observacion(), usuario);
		AtencionResponse respuesta = guardar(atencion);
		cambio.ifPresent(eventos::publishEvent);
		return respuesta;
	}

	/** En borrador quita la línea; si ya está en Cocina la cancela (RN-06) y avisa a Cocina. */
	public AtencionResponse quitarDetalle(Integer id, Integer detalleId, String username) {
		Usuario usuario = exigirMesero(username);
		Atencion atencion = buscarBloqueada(id);
		Optional<CambioEnCocina> cambio = atencion.quitarDetalle(detalleId, usuario);
		AtencionResponse respuesta = guardar(atencion);
		cambio.ifPresent(eventos::publishEvent);
		return respuesta;
	}

	/** RN-05: el borrador se confirma y queda registrado para Cocina, que recibe el aviso en tiempo real (M08). */
	public AtencionResponse enviarACocina(Integer id, String username) {
		exigirMesero(username);
		Atencion atencion = buscar(id);
		exigirSinAgotados(atencion);
		Pedido pedido = atencion.enviarBorrador();
		AtencionResponse respuesta = guardar(atencion);
		int unidades = pedido.getDetalles().stream().mapToInt(PedidoDetalle::getCantidad).sum();
		eventos.publishEvent(CambioEnCocina.de(CambioEnCocina.Tipo.NUEVO, pedido,
				unidades == 1 ? "1 producto" : unidades + " productos"));
		return respuesta;
	}

	/** RN-07: el cliente pide algo más después de pedir la cuenta; vuelve a ABIERTA y sale de cobros. */
	public AtencionResponse reabrir(Integer id, String username) {
		exigirMesero(username);
		Atencion atencion = atenciones.findParaActualizar(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la atención"));
		atencion.reabrir();
		eventos.publishEvent(CanalSalon.aviso("REABIERTA", atencion));
		eventos.publishEvent(new CuentaReabierta(atencion.getId(), atencion.identificacion()));
		return guardar(atencion);
	}

	/**
	 * RN-09: un producto que se agotó mientras estaba en el borrador no se envía a Cocina.
	 * Se revisa contra la carta vigente de la unidad de la atención.
	 */
	private void exigirSinAgotados(Atencion atencion) {
		atencion.borrador().ifPresent(borrador -> {
			Set<Integer> agotados = agotadosDe(atencion);
			borrador.getDetalles().stream()
					.filter(d -> agotados.contains(d.getProducto().getId()))
					.findFirst()
					.ifPresent(d -> {
						throw new ReglaNegocioException(d.getProducto().getNombre()
								+ " se agotó: quítalo del pedido antes de enviarlo a Cocina");
					});
		});
	}

	/**
	 * RN-09: subir la cantidad equivale a pedir más de ese producto; si se agotó,
	 * no se permite (en borrador o ya enviado). Bajar la cantidad siempre se puede.
	 */
	private void exigirNoAgotadoAlAumentar(Atencion atencion, Integer detalleId, Integer nuevaCantidad) {
		atencion.getPedidos().stream()
				.flatMap(p -> p.getDetalles().stream())
				.filter(d -> detalleId.equals(d.getId()))
				.findFirst()
				.filter(d -> nuevaCantidad != null && nuevaCantidad > d.getCantidad())
				.filter(d -> agotadosDe(atencion).contains(d.getProducto().getId()))
				.ifPresent(d -> {
					throw new ReglaNegocioException(d.getProducto().getNombre()
							+ " está agotado: no se puede aumentar la cantidad");
				});
	}

	/** Productos AGOTADOS en la carta vigente de la unidad de la atención (RN-09). */
	private Set<Integer> agotadosDe(Atencion atencion) {
		return cartas.vigente(unidadDe(atencion)).detalles().stream()
				.filter(d -> EstadoProductoCarta.AGOTADO.name().equals(d.estado()))
				.map(d -> d.productoId())
				.collect(Collectors.toSet());
	}

	/** El mesero pide la cuenta: atención y mesa quedan pendientes de pago para Caja. */
	public AtencionResponse solicitarCuenta(Integer id, String username) {
		exigirMesero(username);
		Atencion atencion = buscar(id);
		atencion.solicitarCuenta();
		eventos.publishEvent(CanalSalon.aviso("CUENTA_SOLICITADA", atencion));
		eventos.publishEvent(CuentaSolicitada.de(atencion));
		AtencionResponse respuesta = guardar(atencion);
		// Cuenta de S/ 0: la atención se cerró sola; puede ser la última del turno en cierre.
		turnos.cerrarSiCorresponde(atencion.getTurno());
		return respuesta;
	}

	/** Cierra una atención sin nada enviado a Cocina y libera la mesa. */
	public AtencionResponse liberarSinConsumo(Integer id, String username) {
		exigirMesero(username);
		Atencion atencion = buscar(id);
		atencion.liberarSinConsumo();
		eventos.publishEvent(CanalSalon.aviso("LIBERADA", atencion));
		AtencionResponse respuesta = guardar(atencion);
		turnos.cerrarSiCorresponde(atencion.getTurno());
		return respuesta;
	}

	private AtencionResponse guardar(Atencion atencion) {
		atenciones.flush();
		return AtencionResponse.de(atencion);
	}

	private Atencion buscar(Integer id) {
		return atenciones.findWithRelacionesById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la atención"));
	}

	/** Atención con bloqueo de escritura hasta el fin de la transacción (las líneas se leen después). */
	private Atencion buscarBloqueada(Integer id) {
		return atenciones.findParaActualizar(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la atención"));
	}

	/**
	 * Mesero por rol permanente, por función temporal del turno, o ADMINISTRADOR
	 * (que entra a todos los paneles operativos con su misma identidad).
	 */
	private Usuario exigirMesero(String username) {
		return exigirAcceso(username, false);
	}

	/** Consulta del salón y atenciones en curso: además, SUPERVISOR (monitoreo del panel Inicio). */
	private Usuario exigirConsulta(String username) {
		return exigirAcceso(username, true);
	}

	private Usuario exigirAcceso(String username, boolean supervisorConsulta) {
		Usuario usuario = usuarios.findByUsername(username)
				.orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el usuario"));
		RolNombre rol = usuario.getRol().getNombre();
		String funcion = turnos.funcionActualDe(usuario);
		if (rol == RolNombre.ADMINISTRADOR || rol == RolNombre.MESERO
				|| RolNombre.MESERO.name().equals(funcion)
				|| (supervisorConsulta
						&& (rol == RolNombre.SUPERVISOR || RolNombre.SUPERVISOR.name().equals(funcion)))) {
			return usuario;
		}
		throw new OperacionNoAutorizadaException("Solo el personal con función de mesero puede atender mesas");
	}

	private static String unidadDe(Atencion atencion) {
		return atencion.getTurno().getUnidad().getNombre().name();
	}
}
