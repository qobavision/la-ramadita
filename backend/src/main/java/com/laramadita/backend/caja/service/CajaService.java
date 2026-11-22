package com.laramadita.backend.caja.service;

import com.laramadita.backend.caja.api.dto.CajaRequests;
import com.laramadita.backend.caja.api.dto.CajaSesionResponse;
import com.laramadita.backend.caja.api.dto.MetodoPagoResponse;
import com.laramadita.backend.caja.api.dto.PagoResponse;
import com.laramadita.backend.caja.model.CajaSesion;
import com.laramadita.backend.caja.model.EstadoCaja;
import com.laramadita.backend.caja.model.EstadoPago;
import com.laramadita.backend.caja.model.Pago;
import com.laramadita.backend.caja.model.TipoMovimientoCaja;
import com.laramadita.backend.caja.repository.CajaSesionRepository;
import com.laramadita.backend.caja.repository.MetodoPagoRepository;
import com.laramadita.backend.caja.repository.PagoRepository;
import com.laramadita.backend.shared.error.ConflictoException;
import com.laramadita.backend.shared.error.ReglaNegocioException;
import com.laramadita.backend.turnos.model.Turno;
import com.laramadita.backend.turnos.service.TurnoService;
import com.laramadita.backend.usuarios.model.Usuario;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Control de caja (M10): una caja por turno (índice {@code UX_caja_abierta_turno}
 * de V1), compartida por quienes cobran; cada pago y movimiento guarda su usuario.
 * Apertura con monto inicial, ingresos y egresos, acumulado por método y cierre
 * con arqueo de efectivo.
 */
@Service
@Transactional
public class CajaService {

	private final CajaSesionRepository sesiones;
	private final PagoRepository pagos;
	private final MetodoPagoRepository metodos;
	private final AccesoCaja acceso;
	private final TurnoService turnos;

	public CajaService(CajaSesionRepository sesiones, PagoRepository pagos, MetodoPagoRepository metodos,
			AccesoCaja acceso, TurnoService turnos) {
		this.sesiones = sesiones;
		this.pagos = pagos;
		this.metodos = metodos;
		this.acceso = acceso;
		this.turnos = turnos;
	}

	@Transactional(readOnly = true)
	public List<MetodoPagoResponse> metodos(String username) {
		acceso.exigirConsultar(username);
		return metodos.findByActivoTrueOrderByIdAsc().stream().map(MetodoPagoResponse::de).toList();
	}

	/** Caja abierta (la del turno más reciente), si la hay. */
	@Transactional(readOnly = true)
	public Optional<CajaSesionResponse> cajaAbierta(String username) {
		acceso.exigirCobrar(username);
		return sesiones.findFirstByEstadoOrderByFechaAperturaDesc(EstadoCaja.ABIERTA).map(this::respuesta);
	}

	public CajaSesionResponse abrir(CajaRequests.Apertura request, String username) {
		Usuario cajero = acceso.exigirCobrar(username);
		Turno turno = turnos.exigirTurnoEnCurso();
		sesiones.findFirstByTurno_IdAndEstado(turno.getId(), EstadoCaja.ABIERTA).ifPresent(abierta -> {
			throw new ConflictoException("La caja del turno ya está abierta (la abrió "
					+ PagoResponse.nombreDe(abierta.getUsuarioApertura()) + "). Todos cobran en esa caja.");
		});
		CajaSesion sesion = new CajaSesion(turno, cajero, dosDecimales(request.montoInicial()));
		return respuesta(sesiones.save(sesion));
	}

	public CajaSesionResponse registrarMovimiento(CajaRequests.Movimiento request, String username) {
		Usuario cajero = acceso.exigirCobrar(username);
		TipoMovimientoCaja tipo = TipoMovimientoCaja.desde(request.tipo())
				.orElseThrow(() -> new ReglaNegocioException("El tipo de movimiento debe ser INGRESO o EGRESO"));
		CajaSesion sesion = exigirAbierta();
		sesion.registrarMovimiento(tipo, dosDecimales(request.monto()), request.motivo(), cajero);
		sesiones.flush();
		return respuesta(sesion);
	}

	/** Cualquier usuario de caja puede cerrar la caja del turno (por ejemplo, si el cajero se retiró). */
	public CajaSesionResponse cerrar(CajaRequests.Cierre request, String username) {
		Usuario cajero = acceso.exigirCobrar(username);
		CajaSesion sesion = exigirAbierta();
		List<Pago> cobrados = pagosDe(sesion);
		sesion.cerrar(CajaSesionResponse.efectivoEsperado(sesion, cobrados), dosDecimales(request.efectivoContado()),
				request.observacion(), cajero);
		// Si ya se pidió finalizar el turno y no queda nada por cobrar, el turno se cierra aquí.
		turnos.cerrarSiCorresponde(sesion.getTurno());
		return respuesta(sesion);
	}

	/** Cajas para el control del administrador y el monitoreo de supervisión. */
	@Transactional(readOnly = true)
	public List<CajaSesionResponse> listar(String username) {
		acceso.exigirConsultar(username);
		return sesiones.findTop100ByOrderByFechaAperturaDesc().stream().map(this::respuesta).toList();
	}

	/** Un cobro se registra en la caja del turno de la atención (no se mezclan unidades de negocio). */
	public CajaSesion exigirCajaDelTurno(Turno turno) {
		return sesiones.findFirstByTurno_IdAndEstado(turno.getId(), EstadoCaja.ABIERTA)
				.orElseThrow(() -> new ReglaNegocioException("Abre la caja del turno de "
						+ turno.getUnidad().getNombre().etiqueta() + " antes de registrar cobros"));
	}

	private CajaSesion exigirAbierta() {
		return sesiones.findFirstByEstadoOrderByFechaAperturaDesc(EstadoCaja.ABIERTA)
				.orElseThrow(() -> new ReglaNegocioException("No hay una caja abierta. Ábrela en Control de caja."));
	}

	private CajaSesionResponse respuesta(CajaSesion sesion) {
		return CajaSesionResponse.de(sesion, pagosDe(sesion), sesion.getId() == null ? List.of()
				: pagos.findBySesion_IdAndEstadoOrderByFechaAsc(sesion.getId(), EstadoPago.ANULADO));
	}

	private List<Pago> pagosDe(CajaSesion sesion) {
		return sesion.getId() == null ? List.of()
				: pagos.findBySesion_IdAndEstadoOrderByFechaAsc(sesion.getId(), EstadoPago.CONFIRMADO);
	}

	private static BigDecimal dosDecimales(BigDecimal valor) {
		return valor == null ? null : valor.setScale(2, RoundingMode.HALF_UP);
	}
}
