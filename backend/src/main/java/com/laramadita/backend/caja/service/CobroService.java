package com.laramadita.backend.caja.service;

import com.laramadita.backend.caja.api.dto.CajaRequests;
import com.laramadita.backend.caja.api.dto.CuentaResponse;
import com.laramadita.backend.caja.model.CajaSesion;
import com.laramadita.backend.caja.model.EstadoCaja;
import com.laramadita.backend.caja.model.EstadoPago;
import com.laramadita.backend.caja.model.MetodoPago;
import com.laramadita.backend.caja.model.Pago;
import com.laramadita.backend.caja.repository.MetodoPagoRepository;
import com.laramadita.backend.caja.repository.PagoRepository;
import com.laramadita.backend.pedidos.model.Atencion;
import com.laramadita.backend.pedidos.model.EstadoAtencion;
import com.laramadita.backend.pedidos.model.PedidoDetalle;
import com.laramadita.backend.pedidos.repository.AtencionRepository;
import com.laramadita.backend.pedidos.service.CanalSalon;
import com.laramadita.backend.shared.error.RecursoNoEncontradoException;
import com.laramadita.backend.shared.error.ReglaNegocioException;
import com.laramadita.backend.shared.tiemporeal.EventoTiempoReal;
import com.laramadita.backend.usuarios.model.Usuario;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cobro de cuentas (M09): cuentas pendientes de pago, pagos parciales con
 * distintos métodos (RN-14, RN-15), división por productos o por montos
 * (RN-17) y cierre de la atención con liberación de la mesa al llegar a saldo cero (RN-16, RN-02).
 */
@Service
@Transactional
public class CobroService {

	private final AtencionRepository atenciones;
	private final PagoRepository pagos;
	private final MetodoPagoRepository metodos;
	private final CajaService cajas;
	private final AccesoCaja acceso;
	private final ComprobanteService comprobantes;
	private final ApplicationEventPublisher eventos;

	public CobroService(AtencionRepository atenciones, PagoRepository pagos, MetodoPagoRepository metodos,
			CajaService cajas, AccesoCaja acceso, ComprobanteService comprobantes, ApplicationEventPublisher eventos) {
		this.atenciones = atenciones;
		this.pagos = pagos;
		this.metodos = metodos;
		this.cajas = cajas;
		this.acceso = acceso;
		this.comprobantes = comprobantes;
		this.eventos = eventos;
	}

	/** Mesas y pedidos para llevar cuya cuenta pidió el mesero. */
	@Transactional(readOnly = true)
	public List<CuentaResponse.Resumen> pendientes(String username) {
		acceso.exigirConsultar(username);
		List<Atencion> lista = atenciones.findByEstadoInOrderByFechaInicioAsc(Set.of(EstadoAtencion.PENDIENTE_PAGO));
		Map<Integer, List<Pago>> porAtencion = lista.isEmpty() ? Map.of()
				: pagos.findByAtencion_IdInAndEstado(lista.stream().map(Atencion::getId).toList(), EstadoPago.CONFIRMADO)
						.stream()
						.collect(Collectors.groupingBy(p -> p.getAtencion().getId()));
		return lista.stream()
				.map(a -> CuentaResponse.Resumen.de(a, porAtencion.getOrDefault(a.getId(), List.of())))
				.toList();
	}

	@Transactional(readOnly = true)
	public CuentaResponse cuenta(Integer atencionId, String username) {
		acceso.exigirConsultar(username);
		Atencion atencion = buscar(atencionId);
		return CuentaResponse.de(atencion, pagosDe(atencionId));
	}

	public CuentaResponse pagar(Integer atencionId, CajaRequests.Pago request, String username) {
		Usuario cajero = acceso.exigirCobrar(username);
		// Bloqueo de la atención: dos cobros simultáneos no pueden superar el saldo.
		Atencion atencion = atenciones.findParaActualizar(atencionId)
				.orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la cuenta"));
		CajaSesion caja = cajas.exigirCajaDelTurno(atencion.getTurno());
		if (atencion.getEstado() == EstadoAtencion.ABIERTA) {
			throw new ReglaNegocioException("El mesero todavía no pidió la cuenta de " + atencion.identificacion());
		}
		if (atencion.getEstado() != EstadoAtencion.PENDIENTE_PAGO) {
			throw new ReglaNegocioException("La cuenta de " + atencion.identificacion() + " ya está pagada");
		}
		MetodoPago metodo = metodos.findByNombreIgnoreCase(request.metodo().trim())
				.filter(MetodoPago::isActivo)
				.orElseThrow(() -> new ReglaNegocioException("Método de pago no válido"));

		List<Pago> previos = pagosDe(atencionId);
		BigDecimal saldo = atencion.totalEnviado().subtract(CuentaResponse.sumar(previos));
		Pago pago;
		if (request.lineas() != null && !request.lineas().isEmpty()) {
			pago = pagoPorProductos(atencion, metodo, caja, cajero, request, previos);
		} else {
			if (request.monto() == null) {
				throw new ReglaNegocioException("Indica el monto o selecciona los productos a pagar");
			}
			pago = new Pago(atencion, metodo, caja, cajero, request.monto().setScale(2, RoundingMode.HALF_UP),
					request.observacion());
		}
		if (pago.getMonto().compareTo(saldo) > 0) {
			throw new ReglaNegocioException("El pago supera el saldo pendiente de S/ " + saldo.setScale(2, RoundingMode.HALF_UP));
		}
		pagos.save(pago);

		if (saldo.subtract(pago.getMonto()).signum() == 0) {
			atencion.cerrarPorPago();
			comprobantes.emitirBoleta(atencion, cajero);
			eventos.publishEvent(CanalSalon.aviso("PAGADA", atencion));
			eventos.publishEvent(EventoTiempoReal.paraCanal(AccesoCaja.CANAL, "CUENTA_PAGADA", Map.of(
					"atencionId", atencion.getId(),
					"identificacion", atencion.identificacion())));
		}
		atenciones.flush();
		return CuentaResponse.de(atencion, pagosDe(atencionId));
	}

	/**
	 * Decisión del equipo: solo el ADMINISTRADOR anula un pago, con motivo, y
	 * queda la evidencia (quién, cuándo y por qué). Solo pagos de la caja abierta:
	 * una caja cerrada ya tiene su arqueo. Si la cuenta estaba pagada, vuelve a
	 * pendiente de pago y su boleta queda anulada.
	 */
	public CuentaResponse anularPago(Integer pagoId, String motivo, String username) {
		Usuario administrador = acceso.exigirAdministrador(username);
		Pago pago = pagos.findById(pagoId)
				.orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el pago"));
		Atencion atencion = atenciones.findParaActualizar(pago.getAtencion().getId())
				.orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la cuenta"));
		if (pago.getSesion().getEstado() != EstadoCaja.ABIERTA) {
			throw new ReglaNegocioException("Solo se anulan pagos de la caja abierta: esa caja ya se cerró con su arqueo");
		}
		boolean estabaPagada = atencion.getEstado() == EstadoAtencion.CERRADA;
		pago.anular(administrador, motivo);
		if (estabaPagada) {
			atencion.reabrirPorAnulacion();
			comprobantes.anularDe(atencion.getId());
		}
		atenciones.flush();
		eventos.publishEvent(CanalSalon.aviso("PAGO_ANULADO", atencion));
		eventos.publishEvent(EventoTiempoReal.paraCanal(AccesoCaja.CANAL, "PAGO_ANULADO", Map.of(
				"atencionId", atencion.getId(),
				"identificacion", atencion.identificacion())));
		return CuentaResponse.de(atencion, pagosDe(atencion.getId()));
	}

	/** División por productos: el monto sale de los productos y cantidades elegidos, sin pagar de más. */
	private Pago pagoPorProductos(Atencion atencion, MetodoPago metodo, CajaSesion caja, Usuario cajero,
			CajaRequests.Pago request, List<Pago> previos) {
		Map<Integer, PedidoDetalle> lineas = CuentaResponse.lineasActivas(atencion).stream()
				.collect(Collectors.toMap(PedidoDetalle::getId, Function.identity()));
		Map<Integer, Integer> pagadas = CuentaResponse.cantidadesPagadas(previos);
		Map<Integer, Integer> pedidas = new LinkedHashMap<>();
		request.lineas().forEach(l -> pedidas.merge(l.pedidoDetalleId(), l.cantidad(), Integer::sum));

		BigDecimal monto = BigDecimal.ZERO;
		for (Map.Entry<Integer, Integer> e : pedidas.entrySet()) {
			PedidoDetalle linea = lineas.get(e.getKey());
			if (linea == null) {
				throw new ReglaNegocioException("El producto seleccionado no pertenece a la cuenta");
			}
			int pendiente = linea.getCantidad() - pagadas.getOrDefault(linea.getId(), 0);
			if (e.getValue() > pendiente) {
				throw new ReglaNegocioException("Solo quedan " + Math.max(pendiente, 0) + " de "
						+ linea.nombre() + " por pagar");
			}
			monto = monto.add(linea.getPrecioUnitario().multiply(BigDecimal.valueOf(e.getValue())));
		}
		Pago pago = new Pago(atencion, metodo, caja, cajero, monto, request.observacion());
		pedidas.forEach((id, cantidad) -> {
			BigDecimal aplicado = lineas.get(id).getPrecioUnitario().multiply(BigDecimal.valueOf(cantidad));
			// Las cortesías (precio 0) no generan detalle: pago_detalle exige monto > 0.
			if (aplicado.signum() > 0) {
				pago.agregarDetalle(lineas.get(id), cantidad, aplicado);
			}
		});
		return pago;
	}

	private List<Pago> pagosDe(Integer atencionId) {
		return pagos.findByAtencion_IdAndEstadoOrderByFechaAsc(atencionId, EstadoPago.CONFIRMADO);
	}

	private Atencion buscar(Integer id) {
		return atenciones.findWithRelacionesById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la cuenta"));
	}
}
