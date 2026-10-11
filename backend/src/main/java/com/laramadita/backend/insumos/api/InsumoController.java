package com.laramadita.backend.insumos.api;

import com.laramadita.backend.insumos.api.dto.InsumoActualizarRequest;
import com.laramadita.backend.insumos.api.dto.InsumoRequest;
import com.laramadita.backend.insumos.api.dto.InsumoResponse;
import com.laramadita.backend.insumos.service.InsumoService;
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
@RequestMapping("/api/v1/insumos")
@PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR')")
public class InsumoController {

	private final InsumoService service;

	public InsumoController(InsumoService service) {
		this.service = service;
	}

	@GetMapping
	public List<InsumoResponse> listar(
			@RequestParam(required = false) String q,
			@RequestParam(required = false, defaultValue = "ACTIVOS") String estado) {
		return service.listar(q, estado);
	}

	@GetMapping("/{id}")
	public InsumoResponse obtener(@PathVariable Integer id) {
		return service.obtener(id);
	}

	@PostMapping
	public ResponseEntity<InsumoResponse> crear(@Valid @RequestBody InsumoRequest request) {
		InsumoResponse creado = service.crear(request);
		return ResponseEntity.created(URI.create("/api/v1/insumos/" + creado.id())).body(creado);
	}

	@PutMapping("/{id}")
	public InsumoResponse actualizar(@PathVariable Integer id, @Valid @RequestBody InsumoActualizarRequest request) {
		return service.actualizar(id, request);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<InsumoResponse> eliminar(@PathVariable Integer id) {
		return service.eliminar(id)
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.noContent().build());
	}

	@PatchMapping("/{id}/reactivar")
	public InsumoResponse reactivar(@PathVariable Integer id) {
		return service.reactivar(id);
	}
}
