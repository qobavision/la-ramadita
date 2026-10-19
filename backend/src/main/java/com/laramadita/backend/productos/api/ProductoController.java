package com.laramadita.backend.productos.api;

import com.laramadita.backend.productos.api.dto.ProductoRequest;
import com.laramadita.backend.productos.api.dto.ProductoResponse;
import com.laramadita.backend.productos.service.ProductoService;
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
@RequestMapping("/api/v1/productos")
@PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR')")
public class ProductoController {

	private final ProductoService service;

	public ProductoController(ProductoService service) {
		this.service = service;
	}

	@GetMapping
	public List<ProductoResponse> listar(
			@RequestParam(required = false) String ambito,
			@RequestParam(required = false, defaultValue = "true") boolean activos) {
		return service.listar(ambito, activos);
	}

	@GetMapping("/{id}")
	public ProductoResponse obtener(@PathVariable Integer id) {
		return service.obtener(id);
	}

	@PostMapping
	public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody ProductoRequest request) {
		ProductoResponse creado = service.crear(request);
		return ResponseEntity.created(URI.create("/api/v1/productos/" + creado.id())).body(creado);
	}

	@PutMapping("/{id}")
	public ProductoResponse actualizar(@PathVariable Integer id, @Valid @RequestBody ProductoRequest request) {
		return service.actualizar(id, request);
	}

	@PatchMapping("/{id}/activo")
	public ProductoResponse activo(@PathVariable Integer id, @RequestParam boolean valor) {
		return service.cambiarActivo(id, valor);
	}
}
