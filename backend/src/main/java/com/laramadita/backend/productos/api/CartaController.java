package com.laramadita.backend.productos.api;

import com.laramadita.backend.productos.api.dto.CartaRequest;
import com.laramadita.backend.productos.api.dto.CartaResponse;
import com.laramadita.backend.productos.api.dto.EstadoCartaDetalleRequest;
import com.laramadita.backend.productos.service.CartaService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cartas")
@PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR')")
public class CartaController {

	private final CartaService service;

	public CartaController(CartaService service) {
		this.service = service;
	}

	@GetMapping
	public List<CartaResponse> listar(@RequestParam String ambito) {
		return service.listar(ambito);
	}

	@GetMapping("/vigente")
	public CartaResponse vigente(@RequestParam String ambito) {
		return service.vigente(ambito);
	}

	@GetMapping("/{id}")
	public CartaResponse obtener(@PathVariable Integer id) {
		return service.obtener(id);
	}

	@PostMapping
	public ResponseEntity<CartaResponse> crear(@Valid @RequestBody CartaRequest request) {
		CartaResponse creada = service.crear(request);
		return ResponseEntity.created(URI.create("/api/v1/cartas/" + creada.id())).body(creada);
	}

	@PutMapping("/{id}")
	public CartaResponse actualizar(@PathVariable Integer id, @Valid @RequestBody CartaRequest request) {
		return service.actualizar(id, request);
	}

	@PatchMapping("/{id}/vigente")
	public CartaResponse marcarVigente(@PathVariable Integer id) {
		return service.marcarVigente(id);
	}

	@DeleteMapping("/{id}/detalles/{detalleId}")
	public CartaResponse quitarDetalle(@PathVariable Integer id, @PathVariable Integer detalleId) {
		return service.quitarDetalle(id, detalleId);
	}

	@PatchMapping("/{id}/detalles/{detalleId}/estado")
	public CartaResponse estadoDetalle(
			@PathVariable Integer id,
			@PathVariable Integer detalleId,
			@Valid @RequestBody EstadoCartaDetalleRequest request) {
		return service.cambiarEstadoDetalle(id, detalleId, request.estado());
	}
}
