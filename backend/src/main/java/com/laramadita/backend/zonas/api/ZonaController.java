package com.laramadita.backend.zonas.api;

import com.laramadita.backend.zonas.api.dto.MesaResponse;
import com.laramadita.backend.zonas.api.dto.ZonaRequest;
import com.laramadita.backend.zonas.api.dto.ZonaResponse;
import com.laramadita.backend.zonas.service.ZonaService;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR')")
public class ZonaController {

	private final ZonaService service;

	public ZonaController(ZonaService service) {
		this.service = service;
	}

	@GetMapping("/zonas")
	public List<ZonaResponse> listar() {
		return service.listar();
	}

	@PostMapping("/zonas")
	public ResponseEntity<ZonaResponse> crear(@Valid @RequestBody ZonaRequest request) {
		ZonaResponse creada = service.crear(request);
		return ResponseEntity.created(URI.create("/api/v1/zonas/" + creada.id())).body(creada);
	}

	@PutMapping("/zonas/{id}")
	public ZonaResponse actualizar(@PathVariable Integer id, @Valid @RequestBody ZonaRequest request) {
		return service.actualizar(id, request);
	}

	@PatchMapping("/zonas/{id}/activo")
	public ZonaResponse activo(@PathVariable Integer id) {
		return service.cambiarActivo(id);
	}

	@DeleteMapping("/zonas/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
		service.eliminar(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/mesas")
	public List<MesaResponse> mesas() {
		return service.listarMesas();
	}
}
