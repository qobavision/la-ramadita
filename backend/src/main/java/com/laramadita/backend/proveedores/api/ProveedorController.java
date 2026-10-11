package com.laramadita.backend.proveedores.api;

import com.laramadita.backend.proveedores.api.dto.ProveedorRequest;
import com.laramadita.backend.proveedores.api.dto.ProveedorResponse;
import com.laramadita.backend.proveedores.service.ProveedorService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/v1/proveedores")
@PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR')")
public class ProveedorController {

	private final ProveedorService service;

	public ProveedorController(ProveedorService service) {
		this.service = service;
	}

	@GetMapping
	public List<ProveedorResponse> listar(@RequestParam(defaultValue = "false") boolean soloActivos) {
		return service.listar(soloActivos);
	}

	@GetMapping("/{id}")
	public ProveedorResponse obtener(@PathVariable Integer id) {
		return service.obtener(id);
	}

	@PostMapping
	public ResponseEntity<ProveedorResponse> crear(@Valid @RequestBody ProveedorRequest request) {
		ProveedorResponse creado = service.crear(request);
		return ResponseEntity.created(URI.create("/api/v1/proveedores/" + creado.id())).body(creado);
	}

	@PutMapping("/{id}")
	public ProveedorResponse actualizar(@PathVariable Integer id, @Valid @RequestBody ProveedorRequest request) {
		return service.actualizar(id, request);
	}

	@PatchMapping("/{id}/activo")
	public ProveedorResponse alternarActivo(@PathVariable Integer id) {
		return service.alternarActivo(id);
	}
}
