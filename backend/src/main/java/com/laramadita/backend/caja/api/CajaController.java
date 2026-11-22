package com.laramadita.backend.caja.api;

import com.laramadita.backend.caja.api.dto.CajaRequests;
import com.laramadita.backend.caja.api.dto.CajaSesionResponse;
import com.laramadita.backend.caja.api.dto.ComprobanteResponse;
import com.laramadita.backend.caja.api.dto.CuentaResponse;
import com.laramadita.backend.caja.api.dto.MetodoPagoResponse;
import com.laramadita.backend.caja.service.CajaService;
import com.laramadita.backend.caja.service.CobroService;
import com.laramadita.backend.caja.service.ComprobanteService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Caja (M09 cobros y M10 control de caja). El acceso por rol permanente o
 * función temporal del turno se valida en {@code AccesoCaja}.
 */
@RestController
@RequestMapping("/api/v1/caja")
@PreAuthorize("isAuthenticated()")
public class CajaController {

	private final CajaService cajas;
	private final CobroService cobros;
	private final ComprobanteService comprobantes;

	public CajaController(CajaService cajas, CobroService cobros, ComprobanteService comprobantes) {
		this.cajas = cajas;
		this.cobros = cobros;
		this.comprobantes = comprobantes;
	}

	@GetMapping("/metodos")
	public List<MetodoPagoResponse> metodos(Authentication auth) {
		return cajas.metodos(auth.getName());
	}

	/** Caja abierta del turno (compartida por quienes cobran); 204 si todavía no se abrió. */
	@GetMapping("/sesion")
	public ResponseEntity<CajaSesionResponse> cajaAbierta(Authentication auth) {
		return cajas.cajaAbierta(auth.getName()).map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.noContent().build());
	}

	@PostMapping("/sesion")
	public CajaSesionResponse abrir(@Valid @RequestBody CajaRequests.Apertura request, Authentication auth) {
		return cajas.abrir(request, auth.getName());
	}

	@PostMapping("/sesion/movimientos")
	public CajaSesionResponse movimiento(@Valid @RequestBody CajaRequests.Movimiento request, Authentication auth) {
		return cajas.registrarMovimiento(request, auth.getName());
	}

	@PostMapping("/sesion/cerrar")
	public CajaSesionResponse cerrar(@Valid @RequestBody CajaRequests.Cierre request, Authentication auth) {
		return cajas.cerrar(request, auth.getName());
	}

	/** Cajas abiertas y cerradas: control del administrador y monitoreo de supervisión. */
	@GetMapping("/sesiones")
	public List<CajaSesionResponse> sesiones(Authentication auth) {
		return cajas.listar(auth.getName());
	}

	@GetMapping("/cuentas")
	public List<CuentaResponse.Resumen> pendientes(Authentication auth) {
		return cobros.pendientes(auth.getName());
	}

	@GetMapping("/cuentas/{atencionId}")
	public CuentaResponse cuenta(@PathVariable Integer atencionId, Authentication auth) {
		return cobros.cuenta(atencionId, auth.getName());
	}

	@PostMapping("/cuentas/{atencionId}/pagos")
	public CuentaResponse pagar(@PathVariable Integer atencionId, @Valid @RequestBody CajaRequests.Pago request,
			Authentication auth) {
		return cobros.pagar(atencionId, request, auth.getName());
	}

	/** Boleta simple de la cuenta pagada (M10, sin SUNAT). */
	@GetMapping("/cuentas/{atencionId}/boleta")
	public ComprobanteResponse boleta(@PathVariable Integer atencionId, Authentication auth) {
		return comprobantes.boleta(atencionId, auth.getName());
	}

	/** Solo el ADMINISTRADOR; el pago queda ANULADO con quién, cuándo y por qué. */
	@PostMapping("/pagos/{pagoId}/anular")
	public CuentaResponse anularPago(@PathVariable Integer pagoId, @Valid @RequestBody CajaRequests.Anulacion request,
			Authentication auth) {
		return cobros.anularPago(pagoId, request.motivo(), auth.getName());
	}
}
