package com.laramadita.backend.productos.api;

import com.laramadita.backend.productos.api.dto.RecetaRequest;
import com.laramadita.backend.productos.api.dto.RecetaResponse;
import com.laramadita.backend.productos.service.RecetaService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/recetas")
@PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR')")
public class RecetaController {

	private final RecetaService service;

	public RecetaController(RecetaService service) {
		this.service = service;
	}

	@GetMapping
	public List<RecetaResponse> listar() {
		return service.listar();
	}

	@GetMapping("/{productoId}")
	public RecetaResponse obtener(@PathVariable Integer productoId) {
		return service.obtener(productoId);
	}

	@PostMapping
	public ResponseEntity<RecetaResponse> crear(@Valid @RequestBody RecetaRequest request) {
		RecetaResponse creada = service.crear(request);
		return ResponseEntity.created(URI.create("/api/v1/recetas/" + creada.productoId())).body(creada);
	}

	@PutMapping("/{productoId}")
	public RecetaResponse actualizar(@PathVariable Integer productoId, @Valid @RequestBody RecetaRequest request) {
		return service.actualizar(productoId, request);
	}

	@DeleteMapping("/{productoId}")
	public ResponseEntity<Void> eliminar(@PathVariable Integer productoId) {
		service.eliminar(productoId);
		return ResponseEntity.noContent().build();
	}
}
