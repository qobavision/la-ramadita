package com.laramadita.backend.pedidos.api;

import com.laramadita.backend.pedidos.api.dto.AtencionRequest;
import com.laramadita.backend.pedidos.api.dto.AtencionResponse;
import com.laramadita.backend.pedidos.api.dto.AtencionResumenResponse;
import com.laramadita.backend.pedidos.api.dto.DetalleActualizarRequest;
import com.laramadita.backend.pedidos.api.dto.DetalleRequest;
import com.laramadita.backend.pedidos.api.dto.MesaSalonResponse;
import com.laramadita.backend.pedidos.service.AtencionService;
import com.laramadita.backend.productos.api.dto.CartaResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Atención de mesas y pedidos del mesero. El acceso por rol permanente o
 * función temporal del turno se valida en {@link AtencionService}.
 */
@RestController
@RequestMapping("/api/v1/atenciones")
@PreAuthorize("isAuthenticated()")
public class AtencionController {

	private final AtencionService service;

	public AtencionController(AtencionService service) {
		this.service = service;
	}

	@GetMapping("/salon")
	public List<MesaSalonResponse> salon(Authentication auth) {
		return service.salon(auth.getName());
	}

	@GetMapping
	public List<AtencionResumenResponse> activas(Authentication auth) {
		return service.activas(auth.getName());
	}

	@GetMapping("/{id}/carta")
	public CartaResponse carta(@PathVariable Integer id, Authentication auth) {
		return service.carta(id, auth.getName());
	}

	@PostMapping
	public ResponseEntity<AtencionResponse> abrir(@Valid @RequestBody AtencionRequest request, Authentication auth) {
		AtencionResponse creada = service.abrir(request, auth.getName());
		return ResponseEntity.created(URI.create("/api/v1/atenciones/" + creada.id())).body(creada);
	}

	@GetMapping("/{id}")
	public AtencionResponse obtener(@PathVariable Integer id, Authentication auth) {
		return service.obtener(id, auth.getName());
	}

	@PostMapping("/{id}/detalles")
	public AtencionResponse agregar(@PathVariable Integer id, @Valid @RequestBody DetalleRequest request,
			Authentication auth) {
		return service.agregar(id, request, auth.getName());
	}

	@PatchMapping("/{id}/detalles/{detalleId}")
	public AtencionResponse actualizarDetalle(@PathVariable Integer id, @PathVariable Integer detalleId,
			@Valid @RequestBody DetalleActualizarRequest request, Authentication auth) {
		return service.actualizarDetalle(id, detalleId, request, auth.getName());
	}

	@DeleteMapping("/{id}/detalles/{detalleId}")
	public AtencionResponse quitarDetalle(@PathVariable Integer id, @PathVariable Integer detalleId,
			Authentication auth) {
		return service.quitarDetalle(id, detalleId, auth.getName());
	}

	@PostMapping("/{id}/enviar")
	public AtencionResponse enviar(@PathVariable Integer id, Authentication auth) {
		return service.enviarACocina(id, auth.getName());
	}

	@PostMapping("/{id}/cuenta")
	public AtencionResponse solicitarCuenta(@PathVariable Integer id, Authentication auth) {
		return service.solicitarCuenta(id, auth.getName());
	}

	@PostMapping("/{id}/reabrir")
	public AtencionResponse reabrir(@PathVariable Integer id, Authentication auth) {
		return service.reabrir(id, auth.getName());
	}

	@PostMapping("/{id}/liberar")
	public AtencionResponse liberar(@PathVariable Integer id, Authentication auth) {
		return service.liberarSinConsumo(id, auth.getName());
	}
}
