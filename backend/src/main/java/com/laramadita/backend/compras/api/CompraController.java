package com.laramadita.backend.compras.api;

import com.laramadita.backend.compras.api.dto.CompraRequest;
import com.laramadita.backend.compras.api.dto.CompraResponse;
import com.laramadita.backend.compras.api.dto.RecepcionRequest;
import com.laramadita.backend.compras.service.CompraService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/compras")
@PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR')")
public class CompraController {

	private final CompraService service;

	public CompraController(CompraService service) {
		this.service = service;
	}

	@GetMapping
	public List<CompraResponse> listar() {
		return service.listar();
	}

	@GetMapping("/{id}")
	public CompraResponse obtener(@PathVariable Integer id) {
		return service.obtener(id);
	}

	@PostMapping
	public ResponseEntity<CompraResponse> crear(@Valid @RequestBody CompraRequest request, Authentication auth) {
		CompraResponse creada = service.crear(request, auth.getName());
		return ResponseEntity.created(URI.create("/api/v1/compras/" + creada.id())).body(creada);
	}

	@PutMapping("/{id}")
	public CompraResponse actualizar(@PathVariable Integer id, @Valid @RequestBody CompraRequest request) {
		return service.actualizar(id, request);
	}

	@PatchMapping("/{id}/cancelar")
	public CompraResponse cancelar(@PathVariable Integer id) {
		return service.cancelar(id);
	}

	@PostMapping("/{id}/recepciones")
	public CompraResponse recibir(@PathVariable Integer id, @Valid @RequestBody RecepcionRequest request,
			Authentication auth) {
		return service.recibir(id, request, auth.getName());
	}
}
