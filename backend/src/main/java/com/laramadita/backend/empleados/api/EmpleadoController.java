package com.laramadita.backend.empleados.api;

import com.laramadita.backend.empleados.api.dto.CargoResponse;
import com.laramadita.backend.empleados.api.dto.EmpleadoActualizarRequest;
import com.laramadita.backend.empleados.api.dto.EmpleadoRequest;
import com.laramadita.backend.empleados.api.dto.EmpleadoResponse;
import com.laramadita.backend.empleados.service.EmpleadoService;
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

/** Administración de empleados. Solo ADMINISTRADOR. */
@RestController
@RequestMapping("/api/v1")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class EmpleadoController {

	private final EmpleadoService service;

	public EmpleadoController(EmpleadoService service) {
		this.service = service;
	}

	@GetMapping("/cargos")
	public List<CargoResponse> cargos() {
		return service.listarCargos();
	}

	@GetMapping("/empleados")
	public List<EmpleadoResponse> listar() {
		return service.listar();
	}

	@GetMapping("/empleados/{id}")
	public EmpleadoResponse obtener(@PathVariable Integer id) {
		return service.obtener(id);
	}

	@PostMapping("/empleados")
	public ResponseEntity<EmpleadoResponse> crear(@Valid @RequestBody EmpleadoRequest request) {
		EmpleadoResponse creado = service.crear(request);
		return ResponseEntity.created(URI.create("/api/v1/empleados/" + creado.id())).body(creado);
	}

	@PutMapping("/empleados/{id}")
	public EmpleadoResponse actualizar(@PathVariable Integer id, @Valid @RequestBody EmpleadoActualizarRequest request,
			Authentication auth) {
		return service.actualizar(id, request, auth.getName());
	}

	@PatchMapping("/empleados/{id}/desactivar")
	public EmpleadoResponse desactivar(@PathVariable Integer id, Authentication auth) {
		return service.desactivar(id, auth.getName());
	}

	@PatchMapping("/empleados/{id}/archivar")
	public EmpleadoResponse archivar(@PathVariable Integer id, Authentication auth) {
		return service.archivar(id, auth.getName());
	}

	@PatchMapping("/empleados/{id}/activar")
	public EmpleadoResponse activar(@PathVariable Integer id) {
		return service.activar(id);
	}
}
